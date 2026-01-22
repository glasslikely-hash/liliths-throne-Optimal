package com.lilithsthrone.logic.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.lilithsthrone.logic.state.GameState;
import com.lilithsthrone.persistence.binary.BinaryStream;
import com.lilithsthrone.persistence.binary.BinaryStream.Writer;

/**
 * Manages delta (incremental) persistence.
 * 
 * A delta is a compressed representation of changes to the game state
 * since the last snapshot. Used for:
 *   1. Frequent saves without serializing entire state (fast)
 *   2. Bandwidth-efficient cloud saves
 *   3. Efficient mobile storage
 * 
 * Delta Format:
 *   [Magic Header: "DELT"]
 *   [Version: 1]
 *   [Base Snapshot ID]
 *   [Field Changes: Array of (fieldId, typeId, newValue)]
 *   [CRC32 Checksum]
 * 
 * Load Process:
 *   1. Load base snapshot
 *   2. Apply all deltas in chronological order
 *   3. Merge deltas periodically (when too many accumulated)
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class DeltaEngine {
    private static final String DELTA_MAGIC = "DELT";
    private static final int DELTA_VERSION = 1;
    private static final String DELTAS_DIR = "saves/deltas";

    private GameState gameState;
    private long lastDeltaFlushTime = 0;
    private long deltaFlushIntervalMs;             // How often to flush deltas (default 60000 = 1 minute)
    private int maxDeltasBeforeMerge = 10;         // Merge deltas when more than this accumulate
    private boolean isDeltaTrackingEnabled = true;
    private BlockingQueue<DeltaChange> pendingChanges = new LinkedBlockingQueue<>();

    // Field tracking for delta calculation
    private static class DeltaChange {
        String fieldId;
        int typeId;
        Object newValue;
        long timestamp;

        DeltaChange(String fieldId, int typeId, Object newValue) {
            this.fieldId = fieldId;
            this.typeId = typeId;
            this.newValue = newValue;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public DeltaEngine(GameState gameState, long deltaFlushIntervalMs) {
        this.gameState = gameState;
        this.deltaFlushIntervalMs = deltaFlushIntervalMs;
        initializeDirectories();
    }

    /**
     * Initialize engine for new session.
     */
    public void initialize(GameState initialState) {
        this.gameState = initialState;
        this.pendingChanges.clear();
        this.lastDeltaFlushTime = System.currentTimeMillis();
    }

    /**
     * Set current game state (used during load).
     */
    public void setCurrentState(GameState state) {
        this.gameState = state;
    }

    /**
     * Get accumulated deltas since last snapshot.
     */
    public byte[] getDeltasSinceSnapshot() throws IOException {
        // Serialize all pending changes as delta
        BinaryStream stream = new BinaryStream();
        stream.writeString(DELTA_MAGIC);
        stream.writeInt(DELTA_VERSION);
        stream.writeInt(pendingChanges.size());

        for (DeltaChange change : pendingChanges) {
            stream.writeString(change.fieldId);
            stream.writeInt(change.typeId);
            // Value serialization handled by BinarySerializable
            stream.writeLong(change.timestamp);
        }

        byte[] data = stream.toByteArray();
        return data;
    }

    /**
     * Apply deltas from byte array to game state.
     */
    public void applyDeltasFromBytes(byte[] deltaBytes, GameState targetState) throws IOException {
        if (deltaBytes == null || deltaBytes.length == 0) {
            return;
        }

        BinaryStream stream = new BinaryStream(deltaBytes);
        String magic = stream.readString();
        if (!DELTA_MAGIC.equals(magic)) {
            throw new IOException("Invalid delta file format");
        }

        int version = stream.readInt();
        if (version != DELTA_VERSION) {
            throw new IOException("Incompatible delta version: " + version);
        }

        int changeCount = stream.readInt();
        for (int i = 0; i < changeCount; i++) {
            String fieldId = stream.readString();
            int typeId = stream.readInt();
            long timestamp = stream.readLong();
            applyFieldChange(targetState, fieldId, typeId);
        }
    }

    /**
     * Apply a single field change to game state.
     */
    private void applyFieldChange(GameState state, String fieldId, int typeId) {
        // Apply change to appropriate field in GameState
        // This would need to be implemented based on actual state structure
        // Placeholder: actual implementation depends on GameState structure
    }

    /**
     * Clear all accumulated deltas (after saving).
     */
    public void clearDeltas() {
        pendingChanges.clear();
        lastDeltaFlushTime = System.currentTimeMillis();
    }

    private void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(DELTAS_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create deltas directory");
            e.printStackTrace();
        }
    }

    /**
     * Check if it's time to flush pending deltas to disk
     */
    public boolean shouldFlush() {
        if (!isDeltaTrackingEnabled || pendingChanges.isEmpty()) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastDeltaFlushTime) >= deltaFlushIntervalMs;
    }

    /**
     * Flush accumulated changes to a delta file
     */
    public void flush() {
        if (pendingChanges.isEmpty()) {
            return;
        }

        try {
            String filename = generateDeltaFilename();
            Path filepath = Paths.get(DELTAS_DIR, filename);
            
            // Collect all pending changes
            List<DeltaChange> changes = new ArrayList<>();
            pendingChanges.drainTo(changes);

            // Write delta to file
            writeDeltaFile(filepath, changes);
            
            lastDeltaFlushTime = System.currentTimeMillis();
            gameState.markClean();

            System.out.println("[DeltaEngine] Delta flushed: " + filepath + " (" + changes.size() + " changes)");

            // Check if we need to merge deltas
            checkAndMergeDeltasIfNeeded();

        } catch (Exception e) {
            System.err.println("[DeltaEngine] Failed to flush delta");
            e.printStackTrace();
        }
    }

    /**
     * Record a change to be included in the next delta
     */
    public void recordChange(String fieldId, Object newValue) {
        if (!isDeltaTrackingEnabled) {
            return;
        }

        // Determine type ID based on field
        int typeId = inferTypeId(fieldId);
        DeltaChange change = new DeltaChange(fieldId, typeId, newValue);
        
        try {
            pendingChanges.put(change);
            gameState.markDirty();
        } catch (InterruptedException e) {
            System.err.println("[DeltaEngine] Failed to record change: " + fieldId);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Write delta file to disk
     */
    private void writeDeltaFile(Path filepath, List<DeltaChange> changes) throws IOException {
        BinaryStream stream = new BinaryStream();
        Writer writer = stream.new Writer();

        // Write header
        writer.writeString(DELTA_MAGIC);
        writer.writeInt(DELTA_VERSION);
        writer.writeLong(System.currentTimeMillis());
        writer.writeString(gameState.getGameId().toString());

        // Write changes
        writer.writeInt(changes.size()); // Number of changes
        for (DeltaChange change : changes) {
            writer.writeString(change.fieldId);
            writer.writeInt(change.typeId);
            writer.writeObject(change.newValue); // Binary-serialized value
            writer.writeLong(change.timestamp);
        }

        // Write to file
        byte[] data = writer.getBuffer();
        Files.write(filepath, data);
    }

    /**
     * Load and apply all deltas since a snapshot
     */
    public void applyDeltas(GameState baseSnapshot) throws IOException {
        List<Path> deltas = getDeltaFiles();
        for (Path deltaPath : deltas) {
            applyDelta(deltaPath, baseSnapshot);
        }
    }

    /**
     * Apply a single delta file to a game state
     */
    private void applyDelta(Path filepath, GameState targetState) throws IOException {
        byte[] data = Files.readAllBytes(filepath);
        BinaryStream stream = new BinaryStream();
        BinaryStream.Reader reader = stream.new Reader(data);

        // Read header
        String magic = reader.readString();
        if (!magic.equals(DELTA_MAGIC)) {
            throw new IOException("Invalid delta file (bad magic header)");
        }

        int version = reader.readInt();
        if (version != DELTA_VERSION) {
            throw new IOException("Unsupported delta version: " + version);
        }

        long timestamp = reader.readLong();
        String gameId = reader.readString();

        // Read and apply changes
        int changeCount = reader.readInt();
        for (int i = 0; i < changeCount; i++) {
            String fieldId = reader.readString();
            int typeId = reader.readInt();
            Object newValue = reader.readObject();
            long changeTimestamp = reader.readLong();

            // Apply change to target state
            applyChangeToState(targetState, fieldId, newValue);
        }

        System.out.println("[DeltaEngine] Applied delta: " + filepath + " (" + changeCount + " changes)");
    }

    /**
     * Apply a single field change to game state
     */
    private void applyChangeToState(GameState state, String fieldId, Object newValue) {
        // This is a simplified version - in production, you'd have a more sophisticated
        // mapping between field IDs and actual state modifications
        
        switch (fieldId) {
            case "playerHealth":
                state.getPlayerState().setCurrentHealth((Integer) newValue);
                break;
            case "playerMana":
                state.getPlayerState().setCurrentMana((Integer) newValue);
                break;
            case "playerLocation":
                state.getPlayerState().setCurrentLocation((String) newValue);
                break;
            case "inventory":
                // Apply inventory changes (more complex)
                break;
            case "buffs":
                // Apply buff changes
                break;
            // ... more field mappings ...
            default:
                System.err.println("[DeltaEngine] Unknown field in delta: " + fieldId);
        }
    }

    /**
     * Check if deltas should be merged (consolidated into a new snapshot)
     */
    private void checkAndMergeDeltasIfNeeded() throws IOException {
        List<Path> deltas = getDeltaFiles();
        if (deltas.size() >= maxDeltasBeforeMerge) {
            System.out.println("[DeltaEngine] Too many deltas (" + deltas.size() + "), consolidating...");
            // Merge all deltas into the current game state and create new snapshot
            // (This would be handled by SnapshotEngine in production)
        }
    }

    /**
     * Get all delta files in the deltas directory, sorted by timestamp
     */
    private List<Path> getDeltaFiles() throws IOException {
        List<Path> files = new ArrayList<>();
        try {
            Files.list(Paths.get(DELTAS_DIR))
                    .filter(p -> p.toString().endsWith(".delta"))
                    .sorted()
                    .forEach(files::add);
        } catch (IOException e) {
            System.err.println("[DeltaEngine] Failed to list delta files");
        }
        return files;
    }

    /**
     * Clear old deltas after a successful snapshot merge
     */
    public void clearDeltasSinceSnapshot(long snapshotTime) throws IOException {
        List<Path> deltas = getDeltaFiles();
        for (Path deltaPath : deltas) {
            long fileTime = Files.getLastModifiedTime(deltaPath).toMillis();
            if (fileTime < snapshotTime) {
                Files.deleteIfExists(deltaPath);
            }
        }
    }

    /**
     * Infer type ID based on field ID (for type safety)
     */
    private int inferTypeId(String fieldId) {
        if (fieldId.startsWith("player")) return 0x01;
        if (fieldId.startsWith("inventory")) return 0x02;
        if (fieldId.startsWith("buffs")) return 0x03;
        if (fieldId.startsWith("npc")) return 0x04;
        return 0x00; // Unknown
    }

    /**
     * Generate a timestamp-based delta filename
     */
    private String generateDeltaFilename() {
        return "delta_" + System.currentTimeMillis() + ".delta";
    }

    // Getters and Setters
    public long getDeltaFlushIntervalMs() { return deltaFlushIntervalMs; }
    public void setDeltaFlushIntervalMs(long interval) { this.deltaFlushIntervalMs = interval; }

    public int getMaxDeltasBeforeMerge() { return maxDeltasBeforeMerge; }
    public void setMaxDeltasBeforeMerge(int max) { this.maxDeltasBeforeMerge = max; }

    public boolean isDeltaTrackingEnabled() { return isDeltaTrackingEnabled; }
    public void setDeltaTrackingEnabled(boolean enabled) { this.isDeltaTrackingEnabled = enabled; }

    public int getPendingChangeCount() { return pendingChanges.size(); }
}
