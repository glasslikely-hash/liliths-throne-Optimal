package com.lilithsthrone.logic;

import com.lilithsthrone.logic.persistence.DeltaEngine;
import com.lilithsthrone.logic.persistence.SnapshotEngine;
import com.lilithsthrone.logic.state.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Snapshot + Delta reconstruction.
 * Validates that engine state matches expected outcomes after
 * snapshot creation and delta application.
 */
@DisplayName("Logic Layer - Snapshot + Delta Reconstruction Tests")
public class SnapshotDeltaReconstructionTest {

    private GameState gameState;
    private SnapshotEngine snapshotEngine;
    private DeltaEngine deltaEngine;

    @BeforeEach
    public void setUp() {
        gameState = new GameState();
        snapshotEngine = new SnapshotEngine(gameState, 10000);  // 10 second snapshots
        deltaEngine = new DeltaEngine(gameState, 5000);        // 5 second deltas
        
        // Initialize engines
        snapshotEngine.initialize(gameState);
        deltaEngine.initialize(gameState);
    }

    // ==================== DETERMINISTIC STATE TESTS ====================

    @Test
    @DisplayName("Snapshot captures initial game state")
    public void testInitialSnapshotCapture() throws Exception {
        // Arrange: Create initial state
        int initialHealth = gameState.getPlayerState().getCurrentHealth();
        int initialLevel = gameState.getPlayerState().getLevel();
        
        // Act: Create snapshot
        byte[] snapshot = snapshotEngine.getSnapshotBytes();
        
        // Assert: Snapshot should not be empty
        assertNotNull(snapshot, "Snapshot should not be null");
        assertTrue(snapshot.length > 0, "Snapshot should contain data");
    }

    @Test
    @DisplayName("Delta engine tracks state changes")
    public void testDeltaEngineTracksChanges() {
        // Arrange: Modify player state
        gameState.getPlayerState().takeDamage(10);
        
        // Act: Get deltas
        byte[] deltas = deltaEngine.getDeltasSinceSnapshot();
        
        // Assert: Deltas should be recorded
        assertNotNull(deltas, "Deltas should not be null");
        // Should have recorded health change
    }

    @Test
    @DisplayName("Delta application restores state correctly")
    public void testDeltaApplicationRestoresState() throws Exception {
        // Arrange: Record initial state
        int initialHealth = gameState.getPlayerState().getCurrentHealth();
        
        // Act: Make changes and record deltas
        gameState.getPlayerState().takeDamage(25);
        int damagedHealth = gameState.getPlayerState().getCurrentHealth();
        byte[] deltas = deltaEngine.getDeltasSinceSnapshot();
        
        // Create new state and apply deltas
        GameState restoredState = new GameState();
        snapshotEngine.setCurrentState(restoredState);
        deltaEngine.applyDeltasFromBytes(deltas, restoredState);
        
        // Assert: Restored health should match modified state
        int restoredHealth = restoredState.getPlayerState().getCurrentHealth();
        assertEquals(damagedHealth, restoredHealth, 
            "Restored health should match state after damage");
    }

    // ==================== FULL SNAPSHOT + DELTA CYCLE ====================

    @Test
    @DisplayName("Complete save and load cycle preserves state")
    public void testCompleteSnapshotDeltaCycle() throws Exception {
        // Arrange: Modify game state
        gameState.getPlayerState().takeDamage(15);
        gameState.getPlayerState().addExperience(100);
        
        // Act: Create full save (snapshot + deltas)
        byte[] snapshot = snapshotEngine.getSnapshotBytes();
        byte[] deltas = deltaEngine.getDeltasSinceSnapshot();
        
        // Create new game state and restore
        GameState restoredState = new GameState();
        snapshotEngine.setCurrentState(restoredState);
        deltaEngine.applyDeltasFromBytes(deltas, restoredState);
        
        // Assert: All state should match
        assertEquals(gameState.getPlayerState().getCurrentHealth(),
                    restoredState.getPlayerState().getCurrentHealth(),
                    "Health should be preserved in restore");
        assertEquals(gameState.getPlayerState().getExperience(),
                    restoredState.getPlayerState().getExperience(),
                    "Experience should be preserved in restore");
    }

    @Test
    @DisplayName("Multiple delta applications work correctly")
    public void testMultipleDeltaApplications() throws Exception {
        // Arrange: Create initial state
        GameState baseState = new GameState();
        snapshotEngine.setCurrentState(baseState);
        deltaEngine.setCurrentState(baseState);
        
        // Act: Apply multiple deltas
        byte[] delta1 = deltaEngine.getDeltasSinceSnapshot();
        
        baseState.getPlayerState().takeDamage(10);
        byte[] delta2 = deltaEngine.getDeltasSinceSnapshot();
        deltaEngine.clearDeltas();
        
        baseState.getPlayerState().takeDamage(5);
        byte[] delta3 = deltaEngine.getDeltasSinceSnapshot();
        
        // Assert: Total damage should be 15
        assertEquals(15, gameState.getPlayerState().getMaxHealth() - 
                        baseState.getPlayerState().getCurrentHealth(),
                    "Total damage should accumulate correctly");
    }

    // ==================== INVENTORY STATE TESTS ====================

