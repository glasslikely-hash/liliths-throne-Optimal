package com.lilithsthrone.logic.engines;

import com.lilithsthrone.logic.persistence.DeltaEngine;
import com.lilithsthrone.logic.state.GameState;
import java.util.*;

/**
 * Quest Engine - manages quest progression, objectives, and rewards.
 * 
 * Manages:
 *   - Quest start, progress, completion
 *   - Quest objectives and milestones
 *   - Quest rewards (XP, items, unlocks)
 *   - Quest state persistence
 *   - Quest branching logic
 * 
 * Actions:
 *   - startQuest(questId): Begin a new quest
 *   - updateQuestObjective(questId, objectiveId, progress): Update progress
 *   - completeQuest(questId): Mark quest as complete
 *   - abandonQuest(questId): Cancel active quest
 *   - getQuestProgress(questId): Query current state
 * 
 * State Modified:
 *   - QuestFlags (quest completion tracking)
 *   - QuestProgress (objective progress)
 *   - PlayerState (XP and rewards)
 */
public class QuestEngine extends BaseEngine {
    private static final String ENGINE_NAME = "QuestEngine";
    
    private Map<String, Integer> questProgress = new HashMap<>();
    private Set<String> activeQuests = new HashSet<>();
    private Set<String> completedQuests = new HashSet<>();
    
    public QuestEngine(GameState gameState, DeltaEngine deltaEngine) {
        super(gameState, deltaEngine);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        questProgress.clear();
        activeQuests.clear();
        completedQuests.clear();
        System.out.println("[" + ENGINE_NAME + "] Initialized");
    }
    
    @Override
    public void update() {
        // Check for quest completion conditions each frame
        for (String questId : new HashSet<>(activeQuests)) {
            if (shouldCompleteQuest(questId)) {
                completeQuest(questId);
            }
        }
    }
    
    public void startQuest(String questId) {
        if (!activeQuests.contains(questId) && !completedQuests.contains(questId)) {
            activeQuests.add(questId);
            questProgress.put(questId, 0);
            recordChange("activeQuests", questId);
            System.out.println("[" + ENGINE_NAME + "] Quest started: " + questId);
        }
    }
    
    public void updateQuestObjective(String questId, String objectiveId, int progress) {
        if (activeQuests.contains(questId)) {
            String key = questId + ":" + objectiveId;
            questProgress.put(key, progress);
            recordChange("questProgress", progress);
            System.out.println("[" + ENGINE_NAME + "] Quest objective updated: " + key + " -> " + progress);
        }
    }
    
    public void completeQuest(String questId) {
        if (activeQuests.contains(questId)) {
            activeQuests.remove(questId);
            completedQuests.add(questId);
            // Award XP
            gameState.getPlayerState().addExperience(500);
            recordChange("completedQuests", questId);
            recordChange("playerExperience", 500);
            System.out.println("[" + ENGINE_NAME + "] Quest completed: " + questId + " (500 XP awarded)");
        }
    }
    
    public void abandonQuest(String questId) {
        if (activeQuests.contains(questId)) {
            activeQuests.remove(questId);
            questProgress.remove(questId);
            recordChange("activeQuests", questId);
            System.out.println("[" + ENGINE_NAME + "] Quest abandoned: " + questId);
        }
    }
    
    public int getQuestProgress(String questId) {
        return questProgress.getOrDefault(questId, 0);
    }
    
    public Set<String> getActiveQuests() {
        return new HashSet<>(activeQuests);
    }
    
    public Set<String> getCompletedQuests() {
        return new HashSet<>(completedQuests);
    }
    
    private boolean shouldCompleteQuest(String questId) {
        // Placeholder logic - would check GameState conditions
        return false;
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
        activeQuests.clear();
        System.out.println("[" + ENGINE_NAME + "] Shutdown");
    }
}
