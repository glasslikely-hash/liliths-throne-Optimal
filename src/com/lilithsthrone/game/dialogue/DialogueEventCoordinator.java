package com.lilithsthrone.game.dialogue;

import com.lilithsthrone.main.Main;
import com.lilithsthrone.logic.GameIntegrationBridge;
import java.util.*;

/**
 * Centralized dialogue event coordination system
 * 
 * Manages dialogue consequences, branching logic, and event coordination
 * through integration with EventEngine. Tracks which dialogue branches
 * have been taken and applies cascading consequences.
 * 
 * @since Phase 2.4
 * @version 1.0
 */
public class DialogueEventCoordinator {
	
	private static final String COORDINATOR_NAME = "DialogueEventCoordinator";
	
	// Tracks dialogue choices made by player (prevents re-applying consequences)
	private static Set<String> completedDialogueBranches = new HashSet<>();
	
	// Maps dialogue nodes to their consequence IDs for organization
	private static Map<String, List<String>> dialogueNodeConsequences = new HashMap<>();
	
	// Tracks active dialogue sequences
	private static Queue<String> activeDialogueSequence = new LinkedList<>();
	
	/**
	 * Initializes the dialogue event coordinator
	 * Called once at game start
	 */
	public static void initialize() {
		completedDialogueBranches.clear();
		dialogueNodeConsequences.clear();
		activeDialogueSequence.clear();
		System.out.println("[" + COORDINATOR_NAME + "] Initialized");
	}
	
