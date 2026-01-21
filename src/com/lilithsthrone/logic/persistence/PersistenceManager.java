package com.lilithsthrone.logic.persistence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.lilithsthrone.persistence.binary.BinaryStream;
import com.lilithsthrone.logic.state.GameState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Persistence layer manager - coordinates save/load/autosave operations.
 *
 * Responsibilities:
 *  - Manage file system directories (permanent, cache, temp)
 *  - Coordinate SnapshotEngine and DeltaEngine
 *  - Handle autosave scheduling and async flushing
 *  - Load and reconstruct game state from saved files
 *  - Provide clean API for save/load operations
 *  - Handle platform-specific storage (desktop vs mobile)
 *
 * Architecture:
 *
 *  RAM BUFFER (Current Session)
 *  ├─ GameState (in memory)
 *  ├─ DeltaEngine (pending changes)
 *  └─ SnapshotEngine (periodic snapshots)
 *         │
 *         ├─→ AUTOSAVE (Async, optional recovery)
 *         │   ├─ Desktop: /temp/ folder (deleted on exit)
 *         │   └─ Mobile: App cache dir (managed by OS)
 *         │
 *         └─→ PERMANENT SAVE (Blocking, authoritative)
 *             ├─ Desktop: /saves/ folder
 *             └─ Mobile: App private dir
 *
 * File Structure:
 *
 *  saves/
 *  ├─ permanent/
 *  │  ├─ game_slot_1.snapshot
 *  │  ├─ game_slot_1.delta
 *  │  ├─ game_slot_2.snapshot
 *  │  └─ game_slot_2.delta
 *  ├─ auto/
 *  │  ├─ auto_2024_01_21_143522.snapshot
 *  │  └─ auto_2024_01_21_143522.delta (accumulated)
 *  └─ checkpoints/
 *     ├─ checkpoint_slot_1.snapshot
 *     └─ checkpoint_slot_1.delta
 */
public class PersistenceManager {

    // File structure
    private static final String SAVES_DIR = "saves";
    private static final String PERMANENT_DIR = "saves/permanent";
    private static final String AUTO_DIR = "saves/auto";
    private static final String CHECKPOINT_DIR = "saves/checkpoints";
    private static final String TEMP_DIR = "saves/temp";
    private static final String CACHE_DIR = "saves/cache";

    // Engines
    private SnapshotEngine snapshotEngine;
    private DeltaEngine deltaEngine;

    // Autosave management
    private AutoSaveManager autoSaveManager;
    private boolean autoSaveEnabled;
    private float autoSaveInterval;  // Seconds between autosaves

    // Current game state
    private GameState gameState;
    private boolean isSessionActive;

    /**
     * Constructor.
     *
     * @param snapshotEngine The snapshot engine instance
     * @param deltaEngine The delta engine instance
     * @param gameState The game state instance
     */
    public PersistenceManager(SnapshotEngine snapshotEngine, DeltaEngine deltaEngine, GameState gameState) {
        this.snapshotEngine = snapshotEngine;
        this.deltaEngine = deltaEngine;
        this.gameState = gameState;
        this.autoSaveEnabled = true;
        this.autoSaveInterval = 300f;  // 5 minutes default
        this.isSessionActive = false;

        initializeDirectories();
        this.autoSaveManager = new AutoSaveManager(this, autoSaveInterval);
    }

    /**
     * Initialize save directories.
     * Creates all necessary directories if they don't exist.
     */
    private void initializeDirectories() {
        createDirectoryIfNotExists(SAVES_DIR);
        createDirectoryIfNotExists(PERMANENT_DIR);
        createDirectoryIfNotExists(AUTO_DIR);
        createDirectoryIfNotExists(CHECKPOINT_DIR);

        // Platform-specific temp/cache
        if (isPlatformMobile()) {
            createDirectoryIfNotExists(CACHE_DIR);
            Gdx.app.log("PersistenceManager", "Using cache directory for autosave (mobile)");
        } else {
            createDirectoryIfNotExists(TEMP_DIR);
            Gdx.app.log("PersistenceManager", "Using temp directory for autosave (desktop)");
        }

        Gdx.app.log("PersistenceManager", "Directories initialized");
    }

