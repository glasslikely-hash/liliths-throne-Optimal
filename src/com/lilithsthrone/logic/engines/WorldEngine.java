package com.lilithsthrone.logic.engines;

import com.lilithsthrone.data.DataStore;
import com.lilithsthrone.logic.persistence.DeltaEngine;
import com.lilithsthrone.logic.state.GameState;
import com.lilithsthrone.utils.logging.LogManager;
import java.util.*;

/**
 * World Engine - manages NPC state, locations, and world events.
 * 
 * Manages:
 *   - NPC positions, states, and behaviors
 *   - Location state (visited, unlocked, cleared)
 *   - NPC respawning with timers
 *   - World-wide events and consequences
 *   - Environmental state changes
 * 
 * Actions:
 *   - updateNpcState(npcId, state): Change NPC state
 *   - triggerWorldEvent(eventId): Trigger major event
 *   - updateLocationState(locationId, state): Change location state
 *   - respawnNpcs(locationId): Reset NPCs
 *   - getNpcState(npcId): Query NPC state
 * 
 * State Modified:
 *   - NPCPositions (NPC locations)
 *   - LocationStates (visited, unlocked, cleared flags)
 *   - NPCRespawnTimes (respawn scheduling)
 *   - WorldEvents (global state tracking)
 */
public class WorldEngine extends BaseEngine {
    private static final String ENGINE_NAME = "WorldEngine";
    private static final int RESPAWN_TIME_SECONDS = 300; // 5 minutes
    
    private Map<String, String> npcStates = new HashMap<>();       // npcId -> state
    private Map<String, Long> npcRespawnTimes = new HashMap<>();   // npcId -> respawnTime
    private Set<String> triggeredWorldEvents = new HashSet<>();
    
    public WorldEngine(GameState gameState, DeltaEngine deltaEngine) {
        super(gameState, deltaEngine);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        npcStates.clear();
        npcRespawnTimes.clear();
        triggeredWorldEvents.clear();
        LogManager.info(ENGINE_NAME, "Initialized");
    }
    
    @Override
    public void update() {
        // Check for NPC respawning each frame
        long currentTime = System.currentTimeMillis();
        Set<String> toRespawn = new HashSet<>();
        
        for (Map.Entry<String, Long> entry : npcRespawnTimes.entrySet()) {
            if (entry.getValue() <= currentTime) {
                toRespawn.add(entry.getKey());
            }
        }
        
        for (String npcId : toRespawn) {
            respawnNpc(npcId);
        }
    }
    
    public void updateNpcState(String npcId, String state) {
        if (npcId == null || npcId.isEmpty() || state == null || state.isEmpty()) {
            LogManager.error(ENGINE_NAME, "Cannot update NPC state with null/empty NPC ID or state");
            return;
        }
        
        npcStates.put(npcId, state);
        recordChange("npcState_" + npcId, state);
        LogManager.info(ENGINE_NAME, "NPC state updated: " + npcId + " -> " + state);
    }
    
    public void triggerWorldEvent(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            LogManager.error(ENGINE_NAME, "Cannot trigger world event with null/empty ID");
            return;
        }
        
        if (!triggeredWorldEvents.contains(eventId)) {
            triggeredWorldEvents.add(eventId);
            recordChange("worldEvent", eventId);
            LogManager.info(ENGINE_NAME, "World event triggered: " + eventId);
            
            // Apply world-wide consequences
            // Example: event might change NPC states, unlock locations, etc.
        } else {
            LogManager.warn(ENGINE_NAME, "World event already triggered: " + eventId);
        }
    }
    
    public void updateLocationState(String locationId, String state) {
        if (locationId == null || locationId.isEmpty() || state == null || state.isEmpty()) {
            LogManager.error(ENGINE_NAME, "Cannot update location state with null/empty location ID or state");
            return;
        }
        
        // Mark location with state (visited, unlocked, cleared)
        recordChange("location_" + locationId, state);
        LogManager.info(ENGINE_NAME, "Location state updated: " + locationId + " -> " + state);
    }
    
    public void respawnNpcs(String locationId) {
        if (locationId == null || locationId.isEmpty()) {
            LogManager.error(ENGINE_NAME, "Cannot respawn NPCs with null/empty location ID");
            return;
        }
        
        // Respawn all NPCs in a location
        for (String npcId : getNpcsInLocation(locationId)) {
            respawnNpc(npcId);
        }
        LogManager.info(ENGINE_NAME, "NPCs respawned in location: " + locationId);
    }
    
    private void respawnNpc(String npcId) {
        npcStates.put(npcId, "alive");
        npcRespawnTimes.remove(npcId);
        recordChange("npcRespawned", npcId);
        LogManager.info(ENGINE_NAME, "NPC respawned: " + npcId);
    }
    
    public void scheduleNpcRespawn(String npcId) {
        long respawnTime = System.currentTimeMillis() + (RESPAWN_TIME_SECONDS * 1000);
        npcRespawnTimes.put(npcId, respawnTime);
        npcStates.put(npcId, "dead");
        recordChange("npcScheduledRespawn", npcId);
        LogManager.info(ENGINE_NAME, "NPC respawn scheduled: " + npcId + " in " + RESPAWN_TIME_SECONDS + " seconds");
    }
    
    public String getNpcState(String npcId) {
        return npcStates.getOrDefault(npcId, "unknown");
    }
    
    public List<String> getNpcsInLocation(String locationId) {
        // Placeholder: would query GameState for NPCs in location
        return new ArrayList<>();
    }
    
    public boolean hasWorldEventOccurred(String eventId) {
        return triggeredWorldEvents.contains(eventId);
    }
    
    public Set<String> getTriggeredWorldEvents() {
        return new HashSet<>(triggeredWorldEvents);
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
        npcStates.clear();
        npcRespawnTimes.clear();
        LogManager.info(ENGINE_NAME, "Shutdown");
    }
}
