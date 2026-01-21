package com.lilithsthrone.logic.engines;

import com.lilithsthrone.logic.persistence.DeltaEngine;
import com.lilithsthrone.logic.state.GameState;
import java.util.*;

/**
 * Event Engine - manages world events, dialogue trees, and state transitions.
 * 
 * Manages:
 *   - World events and their triggers
 *   - Dialogue trees and conversation progression
 *   - Event consequences and state changes
 *   - Dialogue flags for preventing re-triggers
 *   - Event branching logic
 * 
 * Actions:
 *   - triggerEvent(eventId): Start an event
 *   - startDialogue(npcId, dialogueId): Begin conversation
 *   - progressDialogue(nodeId): Advance dialogue tree
 *   - endDialogue(npcId): End conversation
 *   - getCurrentDialogue(): Query current state
 * 
 * State Modified:
 *   - EventFlags (event occurrence tracking)
 *   - DialogueStates (current node in tree)
 *   - NPCState (relationship changes from events)
 */
public class EventEngine extends BaseEngine {
    private static final String ENGINE_NAME = "EventEngine";
    
    private Map<String, Boolean> eventFlags = new HashMap<>();
    private String currentDialogueId = "";
    private int currentDialogueNode = 0;
    
    public EventEngine(GameState gameState, DeltaEngine deltaEngine) {
        super(gameState, deltaEngine);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        eventFlags.clear();
        currentDialogueId = "";
        currentDialogueNode = 0;
        System.out.println("[" + ENGINE_NAME + "] Initialized");
    }
    
    @Override
    public void update() {
        // Check for automatic event triggers each frame (if needed)
        // This is placeholder - events typically trigger from user actions
    }
    
    public void triggerEvent(String eventId) {
        if (!eventFlags.getOrDefault(eventId, false)) {
            eventFlags.put(eventId, true);
            recordChange("eventFlags", eventId);
            System.out.println("[" + ENGINE_NAME + "] Event triggered: " + eventId);
            
            // Apply event consequences (would depend on event definition)
            // This is where branching logic would apply state changes
        }
    }
    
    public void startDialogue(String npcId, String dialogueId) {
        this.currentDialogueId = npcId + ":" + dialogueId;
        this.currentDialogueNode = 0;
        recordChange("currentDialogue", currentDialogueId);
        System.out.println("[" + ENGINE_NAME + "] Dialogue started: " + currentDialogueId);
    }
    
    public void progressDialogue(int nodeId) {
        currentDialogueNode = nodeId;
        recordChange("dialogueNode", nodeId);
        
        // Check if dialogue has ended
        if (nodeId < 0) {
            endDialogue("");
        }
    }
    
    public void endDialogue(String npcId) {
        System.out.println("[" + ENGINE_NAME + "] Dialogue ended: " + currentDialogueId);
        currentDialogueId = "";
        currentDialogueNode = 0;
        recordChange("currentDialogue", "");
    }
    
    public String getCurrentDialogue() {
        return currentDialogueId;
    }
    
    public int getCurrentDialogueNode() {
        return currentDialogueNode;
    }
    
    public boolean hasEventOccurred(String eventId) {
        return eventFlags.getOrDefault(eventId, false);
    }
    
    public Map<String, Boolean> getEventFlags() {
        return new HashMap<>(eventFlags);
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
        eventFlags.clear();
        System.out.println("[" + ENGINE_NAME + "] Shutdown");
    }
}
