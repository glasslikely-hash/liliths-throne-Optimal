package com.lilithsthrone.logic;

import com.lilithsthrone.data.DataStore;
import com.lilithsthrone.logic.engines.BaseEngine;
import com.lilithsthrone.logic.engines.CombatEngine;
import com.lilithsthrone.logic.engines.InventoryEngine;
import com.lilithsthrone.logic.engines.MovementEngine;
import com.lilithsthrone.logic.engines.QuestEngine;
import com.lilithsthrone.logic.engines.EventEngine;
import com.lilithsthrone.logic.engines.BuffEngine;
import com.lilithsthrone.logic.engines.CharacterEngine;
import com.lilithsthrone.logic.engines.WorldEngine;
import com.lilithsthrone.logic.persistence.AutoSaveManager;
import com.lilithsthrone.logic.persistence.DeltaEngine;
import com.lilithsthrone.logic.persistence.PersistenceManager;
import com.lilithsthrone.logic.persistence.SnapshotEngine;
import com.lilithsthrone.logic.state.GameState;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified API for UI layer to query and modify game state.
 * 
 * This is the ONLY interface between UI and Logic layers.
 * Constraints:
 *   - UI can only read state through query methods
 *   - UI can only modify state through action methods
 *   - UI cannot directly access GameState object
 *   - All state changes are persisted automatically
 * 
 * Query Methods (Safe - read-only):
 *   - getPlayerHealth() / getMaxHealth()
 *   - getPlayerLocation()
 *   - getInventoryItems()
 *   - getActiveBuffs()
 *   - etc.
 * 
 * Action Methods (Trigger mechanics):
 *   - move(direction)
 *   - attack(target)
 *   - useItem(itemId)
 *   - equip(bodySlot, itemId)
 *   - etc.
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class LogicLayerAPI {
    private GameState gameState;
    private SnapshotEngine snapshotEngine;
    private DeltaEngine deltaEngine;
    private PersistenceManager persistenceManager;
    private AutoSaveManager autoSaveManager;
    private List<BaseEngine> mechanics;
    private boolean isRunning = false;

    // Specific engine instances (for action dispatch)
    private CombatEngine combatEngine;
    private InventoryEngine inventoryEngine;
    private MovementEngine movementEngine;
    private QuestEngine questEngine;
    private EventEngine eventEngine;
    private BuffEngine buffEngine;
    private CharacterEngine characterEngine;
    private WorldEngine worldEngine;

    public LogicLayerAPI() {
        initializeState();
        initializeEngines();
    }

    /**
     * Initialize game state from scratch or load from save
     */
    private void initializeState() {
        this.gameState = new GameState();
        
        // Initialize persistence engines
        this.snapshotEngine = new SnapshotEngine(gameState, 600000); // 10 minute snapshots
        this.deltaEngine = new DeltaEngine(gameState, 60000);        // 1 minute deltas
        
        // Initialize persistence manager and autosave
        this.persistenceManager = new PersistenceManager(snapshotEngine, deltaEngine, gameState);
        this.autoSaveManager = new AutoSaveManager(30); // 30 second autosave interval
    }

    /**
     * Initialize all mechanics engines
     */
    private void initializeEngines() {
        this.mechanics = new ArrayList<>();

        // Create existing engines
        this.combatEngine = new CombatEngine(gameState, deltaEngine);
        this.inventoryEngine = new InventoryEngine(gameState, deltaEngine);
        this.movementEngine = new MovementEngine(gameState, deltaEngine);

        // Add existing engines
        mechanics.add(combatEngine);
        mechanics.add(inventoryEngine);
        mechanics.add(movementEngine);

        // Create new engines
        this.questEngine = new QuestEngine(gameState, deltaEngine);
        this.eventEngine = new EventEngine(gameState, deltaEngine);
        this.buffEngine = new BuffEngine(gameState, deltaEngine);
        this.characterEngine = new CharacterEngine(gameState, deltaEngine);
        this.worldEngine = new WorldEngine(gameState, deltaEngine);

        // Add new engines
        mechanics.add(questEngine);
        mechanics.add(eventEngine);
        mechanics.add(buffEngine);
        mechanics.add(characterEngine);
        mechanics.add(worldEngine);

        // Initialize all engines
        for (BaseEngine engine : mechanics) {
            engine.initialize();
        }
    }

    /**
     * Main game loop called every frame
     */
    public void update(float deltaTime) {
        if (!isRunning) {
            return;
        }

        // Update all mechanics engines
        for (BaseEngine engine : mechanics) {
            engine.update();
        }

        // Update persistence (autosave, snapshots, deltas)
        if (persistenceManager != null) {
            persistenceManager.update(deltaTime);
        }

        // Flush deltas if needed
        if (deltaEngine.shouldFlush()) {
            deltaEngine.flush();
        }

        // Create snapshot if needed
        if (snapshotEngine.shouldSnapshot()) {
            snapshotEngine.snapshot();
        }
    }

    /**
     * Start a new game
     */
    public void newGame() {
        gameState = new GameState();
        snapshotEngine = new SnapshotEngine(gameState, 600000);
        deltaEngine = new DeltaEngine(gameState, 60000);
        persistenceManager = new PersistenceManager(snapshotEngine, deltaEngine, gameState);
        autoSaveManager = new AutoSaveManager(30);
        reinitializeEngines();
        persistenceManager.startSession();
        autoSaveManager.start();
        isRunning = true;
        System.out.println("[LogicLayerAPI] New game started");
    }

    /**
     * Load a game from a save slot
     */
    public void loadGame(String slotName) {
        try {
            if (persistenceManager == null) {
                initializeState();
            }
            gameState = persistenceManager.loadGame(slotName);
            deltaEngine = new DeltaEngine(gameState, 60000);
            reinitializeEngines();
            persistenceManager.startSession();
            autoSaveManager.start();
            isRunning = true;
            System.out.println("[LogicLayerAPI] Game loaded from slot: " + slotName);
        } catch (Exception e) {
            System.err.println("[LogicLayerAPI] Failed to load game: " + slotName);
            e.printStackTrace();
        }
    }

    /**
     * Save game to a slot (manual save)
     */
    public void saveGame(String slotName) {
        if (persistenceManager != null) {
            persistenceManager.manualSave(slotName);
            System.out.println("[LogicLayerAPI] Game saved to slot: " + slotName);
        } else {
            System.err.println("[LogicLayerAPI] Persistence manager not initialized");
        }
    }

    /**
     * Shutdown the logic layer
     */
    public void shutdown() {
        isRunning = false;
        
        // Shutdown persistence
        if (autoSaveManager != null) {
            autoSaveManager.stop();
            autoSaveManager.shutdown();
        }
        if (persistenceManager != null) {
            persistenceManager.endSession();
        }
        
        // Shutdown engines
        for (BaseEngine engine : mechanics) {
            engine.shutdown();
        }
        System.out.println("[LogicLayerAPI] Shutdown complete");
    }

    private void reinitializeEngines() {
        mechanics.clear();
        initializeEngines();
    }

    // ==================== QUERY API (UI -> Logic, Read-Only) ====================

    /**
     * Get player's current health
     */
    public int getPlayerHealth() {
        return gameState.getPlayerState().getCurrentHealth();
    }

    /**
     * Get player's maximum health
     */
    public int getPlayerMaxHealth() {
        return gameState.getPlayerState().getMaxHealth();
    }

    /**
     * Get player's current mana
     */
    public int getPlayerMana() {
        return gameState.getPlayerState().getCurrentMana();
    }

    /**
     * Get player's maximum mana
     */
    public int getPlayerMaxMana() {
        return gameState.getPlayerState().getMaxMana();
    }

    /**
     * Get player's current level
     */
    public int getPlayerLevel() {
        return gameState.getPlayerState().getLevel();
    }

    /**
     * Get player's experience points
     */
    public long getPlayerExperience() {
        return gameState.getPlayerState().getExperiencePoints();
    }

    /**
     * Get player's current location
     */
    public String getPlayerLocation() {
        return gameState.getPlayerState().getCurrentLocation();
    }

    /**
     * Get player's current inventory (copy to prevent modification)
     */
    public java.util.Map<String, Integer> getInventoryItems() {
        return new java.util.HashMap<>(gameState.getInventoryState().getItems());
    }

    /**
     * Get currently equipped items (copy)
     */
    public java.util.Map<String, String> getEquippedItems() {
        return new java.util.HashMap<>(gameState.getInventoryState().getEquippedItems());
    }

    /**
     * Get active buffs/effects
     */
    public java.util.Map<String, Integer> getActiveEffects() {
        return new java.util.HashMap<>(gameState.getBuffState().getActiveEffects());
    }

    /**
     * Get active perks
     */
    public java.util.List<String> getActivePerks() {
        return new java.util.ArrayList<>(gameState.getBuffState().getActivePerkIds());
    }

    /**
     * Check if player is in combat
     */
    public boolean isInCombat() {
        return gameState.isInCombat();
    }

    /**
     * Get player attributes (copy)
     */
    public java.util.Map<String, Integer> getPlayerAttributes() {
        return new java.util.HashMap<>(gameState.getPlayerAttributes().getAttributes());
    }

    /**
     * Get a specific attribute value
     */
    public int getAttribute(String attributeId) {
        return gameState.getPlayerAttributes().getAttribute(attributeId);
    }

    /**
     * Check if a location has been visited
     */
    public boolean hasVisited(String locationId) {
        return gameState.hasVisited(locationId);
    }

    /**
     * Get NPC relationship status
     */
    public String getNpcRelationship(String npcId) {
        return gameState.getNpcRelationship(npcId);
    }

    /**
     * Get NPC affection level
     */
    public int getNpcAffection(String npcId) {
        return gameState.getAffectionLevel(npcId);
    }

    /**
     * Check if a quest flag is set
     */
    public boolean hasQuestFlag(String flagId) {
        return gameState.hasQuestFlag(flagId);
    }

    /**
     * Get quest progress
     */
    public int getQuestProgress(String objectiveId) {
        return gameState.getQuestProgress(objectiveId);
    }

    // ==================== ACTION API (UI -> Logic, State Modification) ====================

    /**
     * Move player to a location
     */
    public void moveToLocation(String locationId) {
        movementEngine.goToLocation(locationId);
    }

    /**
     * Initiate combat
     */
    public void startCombat(String[] enemyIds) {
        combatEngine.initiateCombat(enemyIds);
    }

    /**
     * Apply damage during combat
     */
    public void takeDamage(String combatantId, int damage) {
        combatEngine.takeDamage(combatantId, damage);
    }

    /**
     * End combat
     */
    public void endCombat(boolean playerVictory) {
        combatEngine.endCombat(playerVictory);
    }

    /**
     * Add item to inventory
     */
    public void addItem(String itemId, int quantity) {
        inventoryEngine.addItem(itemId, quantity);
    }

    /**
     * Remove item from inventory
     */
    public void removeItem(String itemId, int quantity) {
        inventoryEngine.removeItem(itemId, quantity);
    }

    /**
     * Equip an item
     */
    public void equipItem(String bodySlot, String itemId) {
        inventoryEngine.equip(bodySlot, itemId);
    }

    /**
     * Unequip an item
     */
    public void unequipItem(String bodySlot) {
        inventoryEngine.unequip(bodySlot);
    }

    /**
     * Use a consumable item
     */
    public void useItem(String itemId, String targetId) {
        inventoryEngine.useItem(itemId, targetId);
    }

    /**
     * Set a quest flag (for story progression)
     */
    public void setQuestFlag(String flagId, boolean value) {
        gameState.setQuestFlag(flagId, value);
    }

    /**
     * Update quest progress
     */
    public void updateQuestProgress(String objectiveId, int value) {
        gameState.setQuestProgress(objectiveId, value);
    }

    /**
     * Update NPC relationship
     */
    public void setNpcRelationship(String npcId, String status) {
        gameState.setNpcRelationship(npcId, status);
    }

    /**
     * Modify NPC affection
     */
    public void modifyAffection(String npcId, int delta) {
        gameState.addAffection(npcId, delta);
    }

    // ==================== QUEST ENGINE ACTIONS ====================

    /**
     * Start a new quest
     */
    public void startQuest(String questId) {
        questEngine.startQuest(questId);
    }

    /**
     * Update quest objective progress
     */
    public void updateQuestObjectiveProgress(String questId, String objectiveId, int progress) {
        questEngine.updateQuestObjective(questId, objectiveId, progress);
    }

    /**
     * Complete a quest
     */
    public void completeQuest(String questId) {
        questEngine.completeQuest(questId);
    }

    /**
     * Abandon a quest
     */
    public void abandonQuest(String questId) {
        questEngine.abandonQuest(questId);
    }

    /**
     * Get all active quests
     */
    public java.util.Set<String> getActiveQuests() {
        return questEngine.getActiveQuests();
    }

    // ==================== EVENT ENGINE ACTIONS ====================

    /**
     * Trigger a world event
     */
    public void triggerEvent(String eventId) {
        eventEngine.triggerEvent(eventId);
    }

    /**
     * Start a dialogue with an NPC
     */
    public void startDialogue(String npcId, String dialogueId) {
        eventEngine.startDialogue(npcId, dialogueId);
    }

    /**
     * Progress dialogue to next node
     */
    public void progressDialogue(int nodeId) {
        eventEngine.progressDialogue(nodeId);
    }

    /**
     * End current dialogue
     */
    public void endDialogue(String npcId) {
        eventEngine.endDialogue(npcId);
    }

    /**
     * Check if an event has occurred
     */
    public boolean hasEventOccurred(String eventId) {
        return eventEngine.hasEventOccurred(eventId);
    }

    // ==================== BUFF ENGINE ACTIONS ====================

    /**
     * Apply a temporary effect/buff
     */
    public void applyEffect(String effectId, int durationSeconds) {
        buffEngine.applyEffect(effectId, durationSeconds);
    }

    /**
     * Remove an active effect
     */
    public void removeEffect(String effectId) {
        buffEngine.removeEffect(effectId);
    }

    /**
     * Add a permanent perk
     */
    public void addPerk(String perkId) {
        buffEngine.addPerk(perkId);
    }

    /**
     * Remove a perk
     */
    public void removePerk(String perkId) {
        buffEngine.removePerk(perkId);
    }

    /**
     * Get active effects with durations
     */
    public java.util.Map<String, Integer> getAllActiveEffects() {
        return buffEngine.getActiveEffects();
    }

    /**
     * Get all active perks
     */
    public java.util.Set<String> getAllActivePerks() {
        return buffEngine.getActivePerks();
    }

    // ==================== CHARACTER ENGINE ACTIONS ====================

    /**
     * Gain experience points
     */
    public void gainExperience(int amount) {
        characterEngine.gainExperience(amount);
    }

    /**
     * Modify a player attribute
     */
    public void modifyPlayerAttribute(String attributeId, int delta) {
        characterEngine.modifyAttribute(attributeId, delta);
    }

    /**
     * Learn a new skill
     */
    public void learnSkill(String skillId) {
        characterEngine.learnSkill(skillId);
    }

    // ==================== WORLD ENGINE ACTIONS ====================

    /**
     * Update NPC state (alive, dead, sleeping, etc.)
     */
    public void updateNpcState(String npcId, String state) {
        worldEngine.updateNpcState(npcId, state);
    }

    /**
     * Trigger a world-wide event
     */
    public void triggerWorldEvent(String eventId) {
        worldEngine.triggerWorldEvent(eventId);
    }

    /**
     * Update location state
     */
    public void updateLocationState(String locationId, String state) {
        worldEngine.updateLocationState(locationId, state);
    }

    /**
     * Respawn all NPCs in a location
     */
    public void respawnNpcsInLocation(String locationId) {
        worldEngine.respawnNpcs(locationId);
    }

    /**
     * Schedule an NPC for respawn
     */
    public void scheduleNpcRespawn(String npcId) {
        worldEngine.scheduleNpcRespawn(npcId);
    }

    /**
     * Check if a world event has occurred
     */
    public boolean hasWorldEventOccurred(String eventId) {
        return worldEngine.hasWorldEventOccurred(eventId);
    }

    // Getters
    public GameState getGameState() {
        return gameState;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
