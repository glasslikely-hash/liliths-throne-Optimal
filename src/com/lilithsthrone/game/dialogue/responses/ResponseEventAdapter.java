package com.lilithsthrone.game.dialogue.responses;

import com.lilithsthrone.game.dialogue.DialogueEventAdapter;
import com.lilithsthrone.game.dialogue.DialogueEventCoordinator;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.logic.GameIntegrationBridge;

/**
 * Adapter for Response (dialogue choice) ↔ EventEngine integration
 * 
 * Bridges dialogue response execution with event system. Routes response
 * effects through EventEngine for centralized tracking and consequence
 * management. Enables dialog-driven narrative progression and world state
 * changes through event coordination.
 * 
 * @since Phase 2.4
 * @version 1.0
 */
public class ResponseEventAdapter {
	
	private static final String ADAPTER_NAME = "ResponseEventAdapter";
	
	/**
	 * Delegates response execution to event system
	 * Routes response selection and effects through EventEngine
	 * 
	 * @param responseId Unique identifier for this response option
	 * @param responseText Display text of the response
	 * @param dialogueNodeId Source dialogue node
	 * @param choiceIndex Index of this response in choice list
	 * @return true if response was executed, false if already executed
	 */
	public static boolean delegateResponseExecution(String responseId, String responseText, 
		String dialogueNodeId, int choiceIndex) {
		
		if (isBridgeAvailable()) {
			try {
				// Check if this response has been chosen before
				if (!DialogueEventCoordinator.hasDialogueChoiceBeenMade(dialogueNodeId, choiceIndex)) {
					// First time choosing this response
					DialogueEventAdapter.delegateDialogueEventTrigger("response_" + responseId);
					
					// Track response selection
					DialogueEventAdapter.delegateDialogueEventTrigger("response_choice_" + dialogueNodeId + "_" + choiceIndex);
					
					System.out.println("[" + ADAPTER_NAME + "] Response executed: " + responseId + 
						" at node: " + dialogueNodeId);
					
					return true;
				}
				return false;
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error executing response: " + responseId);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates response effects (consequences) to event system
	 * Routes all dialogue choice effects through event coordination
	 * 
	 * @param responseId Response identifier
	 * @param dialogueNodeId Source dialogue node
	 * @param consequenceArray Array of consequence identifiers to apply
	 */
	public static void delegateResponseEffects(String responseId, String dialogueNodeId, 
		String[] consequenceArray) {
		
		if (isBridgeAvailable()) {
			try {
				// Get response choice index from ID (framework - can be enhanced)
				int choiceIndex = extractChoiceIndex(responseId);
				
				// Execute dialogue choice through coordinator (applies all consequences)
				DialogueEventCoordinator.executeDialogueChoice(dialogueNodeId, choiceIndex, consequenceArray);
				
				System.out.println("[" + ADAPTER_NAME + "] Response effects applied: " + responseId);
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error applying response effects: " + responseId);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates quest-related response
	 * Routes quest consequence responses through quest engine integration
	 * 
	 * @param questId Quest identifier
	 * @param questStage Current stage of quest
	 * @param responseId Response identifier
	 */
	public static void delegateQuestResponse(String questId, String questStage, String responseId) {
		if (isBridgeAvailable()) {
			try {
				// Trigger quest dialogue event
				DialogueEventAdapter.delegateQuestDialogueTrigger(questId, questStage);
				
				// Record response choice for this quest moment
				DialogueEventAdapter.delegateDialogueEventTrigger("quest_response_" + questId + "_" + questStage + "_" + responseId);
				
				System.out.println("[" + ADAPTER_NAME + "] Quest response recorded: " + questId + 
					" stage: " + questStage + " response: " + responseId);
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error recording quest response");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks if response can be selected (not already completed)
	 * Enables conditional response availability based on history
	 * 
	 * @param responseId Response identifier
	 * @param dialogueNodeId Source dialogue node
	 * @param choiceIndex Choice index
	 * @return true if response can be selected, false if already completed
	 */
	public static boolean isResponseSelectable(String responseId, String dialogueNodeId, int choiceIndex) {
		if (isBridgeAvailable()) {
			try {
				// Response is selectable if dialogue choice hasn't been made yet
				return !DialogueEventCoordinator.hasDialogueChoiceBeenMade(dialogueNodeId, choiceIndex);
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error checking response selectability: " + responseId);
				e.printStackTrace();
			}
		}
		return true; // Default to selectable if bridge unavailable
	}
	
	/**
	 * Delegates conditional response availability
	 * Checks if response should be available based on event flags
	 * 
	 * @param responseId Response identifier
	 * @param requiredEventId Event that must have occurred for response to appear
	 * @return true if response meets conditions, false otherwise
	 */
	public static boolean delegateResponseCondition(String responseId, String requiredEventId) {
		if (isBridgeAvailable() && requiredEventId != null && !requiredEventId.isEmpty()) {
			try {
				// Response available only if required event has occurred
				boolean hasEvent = DialogueEventAdapter.hasEventOccurred(requiredEventId);
				
				if (!hasEvent) {
					System.out.println("[" + ADAPTER_NAME + "] Response unavailable (missing event): " + 
						responseId + " needs: " + requiredEventId);
				}
				return hasEvent;
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error checking response condition: " + responseId);
				e.printStackTrace();
			}
		}
		return true; // Default to available if no condition or bridge unavailable
	}
	
	/**
	 * Delegates conditional response based on multiple events
	 * Advanced condition checking for complex requirements
	 * 
	 * @param responseId Response identifier
	 * @param requireAllEvents true to require all events, false for any event
	 * @param eventIds Event identifiers to check
	 * @return true if response conditions are met
	 */
	public static boolean delegateResponseConditionMultiple(String responseId, boolean requireAllEvents, 
		String[] eventIds) {
		
		if (isBridgeAvailable() && eventIds != null && eventIds.length > 0) {
			try {
				if (requireAllEvents) {
					// All events must have occurred
					for (String eventId : eventIds) {
						if (!DialogueEventAdapter.hasEventOccurred(eventId)) {
							return false;
						}
					}
					return true;
				} else {
					// At least one event must have occurred
					for (String eventId : eventIds) {
						if (DialogueEventAdapter.hasEventOccurred(eventId)) {
							return true;
						}
					}
					return false;
				}
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error checking multi-event response condition: " + responseId);
				e.printStackTrace();
			}
		}
		return true;
	}
	
	/**
	 * Gets next dialogue node based on response selection
	 * Queries event coordinator for consequence-driven branching
	 * 
	 * @param responseId Response identifier
	 * @param defaultNextNodeId Default node if no special branching
	 * @return Next dialogue node ID to display
	 */
	public static String getNextDialogueNode(String responseId, String defaultNextNodeId) {
		if (isBridgeAvailable()) {
			try {
				// Could implement consequence-based branching here
				// For now, returns default node
				return defaultNextNodeId;
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error determining next dialogue node");
				e.printStackTrace();
			}
		}
		return defaultNextNodeId;
	}
	
	/**
	 * Delegates response tag application (combat, sex, trade, etc.)
	 * Routes special response types through event system
	 * 
	 * @param responseId Response identifier
	 * @param tagType Type of response (combat, sex, trade)
	 */
	public static void delegateResponseTag(String responseId, String tagType) {
		if (isBridgeAvailable()) {
			try {
				// Record response tag event
				DialogueEventAdapter.delegateDialogueEventTrigger("response_tag_" + tagType + "_" + responseId);
				
				System.out.println("[" + ADAPTER_NAME + "] Response tag applied: " + tagType + 
					" for response: " + responseId);
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error applying response tag");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Extracts choice index from response ID
	 * Framework method for parsing response identifiers
	 * 
	 * @param responseId Response identifier
	 * @return Choice index, defaults to 0 if unable to parse
	 */
	private static int extractChoiceIndex(String responseId) {
		try {
			if (responseId != null && responseId.contains("_")) {
				String[] parts = responseId.split("_");
				int lastPart = Integer.parseInt(parts[parts.length - 1]);
				return lastPart;
			}
		} catch (NumberFormatException e) {
			// Could not parse, use default
		}
		return 0;
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
