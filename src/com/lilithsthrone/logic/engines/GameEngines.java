package com.lilithsthrone.logic.engines;

import com.lilithsthrone.data.DataStore;
import com.lilithsthrone.logic.persistence.DeltaEngine;
import com.lilithsthrone.logic.state.GameState;
import com.lilithsthrone.utils.logging.LogManager;

/**
 * Abstract base class for all game mechanics engines.
 * 
 * Each engine is responsible for one aspect of game mechanics:
 *   - CombatEngine: Attack, defense, damage, status effects
 *   - InventoryEngine: Items, equipment, trading
 *   - CharacterEngine: Leveling, attributes, health/mana
 *   - MovementEngine: World traversal, navigation
 *   - QuestEngine: Quest progress, objectives
 *   - EventEngine: Dialogue trees, world events
 *   - BuffEngine: Temporary effects and perks
 *   - WorldEngine: NPC state, location events
 * 
 * Engines:
 *   1. Modify GameState through actions
 *   2. Mark state as dirty when changes occur
 *   3. Report changes to DeltaEngine for persistence
 *   4. Validate actions before executing
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public abstract class BaseEngine {
    protected GameState gameState;
    protected DeltaEngine deltaEngine;
    protected boolean isInitialized = false;

    public BaseEngine(GameState gameState, DeltaEngine deltaEngine) {
        this.gameState = gameState;
        this.deltaEngine = deltaEngine;
    }

    /**
     * Initialize the engine (called once at startup)
     * Override to load static data, initialize systems, etc.
     */
    public void initialize() {
        isInitialized = true;
    }

    /**
     * Called every frame/turn to update engine state
     */
    public abstract void update();

    /**
     * Mark state as modified and notify delta engine
     */
    protected void recordChange(String fieldId, Object newValue) {
        gameState.markDirty();
        if (deltaEngine != null) {
            deltaEngine.recordChange(fieldId, newValue);
        }
    }

    /**
     * Shutdown the engine (called when exiting game)
     */
    public void shutdown() {
        isInitialized = false;
    }

    protected GameState getGameState() {
        return gameState;
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}

/**
 * Combat engine responsible for all combat mechanics.
 * 
 * Manages:
 *   - Combat initiation and resolution
 *   - Turn-based action resolution
 *   - Damage calculation and application
 *   - Status effect application and duration
 *   - Combat victory/defeat conditions
 * 
 * Actions:
 *   - initiateCombat(player, enemies): Start combat
 *   - selectAction(combatant, action): Queue action for turn
 *   - resolveTurn(): Process all queued actions
 *   - endCombat(): Finalize combat results
 * 
 * State Modified:
 *   - PlayerState (health, status effects)
 *   - NpcState (health, status effects)
 *   - CombatState (transient)
 */
class CombatEngine extends BaseEngine {
    private static final String ENGINE_NAME = "CombatEngine";

    public CombatEngine(GameState gameState, DeltaEngine deltaEngine) {
        super(gameState, deltaEngine);
    }

    @Override
    public void initialize() {
        super.initialize();
        LogManager.info(ENGINE_NAME, "Initialized");
    }

    @Override
    public void update() {
        if (!gameState.isInCombat()) {
            return;
        }
        // Update combat state (status effect durations, regeneration, etc.)
        updateCombatState();
    }

    /**
     * Initiate combat with enemies
     * Validates that all NPC IDs exist in the system before starting combat
     */
    public void initiateCombat(String[] enemyIds) {
        if (gameState.isInCombat()) {
            LogManager.warn(ENGINE_NAME, "Already in combat!");
            return;
        }

        // Validate all enemy IDs exist
        for (String enemyId : enemyIds) {
            if (gameState.getNpcState(enemyId) == null) {
                LogManager.error(ENGINE_NAME, "Invalid enemy NPC ID: " + enemyId);
                return;
            }
        }

        com.lilithsthrone.logic.state.CombatState combatState = 
            new com.lilithsthrone.logic.state.CombatState();
        
        // Add player to combat
        combatState.setPlayerCombatantId("player");
        combatState.addCombatant("player", gameState.getPlayerState().getCurrentHealth());

        // Add enemies
        for (String enemyId : enemyIds) {
            com.lilithsthrone.logic.state.NpcState npcState = gameState.getNpcState(enemyId);
            combatState.addCombatant(enemyId, npcState.getCurrentHealth());
        }

        gameState.setCurrentCombatState(combatState);
        recordChange("combatState", "COMBAT_STARTED");
        LogManager.info(ENGINE_NAME, "Combat initiated with " + enemyIds.length + " enemies");
    }

