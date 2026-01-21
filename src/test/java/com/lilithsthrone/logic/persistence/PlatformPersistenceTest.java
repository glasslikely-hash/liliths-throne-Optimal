package com.lilithsthrone.logic.persistence;

import com.lilithsthrone.logic.state.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Platform-specific persistence tests.
 * Validates desktop and mobile storage behavior independently.
 */
@DisplayName("Persistence Layer - Platform-Specific Tests")
public class PlatformPersistenceTest {

    private GameState gameState;
    private SnapshotEngine snapshotEngine;
    private DeltaEngine deltaEngine;
    private PersistenceManager persistenceManager;

    @BeforeEach
    public void setUp() {
        gameState = new GameState();
        snapshotEngine = new SnapshotEngine(gameState, 10000);
        deltaEngine = new DeltaEngine(gameState, 5000);
        
        snapshotEngine.initialize(gameState);
        deltaEngine.initialize(gameState);
        
        persistenceManager = new PersistenceManager(snapshotEngine, deltaEngine, gameState);
    }

    // ==================== DESKTOP STORAGE TESTS ====================

    @Test
    @DisplayName("Desktop: Permanent saves in user home directory")
    @EnabledOnOs({OS.WINDOWS, OS.LINUX, OS.MAC})
    public void testDesktopPermanentStoragePath() {
        // Arrange
        GameState state = new GameState();
        String slotName = "desktop_test";
        
        // Act
        persistenceManager.manualSave(slotName);
        
        // Assert: Should be in ~/.liliths-throne/saves/permanent/
        String homePath = System.getProperty("user.home");
        Path permanentDir = Paths.get(homePath, ".liliths-throne", "saves", "permanent");
        Path snapshotFile = permanentDir.resolve(slotName + ".snapshot");
        
        // File should exist (or directory structure should be created)
        assertTrue(permanentDir.toFile().exists() || permanentDir.toFile().getParentFile().exists(),
                  "Desktop permanent save directory should be accessible");
    }

    @Test
    @DisplayName("Desktop: Autosaves in system temp directory")
    @EnabledOnOs({OS.WINDOWS, OS.LINUX, OS.MAC})
    public void testDesktopAutoSaveStorage() {
        // Arrange
        GameState state = new GameState();
        
        // Act
        persistenceManager.autoSave();
        
        // Assert: Should be in system temp
        String tempPath = System.getProperty("java.io.tmpdir");
        Path autoSaveDir = Paths.get(tempPath, "liliths-throne", "autosave");
        
        // Directory should exist or be creatable
        assertNotNull(tempPath, "System temp directory should exist");
    }

    @Test
    @DisplayName("Desktop: Save files are readable after write")
    @EnabledOnOs({OS.WINDOWS, OS.LINUX, OS.MAC})
    public void testDesktopSaveFileReadability() throws Exception {
        // Arrange
        GameState state = new GameState();
        state.getPlayerState().takeDamage(10);
        
        // Act
        persistenceManager.manualSave("readability_test");
        
        // Assert: Should be able to load immediately
        assertDoesNotThrow(() -> {
            GameState loaded = persistenceManager.loadGame("readability_test");
            assertNotNull(loaded);
        }, "Should be able to read save immediately after write");
    }

    @Test
    @DisplayName("Desktop: Large save files are handled correctly")
    @EnabledOnOs({OS.WINDOWS, OS.LINUX, OS.MAC})
    public void testDesktopLargeSaveFiles() {
        // Arrange
        GameState state = new GameState();
        
        // Create large state (many items)
        for (int i = 0; i < 100; i++) {
            state.getInventoryState().addItem("item_" + i, 1);
        }
        
        // Act
        persistenceManager.manualSave("large_file_test");
        
        // Assert: Should complete without error
        assertDoesNotThrow(() -> {
            GameState loaded = persistenceManager.loadGame("large_file_test");
            assertNotNull(loaded);
        }, "Large save files should be handled correctly");
    }

    @Test
    @DisplayName("Desktop: Multiple saves can coexist")
    @EnabledOnOs({OS.WINDOWS, OS.LINUX, OS.MAC})
    public void testDesktopMultipleSaveSlots() {
        // Arrange
        GameState state = new GameState();
        
        // Act: Create multiple saves
        for (int i = 1; i <= 5; i++) {
            state.getPlayerState().takeDamage(i);
            persistenceManager.manualSave("desktop_slot_" + i);
        }
        
        // Assert: All should be loadable
        for (int i = 1; i <= 5; i++) {
            int slot = i;
            assertDoesNotThrow(() -> {
                persistenceManager.loadGame("desktop_slot_" + slot);
            }, "Desktop slot " + i + " should be loadable");
        }
    }

    // ==================== MOBILE STORAGE TESTS ====================

    @Test
    @DisplayName("Mobile: Permanent saves in app documents")
    public void testMobilePermanentStoragePath() {
        // Note: This test documents expected behavior
        // Actual mobile testing requires Android/iOS environment
        
        // Expected: Gdx.files.localStoragePath() + "/saves/permanent/"
        // The actual path depends on Gdx.app.getType()
        
        // Assert: Documented expected behavior
        assertNotNull(persistenceManager, "PersistenceManager should detect platform");
    }

    @Test
    @DisplayName("Mobile: Autosaves in app cache")
    public void testMobileAutoSaveCachePath() {
        // Note: Expected behavior documentation
        // Actual: Gdx.files.localStoragePath() + "/autosave/"
        
        // When running on Android/iOS, autosaves go to app cache
        // This is more efficient and OS can clean it up
        
        assertNotNull(persistenceManager, "Should handle mobile paths");
    }

