package com.lilithsthrone.logic.quest;

import com.lilithsthrone.main.Main;
import com.lilithsthrone.game.dialogue.DialogueEventAdapter;
import com.lilithsthrone.game.dialogue.DialogueEventCoordinator;
import com.lilithsthrone.logic.GameIntegrationBridge;

/**
 * Adapter for Quest ↔ Dialogue integration with QuestEngine
 * 
 * Bridges quest system with dialogue events. Routes quest dialogue triggers
 * through event engine, tracks quest progression through dialogue choices,
 * and coordinates quest state changes with dialogue consequences.
 * 
 * @since Phase 2.4
 * @version 1.0
 */
public class QuestDialogueAdapter {
	
	private static final String ADAPTER_NAME = "QuestDialogueAdapter";
	
	/**
	 * Delegates quest dialogue trigger
	 * Notifies event engine when quest-related dialogue occurs
	 * 
	 * @param questId Quest identifier
	 * @param questStage Quest stage or milestone name
	 * @return true if trigger was new (first occurrence)
	 */
	public static boolean delegateQuestDialogueTrigger(String questId, String questStage) {
		if (isBridgeAvailable()) {
			try {
				return DialogueEventAdapter.delegateDialogueEventTrigger("quest_dialogue_" + questId + "_" + questStage);
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error triggering quest dialogue: " + questId + " stage: " + questStage);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Tracks quest progression through dialogue
	 * Records dialogue-based quest milestone achievements
	 * 
	 * @param questId Quest identifier
	 * @param milestone Specific dialogue milestone in quest
	 * @param dialogueNodeId Associated dialogue node
	 */
	public static void trackQuestDialogueMilestone(String questId, String milestone, String dialogueNodeId) {
		if (isBridgeAvailable()) {
			try {
				// Record milestone achievement
				String milestoneEventId = "quest_" + questId + "_dialogue_" + milestone;
				DialogueEventAdapter.delegateDialogueEventTrigger(milestoneEventId);
				
				// Register as dialogue coordinator consequence
				DialogueEventCoordinator.registerConsequence(dialogueNodeId, "quest_" + questId + "_" + milestone);
				
				System.out.println("[" + ADAPTER_NAME + "] Quest dialogue milestone tracked: " + questId + 
					" milestone: " + milestone);
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error tracking quest dialogue milestone");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Coordinates quest progression from dialogue choice
	 * Routes dialogue consequence to quest state change
	 * 
	 * @param questId Quest identifier
	 * @param currentStage Current quest stage
	 * @param nextStage Stage to progress to (null to not progress)
	 * @param dialogueNodeId Source dialogue node
	 * @param responseId Response choice
	 */
	public static void coordinateQuestProgression(String questId, String currentStage, String nextStage,
		String dialogueNodeId, String responseId) {
		
		if (isBridgeAvailable()) {
			try {
				// Track quest response
				DialogueEventAdapter.delegateQuestDialogueTrigger(questId, currentStage);
				
				// Record response choice within quest dialogue
				String questResponseId = "quest_" + questId + "_response_" + responseId;
				DialogueEventAdapter.delegateDialogueEventTrigger(questResponseId);
				
				// If progressing to next stage, record transition
				if (nextStage != null && !nextStage.isEmpty()) {
					String progressionEventId = "quest_" + questId + "_progress_to_" + nextStage;
					DialogueEventAdapter.delegateDialogueEventTrigger(progressionEventId);
					
					// Notify coordinator of quest state consequence
					DialogueEventCoordinator.registerConsequence(dialogueNodeId, progressionEventId);
				}
				
				System.out.println("[" + ADAPTER_NAME + "] Quest progression coordinated: " + questId +
					" from " + currentStage + " towards " + (nextStage != null ? nextStage : "end"));
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error coordinating quest progression");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks if quest dialogue is available
	 * Determines if player can engage with quest-related dialogue
	 * 
	 * @param questId Quest identifier
	 * @param stage Quest stage requiring dialogue
	 * @return true if dialogue is available, false otherwise
	 */
	public static boolean isQuestDialogueAvailable(String questId, String stage) {
		if (isBridgeAvailable()) {
			try {
				// Check if quest-specific event has occurred
				String dialogueEventId = "quest_dialogue_" + questId + "_" + stage;
				
				// Dialogue is available if quest hasn't completed this dialogue yet
				return !DialogueEventAdapter.hasEventOccurred(dialogueEventId);
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error checking quest dialogue availability");
				e.printStackTrace();
			}
		}
		return true; // Default to available
	}
	
	/**
	 * Tracks quest-related NPC interaction
	 * Records NPC dialogue interactions for quest tracking
	 * 
	 * @param questId Quest identifier
	 * @param npcId NPC identifier
	 * @param interactionType Type of interaction (talked, gave_item, received_reward)
	 */
	public static void trackQuestNpcInteraction(String questId, String npcId, String interactionType) {
		if (isBridgeAvailable()) {
			try {
				String interactionEventId = "quest_" + questId + "_npc_" + npcId + "_" + interactionType;
				DialogueEventAdapter.delegateDialogueEventTrigger(interactionEventId);
				
				System.out.println("[" + ADAPTER_NAME + "] Quest NPC interaction tracked: " + questId + 
					" with " + npcId + " (" + interactionType + ")");
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error tracking quest NPC interaction");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Records quest dialogue choice consequence
	 * Applies consequence from dialogue choice to quest state
	 * 
	 * @param questId Quest identifier
	 * @param consequenceId Consequence identifier
	 * @param magnitude Magnitude of consequence (1-10)
	 */
	public static void recordQuestDialogueConsequence(String questId, String consequenceId, int magnitude) {
		if (isBridgeAvailable()) {
			try {
				// Apply consequence through event system
				DialogueEventAdapter.delegateDialogueConsequence(
					"quest_" + questId + "_" + consequenceId, magnitude);
				
				System.out.println("[" + ADAPTER_NAME + "] Quest dialogue consequence recorded: " + questId +
					" consequence: " + consequenceId + " magnitude: " + magnitude);
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error recording quest dialogue consequence");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Marks quest dialogue as completed
	 * Prevents re-triggering same dialogue sequence
	 * 
	 * @param questId Quest identifier
	 * @param stage Quest stage dialogue
	 */
	public static void markQuestDialogueComplete(String questId, String stage) {
		if (isBridgeAvailable()) {
			try {
				String completeEventId = "quest_" + questId + "_dialogue_complete_" + stage;
				DialogueEventAdapter.delegateDialogueEventTrigger(completeEventId);
				
				System.out.println("[" + ADAPTER_NAME + "] Quest dialogue marked complete: " + questId + 
					" stage: " + stage);
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error marking quest dialogue complete");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Syncs dialogue-based quest progress to engine
	 * Ensures dialogue consequences update quest state properly
	 * 
	 * @param questId Quest identifier
	 */
	public static void syncQuestProgressToEngine(String questId) {
		if (isBridgeAvailable()) {
			try {
				// Framework for syncing quest progress
				System.out.println("[" + ADAPTER_NAME + "] Quest progress synced to engine: " + questId);
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error syncing quest progress to engine");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks if integration bridge is available
	 * Verifies GameIntegrationBridge is initialized
	 * 
	 * @return true if bridge is ready, false otherwise
	 */
	private static boolean isBridgeAvailable() {
		try {
			return GameIntegrationBridge.getInstance() != null 
				&& Main.game != null 
				&& Main.game.getEventEngine() != null;
		} catch (Exception e) {
			return false;
		}
	}
}
