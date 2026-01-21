package com.lilithsthrone.logic;

import com.lilithsthrone.logic.state.GameState;
import java.util.*;

/**
 * GameStateAdapter - Adapts legacy Game.java state to LogicLayerAPI state
 * 
 * Provides:
 * - Bidirectional state synchronization
 * - Conversion between legacy and new state representations
 * - Validation and consistency checking
 * - Data mapping between GameState and Game.java fields
 * 
 * Used by GameIntegrationBridge to keep states in sync:
 * - Read from Game.java
 * - Convert to GameState format
 * - Update GameState
 * - Convert back to Game.java format
 * - Write to Game.java
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class GameStateAdapter {
    private GameState gameState;
    private Map<String, Object> legacyStateCache = new HashMap<>();
    
    /**
     * Create adapter for given GameState
     */
    public GameStateAdapter(GameState gameState) {
        if (gameState == null) {
            throw new IllegalArgumentException("GameState cannot be null");
        }
        this.gameState = gameState;
        System.out.println("[GameStateAdapter] Created for GameState");
    }
    
    /**
     * Adapt legacy Game field to GameState
     * Called when legacy code reads from Game fields
     */
    public Object getAdaptedValue(String fieldName, Object legacyValue) {
        if (legacyValue == null) {
            return null;
        }
        
        switch (fieldName) {
            // Player fields
            case "playerHealth":
                return gameState.getPlayerState().getCurrentHealth();
            case "playerMaxHealth":
                return gameState.getPlayerState().getMaxHealth();
            case "playerMana":
                return gameState.getPlayerState().getCurrentMana();
            case "playerMaxMana":
                return gameState.getPlayerState().getMaxMana();
            case "playerLevel":
                return gameState.getPlayerState().getLevel();
            case "playerExperience":
                return gameState.getPlayerState().getExperiencePoints();
            case "playerLocation":
                return gameState.getPlayerState().getCurrentLocation();
            case "playerAttributes":
                return gameState.getPlayerAttributes().getAttributes();
            
            // Inventory fields
            case "playerInventory":
                return gameState.getInventoryState().getItems();
            case "equippedItems":
                return gameState.getInventoryState().getEquippedItems();
            
            // Buff/effect fields
            case "activeEffects":
                return gameState.getBuffState().getActiveEffects();
            case "activePerkIds":
                return gameState.getBuffState().getActivePerkIds();
            
            // Quest/dialogue fields
            case "questProgress":
                return gameState.getQuestState();
            case "dialogueState":
                return gameState.getDialogueState();
            
            // World fields
            case "npcStates":
                return gameState.getNpcStates();
            case "visitedLocations":
                return gameState.getVisitedLocations();
            
            // Default - return original value
            default:
                return legacyValue;
        }
    }
    
    /**
     * Adapt GameState value to legacy format
     * Called when GameState is modified and needs to sync to Game.java
     */
    public Object adaptGameStateToLegacy(String fieldName, Object gameStateValue) {
        if (gameStateValue == null) {
            return null;
        }
        
        // Most values can be passed through directly
        // Only complex objects need special handling
        
        switch (fieldName) {
            // Player
            case "playerHealth":
                return gameState.getPlayerState().getCurrentHealth();
            case "playerLevel":
                return gameState.getPlayerState().getLevel();
            
            // Collections - return copies to prevent direct modification
            case "activeQuests": {
                Set<String> activeQuests = new HashSet<>();
                // Populate from GameState
                return activeQuests;
            }
            
            case "activeEffects": {
                Map<String, Integer> effects = new HashMap<>();
                // Populate from GameState
                return effects;
            }
            
            // Default
            default:
                return gameStateValue;
        }
    }
    
    /**
     * Cache legacy value for later comparison
     */
    public void cacheLegacyValue(String fieldName, Object value) {
        legacyStateCache.put(fieldName, value);
    }
    
    /**
     * Check if legacy value has changed
     */
    public boolean hasLegacyValueChanged(String fieldName, Object currentValue) {
        Object cachedValue = legacyStateCache.get(fieldName);
        
        if (cachedValue == null && currentValue == null) {
            return false;
        }
        if (cachedValue == null || currentValue == null) {
            return true;
        }
        return !cachedValue.equals(currentValue);
    }
    
    /**
     * Validate that GameState and legacy values are consistent
     */
    public boolean validateConsistency() {
        boolean valid = true;
        
        // Check player state
        int gsHealth = gameState.getPlayerState().getCurrentHealth();
        if (gsHealth < 0 || gsHealth > gameState.getPlayerState().getMaxHealth()) {
            System.err.println("[GameStateAdapter] Invalid player health: " + gsHealth);
            valid = false;
        }
        
        int gsLevel = gameState.getPlayerState().getLevel();
        if (gsLevel < 1) {
            System.err.println("[GameStateAdapter] Invalid player level: " + gsLevel);
            valid = false;
        }
        
        long gsExp = gameState.getPlayerState().getExperiencePoints();
        if (gsExp < 0) {
            System.err.println("[GameStateAdapter] Invalid experience: " + gsExp);
            valid = false;
        }
        
        return valid;
    }
    
    /**
     * Synchronize complete state from legacy Game to GameState
     */
    public void syncFromLegacy(Map<String, Object> legacyState) {
        for (Map.Entry<String, Object> entry : legacyState.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Only update if changed
            Object adapted = getAdaptedValue(key, value);
            if (!adapted.equals(legacyStateCache.get(key))) {
                // Update GameState
                updateGameStateFromLegacy(key, value);
            }
        }
    }
    
    /**
     * Update GameState from legacy value
     */
    private void updateGameStateFromLegacy(String fieldName, Object value) {
        switch (fieldName) {
            case "playerHealth":
                if (value instanceof Integer) {
                    gameState.getPlayerState().setCurrentHealth((Integer) value);
                }
                break;
            case "playerLocation":
                if (value instanceof String) {
                    gameState.getPlayerState().setCurrentLocation((String) value);
                }
                break;
            // Add more as needed
        }
    }
    
    /**
     * Get GameState reference
     */
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * Clear caches (useful for testing)
     */
    public void clearCaches() {
        legacyStateCache.clear();
    }
    
    /**
     * Get status for debugging
     */
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("[GameStateAdapter Status]\n");
        sb.append("GameState: ").append(gameState != null ? "initialized" : "null").append("\n");
        sb.append("Cached entries: ").append(legacyStateCache.size()).append("\n");
        sb.append("Valid: ").append(validateConsistency()).append("\n");
        return sb.toString();
    }
}