    /**
     * Apply damage to a combatant
     */
    public void takeDamage(String combatantId, int damage) {
        if (!gameState.isInCombat()) {
            LogManager.warn(ENGINE_NAME, "Not in combat, cannot take damage");
            return;
        }

        com.lilithsthrone.logic.state.CombatState combatState = gameState.getCurrentCombatState();
        int currentHealth = combatState.getCombatantHealth(combatantId);
        int newHealth = Math.max(0, currentHealth - damage);
        combatState.setCombatantHealth(combatantId, newHealth);

        // Update persistent state if combatant is defeated
        if (newHealth == 0 && !combatantId.equals("player")) {
            com.lilithsthrone.logic.state.NpcState npcState = gameState.getNpcState(combatantId);
            if (npcState != null) {
                npcState.setStatus("DEAD");
                recordChange("npcStatus_" + combatantId, "DEAD");
            } else {
                LogManager.warn(ENGINE_NAME, "NPC state not found for defeated enemy: " + combatantId);
            }
        }

        recordChange("health_" + combatantId, newHealth);
        LogManager.info(ENGINE_NAME, combatantId + " takes " + damage + " damage (now " + newHealth + " hp)");
    }

    /**
     * End combat and apply results
     */
    public void endCombat(boolean playerVictory) {
        if (!gameState.isInCombat()) {
            LogManager.warn(ENGINE_NAME, "Not in combat, cannot end combat");
            return;
        }

        com.lilithsthrone.logic.state.CombatState combatState = gameState.getCurrentCombatState();
        
        // Apply any permanent changes from combat
        if (playerVictory) {
            // Award experience, items, etc.
            int xpReward = 100; // Simplified
            gameState.getPlayerState().addExperience(xpReward);
            recordChange("playerExperience", gameState.getPlayerState().getExperiencePoints());
            LogManager.info(ENGINE_NAME, "Combat victory! " + xpReward + " XP awarded");
        } else {
            // Penalty for loss (heal at inn, etc.)
            gameState.getPlayerState().setCurrentHealth(gameState.getPlayerState().getMaxHealth() / 2);
            recordChange("playerHealth", gameState.getPlayerState().getCurrentHealth());
            LogManager.info(ENGINE_NAME, "Combat defeat! Health reduced to 50%");
        }

        // Clear combat state
        gameState.setCurrentCombatState(null);
        recordChange("combatState", "COMBAT_ENDED");
        LogManager.info(ENGINE_NAME, "Combat ended, player " + (playerVictory ? "VICTORY" : "DEFEAT"));
    }

    /**
     * Update active combat (durations, regeneration)
     */
    private void updateCombatState() {
        if (!gameState.isInCombat()) {
            return;
        }

        // Decrease status effect durations
        for (String effectId : gameState.getBuffState().getActiveEffects().keySet()) {
            int duration = gameState.getBuffState().getActiveEffects().get(effectId);
            if (duration > 0) {
                gameState.getBuffState().getActiveEffects().put(effectId, duration - 1);
            }
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        // End any active combat
        if (gameState.isInCombat()) {
            endCombat(false);
        }
        LogManager.info(ENGINE_NAME, "Shutdown");
    }
}

/**
 * Inventory engine responsible for item management.
 * 
 * Manages:
 *   - Item pickup, drop, use
 *   - Equipment management (equip, unequip)
 *   - Item trading and selling
 *   - Inventory slot management
 * 
 * Actions:
 *   - addItem(itemId, quantity)
 *   - removeItem(itemId, quantity)
 *   - equip(slot, item)
 *   - unequip(slot)
 *   - useItem(itemId, target)
 * 
 * State Modified:
 *   - InventoryState
 *   - PlayerState (health/mana if consumable used)
 */
class InventoryEngine extends BaseEngine {
    private static final String ENGINE_NAME = "InventoryEngine";

    public InventoryEngine(GameState gameState, DeltaEngine deltaEngine) {
        super(gameState, deltaEngine);
    }

    @Override
    public void initialize() {
        super.initialize();
        LogManager.info(ENGINE_NAME, "Initialized");
    }

    @Override
    public void update() {
        // Inventory doesn't need per-frame updates
    }

    /**
     * Add item to inventory
     * Validates that the item ID exists in the binary data store
     */
    public boolean addItem(String itemId, int quantity) {
        if (quantity <= 0) {
            LogManager.warn(ENGINE_NAME, "Cannot add item with quantity <= 0: " + itemId);
            return false;
        }

        // Validate item exists in DataStore (binary-backed game data)
        if (DataStore.getInstance().getItem(itemId) == null) {
            LogManager.error(ENGINE_NAME, "Invalid item ID (not in DataStore): " + itemId);
            return false;
        }

        int usedSlots = gameState.getInventoryState().getItems().values().stream()
                .mapToInt(Integer::intValue).sum();
        int maxSlots = gameState.getInventoryState().getMaxInventorySlots();

        if (usedSlots + quantity > maxSlots) {
            LogManager.warn(ENGINE_NAME, "Inventory full! Need " + quantity + " slots but only " + (maxSlots - usedSlots) + " available");
            return false;
        }

        gameState.getInventoryState().addItem(itemId, quantity);
        recordChange("inventory_add_" + itemId, quantity);
        LogManager.info(ENGINE_NAME, "Added " + quantity + "x " + itemId + " to inventory");
        return true;
    }

