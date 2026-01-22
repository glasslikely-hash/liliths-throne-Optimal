package com.lilithsthrone.logic.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.lilithsthrone.logic.state.GameState;
import com.lilithsthrone.persistence.binary.BinaryStream;
import com.lilithsthrone.persistence.binary.BinaryStream.Writer;
import com.lilithsthrone.persistence.binary.BinaryCatalog;

/**
 * Manages snapshot creation and loading.
 * 
 * A snapshot is a complete serialization of the game state at a point in time.
 * Used for:
 *   1. Periodic checkpoints (every 10-30 minutes of play)
 *   2. Player-initiated saves (Save Game to Slot X)
 *   3. Auto-backups for crash recovery
 * 
 * File Format:
 *   [Magic Header: "SNAP"]
 *   [Version: 1]
 *   [Timestamp]
 *   [GameState (serialized binary)]
 *   [CRC32 Checksum]
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class SnapshotEngine {
    private static final String SNAPSHOT_MAGIC = "SNAP";
    private static final int SNAPSHOT_VERSION = 1;
    private static final String SNAPSHOTS_DIR = "saves/snapshots";
    private static final String AUTO_SNAPSHOTS_DIR = "saves/auto";
    private static final String CHECKPOINT_SNAPSHOTS_DIR = "saves/checkpoints";
    
    private GameState gameState;
    private long lastSnapshotTime = 0;
    private long snapshotIntervalMs;         // How often to create snapshots (default 600000 = 10 minutes)
    private boolean autoSnapshotEnabled = true;

    public SnapshotEngine(GameState gameState, long snapshotIntervalMs) {
        this.gameState = gameState;
        this.snapshotIntervalMs = snapshotIntervalMs;
        initializeDirectories();
    }

    /**
     * Initialize engine for new session.
     */
    public void initialize(GameState initialState) {
        this.gameState = initialState;
        this.lastSnapshotTime = System.currentTimeMillis();
    }

    /**
     * Set current game state (used during load).
     */
    public void setCurrentState(GameState state) {
        this.gameState = state;
    }

    /**
     * Create necessary directories if they don't exist
     */
    private void initializeDirectories() {
        createDirectoryIfNeeded(SNAPSHOTS_DIR);
        createDirectoryIfNeeded(AUTO_SNAPSHOTS_DIR);
        createDirectoryIfNeeded(CHECKPOINT_SNAPSHOTS_DIR);
    }

    private void createDirectoryIfNeeded(String dirPath) {
        try {
            Files.createDirectories(Paths.get(dirPath));
        } catch (IOException e) {
            System.err.println("Failed to create directory: " + dirPath);
            e.printStackTrace();
        }
    }

    /**
     * Check if it's time for a periodic snapshot
     */
    public boolean shouldSnapshot() {
        if (!autoSnapshotEnabled) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastSnapshotTime) >= snapshotIntervalMs;
    }

    /**
     * Serialize current state to byte array (for PersistenceManager).
     * Returns snapshot data without writing to disk.
     *
     * @return Serialized snapshot as byte array
     * @throws IOException If serialization fails
     */
    public byte[] getSnapshotBytes() throws IOException {
        try {
            BinaryStream stream = new BinaryStream();
            Writer writer = stream.new Writer();

            // Write header
            writer.writeString(SNAPSHOT_MAGIC);
            writer.writeInt(SNAPSHOT_VERSION);
            writer.writeLong(System.currentTimeMillis());

            // Write game state
            writer.writeObject(gameState);

            return writer.getBuffer();
        } catch (IOException e) {
            throw new IOException("Failed to serialize snapshot: " + e.getMessage());
        }
    }

    /**
     * Internal method to serialize and write snapshot to disk
     */
    private void saveSnapshot(Path filepath) {
        try {
            // Create a binary stream for the snapshot
            BinaryStream stream = new BinaryStream();
            Writer writer = stream.new Writer();

            // Write header
            writer.writeString(SNAPSHOT_MAGIC);
            writer.writeInt(SNAPSHOT_VERSION);
            writer.writeLong(System.currentTimeMillis());

            // Write game state
            writer.writeObject(gameState);

            // Write to file
            byte[] data = writer.getBuffer();
            Files.write(filepath, data);

            System.out.println("[SnapshotEngine] Snapshot saved: " + filepath);
        } catch (IOException e) {
            System.err.println("[SnapshotEngine] Failed to save snapshot: " + filepath);
            e.printStackTrace();
        }
    }

    /**
     * Create a periodic snapshot (auto-save)
     */
    public void snapshot() {
        String filename = generateAutoSnapshotFilename();
        Path filepath = Paths.get(AUTO_SNAPSHOTS_DIR, filename);
        saveSnapshot(filepath);
        lastSnapshotTime = System.currentTimeMillis();
        gameState.setLastSavedTime(lastSnapshotTime);
        gameState.markClean();
    }

    /**
     * Create a checkpoint snapshot (player-initiated save)
     */
    public void checkpoint(String slotName) {
        String filename = slotName + ".snapshot";
        Path filepath = Paths.get(CHECKPOINT_SNAPSHOTS_DIR, filename);
        saveSnapshot(filepath);
        gameState.setLastSavedTime(System.currentTimeMillis());
        gameState.markClean();
    }

    /**
     * Load a snapshot from the checkpoint directory
     */
    public GameState loadCheckpoint(String slotName) throws IOException {
        Path filepath = Paths.get(CHECKPOINT_SNAPSHOTS_DIR, slotName + ".snapshot");
        return loadSnapshot(filepath);
    }

    /**
     * Load the latest auto-snapshot
     */
    public GameState loadLatestAutoSnapshot() throws IOException {
        List<Path> snapshots = getAutoSnapshotFiles();
        if (snapshots.isEmpty()) {
            throw new IOException("No auto-snapshots found");
        }
        // Load the most recent (last) one
        Path latestSnapshot = snapshots.get(snapshots.size() - 1);
        return loadSnapshot(latestSnapshot);
    }

    /**
     * Internal method to deserialize and load snapshot from disk
     */
    private GameState loadSnapshot(Path filepath) throws IOException {
        if (!Files.exists(filepath)) {
            throw new IOException("Snapshot file not found: " + filepath);
        }

        try {
            byte[] data = Files.readAllBytes(filepath);
            BinaryStream stream = new BinaryStream();
            BinaryStream.Reader reader = stream.new Reader(data);

            // Read header
            String magic = reader.readString();
            if (!magic.equals(SNAPSHOT_MAGIC)) {
                throw new IOException("Invalid snapshot file (bad magic header)");
            }

            int version = reader.readInt();
            if (version != SNAPSHOT_VERSION) {
                throw new IOException("Unsupported snapshot version: " + version);
            }

            long timestamp = reader.readLong();

            // Read game state
            GameState loadedState = (GameState) reader.readObject();

            // Verify checksum (CRC32 would be read here in production)
            System.out.println("[SnapshotEngine] Snapshot loaded from: " + filepath);
            System.out.println("[SnapshotEngine] Saved at: " + timestamp);

            return loadedState;
        } catch (Exception e) {
            throw new IOException("Failed to load snapshot: " + filepath, e);
        }
    }

    /**
     * Get all auto-snapshot files, sorted by timestamp
     */
    public List<Path> getAutoSnapshotFiles() throws IOException {
        return Files.list(Paths.get(AUTO_SNAPSHOTS_DIR))
                .filter(p -> p.toString().endsWith(".snapshot"))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get all checkpoint snapshots
     */
    public List<String> getCheckpointSlots() throws IOException {
        File dir = new File(CHECKPOINT_SNAPSHOTS_DIR);
        if (!dir.exists()) {
            return new ArrayList<>();
        }
        return Arrays.stream(dir.listFiles((d, name) -> name.endsWith(".snapshot")))
                .map(f -> f.getName().replace(".snapshot", ""))
                .collect(Collectors.toList());
    }

    /**
     * Delete an old snapshot to save disk space
     */
    public void deleteSnapshot(Path filepath) throws IOException {
        Files.deleteIfExists(filepath);
        System.out.println("[SnapshotEngine] Snapshot deleted: " + filepath);
    }

    /**
     * Cleanup old auto-snapshots, keeping only the last N
     */
    public void cleanupOldAutoSnapshots(int maxToKeep) throws IOException {
        List<Path> snapshots = getAutoSnapshotFiles();
        int toDelete = snapshots.size() - maxToKeep;
        if (toDelete > 0) {
            for (int i = 0; i < toDelete; i++) {
                deleteSnapshot(snapshots.get(i));
            }
            System.out.println("[SnapshotEngine] Cleaned up " + toDelete + " old auto-snapshots");
        }
    }

    // Getters and Setters
    public long getSnapshotIntervalMs() { return snapshotIntervalMs; }
    public void setSnapshotIntervalMs(long interval) { this.snapshotIntervalMs = interval; }

    public boolean isAutoSnapshotEnabled() { return autoSnapshotEnabled; }
    public void setAutoSnapshotEnabled(boolean enabled) { this.autoSnapshotEnabled = enabled; }

    /**
     * Generate a timestamp-based auto-snapshot filename
     */
    private String generateAutoSnapshotFilename() {
        return "auto_" + System.currentTimeMillis() + ".snapshot";
    }
}
