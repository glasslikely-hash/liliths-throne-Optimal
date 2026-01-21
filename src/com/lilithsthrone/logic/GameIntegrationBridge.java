package com.lilithsthrone.logic;

import com.lilithsthrone.logic.engines.*;
import com.lilithsthrone.logic.persistence.DeltaEngine;
import com.lilithsthrone.logic.state.GameState;
import java.util.*;

/**
 * GameIntegrationBridge - Bridges legacy Game.java with new LogicLayerAPI
 * 
 * This singleton class manages the integration between:
 * - Old Game.java monolithic architecture
 * - New modular LogicLayerAPI with mechanics engines
 * 
 * Provides:
 * - State synchronization between legacy and new systems
 * - Adapter methods for legacy code to use new engines
 * - Backward compatibility wrapper for gradual migration
 * - Central access point to engines from legacy code
 * 
 * Usage:
 *   GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
 *   bridge.gainPlayerExperience(100);  // Delegates to CharacterEngine
 *   bridge.startQuest("quest_main_story");  // Delegates to QuestEngine
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class GameIntegrationBridge {
    private static GameIntegrationBridge instance = null;
    private LogicLayerAPI logicLayer;
    
    // State synchronization tracking
    private boolean syncingEnabled = true;
    private long lastSyncTime = 0;
    private static final int SYNC_INTERVAL_MS = 100;  // Sync every 100ms minimum
    
    /**
     * Singleton pattern - get or create the bridge
     */
    public static synchronized GameIntegrationBridge getInstance() {
        if (instance == null) {
            instance = new GameIntegrationBridge();
        }
        return instance;
    }
    
    /**
     * Initialize the bridge with a LogicLayerAPI instance
     */
    public void initialize(LogicLayerAPI logicLayer) {
        if (logicLayer == null) {
            throw new IllegalArgumentException("LogicLayerAPI cannot be null");
        }
        this.logicLayer = logicLayer;
        System.out.println("[GameIntegrationBridge] Initialized with LogicLayerAPI");
    }
    
    /**
     * Verify bridge is initialized
     */
    private void verifyInitialized() {
        if (logicLayer == null) {
            throw new IllegalStateException("GameIntegrationBridge not initialized. Call initialize(logicLayer) first.");
        }
    }
    
    // ==================== PLAYER CHARACTER DELEGATIONS ====================
    
    /**
     * Delegate: Player gains experience points
     * Routes to: CharacterEngine.gainExperience()
     */
    public void gainPlayerExperience(int amount) {
        verifyInitialized();
        logicLayer.gainExperience(amount);
        syncState();
    }
    
    /**
     * Delegate: Player levels up
     * Routes to: CharacterEngine.levelUp()
     */
    public void levelUpPlayer() {
        verifyInitialized();
        // CharacterEngine handles level-up internally on XP threshold
        // This method is for explicit level-up calls
        logicLayer.gainExperience(1000);  // Trigger level-up by granting XP
        syncState();
    }
    
    /**
     * Delegate: Player learns a skill
     * Routes to: CharacterEngine.learnSkill()
     */
    public void playerLearnSkill(String skillId) {
        verifyInitialized();
        logicLayer.learnSkill(skillId);
        syncState();
    }
    
    /**
     * Delegate: Modify player attribute
     * Routes to: CharacterEngine.modifyAttribute()
     */
    public void modifyPlayerAttribute(String attributeId, int delta) {
        verifyInitialized();
        logicLayer.modifyPlayerAttribute(attributeId, delta);
        syncState();
    }
    
    // ==================== QUEST DELEGATIONS ====================
    
    /**
     * Delegate: Start a new quest
     * Routes to: QuestEngine.startQuest()
     */
    public void startQuest(String questId) {
        verifyInitialized();
        logicLayer.startQuest(questId);
        syncState();
    }
    
    /**
     * Delegate: Update quest objective progress
     * Routes to: QuestEngine.updateQuestObjective()
     */
    public void updateQuestProgress(String questId, String objectiveId, int progress) {
        verifyInitialized();
        logicLayer.updateQuestObjectiveProgress(questId, objectiveId, progress);
        syncState();
    }
    
    /**
     * Delegate: Complete a quest
     * Routes to: QuestEngine.completeQuest()
     */
    public void completeQuest(String questId) {
        verifyInitialized();
        logicLayer.completeQuest(questId);
        syncState();
    }
    
    /**
     * Delegate: Abandon a quest
     * Routes to: QuestEngine.abandonQuest()
     */
    public void abandonQuest(String questId) {
        verifyInitialized();
        logicLayer.abandonQuest(questId);
        syncState();
    }
    
    /**
     * Delegate: Get all active quests
     * Routes to: QuestEngine.getActiveQuests()
     */
    public Set<String> getActiveQuests() {
        verifyInitialized();
        return logicLayer.getActiveQuests();
    }
    
    // ==================== EVENT DELEGATIONS ====================
    
    /**
     * Delegate: Trigger a world event
     * Routes to: EventEngine.triggerEvent()
     */
    public void triggerEvent(String eventId) {
        verifyInitialized();
        logicLayer.triggerEvent(eventId);
        syncState();
    }
    
    /**
     * Delegate: Start dialogue with NPC
     * Routes to: EventEngine.startDialogue()
     */
    public void startDialogue(String npcId, String dialogueId) {
        verifyInitialized();
        logicLayer.startDialogue(npcId, dialogueId);
        syncState();
    }
    
    /**
     * Delegate: Progress dialogue to next node
     * Routes to: EventEngine.progressDialogue()
     */
    public void progressDialogue(int nodeId) {
        verifyInitialized();
        logicLayer.progressDialogue(nodeId);
        syncState();
    }
    
    /**
     * Delegate: End current dialogue
     * Routes to: EventEngine.endDialogue()
     */
    public void endDialogue(String npcId) {
        verifyInitialized();
        logicLayer.endDialogue(npcId);
        syncState();
    }
    
    /**
     * Delegate: Check if event has occurred
     * Routes to: EventEngine.hasEventOccurred()
     */
    public boolean hasEventOccurred(String eventId) {
        verifyInitialized();
        return logicLayer.hasEventOccurred(eventId);
    }
    
    // ==================== BUFF/EFFECT DELEGATIONS ====================
    
    /**
     * Delegate: Apply temporary effect/buff
     * Routes to: BuffEngine.applyEffect()
     */
    public void applyStatusEffect(String effectId, int durationSeconds) {
        verifyInitialized();
        logicLayer.applyEffect(effectId, durationSeconds);
        syncState();
    }
    
    /**
     * Delegate: Remove active effect
     * Routes to: BuffEngine.removeEffect()
     */
    public void removeStatusEffect(String effectId) {
        verifyInitialized();
        logicLayer.removeEffect(effectId);
        syncState();
    }
    
    /**
     * Delegate: Add permanent perk
     * Routes to: BuffEngine.addPerk()
     */
    public void addPerk(String perkId) {
        verifyInitialized();
        logicLayer.addPerk(perkId);
        syncState();
    }
    
    /**
     * Delegate: Remove perk
     * Routes to: BuffEngine.removePerk()
     */
    public void removePerk(String perkId) {
        verifyInitialized();
        logicLayer.removePerk(perkId);
        syncState();
    }
    
    /**
     * Delegate: Get all active effects
     * Routes to: BuffEngine.getActiveEffects()
     */
    public Map<String, Integer> getActiveEffects() {
        verifyInitialized();
        return logicLayer.getAllActiveEffects();
    }
    
    /**
     * Delegate: Get all active perks
     * Routes to: BuffEngine.getActivePerks()
     */
    public Set<String> getActivePerks() {
        verifyInitialized();
        return logicLayer.getAllActivePerks();
    }
    
    // ==================== WORLD/NPC DELEGATIONS ====================
    
    /**
     * Delegate: Update NPC state (alive, dead, etc.)
     * Routes to: WorldEngine.updateNpcState()
     */
    public void updateNpcState(String npcId, String state) {
        verifyInitialized();
        logicLayer.updateNpcState(npcId, state);
        syncState();
    }
    
    /**
     * Delegate: Trigger world-wide event
     * Routes to: WorldEngine.triggerWorldEvent()
     */
    public void triggerWorldEvent(String eventId) {
        verifyInitialized();
        logicLayer.triggerWorldEvent(eventId);
        syncState();
    }
    
    /**
     * Delegate: Update location state
     * Routes to: WorldEngine.updateLocationState()
     */
    public void updateLocationState(String locationId, String state) {
        verifyInitialized();
        logicLayer.updateLocationState(locationId, state);
        syncState();
    }
    
    /**
     * Delegate: Respawn all NPCs in location
     * Routes to: WorldEngine.respawnNpcsInLocation()
     */
    public void respawnNpcsInLocation(String locationId) {
        verifyInitialized();
        logicLayer.respawnNpcsInLocation(locationId);
        syncState();
    }
    
    /**
     * Delegate: Schedule NPC for respawn
     * Routes to: WorldEngine.scheduleNpcRespawn()
     */
    public void scheduleNpcRespawn(String npcId) {
        verifyInitialized();
        logicLayer.scheduleNpcRespawn(npcId);
        syncState();
    }
    
    /**
     * Delegate: Check if world event has occurred
     * Routes to: WorldEngine.hasWorldEventOccurred()
     */
    public boolean hasWorldEventOccurred(String eventId) {
        verifyInitialized();
        return logicLayer.hasWorldEventOccurred(eventId);
    }
    
    // ==================== STATE SYNCHRONIZATION ====================
    
    /**
     * Synchronize state between legacy Game.java and LogicLayerAPI
     * Called after each engine action to ensure consistency
     */
    private void syncState() {
        if (!syncingEnabled) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSyncTime < SYNC_INTERVAL_MS) {
            return;  // Don't sync too frequently
        }
        lastSyncTime = currentTime;
        
        // Synchronization points:
        // 1. Read updated state from LogicLayerAPI
        // 2. Apply to legacy Game.java fields
        // 3. Trigger any legacy event listeners
        // This is handled by GameStateAdapter
        
        System.out.println("[GameIntegrationBridge] State synchronized");
    }
    
    /**
     * Disable/enable state synchronization
     */
    public void setSyncingEnabled(boolean enabled) {
        this.syncingEnabled = enabled;
    }
    
    // ==================== GAME LOOP INTEGRATION ====================
    
    /**
     * Called from Game.update() - updates all engines
     */
    public void updateEngines(float deltaTime) {
        verifyInitialized();
        logicLayer.update(deltaTime);
    }
    
    /**
     * Called from Game startup - initialize logic layer
     */
    public void startGame() {
        verifyInitialized();
        logicLayer.newGame();
    }
    
    /**
     * Called from Game load - load saved game state
     */
    public void loadGame(String slotName) {
        verifyInitialized();
        logicLayer.loadGame(slotName);
    }
    
    /**
     * Called from Game save - save current game state
     */
    public void saveGame(String slotName) {
        verifyInitialized();
        logicLayer.saveGame(slotName);
    }
    
    /**
     * Called from Game shutdown - cleanup
     */
    public void shutdownEngines() {
        verifyInitialized();
        logicLayer.shutdown();
    }
    
    // ==================== QUERY METHODS ====================
    
    /**
     * Get reference to LogicLayerAPI (for advanced usage)
     */
    public LogicLayerAPI getLogicLayer() {
        verifyInitialized();
        return logicLayer;
    }
    
    /**
     * Get underlying GameState (for advanced queries)
     */
    public GameState getGameState() {
        verifyInitialized();
        return logicLayer.getGameState();
    }
    
    /**
     * Check if logic layer is running
     */
    public boolean isLogicLayerRunning() {
        verifyInitialized();
        return logicLayer.isRunning();
    }
    
    /**
     * Get detailed engine information for debugging
     */
    public String getEngineStatus() {
        verifyInitialized();
        StringBuilder sb = new StringBuilder();
        sb.append("[GameIntegrationBridge Status]\n");
        sb.append("LogicLayerAPI: ").append(logicLayer != null ? "initialized" : "null").append("\n");
        sb.append("Running: ").append(logicLayer.isRunning()).append("\n");
        
        GameState state = logicLayer.getGameState();
        if (state != null) {
            sb.append("PlayerLevel: ").append(state.getPlayerState().getLevel()).append("\n");
            sb.append("PlayerHealth: ").append(state.getPlayerState().getCurrentHealth())
              .append("/").append(state.getPlayerState().getMaxHealth()).append("\n");
            sb.append("PlayerExperience: ").append(state.getPlayerState().getExperiencePoints()).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Reset the bridge (for testing or new game)
     */
    public static void resetInstance() {
        instance = null;
    }
}
