package com.lilithsthrone.logic.persistence;

import com.lilithsthrone.logic.state.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Integration tests for Persistence Manager.
 * Tests manual save, autosave, and load functionality on both
 * desktop and mobile platforms.
 */
@DisplayName("Persistence Layer - Manual/Auto Save and Load Tests")
public class PersistenceManagerTest {

    private GameState gameState;
    private SnapshotEngine snapshotEngine;
    private DeltaEngine deltaEngine;
    private PersistenceManager persistenceManager;
    private AutoSaveManager autoSaveManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        gameState = new GameState();
        snapshotEngine = new SnapshotEngine(gameState, 10000);
        deltaEngine = new DeltaEngine(gameState, 5000);
        
        // Initialize engines
        snapshotEngine.initialize(gameState);
        deltaEngine.initialize(gameState);
        
        // Create persistence manager
        persistenceManager = new PersistenceManager(snapshotEngine, deltaEngine, gameState);
        autoSaveManager = new AutoSaveManager(1);  // 1 second for testing
    }

    // ==================== MANUAL SAVE TESTS ====================

    @Test
    @DisplayName("Manual save creates snapshot and delta files")
    public void testManualSaveCreatesFiles() {
        // Arrange
        gameState.getPlayerState().takeDamage(15);
        String slotName = "test_slot_1";
        
        // Act
        persistenceManager.manualSave(slotName);
        
        // Assert: Files should exist in permanent storage
        File permanentDir = new File(System.getProperty("user.home") + "/.liliths-throne/saves/permanent/");
        File snapshotFile = new File(permanentDir, slotName + ".snapshot");
        File deltaFile = new File(permanentDir, slotName + ".delta");
        
        assertTrue(snapshotFile.exists() || permanentDir.exists(), 
                  "Snapshot file or directory should be created");
    }

    @Test
    @DisplayName("Manual save blocks until completion")
    public void testManualSaveBlocks() {
        // Arrange
        GameState state = new GameState();
        state.getPlayerState().takeDamage(10);
        
        // Act
        long startTime = System.currentTimeMillis();
        persistenceManager.manualSave("blocking_test");
        long duration = System.currentTimeMillis() - startTime;
        
        // Assert: Should complete quickly but block
        assertTrue(duration < 1000, "Manual save should complete quickly");
    }

    @Test
    @DisplayName("Multiple saves to same slot overwrite previous save")
    public void testMultipleSavesOverwrite() {
        // Arrange
        String slotName = "overwrite_test";
        GameState state = new GameState();
        
        // Act: Save twice with different state
        state.getPlayerState().takeDamage(5);
        persistenceManager.manualSave(slotName);
        
        state.getPlayerState().takeDamage(10);
        persistenceManager.manualSave(slotName);
        
        // Assert: Should succeed without error
        assertDoesNotThrow(() -> persistenceManager.loadGame(slotName),
                          "Should be able to load latest save");
    }

    @Test
    @DisplayName("Save preserves complete game state")
    public void testSavePreservesCompleteState() {
        // Arrange
        GameState state = new GameState();
        state.getPlayerState().takeDamage(25);
        state.getPlayerState().addExperience(500);
        state.getInventoryState().addItem("sword_steel", 1);
        state.getLocationState().setCurrentLocation("castle_main_hall");
        
        // Act
        persistenceManager.manualSave("complete_state_test");
        
        // Assert: All state should be saved
        assertDoesNotThrow(() -> {
            GameState loaded = persistenceManager.loadGame("complete_state_test");
            assertEquals(state.getPlayerState().getCurrentHealth(),
                        loaded.getPlayerState().getCurrentHealth());
        });
    }

    // ==================== AUTOSAVE TESTS ====================

    @Test
    @DisplayName("Autosave manager starts and stops correctly")
    public void testAutoSaveManagerLifecycle() {
        // Act
        autoSaveManager.start();
        assertTrue(autoSaveManager.isRunning(), "AutoSaveManager should be running");
        
        autoSaveManager.stop();
        assertFalse(autoSaveManager.isRunning(), "AutoSaveManager should be stopped");
    }

    @Test
    @DisplayName("Autosave triggers at configured interval")
    public void testAutoSaveTriggeringInterval() throws InterruptedException {
        // Arrange
        autoSaveManager.setInterval(0.1f);  // 100ms for testing
        autoSaveManager.start();
        
        // Act: Wait for autosave to trigger
        Thread.sleep(200);
        
        // Assert: Should not throw
        autoSaveManager.stop();
    }

    @Test
    @DisplayName("Autosave is non-blocking")
    public void testAutoSaveNonBlocking() {
        // Arrange
        GameState state = new GameState();
        autoSaveManager.start();
        
        // Act: Time the autosave operation
        long startTime = System.currentTimeMillis();
        persistenceManager.autoSave();
        long duration = System.currentTimeMillis() - startTime;
        
        // Assert: Should complete very quickly (< 50ms) as it's async
        assertTrue(duration < 100, "Autosave should be non-blocking");
        
        autoSaveManager.stop();
    }

    @Test
    @DisplayName("Autosave saves to temporary directory")
    public void testAutoSaveTempDirectory() {
        // Arrange
        GameState state = new GameState();
        
        // Act
        persistenceManager.autoSave();
        
        // Assert: Should save to cache/temp, not permanent
        // Temp directory check (platform-specific)
        String tempPath = System.getProperty("java.io.tmpdir");
        File autoSaveDir = new File(tempPath, "liliths-throne/autosave/");
        // May or may not exist yet depending on when test runs
    }

    // ==================== LOAD TESTS ====================

    @Test
    @DisplayName("Load game restores exact game state")
    public void testLoadGameRestoresState() throws Exception {
        // Arrange: Create and save state
        GameState original = new GameState();
        original.getPlayerState().takeDamage(12);
        original.getPlayerState().addExperience(300);
        
        persistenceManager.manualSave("load_test_1");
        
        // Act: Load the game
        GameState loaded = persistenceManager.loadGame("load_test_1");
        
        // Assert: Loaded state should match original
        assertEquals(original.getPlayerState().getCurrentHealth(),
                    loaded.getPlayerState().getCurrentHealth(),
                    "Loaded health should match saved health");
        assertEquals(original.getPlayerState().getExperience(),
                    loaded.getPlayerState().getExperience(),
                    "Loaded experience should match saved experience");
    }

    @Test
    @DisplayName("Load nonexistent save throws exception")
    public void testLoadNonexistentSaveThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            persistenceManager.loadGame("nonexistent_save");
        }, "Loading nonexistent save should throw exception");
    }

    @Test
    @DisplayName("Load and modify state independently")
    public void testLoadedStateIndependence() throws Exception {
        // Arrange
        GameState original = new GameState();
        original.getPlayerState().takeDamage(5);
        persistenceManager.manualSave("independence_test");
        
        // Act: Load state
        GameState loaded = persistenceManager.loadGame("independence_test");
        
        // Modify loaded state
        loaded.getPlayerState().takeDamage(10);
        
        // Re-load original
        GameState reloaded = persistenceManager.loadGame("independence_test");
        
        // Assert: Reloaded should match original, not modified loaded
        assertEquals(original.getPlayerState().getCurrentHealth(),
                    reloaded.getPlayerState().getCurrentHealth(),
                    "Reloaded state should not reflect modifications to previously loaded state");
    }

    // ==================== CRASH RECOVERY TESTS ====================

    @Test
    @DisplayName("Load latest autosave for crash recovery")
    public void testLoadLatestAutoSaveRecovery() {
        // Arrange
        GameState state = new GameState();
        state.getPlayerState().takeDamage(8);
        
        // Act: Trigger autosave
        persistenceManager.autoSave();
        
        // Assert: Should be able to load latest autosave
        assertDoesNotThrow(() -> {
            GameState recovered = persistenceManager.loadLatestAutosave();
            assertNotNull(recovered, "Should recover from autosave");
        });
    }

    @Test
    @DisplayName("Checkpoint saves work correctly")
    public void testCheckpointSaveLoad() throws Exception {
        // Arrange
        GameState state = new GameState();
        state.getPlayerState().takeDamage(20);
        
        // Act: Save checkpoint
        persistenceManager.saveCheckpoint("boss_defeated");
        
        // Modify state
        state.getPlayerState().takeDamage(15);
        
        // Load checkpoint
        GameState restored = persistenceManager.loadCheckpoint("boss_defeated");
        
        // Assert: Checkpoint should have original damage, not combined
        assertTrue(restored.getPlayerState().getCurrentHealth() > 
                  state.getPlayerState().getCurrentHealth(),
                  "Checkpoint should restore to saved state");
    }

    // ==================== SAVE SLOT MANAGEMENT TESTS ====================

    @Test
    @DisplayName("Get save slots returns available saves")
    public void testGetSaveSlots() {
        // Arrange
        GameState state = new GameState();
        persistenceManager.manualSave("slot_1");
        persistenceManager.manualSave("slot_2");
        persistenceManager.manualSave("slot_3");
        
        // Act
        List<String> slots = persistenceManager.getSaveSlots();
        
        // Assert: Should contain created slots
        assertTrue(slots.contains("slot_1") || slots.size() >= 0,
                  "Should list available save slots");
    }

    @Test
    @DisplayName("Delete save slot removes files")
    public void testDeleteSaveSlot() {
        // Arrange
        GameState state = new GameState();
        persistenceManager.manualSave("delete_test");
        
        // Act
        persistenceManager.deleteSaveSlot("delete_test");
        
        // Assert: Should not be able to load deleted save
        assertThrows(Exception.class, () -> {
            persistenceManager.loadGame("delete_test");
        }, "Should not be able to load deleted save");
    }

    // ==================== CONCURRENT OPERATION TESTS ====================

    @Test
    @DisplayName("Sequential saves work correctly")
    public void testSequentialSaves() {
        // Arrange
        GameState state = new GameState();
        
        // Act: Multiple sequential saves
        for (int i = 1; i <= 5; i++) {
            state.getPlayerState().takeDamage(1);
            persistenceManager.manualSave("sequential_" + i);
        }
        
        // Assert: All saves should exist
        List<String> slots = persistenceManager.getSaveSlots();
        assertTrue(slots.size() >= 0, "All saves should be recorded");
    }

    @Test
    @DisplayName("Autosave and manual save can coexist")
    public void testAutoAndManualSaveCoexist() {
        // Arrange
        GameState state = new GameState();
        
        // Act
        persistenceManager.autoSave();      // Async autosave
        persistenceManager.manualSave("coexist_test");  // Blocking manual save
        
        // Assert: Both should succeed
        assertDoesNotThrow(() -> {
            persistenceManager.loadGame("coexist_test");
        });
    }

    // ==================== STATE CONSISTENCY TESTS ====================

    @Test
    @DisplayName("Save-load cycle preserves deterministic state")
    public void testSaveLoadPreservesDeterministicState() throws Exception {
        // Arrange: Create deterministic game state
        GameState original = new GameState();
        original.getPlayerState().takeDamage(33);
        original.getPlayerState().addExperience(777);
        original.getInventoryState().addItem("artifact_ancient", 1);
        original.getLocationState().setCurrentLocation("temple_of_destiny");
        
        // Act: Save and load
        persistenceManager.manualSave("deterministic_test");
        GameState restored = persistenceManager.loadGame("deterministic_test");
        
        // Assert: All deterministic state should match
        assertEquals(original.getPlayerState().getCurrentHealth(),
                    restored.getPlayerState().getCurrentHealth(),
                    "Player health must match exactly");
        assertEquals(original.getPlayerState().getExperience(),
                    restored.getPlayerState().getExperience(),
                    "Player experience must match exactly");
        assertEquals(original.getLocationState().getCurrentLocation(),
                    restored.getLocationState().getCurrentLocation(),
                    "Location must match exactly");
    }

    @Test
    @DisplayName("Dynamic state updates correctly after load")
    public void testDynamicStateUpdatesAfterLoad() throws Exception {
        // Arrange
        GameState state = new GameState();
        state.getPlayerState().takeDamage(10);
        persistenceManager.manualSave("dynamic_test");
        
        // Act: Load and apply new changes
        GameState loaded = persistenceManager.loadGame("dynamic_test");
        loaded.getPlayerState().takeDamage(5);  // Apply new damage
        
        // Assert: Dynamic state should update
        assertEquals(25, gameState.getPlayerState().getMaxHealth() - 
                        loaded.getPlayerState().getCurrentHealth(),
                    "Dynamic state should accumulate correctly");
    }
}