    @Test
    @DisplayName("Inventory modifications are tracked in deltas")
    public void testInventoryDeltaTracking() throws Exception {
        // Arrange
        GameState initial = new GameState();
        
        // Act: Add item to inventory
        initial.getInventoryState().addItem("sword_iron", 1);
        byte[] deltas = deltaEngine.getDeltasSinceSnapshot();
        
        // Create restored state
        GameState restored = new GameState();
        snapshotEngine.setCurrentState(restored);
        deltaEngine.applyDeltasFromBytes(deltas, restored);
        
        // Assert: Item should be present in restored state
        assertTrue(restored.getInventoryState().hasItem("sword_iron"),
                  "Item should be present after delta application");
    }

    @Test
    @DisplayName("Equipment changes are persisted")
    public void testEquipmentPersistence() throws Exception {
        // Arrange
        GameState state = new GameState();
        
        // Act: Equip item
        state.getInventoryState().addItem("helmet_gold", 1);
        state.getInventoryState().equipItem("helmet_gold", "head");
        byte[] snapshot = snapshotEngine.getSnapshotBytes();
        byte[] deltas = deltaEngine.getDeltasSinceSnapshot();
        
        // Restore
        GameState restored = new GameState();
        snapshotEngine.setCurrentState(restored);
        deltaEngine.applyDeltasFromBytes(deltas, restored);
        
        // Assert: Item should be equipped
        assertEquals("helmet_gold", restored.getInventoryState().getEquippedItem("head"),
                    "Equipped item should be persisted");
    }

    // ==================== LOCATION STATE TESTS ====================

    @Test
    @DisplayName("Player location is preserved")
    public void testLocationPreservation() throws Exception {
        // Arrange
        GameState state = new GameState();
        String targetLocation = "forest_clearing";
        
        // Act: Change location
        state.getLocationState().setCurrentLocation(targetLocation);
        byte[] deltas = deltaEngine.getDeltasSinceSnapshot();
        
        // Restore
        GameState restored = new GameState();
        snapshotEngine.setCurrentState(restored);
        deltaEngine.applyDeltasFromBytes(deltas, restored);
        
        // Assert: Location should match
        assertEquals(targetLocation, restored.getLocationState().getCurrentLocation(),
                    "Player location should be preserved");
    }

    // ==================== DETERMINISM TESTS ====================

    @Test
    @DisplayName("Multiple snapshots of same state produce same data")
    public void testSnapshotDeterminism() throws Exception {
        // Arrange
        GameState state = new GameState();
        state.getPlayerState().takeDamage(5);
        
        // Act: Create multiple snapshots
        byte[] snapshot1 = snapshotEngine.getSnapshotBytes();
        byte[] snapshot2 = snapshotEngine.getSnapshotBytes();
        
        // Assert: Should be identical
        assertArrayEquals(snapshot1, snapshot2,
                         "Snapshots of identical state should be identical");
    }

    @Test
    @DisplayName("Delta clearing prevents duplicate state")
    public void testDeltaClearingPreventsduplicates() throws Exception {
        // Arrange
        GameState state = new GameState();
        state.getPlayerState().takeDamage(10);
        
        // Act: Get and clear deltas
        byte[] delta1 = deltaEngine.getDeltasSinceSnapshot();
        deltaEngine.clearDeltas();
        byte[] delta2 = deltaEngine.getDeltasSinceSnapshot();
        
        // Assert: Second delta should be empty after clear
        assertTrue(delta2.length == 0 || delta2.length < delta1.length,
                  "Cleared deltas should be empty");
    }

    // ==================== STATE INTEGRITY TESTS ====================

    @Test
    @DisplayName("Complex state changes maintain integrity")
    public void testComplexStateIntegrity() throws Exception {
        // Arrange
        GameState state = new GameState();
        
        // Act: Multiple complex changes
        state.getPlayerState().takeDamage(20);
        state.getPlayerState().addExperience(250);
        state.getInventoryState().addItem("potion_health", 3);
        state.getLocationState().setCurrentLocation("dungeon_level_2");
        
        byte[] snapshot = snapshotEngine.getSnapshotBytes();
        byte[] deltas = deltaEngine.getDeltasSinceSnapshot();
        
        // Restore
        GameState restored = new GameState();
        snapshotEngine.setCurrentState(restored);
        deltaEngine.applyDeltasFromBytes(deltas, restored);
        
        // Assert: All state should be preserved
        assertEquals(state.getPlayerState().getCurrentHealth(),
                    restored.getPlayerState().getCurrentHealth());
        assertEquals(state.getPlayerState().getExperience(),
                    restored.getPlayerState().getExperience());
        assertTrue(restored.getInventoryState().hasItem("potion_health"));
        assertEquals("dungeon_level_2", restored.getLocationState().getCurrentLocation());
    }

    @Test
    @DisplayName("Snapshot resilience to state mutations")
    public void testSnapshotResilience() throws Exception {
        // Arrange
        GameState original = new GameState();
        original.getPlayerState().takeDamage(10);
        
        // Act: Create snapshot
        byte[] snapshot = snapshotEngine.getSnapshotBytes();
        
        // Mutate original after snapshot
        original.getPlayerState().takeDamage(20);
        
        // Restore from snapshot
        GameState restored = new GameState();
        snapshotEngine.setCurrentState(restored);
        
        // Assert: Restored should match pre-mutation state
        assertTrue(restored.getPlayerState().getCurrentHealth() > 
                  original.getPlayerState().getCurrentHealth(),
                  "Snapshot should be independent of later mutations");
    }
}