	/**
	 * Coordinates dialogue choice execution with event consequences
	 * 
	 * @param dialogueNodeId Current dialogue node identifier
	 * @param choiceIndex Index of the player's chosen response
	 * @param consequenceIds Array of consequence identifiers to apply
	 */
	public static void executeDialogueChoice(String dialogueNodeId, int choiceIndex, String[] consequenceIds) {
		String branchKey = dialogueNodeId + "_choice_" + choiceIndex;
		
		// Only execute if this branch hasn't been completed
		if (!completedDialogueBranches.contains(branchKey)) {
			try {
				// Mark branch as completed
				completedDialogueBranches.add(branchKey);
				
				// Apply all consequences for this choice
				if (consequenceIds != null) {
					for (String consequenceId : consequenceIds) {
						applyDialogueConsequence(dialogueNodeId, consequenceId);
					}
				}
				
				// Trigger dialogue choice event for engine tracking
				DialogueEventAdapter.delegateDialogueEventTrigger("dialogue_choice_" + branchKey);
				
				System.out.println("[" + COORDINATOR_NAME + "] Executed dialogue choice: " + branchKey);
			} catch (Exception e) {
				System.err.println("[" + COORDINATOR_NAME + "] Error executing dialogue choice: " + branchKey);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Applies a single dialogue consequence
	 * Routes consequence through DialogueEventAdapter for engine recording
	 * 
	 * @param dialogueNodeId Source dialogue node
	 * @param consequenceId Consequence identifier
	 */
	private static void applyDialogueConsequence(String dialogueNodeId, String consequenceId) {
		try {
			// Record this consequence in engine
			DialogueEventAdapter.delegateDialogueConsequence(dialogueNodeId + "_" + consequenceId, 1);
			
			// Store in coordinator's consequence mapping
			dialogueNodeConsequences.computeIfAbsent(dialogueNodeId, k -> new ArrayList<>())
				.add(consequenceId);
			
			System.out.println("[" + COORDINATOR_NAME + "] Applied consequence: " + consequenceId + 
				" from dialogue: " + dialogueNodeId);
		} catch (Exception e) {
			System.err.println("[" + COORDINATOR_NAME + "] Error applying consequence: " + consequenceId);
			e.printStackTrace();
		}
	}
	
	/**
	 * Tracks dialogue progression through conversation tree
	 * 
	 * @param npcId NPC in conversation
	 * @param dialogueId Dialogue tree identifier
	 * @param nodeIndex Current position in dialogue
	 */
	public static void trackDialogueProgression(String npcId, String dialogueId, int nodeIndex) {
		try {
			String dialogueKey = npcId + "_" + dialogueId;
			activeDialogueSequence.offer(dialogueKey);
			
			// Update engine with progression
			DialogueEventAdapter.delegateDialogueProgress(nodeIndex);
			
			// Trigger progression event for recording
			DialogueEventAdapter.delegateDialogueEventTrigger("dialogue_progress_" + npcId + "_node_" + nodeIndex);
			
			System.out.println("[" + COORDINATOR_NAME + "] Dialogue progression tracked: " + dialogueKey + 
				" at node " + nodeIndex);
		} catch (Exception e) {
			System.err.println("[" + COORDINATOR_NAME + "] Error tracking dialogue progression");
			e.printStackTrace();
		}
	}
	
	/**
	 * Marks dialogue sequence as complete
	 * Routes completion through event engine
	 * 
	 * @param npcId NPC in conversation
	 * @param dialogueId Dialogue tree identifier
	 */
	public static void completeDialogue(String npcId, String dialogueId) {
		try {
			String dialogueKey = npcId + "_" + dialogueId;
			activeDialogueSequence.remove(dialogueKey); // Remove from active
			
			// End dialogue in engine
			DialogueEventAdapter.delegateDialogueEnd(npcId);
			
			// Trigger completion event
			DialogueEventAdapter.delegateDialogueEventTrigger("dialogue_complete_" + dialogueKey);
			
			System.out.println("[" + COORDINATOR_NAME + "] Dialogue completed: " + dialogueKey);
		} catch (Exception e) {
			System.err.println("[" + COORDINATOR_NAME + "] Error completing dialogue");
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks if dialogue choice has been made before
	 * Queries completed branches to prevent re-execution
	 * 
	 * @param dialogueNodeId Dialogue node identifier
	 * @param choiceIndex Choice index
	 * @return true if choice was previously selected, false otherwise
	 */
	public static boolean hasDialogueChoiceBeenMade(String dialogueNodeId, int choiceIndex) {
		String branchKey = dialogueNodeId + "_choice_" + choiceIndex;
		return completedDialogueBranches.contains(branchKey);
	}
	
	/**
	 * Gets all consequences applied from a dialogue node
	 * Used for checking dialogue outcomes
	 * 
	 * @param dialogueNodeId Node identifier
	 * @return List of applied consequence IDs, empty if none
	 */
	public static List<String> getAppliedConsequences(String dialogueNodeId) {
		return new ArrayList<>(dialogueNodeConsequences.getOrDefault(dialogueNodeId, new ArrayList<>()));
	}
	
	/**
	 * Gets currently active dialogue sequences
	 * Useful for debugging or conditional logic
	 * 
	 * @return Copy of active dialogue queue
	 */
	public static Queue<String> getActiveDialogues() {
		return new LinkedList<>(activeDialogueSequence);
	}
	
	/**
	 * Registers consequence for a dialogue node
	 * Pre-registers known consequences for organization
	 * 
	 * @param dialogueNodeId Node identifier
	 * @param consequenceId Consequence identifier
	 */
	public static void registerConsequence(String dialogueNodeId, String consequenceId) {
		dialogueNodeConsequences.computeIfAbsent(dialogueNodeId, k -> new ArrayList<>())
			.add(consequenceId);
	}
	
	/**
	 * Clears dialogue coordinator state (new game)
	 * Resets all tracking for fresh game start
	 */
	public static void resetState() {
		completedDialogueBranches.clear();
		dialogueNodeConsequences.clear();
		activeDialogueSequence.clear();
		System.out.println("[" + COORDINATOR_NAME + "] State reset for new game");
	}
	
	/**
	 * Gets status information for debugging
	 * Returns summary of dialogue tracking state
	 * 
	 * @return String containing coordinator status
	 */
	public static String getCoordinatorStatus() {
		return "[" + COORDINATOR_NAME + "] " +
			"CompletedBranches: " + completedDialogueBranches.size() + ", " +
			"MappedConsequences: " + dialogueNodeConsequences.size() + ", " +
			"ActiveDialogues: " + activeDialogueSequence.size();
	}
}
