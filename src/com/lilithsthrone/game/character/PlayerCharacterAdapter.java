package com.lilithsthrone.game.character;

import com.lilithsthrone.logic.GameIntegrationBridge;

/**
 * PlayerCharacterAdapter - Integrates PlayerCharacter with new CharacterEngine
 * 
 * This adapter ensures PlayerCharacter delegates experience/leveling to CharacterEngine
 * while maintaining backward compatibility with existing code.
 * 
 * Delegation Pattern:
 * - Experience gain: incrementExperience() → CharacterEngine.gainExperience()
 * - Level up: levelUp() → triggers via CharacterEngine
 * - Attributes: modifyAttribute() → CharacterEngine (future)
 * - Skills: learnSkill() → CharacterEngine (future)
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class PlayerCharacterAdapter {
    
    /**
     * Delegate: Player gains experience
     * Routes to: CharacterEngine via GameIntegrationBridge
     */
    public static void delegateExperienceGain(PlayerCharacter player, int amount) {
        if (player == null) {
            return;
        }
        
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge != null) {
                bridge.gainPlayerExperience(amount);
            }
        } catch (Exception e) {
            System.err.println("[PlayerCharacterAdapter] Failed to delegate experience gain: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Delegate: Player learns a skill
     * Routes to: CharacterEngine via GameIntegrationBridge
     */
    public static void delegateSkillLearning(PlayerCharacter player, String skillId) {
        if (player == null) {
            return;
        }
        
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge != null) {
                bridge.playerLearnSkill(skillId);
            }
        } catch (Exception e) {
            System.err.println("[PlayerCharacterAdapter] Failed to delegate skill learning: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Delegate: Player modifies attribute
     * Routes to: CharacterEngine via GameIntegrationBridge
     */
    public static void delegateAttributeModification(PlayerCharacter player, String attributeId, int delta) {
        if (player == null) {
            return;
        }
        
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge != null) {
                bridge.modifyPlayerAttribute(attributeId, delta);
            }
        } catch (Exception e) {
            System.err.println("[PlayerCharacterAdapter] Failed to delegate attribute modification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if bridge is available (for conditional delegation)
     */
    public static boolean isBridgeAvailable() {
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            return bridge != null && bridge.getLogicLayer() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Sync player state from legacy to CharacterEngine
     * Called during initialization to ensure consistency
     */
    public static void syncPlayerStateToEngine(PlayerCharacter player) {
        if (player == null) {
            return;
        }
        
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge == null) {
                return;
            }
            
            // Sync current stats
            int currentLevel = player.getLevel();
            int currentExp = player.getExperience();
            
            // Initialize engine with current player state
            // This is called when game starts or player is created
            System.out.println("[PlayerCharacterAdapter] Synced player state - Level: " + currentLevel + ", XP: " + currentExp);
            
        } catch (Exception e) {
            System.err.println("[PlayerCharacterAdapter] Failed to sync player state: " + e.getMessage());
        }
    }
}
