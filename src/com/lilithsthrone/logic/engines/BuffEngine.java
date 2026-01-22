package com.lilithsthrone.logic.engines;

import com.lilithsthrone.data.DataStore;
import com.lilithsthrone.logic.persistence.DeltaEngine;
import com.lilithsthrone.logic.state.GameState;
import com.lilithsthrone.utils.logging.LogManager;
import java.util.*;

/**
 * Buff Engine - manages temporary effects, perks, and attribute modifiers.
 * 
 * Manages:
 *   - Active effects with durations
 *   - Effect stacking and deduplication
 *   - Permanent perks
 *   - Attribute modifiers from effects
 *   - Effect expiration
 * 
 * Actions:
 *   - applyEffect(effectId, duration): Add temporary effect
 *   - removeEffect(effectId): Remove effect
 *   - addPerk(perkId): Add permanent perk
 *   - removePerk(perkId): Remove perk
 *   - updateEffectDurations(): Decrement timers
 * 
 * State Modified:
 *   - ActiveEffects (effect tracking with durations)
 *   - ActivePerks (permanent perk list)
 *   - PlayerAttributes (temporary stat modifiers)
 */
public class BuffEngine extends BaseEngine {
    private static final String ENGINE_NAME = "BuffEngine";
    
    private Map<String, Integer> activeEffects = new HashMap<>(); // effectId -> remainingTime
    private Map<String, Integer> effectStacks = new HashMap<>();    // effectId -> stackCount
    private Set<String> activePerkIds = new HashSet<>();
    private Map<String, Integer> attributeModifiers = new HashMap<>();
    
    public BuffEngine(GameState gameState, DeltaEngine deltaEngine) {
        super(gameState, deltaEngine);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        activeEffects.clear();
        effectStacks.clear();
        activePerkIds.clear();
        attributeModifiers.clear();
        LogManager.info(ENGINE_NAME, "Initialized");
    }
    
    @Override
    public void update() {
        // Update effect durations each frame
        updateEffectDurations();
    }
    
    public void applyEffect(String effectId, int durationSeconds) {
        if (durationSeconds <= 0) {
            LogManager.warn(ENGINE_NAME, "Cannot apply effect with duration <= 0: " + effectId);
            return;
        }
        
        int currentDuration = activeEffects.getOrDefault(effectId, 0);
        activeEffects.put(effectId, currentDuration + durationSeconds);
        
        // Handle stacking
        int stacks = effectStacks.getOrDefault(effectId, 1);
        effectStacks.put(effectId, stacks + 1);
        
        // Apply attribute modifiers from effect
        applyEffectModifiers(effectId, stacks);
        
        recordChange("activeEffects", durationSeconds);
        LogManager.info(ENGINE_NAME, "Effect applied: " + effectId + " for " + durationSeconds + " seconds (stacks: " + stacks + ")");
    }
    
    public void removeEffect(String effectId) {
        activeEffects.remove(effectId);
        effectStacks.remove(effectId);
        removeEffectModifiers(effectId);
        recordChange("activeEffects", effectId);
        LogManager.info(ENGINE_NAME, "Effect removed: " + effectId);
    }
    
    public void addPerk(String perkId) {
        if (!activePerkIds.contains(perkId)) {
            activePerkIds.add(perkId);
            recordChange("activePerks", perkId);
            System.out.println("[" + ENGINE_NAME + "] Perk added: " + perkId);
        }
    }
    
    public void removePerk(String perkId) {
        if (activePerkIds.contains(perkId)) {
            activePerkIds.remove(perkId);
            recordChange("activePerks", perkId);
            System.out.println("[" + ENGINE_NAME + "] Perk removed: " + perkId);
        }
    }
    
    public Map<String, Integer> getActiveEffects() {
        return new HashMap<>(activeEffects);
    }
    
    public Set<String> getActivePerks() {
        return new HashSet<>(activePerkIds);
    }
    
    public int getEffectStacks(String effectId) {
        return effectStacks.getOrDefault(effectId, 0);
    }
    
    private void updateEffectDurations() {
        Set<String> toRemove = new HashSet<>();
        
        for (Map.Entry<String, Integer> entry : activeEffects.entrySet()) {
            int newDuration = entry.getValue() - 1;  // Decrement by 1 per frame
            if (newDuration <= 0) {
                toRemove.add(entry.getKey());
            } else {
                activeEffects.put(entry.getKey(), newDuration);
            }
        }
        
        for (String effectId : toRemove) {
            removeEffect(effectId);
        }
    }
    
    private void applyEffectModifiers(String effectId, int stackCount) {
        // Placeholder: would apply attribute modifiers based on effect definition
        // Example: "strength_boost" might add 2 * stackCount to strength
        int modifier = 5 * stackCount;
        attributeModifiers.put(effectId, modifier);
    }
    
    private void removeEffectModifiers(String effectId) {
        attributeModifiers.remove(effectId);
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
        activeEffects.clear();
        LogManager.info(ENGINE_NAME, "Shutdown");
    }
}
