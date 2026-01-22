package com.lilithsthrone.logic.engines;

import com.lilithsthrone.logic.persistence.DeltaEngine;
import com.lilithsthrone.logic.state.GameState;
import java.util.*;

/**
 * Character Engine - manages experience, leveling, attributes, and skills.
 * 
 * Manages:
 *   - Experience points and leveling
 *   - Attribute points and allocation
 *   - Skill learning and progression
 *   - Health and mana calculations
 *   - Stat recalculation on level up
 * 
 * Actions:
 *   - gainExperience(amount): Add XP
 *   - levelUp(): Advance level and grant points
 *   - modifyAttribute(attributeId, delta): Change attribute
 *   - calculateStats(): Recalculate derived stats
 *   - learnSkill(skillId): Learn new skill
 * 
 * State Modified:
 *   - PlayerExperience (XP tracking)
 *   - PlayerLevel (character level)
 *   - PlayerAttributes (strength, intellect, vitality, etc.)
 *   - HealthMana (max health/mana)
 */
public class CharacterEngine extends BaseEngine {
    private static final String ENGINE_NAME = "CharacterEngine";
    private static final int XP_PER_LEVEL = 1000;
    private static final int ATTRIBUTE_POINTS_PER_LEVEL = 5;
    
    public CharacterEngine(GameState gameState, DeltaEngine deltaEngine) {
        super(gameState, deltaEngine);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        calculateStats();
        System.out.println("[" + ENGINE_NAME + "] Initialized");
    }
    
    @Override
    public void update() {
        // Character calculations happen on level up or attribute change
        // No continuous update needed
    }
    
    public void gainExperience(int amount) {
        long currentExp = gameState.getPlayerState().getExperiencePoints();
        long newExp = currentExp + amount;
        gameState.getPlayerState().setExperiencePoints(newExp);
        
        // Check for level up
        int currentLevel = gameState.getPlayerState().getLevel();
        int newLevel = (int) (newExp / XP_PER_LEVEL);
        
        while (currentLevel < newLevel) {
            currentLevel++;
            levelUp();
        }
        
        recordChange("playerExperience", amount);
        System.out.println("[" + ENGINE_NAME + "] Experience gained: " + amount + " (total: " + newExp + ")");
    }
    
    public void levelUp() {
        int currentLevel = gameState.getPlayerState().getLevel();
        int newLevel = currentLevel + 1;
        gameState.getPlayerState().setLevel(newLevel);
        
        // Grant attribute points
        int attributePoints = ATTRIBUTE_POINTS_PER_LEVEL;
        gameState.getPlayerState().addAttributePoints(attributePoints);
        
        // Recalculate all stats
        calculateStats();
        
        recordChange("playerLevel", newLevel);
        recordChange("attributePoints", attributePoints);
        System.out.println("[" + ENGINE_NAME + "] Level up! New level: " + newLevel + " (" + attributePoints + " attribute points gained)");
    }
    
    public void modifyAttribute(String attributeId, int delta) {
        int currentValue = getAttributeValue(attributeId);
        int newValue = Math.max(1, currentValue + delta);
        // Would set attribute in GameState
        recordChange("attribute_" + attributeId, newValue);
        calculateStats();
        System.out.println("[" + ENGINE_NAME + "] Attribute modified: " + attributeId + " -> " + newValue);
    }
    
    public void learnSkill(String skillId) {
        // Add skill to player's skill list
        recordChange("skill_" + skillId, 1);
        System.out.println("[" + ENGINE_NAME + "] Skill learned: " + skillId);
    }
    
    public void calculateStats() {
        // Recalculate maximum health
        int vitalityAttr = getAttributeValue("vitality");
        int maxHealth = 100 + (vitalityAttr * 10);
        gameState.getPlayerState().setMaxHealth(maxHealth);

        // Recalculate mana
        int intellectAttr = getAttributeValue("intellect");
        int maxMana = 50 + (intellectAttr * 5);
        gameState.getPlayerState().setMaxMana(maxMana);

        // Recalculate damage
        int strengthAttr = getAttributeValue("strength");
        // base damage calculation would use strength stat
        @SuppressWarnings("unused")
        int baseDamage = 5 + strengthAttr;
        recordChange("playerMaxHealth", maxHealth);
        recordChange("playerMaxMana", maxMana);

        System.out.println("[" + ENGINE_NAME + "] Stats calculated");
    }

    private int getAttributeValue(String attributeId) {
        return 10;  // Placeholder, would get from GameState
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
        System.out.println("[" + ENGINE_NAME + "] Shutdown");
    }
}