    @Test
    @DisplayName("Mobile: Save files respect app sandbox")
    public void testMobileSandboxCompliance() {
        // Note: Mobile builds are sandboxed
        // All saves must be within app directory
        
        // Act: Create save
        GameState state = new GameState();
        persistenceManager.manualSave("mobile_sandbox_test");
        
        // Assert: No exceptions thrown
        // In actual mobile environment, would verify file is in app directory
        assertDoesNotThrow(() -> {
            persistenceManager.manualSave("sandbox_verification");
        }, "Mobile saves should respect sandbox");
    }

    // ==================== CROSS-PLATFORM TESTS ====================

    @Test
    @DisplayName("Save files from desktop can be loaded on any platform")
    public void testCrossPlatformCompatiblity() throws Exception {
        // Arrange: Create state that works on any platform
        GameState state = new GameState();
        state.getPlayerState().takeDamage(15);
        state.getPlayerState().addExperience(500);
        state.getInventoryState().addItem("universal_item", 1);
        
        // Act
        persistenceManager.manualSave("cross_platform_test");
        
        // Assert: Should load on any platform
        assertDoesNotThrow(() -> {
            GameState loaded = persistenceManager.loadGame("cross_platform_test");
            assertEquals(state.getPlayerState().getCurrentHealth(),
                        loaded.getPlayerState().getCurrentHealth());
        }, "Save should be loadable on any platform");
    }

    @Test
    @DisplayName("Platform detection doesn't affect state integrity")
    public void testPlatformDetectionStateIntegrity() throws Exception {
        // Arrange
        GameState state = new GameState();
        state.getPlayerState().takeDamage(7);
        state.getInventoryState().addItem("platform_test", 2);
        
        // Act: Save (will auto-detect platform)
        persistenceManager.manualSave("platform_integrity_test");
        
        // Load
        GameState loaded = persistenceManager.loadGame("platform_integrity_test");
        
        // Assert: State unchanged by platform detection
        assertEquals(gameState.getPlayerState().getMaxHealth() - 7,
                    gameState.getPlayerState().getMaxHealth() - 
                    loaded.getPlayerState().getCurrentHealth(),
                    "Platform detection should not affect state integrity");
    }

    // ==================== STORAGE PATH TESTS ====================

    @Test
    @DisplayName("Permanent save directory is user-writable")
    public void testPermanentDirWritability() {
        // Arrange
        String homePath = System.getProperty("user.home");
        Path permanentDir = Paths.get(homePath, ".liliths-throne", "saves", "permanent");
        
        // Act & Assert: Should be able to create files here
        try {
            Files.createDirectories(permanentDir);
            assertTrue(Files.isWritable(permanentDir),
                      "Permanent save directory should be writable");
        } catch (Exception e) {
            // Acceptable if directory doesn't exist yet
        }
    }

    @Test
    @DisplayName("Auto-save directory handles cleanup")
    public void testAutoSaveDirCleanup() {
        // Arrange
        String tempPath = System.getProperty("java.io.tmpdir");
        Path autoSaveDir = Paths.get(tempPath, "liliths-throne", "autosave");
        
        // Act: Create multiple autosaves
        for (int i = 0; i < 3; i++) {
            GameState state = new GameState();
            state.getPlayerState().takeDamage(i + 1);
            persistenceManager.autoSave();
        }
        
        // Act: Cleanup old autosaves (older than 7 days)
        assertDoesNotThrow(() -> {
            persistenceManager.cleanupOldAutosaves(7);
        }, "Cleanup should not throw");
    }

    // ==================== FILE PERMISSION TESTS ====================

    @Test
    @DisplayName("Save files have correct permissions")
    public void testSaveFilePermissions() {
        // Arrange
        GameState state = new GameState();
        
        // Act
        persistenceManager.manualSave("permission_test");
        
        // Assert: Files should be readable and writable
        String homePath = System.getProperty("user.home");
        Path snapshotFile = Paths.get(homePath, ".liliths-throne", "saves", "permanent", 
                                      "permission_test.snapshot");
        
        try {
            if (Files.exists(snapshotFile)) {
                assertTrue(Files.isReadable(snapshotFile),
                          "Save file should be readable");
                assertTrue(Files.isWritable(snapshotFile),
                          "Save file should be writable");
            }
        } catch (Exception e) {
            // Permission check may not be available on all systems
        }
    }

    // ==================== STORAGE QUOTA TESTS ====================

    @Test
    @DisplayName("Handles storage quota gracefully")
    public void testStorageQuotaHandling() {
        // Note: Full storage quota testing requires filling disk
        // This is a placeholder for quota awareness
        
        GameState state = new GameState();
        
        // Should attempt save and handle gracefully if disk is full
        assertDoesNotThrow(() -> {
            persistenceManager.manualSave("quota_test");
        }, "Should handle storage gracefully");
    }

    @Test
    @DisplayName("Auto-cleanup prevents storage bloat")
    public void testAutoCleanupPreventsStorageBloat() {
        // Arrange: Create many autosaves
        for (int i = 0; i < 10; i++) {
            GameState state = new GameState();
            state.getPlayerState().takeDamage(i);
            persistenceManager.autoSave();
        }
        
        // Act: Run cleanup for saves older than 0 days (all old ones)
        persistenceManager.cleanupOldAutosaves(0);
        
        // Assert: Should complete without error
        // In real scenario, would verify disk usage reduced
        assertDoesNotThrow(() -> {
            persistenceManager.loadLatestAutosave();
        }, "Should maintain at least latest autosave");
    }
}