    /**
     * Create directory if it doesn't exist.
     */
    private void createDirectoryIfNotExists(String path) {
        try {
            Path dir = Paths.get(path);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            Gdx.app.error("PersistenceManager", "Failed to create directory " + path + ": " + e.getMessage());
        }
    }

    /**
     * Start a new game session.
     * Initializes engines and starts autosave timer.
     */
    public void startSession() {
        isSessionActive = true;
        snapshotEngine.initialize(gameState);
        deltaEngine.initialize(gameState);
        autoSaveManager.start();
        Gdx.app.log("PersistenceManager", "Game session started");
    }

    /**
     * End current game session.
     * Stops autosave and performs final save if needed.
     */
    public void endSession(boolean saveBeforeExit) {
        if (!isSessionActive) {
            return;
        }

        try {
            autoSaveManager.stop();

            if (saveBeforeExit) {
                // Flush pending deltas before exit
                deltaEngine.flush();
            }

            isSessionActive = false;
            Gdx.app.log("PersistenceManager", "Game session ended");
        } catch (Exception e) {
            Gdx.app.error("PersistenceManager", "Error ending session: " + e.getMessage());
        }
    }

    /**
     * Update function (call once per frame).
     * Used by autosave manager to check timing.
     *
     * @param delta Time since last frame in seconds
     */
    public void update(float delta) {
        if (isSessionActive && autoSaveEnabled) {
            autoSaveManager.update(delta);
        }
    }

    // ================== SAVE OPERATIONS ==================