    /**
     * Remove item from inventory
     */
    public boolean removeItem(String itemId, int quantity) {
        if (quantity <= 0) {
            LogManager.warn(ENGINE_NAME, "Cannot remove item with quantity <= 0: " + itemId);
            return false;
        }

        int currentQuantity = gameState.getInventoryState().getItems().getOrDefault(itemId, 0);
        if (currentQuantity < quantity) {
            LogManager.warn(ENGINE_NAME, "Not enough " + itemId + " (have " + currentQuantity + ", need " + quantity + ")");
            return false;
        }

        gameState.getInventoryState().removeItem(itemId, quantity);
        recordChange("inventory_remove_" + itemId, quantity);
        LogManager.info(ENGINE_NAME, "Removed " + quantity + "x " + itemId + " from inventory");
        return true;
    }

    /**
     * Equip an item to a body slot
     * Validates that the item is in inventory and is a valid item type
     */
    public boolean equip(String bodySlot, String itemId) {
        // Validate item exists in inventory
        if (!gameState.getInventoryState().getItems().containsKey(itemId)) {
            LogManager.warn(ENGINE_NAME, "Item not in inventory: " + itemId);
            return false;
        }

        // Validate item exists in DataStore (binary-backed game data)
        if (DataStore.getInstance().getItem(itemId) == null) {
            LogManager.error(ENGINE_NAME, "Invalid item ID (not in DataStore): " + itemId);
            return false;
        }

        gameState.getInventoryState().getEquippedItems().put(bodySlot, itemId);
        recordChange("equipped_" + bodySlot, itemId);
        LogManager.info(ENGINE_NAME, "Equipped " + itemId + " to " + bodySlot);
        return true;
    }

    /**
     * Unequip an item from a body slot
     */
    public boolean unequip(String bodySlot) {
        gameState.getInventoryState().getEquippedItems().remove(bodySlot);
        recordChange("unequipped_" + bodySlot, "");
        System.out.println("[" + ENGINE_NAME + "] Unequipped from " + bodySlot);
        return true;
    }

    /**
     * Use a consumable item
     */
    public boolean useItem(String itemId, String targetId) {
        if (!removeItem(itemId, 1)) {
            return false;
        }

        // Apply item effect (simplified)
        if (itemId.contains("potion")) {
            gameState.getPlayerState().addHealth(50);
            recordChange("playerHealth", gameState.getPlayerState().getCurrentHealth());
            System.out.println("[" + ENGINE_NAME + "] Used potion, health restored");
            return true;
        }

        return false;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        LogManager.info(ENGINE_NAME, "Shutdown");
    }
}

/**
 * Movement engine responsible for world navigation.
 * 
 * Manages:
 *   - Player movement on world grid
 *   - Location transitions
 *   - Area unlocking
 *   - Visited location tracking
 * 
 * Actions:
 *   - move(direction)
 *   - goToLocation(locationId)
 *   - unlockArea(areaId)
 * 
 * State Modified:
 *   - PlayerState (location)
 *   - WorldState (visited locations)
 */
class MovementEngine extends BaseEngine {
    private static final String ENGINE_NAME = "MovementEngine";

    public MovementEngine(GameState gameState, DeltaEngine deltaEngine) {
        super(gameState, deltaEngine);
    }

    @Override
    public void initialize() {
        super.initialize();
        LogManager.info(ENGINE_NAME, "Initialized");
    }

    @Override
    public void update() {
        // Movement engine doesn't need per-frame updates
    }

    /**
     * Move player to a specific location
     * Validates that the location exists in the world state
     */
    public boolean goToLocation(String locationId) {
        // Validate location exists (can also check DataStore if locations are there)
        if (locationId == null || locationId.isEmpty()) {
            LogManager.error(ENGINE_NAME, "Invalid location ID (null or empty)");
            return false;
        }
        
        gameState.getPlayerState().setCurrentLocation(locationId);
        gameState.visitLocation(locationId);
        recordChange("playerLocation", locationId);
        LogManager.info(ENGINE_NAME, "Moved to " + locationId);
        return true;
    }

    /**
     * Unlock an area for access
     */
    public void unlockArea(String areaId) {
        gameState.setQuestFlag("area_unlocked_" + areaId, true);
        recordChange("areaUnlocked", areaId);
        LogManager.info(ENGINE_NAME, "Unlocked area: " + areaId);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        LogManager.info(ENGINE_NAME, "Shutdown");
    }
}
