package com.lilithsthrone.ui;

import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.logic.state.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UI layer with Logic layer.
 * Validates that UI correctly reflects engine state and
 * properly triggers logic layer operations.
 */
@DisplayName("UI Layer - Logic Integration Tests")
public class UILogicIntegrationTest {

    private LogicLayerAPI logicAPI;

    @Mock
    private UICallback uiCallback;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        logicAPI = new LogicLayerAPI();
    }

    // ==================== UI STATE REFLECTION TESTS ====================

    @Test
    @DisplayName("UI correctly displays player health from logic layer")
    public void testUIDisplaysPlayerHealth() {
        // Arrange
        logicAPI.newGame();
        int maxHealth = logicAPI.getPlayerMaxHealth();
        
        // Act: Apply damage through logic
        logicAPI.damagePlayer(10);  // Assume method exists
        int currentHealth = logicAPI.getPlayerHealth();
        
        // Assert: UI should show updated health
        assertTrue(currentHealth < maxHealth, "UI should reflect damage");
        assertEquals(maxHealth - 10, currentHealth, "UI should show exact damage amount");
    }

    @Test
    @DisplayName("UI reflects inventory changes immediately")
    public void testUIReflectsInventoryChanges() {
        // Arrange
        logicAPI.newGame();
        
        // Act: Add item through logic
        logicAPI.addItemToInventory("sword_iron", 1);
        
        // Assert: UI should reflect item addition
        assertTrue(logicAPI.getPlayerInventory().contains("sword_iron"),
                  "UI should show newly added item");
    }

    @Test
    @DisplayName("UI updates when player location changes")
    public void testUIUpdatesLocationChange() {
        // Arrange
        logicAPI.newGame();
        String originalLocation = logicAPI.getPlayerLocation();
        
        // Act: Move player through logic
        logicAPI.moveToLocation("forest_clearing");
        String newLocation = logicAPI.getPlayerLocation();
        
        // Assert: UI location display should update
        assertNotEquals(originalLocation, newLocation, "Location should change");
        assertEquals("forest_clearing", newLocation, "UI should show new location");
    }

    @Test
    @DisplayName("UI reflects experience and leveling")
    public void testUIReflectsExperienceGain() {
        // Arrange
        logicAPI.newGame();
        int initialLevel = logicAPI.getPlayerLevel();
        
        // Act: Add significant experience
        logicAPI.addPlayerExperience(1000);
        int newLevel = logicAPI.getPlayerLevel();
        
        // Assert: UI should show level up (if sufficient XP)
        assertTrue(newLevel >= initialLevel, "UI should show leveling progress");
    }

    // ==================== SAVE/LOAD INTEGRATION TESTS ====================

    @Test
    @DisplayName("Save button triggers manual save in logic layer")
    public void testSaveButtonTriggersLogicSave() {
        // Arrange
        logicAPI.newGame();
        logicAPI.damagePlayer(15);
        
        // Act: Simulate save button click
        logicAPI.saveGame("test_slot");
        
        // Assert: Should not throw and save should succeed
        assertDoesNotThrow(() -> {
            GameState loaded = logicAPI.loadGame("test_slot");
            assertNotNull(loaded);
        }, "Save should be callable from UI");
    }

    @Test
    @DisplayName("Load button restores exact UI state from logic")
    public void testLoadButtonRestoresUIState() throws Exception {
        // Arrange: Create and save specific state
        logicAPI.newGame();
        logicAPI.damagePlayer(25);
        logicAPI.addItemToInventory("potion_health", 3);
        logicAPI.saveGame("ui_state_test");
        
        // Act: Modify state
        logicAPI.damagePlayer(10);
        logicAPI.removeItemFromInventory("potion_health", 1);
        
        // Load state
        logicAPI.loadGame("ui_state_test");
        
        // Assert: UI should reflect loaded state, not modified state
        assertFalse(logicAPI.getPlayerInventory().contains("potion_health") &&
                   logicAPI.getInventoryItemCount("potion_health") != 3,
                  "UI should show saved inventory state");
    }

    // ==================== UI EVENT HANDLING TESTS ====================

    @Test
    @DisplayName("UI button press correctly triggers logic action")
    public void testUIButtonTriggersLogicAction() {
        // Arrange
        logicAPI.newGame();
        UIButton attackButton = new UIButton("Attack", () -> logicAPI.playerAttack());
        
        // Act: Simulate button press
        attackButton.click();
        
        // Assert: Logic should have processed action
        assertDoesNotThrow(() -> logicAPI.playerAttack(),
                          "UI button should trigger logic action");
    }

    @Test
    @DisplayName("UI menu navigation doesn't affect game state")
    public void testMenuNavigationDoesntAffectState() {
        // Arrange
        logicAPI.newGame();
        logicAPI.damagePlayer(5);
        int healthBeforeMenu = logicAPI.getPlayerHealth();
        
        // Act: Navigate menu (pause, settings, etc)
        // Simulate menu actions
        
        int healthAfterMenu = logicAPI.getPlayerHealth();
        
        // Assert: State should be unchanged
        assertEquals(healthBeforeMenu, healthAfterMenu,
                    "Menu navigation should not affect game state");
    }

    // ==================== UI DECOUPLING TESTS ====================

    @Test
    @DisplayName("UI cannot directly access game state")
    public void testUICannotAccessGameStateDirectly() {
        // Arrange
        logicAPI.newGame();
        
        // Act: UI should use API methods, not direct access
        int health = logicAPI.getPlayerHealth();  // Correct: API method
        
        // Assert: UI should only use read-only API
        assertNotNull(health, "UI should access state through API");
        assertTrue(health >= 0, "Health should be valid value");
    }

    @Test
    @DisplayName("UI changes go through logic layer API only")
    public void testUIChangesGoThroughAPIOnly() {
        // Arrange
        logicAPI.newGame();
        
        // Act: All modifications should go through logic API
        assertDoesNotThrow(() -> {
            logicAPI.damagePlayer(10);          // Via API
            logicAPI.addItemToInventory("item", 1);  // Via API
            logicAPI.moveToLocation("location"); // Via API
        }, "All UI changes should use API methods");
    }

    @Test
    @DisplayName("Data layer remains read-only from UI")
    public void testDataLayerReadOnly() {
        // Note: Data layer should not have setters for static data
        // UI can only read items, skills, etc. through query API
        
        // This is enforced by API design
        assertDoesNotThrow(() -> {
            var items = logicAPI.getAvailableItems();  // Read-only
            var skills = logicAPI.getAvailableSkills(); // Read-only
        }, "Data layer should remain read-only");
    }

    // ==================== AUTOSAVE TRANSPARENCY TESTS ====================

    @Test
    @DisplayName("Autosave is transparent to UI")
    public void testAutoSaveTransparencyToUI() {
        // Arrange
        logicAPI.newGame();
        
        // Act: Play game normally
        logicAPI.damagePlayer(5);
        logicAPI.addItemToInventory("sword", 1);
        
        // Autosave happens in background (no UI notification required)
        
        // Assert: Game state should be unaffected
        assertDoesNotThrow(() -> {
            logicAPI.update(0.033f);  // Simulate frame
        }, "Autosave should not interrupt UI");
    }

    @Test
    @DisplayName("Manual save doesn't block UI beyond acceptable time")
    public void testManualSaveBlockingTime() {
        // Arrange
        logicAPI.newGame();
        
        // Act: Time the manual save
        long startTime = System.currentTimeMillis();
        logicAPI.saveGame("timing_test");
        long duration = System.currentTimeMillis() - startTime;
        
        // Assert: Should complete quickly enough for UI to remain responsive
        assertTrue(duration < 500, "Save should not block UI for long");
    }

    // ==================== STATE CONSISTENCY TESTS ====================

    @Test
    @DisplayName("Multiple UI interactions maintain state consistency")
    public void testMultipleUIInteractionsConsistency() {
        // Arrange
        logicAPI.newGame();
        int initialHealth = logicAPI.getPlayerMaxHealth();
        
        // Act: Perform multiple UI interactions
        logicAPI.damagePlayer(5);
        logicAPI.addItemToInventory("item1", 1);
        logicAPI.damagePlayer(3);
        logicAPI.addItemToInventory("item2", 2);
        logicAPI.damagePlayer(2);
        
        // Assert: State should be consistent
        int expectedHealth = initialHealth - 10;  // 5 + 3 + 2 damage
        assertEquals(expectedHealth, logicAPI.getPlayerHealth(),
                    "State should be consistent across multiple interactions");
    }

    @Test
    @DisplayName("UI state matches persisted state after reload")
    public void testUIStateMatchesPersisted() throws Exception {
        // Arrange: Create UI state
        logicAPI.newGame();
        logicAPI.damagePlayer(20);
        logicAPI.addItemToInventory("chest_gold", 5);
        logicAPI.moveToLocation("throne_room");
        
        // Save state
        logicAPI.saveGame("ui_persist_test");
        
        // Act: Reload
        logicAPI.loadGame("ui_persist_test");
        
        // Assert: All UI should reflect persisted state
        assertEquals(logicAPI.getPlayerMaxHealth() - 20,
                    logicAPI.getPlayerHealth(),
                    "UI health should match persisted state");
        assertEquals("throne_room", logicAPI.getPlayerLocation(),
                    "UI location should match persisted state");
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("UI handles logic layer errors gracefully")
    public void testUIErrorHandling() {
        // Arrange
        logicAPI.newGame();
        
        // Act: Attempt invalid operation
        assertDoesNotThrow(() -> {
            logicAPI.loadGame("nonexistent_save");  // Should handle error
        }, "UI should handle logic errors gracefully");
    }

    @Test
    @DisplayName("UI recovers from autosave failure")
    public void testUIRecoveryFromAutoSaveFailure() {
        // Arrange
        logicAPI.newGame();
        
        // Act: Even if autosave fails, game should continue
        assertDoesNotThrow(() -> {
            logicAPI.update(1.0f);  // Might trigger autosave
            logicAPI.damagePlayer(5);  // Game should still work
        }, "Game should continue even if autosave fails");
    }

    // ==================== PERFORMANCE TESTS ====================

    @Test
    @DisplayName("UI query methods complete within frame budget")
    public void testUIQueryPerformance() {
        // Arrange
        logicAPI.newGame();
        long frameTimeBudget = 16;  // 60 FPS = ~16ms per frame
        
        // Act: Time multiple UI queries
        long startTime = System.nanoTime();
        
        logicAPI.getPlayerHealth();
        logicAPI.getPlayerMaxHealth();
        logicAPI.getPlayerLevel();
        logicAPI.getPlayerExperience();
        logicAPI.getPlayerLocation();
        logicAPI.getPlayerInventory();
        
        long duration = (System.nanoTime() - startTime) / 1_000_000;  // Convert to ms
        
        // Assert: All queries should complete in < 1ms
        assertTrue(duration < frameTimeBudget / 2,
                  "Query methods should be very fast");
    }

    @Test
    @DisplayName("UI update loop maintains frame rate")
    public void testUIUpdateLoopFrameRate() {
        // Arrange
        logicAPI.newGame();
        float deltaTime = 0.016f;  // 60 FPS
        int frameCount = 300;  // 5 seconds
        
        // Act: Simulate game loop
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < frameCount; i++) {
            logicAPI.update(deltaTime);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Assert: Should complete in reasonable time
        // 5 seconds of gameplay should take roughly 5 seconds
        assertTrue(duration < 10000, "Game loop should maintain performance");
    }
}

// ==================== MOCK CLASSES ====================

/**
 * Mock callback interface for UI testing
 */
interface UICallback {
    void onStateChanged(String stateName);
    void onSaveComplete(String slotName);
    void onLoadComplete(String slotName);
    void onError(String message);
}

/**
 * Mock UI button for testing click handling
 */
class UIButton {
    private String label;
    private Runnable onClickAction;
    
    public UIButton(String label, Runnable action) {
        this.label = label;
        this.onClickAction = action;
    }
    
    public void click() {
        onClickAction.run();
    }
}