    /**
     * Manual save to permanent storage.
     * Creates full snapshot + accumulated deltas.
     * Blocks until complete (should run on background thread for large saves).
     *
     * @param slotName Save slot name (e.g., "game_slot_1", "quicksave")
     * @throws IOException If save fails
     */
    public void manualSave(String slotName) throws IOException {
        Gdx.app.log("PersistenceManager", "Manual save starting: " + slotName);

        try {
            // Flush pending deltas to engine
            deltaEngine.flush();

            // Create snapshot
            byte[] snapshotData = snapshotEngine.getSnapshotBytes();
            saveToPermanentStorage(slotName, snapshotData, true);

            // Get accumulated deltas
            byte[] deltaData = deltaEngine.getDeltasSinceSnapshot();
            if (deltaData.length > 0) {
                saveToPermanentStorage(slotName, deltaData, false);
            }

            Gdx.app.log("PersistenceManager", "Manual save completed: " + slotName);
        } catch (IOException e) {
            Gdx.app.error("PersistenceManager", "Manual save failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Autosave to temp/cache directory (for crash recovery).
     * Runs asynchronously in background.
     * Should be called periodically by AutoSaveManager.
     */
    public void autoSave() {
        if (!isSessionActive) {
            return;
        }

        // Run on background thread (non-blocking)
        new Thread(() -> {
            try {
                Gdx.app.log("PersistenceManager", "Autosave starting");

                // Flush pending deltas
                deltaEngine.flush();

                // Get current snapshot
                byte[] snapshotData = snapshotEngine.getSnapshotBytes();
                String timestamp = getTimestamp();
                String autoFileName = "auto_" + timestamp;

                if (isPlatformMobile()) {
                    saveToCacheDirectory(autoFileName, snapshotData, true);
                    byte[] deltaData = deltaEngine.getDeltasSinceSnapshot();
                    if (deltaData.length > 0) {
                        saveToCacheDirectory(autoFileName, deltaData, false);
                    }
                } else {
                    saveToTempDirectory(autoFileName, snapshotData, true);
                    byte[] deltaData = deltaEngine.getDeltasSinceSnapshot();
                    if (deltaData.length > 0) {
                        saveToTempDirectory(autoFileName, deltaData, false);
                    }
                }

                Gdx.app.log("PersistenceManager", "Autosave completed: " + autoFileName);
            } catch (Exception e) {
                Gdx.app.error("PersistenceManager", "Autosave failed: " + e.getMessage());
                // Non-blocking, so don't throw
            }
        }).start();
    }

    /**
     * Save checkpoint (explicit save by player during game).
     * Similar to manual save but to checkpoint directory.
     *
     * @param checkpointName Checkpoint name
     * @throws IOException If save fails
     */
    public void saveCheckpoint(String checkpointName) throws IOException {
        Gdx.app.log("PersistenceManager", "Checkpoint save starting: " + checkpointName);

        try {
            deltaEngine.flush();

            byte[] snapshotData = snapshotEngine.getSnapshotBytes();
            saveToCheckpointDirectory(checkpointName, snapshotData, true);

            byte[] deltaData = deltaEngine.getDeltasSinceSnapshot();
            if (deltaData.length > 0) {
                saveToCheckpointDirectory(checkpointName, deltaData, false);
            }

            Gdx.app.log("PersistenceManager", "Checkpoint save completed: " + checkpointName);
        } catch (IOException e) {
            Gdx.app.error("PersistenceManager", "Checkpoint save failed: " + e.getMessage());
            throw e;
        }
    }

    // ================== LOAD OPERATIONS ==================

    /**
     * Load game from permanent storage.
     * Reconstructs state from snapshot + deltas.
     *
     * @param slotName Save slot name
     * @return Reconstructed GameState
     * @throws IOException If load fails
     */
    public GameState loadGame(String slotName) throws IOException {
        Gdx.app.log("PersistenceManager", "Loading game: " + slotName);

        try {
            // Load snapshot
            byte[] snapshotData = loadFromPermanentStorage(slotName, true);
            if (snapshotData == null) {
                throw new FileNotFoundException("No snapshot found for: " + slotName);
            }

            // Deserialize snapshot
            GameState state = deserializeSnapshot(snapshotData);

            // Load and apply deltas if available
            byte[] deltaData = loadFromPermanentStorage(slotName, false);
            if (deltaData != null && deltaData.length > 0) {
                applyDeltasToState(state, deltaData);
            }

            // Update engines with loaded state
            snapshotEngine.setCurrentState(state);
            deltaEngine.setCurrentState(state);
            deltaEngine.clearDeltas();

            this.gameState = state;

            Gdx.app.log("PersistenceManager", "Game loaded successfully: " + slotName);
            return state;
        } catch (IOException e) {
            Gdx.app.error("PersistenceManager", "Load failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Load from latest autosave.
     * Useful for crash recovery.
     *
     * @return Reconstructed GameState, or null if no autosave exists
     * @throws IOException If load fails
     */
    public GameState loadLatestAutosave() throws IOException {
        Gdx.app.log("PersistenceManager", "Loading latest autosave");

        try {
            // Find latest autosave file
            String latestAutosave = findLatestAutosave();
            if (latestAutosave == null) {
                Gdx.app.log("PersistenceManager", "No autosave found");
                return null;
            }

            return loadAutosave(latestAutosave);
        } catch (IOException e) {
            Gdx.app.error("PersistenceManager", "Autosave load failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Load from specific autosave.
     *
     * @param fileName Autosave file name (without extension)
     * @return Reconstructed GameState
     * @throws IOException If load fails
     */
    public GameState loadAutosave(String fileName) throws IOException {
        try {
            byte[] snapshotData;
            byte[] deltaData;

            if (isPlatformMobile()) {
                snapshotData = loadFromCacheDirectory(fileName, true);
                deltaData = loadFromCacheDirectory(fileName, false);
            } else {
                snapshotData = loadFromTempDirectory(fileName, true);
                deltaData = loadFromTempDirectory(fileName, false);
            }

            if (snapshotData == null) {
                throw new FileNotFoundException("Autosave snapshot not found: " + fileName);
            }

            GameState state = deserializeSnapshot(snapshotData);

            if (deltaData != null && deltaData.length > 0) {
                applyDeltasToState(state, deltaData);
            }

            snapshotEngine.setCurrentState(state);
            deltaEngine.setCurrentState(state);
            deltaEngine.clearDeltas();

            this.gameState = state;

            Gdx.app.log("PersistenceManager", "Autosave loaded: " + fileName);
            return state;
        } catch (IOException e) {
            Gdx.app.error("PersistenceManager", "Autosave load failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Load from checkpoint.
     *
     * @param checkpointName Checkpoint name
     * @return Reconstructed GameState
     * @throws IOException If load fails
     */
    public GameState loadCheckpoint(String checkpointName) throws IOException {
        try {
            byte[] snapshotData = loadFromCheckpointDirectory(checkpointName, true);
            if (snapshotData == null) {
                throw new FileNotFoundException("Checkpoint not found: " + checkpointName);
            }

            GameState state = deserializeSnapshot(snapshotData);

            byte[] deltaData = loadFromCheckpointDirectory(checkpointName, false);
            if (deltaData != null && deltaData.length > 0) {
                applyDeltasToState(state, deltaData);
            }

            snapshotEngine.setCurrentState(state);
            deltaEngine.setCurrentState(state);
            deltaEngine.clearDeltas();

            this.gameState = state;

            Gdx.app.log("PersistenceManager", "Checkpoint loaded: " + checkpointName);
            return state;
        } catch (IOException e) {
            Gdx.app.error("PersistenceManager", "Checkpoint load failed: " + e.getMessage());
            throw e;
        }
    }

    // ================== FILE I/O (Platform-Specific) ==================

    /**
     * Save to permanent storage (authorized save).
     */
    private void saveToPermanentStorage(String slotName, byte[] data, boolean isSnapshot) throws IOException {
        String extension = isSnapshot ? ".snapshot" : ".delta";
        String filePath = PERMANENT_DIR + "/" + slotName + extension;

        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(data);
            bos.flush();
        }
    }

    /**
     * Load from permanent storage.
     */
    private byte[] loadFromPermanentStorage(String slotName, boolean isSnapshot) throws IOException {
        String extension = isSnapshot ? ".snapshot" : ".delta";
        String filePath = PERMANENT_DIR + "/" + slotName + extension;

        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            bis.read(buffer);
        }
        return buffer;
    }

    /**
     * Save to temp directory (desktop autosave for crash recovery).
     */
    private void saveToTempDirectory(String fileName, byte[] data, boolean isSnapshot) throws IOException {
        String extension = isSnapshot ? ".snapshot" : ".delta";
        String filePath = TEMP_DIR + "/" + fileName + extension;

        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(data);
            bos.flush();
        }
    }

    /**
     * Load from temp directory.
     */
    private byte[] loadFromTempDirectory(String fileName, boolean isSnapshot) throws IOException {
        String extension = isSnapshot ? ".snapshot" : ".delta";
        String filePath = TEMP_DIR + "/" + fileName + extension;

        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            bis.read(buffer);
        }
        return buffer;
    }

    /**
     * Save to cache directory (mobile autosave).
     */
    private void saveToCacheDirectory(String fileName, byte[] data, boolean isSnapshot) throws IOException {
        String extension = isSnapshot ? ".snapshot" : ".delta";
        String filePath = CACHE_DIR + "/" + fileName + extension;

        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(data);
            bos.flush();
        }
    }

    /**
     * Load from cache directory.
     */
    private byte[] loadFromCacheDirectory(String fileName, boolean isSnapshot) throws IOException {
        String extension = isSnapshot ? ".snapshot" : ".delta";
        String filePath = CACHE_DIR + "/" + fileName + extension;

        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            bis.read(buffer);
        }
        return buffer;
    }

    /**
     * Save to checkpoint directory.
     */
    private void saveToCheckpointDirectory(String checkpointName, byte[] data, boolean isSnapshot) throws IOException {
        String extension = isSnapshot ? ".snapshot" : ".delta";
        String filePath = CHECKPOINT_DIR + "/" + checkpointName + extension;

        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(data);
            bos.flush();
        }
    }

    /**
     * Load from checkpoint directory.
     */
    private byte[] loadFromCheckpointDirectory(String checkpointName, boolean isSnapshot) throws IOException {
        String extension = isSnapshot ? ".snapshot" : ".delta";
        String filePath = CHECKPOINT_DIR + "/" + checkpointName + extension;

        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            bis.read(buffer);
        }
        return buffer;
    }

    // ================== STATE RECONSTRUCTION ==================

    /**
     * Deserialize snapshot into GameState.
     */
    private GameState deserializeSnapshot(byte[] data) throws IOException {
        BinaryStream stream = new BinaryStream(data);
        GameState state = new GameState();
        state.deserialize(stream);
        return state;
    }

    /**
     * Apply deltas to a game state.
     */
    private void applyDeltasToState(GameState state, byte[] deltaData) throws IOException {
        // Deltas are applied by DeltaEngine
        deltaEngine.applyDeltasFromBytes(deltaData, state);
    }

    // ================== UTILITY ==================

    /**
     * Find latest autosave file in temp/cache directory.
     */
    private String findLatestAutosave() {
        String searchDir = isPlatformMobile() ? CACHE_DIR : TEMP_DIR;
        File dir = new File(searchDir);

        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }

        File[] files = dir.listFiles((d, name) -> name.startsWith("auto_") && name.endsWith(".snapshot"));
        if (files == null || files.length == 0) {
            return null;
        }

        // Sort by modification time, get newest
        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return files[0].getName().replace(".snapshot", "");
    }

    /**
     * Get timestamp string for autosave files.
     */
    private String getTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy_MM_dd_HHmmss");
        return sdf.format(new java.util.Date());
    }

    /**
     * Check if running on mobile platform.
     */
    private boolean isPlatformMobile() {
        return Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android ||
               Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.iOS;
    }

    // ================== CONFIGURATION ==================

    /**
     * Enable/disable autosave.
     */
    public void setAutoSaveEnabled(boolean enabled) {
        this.autoSaveEnabled = enabled;
        if (enabled && isSessionActive) {
            autoSaveManager.start();
        } else {
            autoSaveManager.stop();
        }
    }

    /**
     * Set autosave interval in seconds.
     */
    public void setAutoSaveInterval(float seconds) {
        this.autoSaveInterval = seconds;
        autoSaveManager.setInterval(seconds);
    }

    /**
     * Get list of save slots.
     */
    public List<String> getSaveSlots() {
        List<String> slots = new ArrayList<>();
        File dir = new File(PERMANENT_DIR);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".snapshot"));
            if (files != null) {
                for (File file : files) {
                    String slotName = file.getName().replace(".snapshot", "");
                    slots.add(slotName);
                }
            }
        }

        Collections.sort(slots);
        return slots;
    }

    /**
     * Delete a save slot.
     */
    public boolean deleteSaveSlot(String slotName) {
        try {
            new File(PERMANENT_DIR + "/" + slotName + ".snapshot").delete();
            new File(PERMANENT_DIR + "/" + slotName + ".delta").delete();
            Gdx.app.log("PersistenceManager", "Save slot deleted: " + slotName);
            return true;
        } catch (Exception e) {
            Gdx.app.error("PersistenceManager", "Failed to delete save slot: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clear autosaves (cleanup for old temp files).
     */
    public void clearAutosaves() {
        String searchDir = isPlatformMobile() ? CACHE_DIR : TEMP_DIR;
        File dir = new File(searchDir);

        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.startsWith("auto_"));
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }

        Gdx.app.log("PersistenceManager", "Autosaves cleared");
    }
}
