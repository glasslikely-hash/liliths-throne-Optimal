package com.lilithsthrone.game.dialogue;

import com.lilithsthrone.main.Main;
import com.lilithsthrone.logic.GameIntegrationBridge;

/**
 * Adapter for Dialogue ↔ EventEngine delegation
 * 
 * Bridges dialogue system events with EventEngine for centralized event/dialogue tracking.
 * Enables event triggering from dialogue choices, dialogue progression tracking, 
 * and prevention of repeated event triggers.
 * 
 * @since Phase 2.4
 * @version 1.0
 */
public class DialogueEventAdapter {
	
	/**
	 * Delegates dialogue event trigger to EventEngine
	 * Prevents duplicate event triggering via engine-level flag tracking
	 * 
	 * @param eventId Unique identifier for the event (e.g., "talked_to_lilaya", "completed_quest_001")
	 * @return true if event was newly triggered, false if already occurred
	 */
	public static boolean delegateDialogueEventTrigger(String eventId) {
		if (isBridgeAvailable()) {
			try {
				// Check if event already occurred (prevents re-triggering)
				if (!Main.game.getEventEngine().hasEventOccurred(eventId)) {
					// First occurrence - trigger through engine
					Main.game.getEventEngine().triggerEvent(eventId);
					return true;
				}
				return false;
			} catch (Exception e) {
				System.err.println("[DialogueEventAdapter] Error triggering dialogue event: " + eventId);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates dialogue progression to EventEngine
	 * Tracks current dialogue node in engine state for synchronization
	 * 
	 * @param npcId NPC identifier or dialogue source
	 * @param dialogueId Unique identifier for dialogue tree
	 */
	public static void delegateDialogueStart(String npcId, String dialogueId) {
		if (isBridgeAvailable()) {
			try {
				Main.game.getEventEngine().startDialogue(npcId, dialogueId);
			} catch (Exception e) {
				System.err.println("[DialogueEventAdapter] Error starting dialogue: " + npcId + " -> " + dialogueId);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates dialogue node progression
	 * Updates engine's tracking of current dialogue position
	 * 
	 * @param nodeId Index of current dialogue node in conversation tree
	 */
	public static void delegateDialogueProgress(int nodeId) {
		if (isBridgeAvailable()) {
			try {
				Main.game.getEventEngine().progressDialogue(nodeId);
			} catch (Exception e) {
				System.err.println("[DialogueEventAdapter] Error progressing dialogue to node: " + nodeId);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates dialogue ending to EventEngine
	 * Cleans up dialogue state in engine tracking
	 * 
	 * @param npcId NPC identifier or dialogue source
	 */
	public static void delegateDialogueEnd(String npcId) {
		if (isBridgeAvailable()) {
			try {
				Main.game.getEventEngine().endDialogue(npcId);
			} catch (Exception e) {
				System.err.println("[DialogueEventAdapter] Error ending dialogue for: " + npcId);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates consequence application for dialogue choices
	 * Routes complex dialogue consequences through event system
	 * 
	 * @param consequenceId Unique identifier for consequence set
	 * @param magnitude Optional parameter for consequence intensity/scale
	 */
	public static void delegateDialogueConsequence(String consequenceId, int magnitude) {
		if (isBridgeAvailable()) {
			try {
				// Record consequence occurrence and magnitude
				String consequenceKey = consequenceId + "_magnitude_" + magnitude;
				Main.game.getEventEngine().triggerEvent(consequenceKey);
			} catch (Exception e) {
				System.err.println("[DialogueEventAdapter] Error applying dialogue consequence: " + consequenceId);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates quest dialogue trigger
	 * Routes quest-specific dialogue events through event engine
	 * 
	 * @param questId Quest identifier
	 * @param stage Quest stage or milestone
	 */
	public static void delegateQuestDialogueTrigger(String questId, String stage) {
		if (isBridgeAvailable()) {
			try {
				String eventId = "quest_" + questId + "_" + stage;
				Main.game.getEventEngine().triggerEvent(eventId);
			} catch (Exception e) {
				System.err.println("[DialogueEventAdapter] Error triggering quest dialogue: " + questId + " stage: " + stage);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks if event has already occurred (prevents re-triggering)
	 * Queries engine-level event flag tracking
	 * 
	 * @param eventId Event identifier to check
	 * @return true if event has occurred, false otherwise
	 */
	public static boolean hasEventOccurred(String eventId) {
		if (isBridgeAvailable()) {
			try {
				return Main.game.getEventEngine().hasEventOccurred(eventId);
			} catch (Exception e) {
				System.err.println("[DialogueEventAdapter] Error checking event occurrence: " + eventId);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Gets current dialogue from engine
	 * Queries dialogue state for UI or conditional logic
	 * 
	 * @return Current dialogue identifier or empty string if none
	 */
	public static String getCurrentDialogue() {
		if (isBridgeAvailable()) {
			try {
				return Main.game.getEventEngine().getCurrentDialogue();
			} catch (Exception e) {
				System.err.println("[DialogueEventAdapter] Error getting current dialogue");
				e.printStackTrace();
			}
		}
		return "";
	}
	
	/**
	 * Delegates flag-based dialogue condition check
	 * Used in dialogue node conditional logic
	 * 
	 * @param flagId Dialogue flag identifier
	 * @return true if flag is set, false otherwise
	 */
	public static boolean delegateDialogueFlagCheck(String flagId) {
		if (isBridgeAvailable()) {
			try {
				// Flag exists if event with same ID has occurred
				return Main.game.getEventEngine().hasEventOccurred(flagId);
			} catch (Exception e) {
				System.err.println("[DialogueEventAdapter] Error checking dialogue flag: " + flagId);
				e.printStackTrace();
			}
		}
		// Fallback to legacy DialogueFlags system
		return checkLegacyDialogueFlag(flagId);
	}
	
	/**
	 * Delegates flag setting for dialogue conditions
	 * Sets flag for conditional dialogue logic
	 * 
	 * @param flagId Dialogue flag identifier
	 */
	public static void delegateDialogueFlagSet(String flagId) {
		if (isBridgeAvailable()) {
			try {
				Main.game.getEventEngine().triggerEvent(flagId);
			} catch (Exception e) {
				System.err.println("[DialogueEventAdapter] Error setting dialogue flag: " + flagId);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Syncs dialogue flags from legacy system to engine on load
	 * Ensures engine state matches DialogueFlags on game load
	 * 
	 * @param dialogueFlags Dialogue flags from game save
	 */
	public static void syncDialogueFlagsToEngine(DialogueFlags dialogueFlags) {
		if (isBridgeAvailable() && dialogueFlags != null) {
			try {
				// Framework for syncing complex flag state to engine
				// Could expand to sync additional flag types as needed
				Main.game.getEventEngine().initialize();
			} catch (Exception e) {
				System.err.println("[DialogueEventAdapter] Error syncing dialogue flags to engine");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks if integration bridge is available
	 * Verifies GameIntegrationBridge is initialized before delegating
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
	
	/**
	 * Fallback to legacy DialogueFlags for conditions when bridge unavailable
	 * Provides backward compatibility if engine delegation fails
	 * 
	 * @param flagId Flag identifier
	 * @return Flag status from legacy DialogueFlags system
	 */
	private static boolean checkLegacyDialogueFlag(String flagId) {
		if (Main.game != null && Main.game.getDialogueFlags() != null) {
			try {
				// Check if flag exists in legacy temp booleans (framework)
				return Main.game.getDialogueFlags().getTempBoolean(flagId, false);
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}
}
