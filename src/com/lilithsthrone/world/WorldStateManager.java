package com.lilithsthrone.world;

import com.lilithsthrone.logic.GameIntegrationBridge;
import java.util.*;

/**
 * WorldStateManager - Centralized management of world state through WorldEngine
 * 
 * Provides:
 * - Location state tracking (visited, unlocked, cleared)
 * - World event coordination
 * - Environmental state management
 * - NPC spawning coordination
 * 
 * This class acts as the primary interface between legacy Game world system
 * and the new modular WorldEngine.
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class WorldStateManager {
    
    // World state tracking
    private static Set<String> visitedLocations = new HashSet<>();
    private static Set<String> unlockedLocations = new HashSet<>();
    private static Set<String> clearedLocations = new HashSet<>();
    private static Map<String, String> locationStates = new HashMap<>();
    
    /**
     * Update location state (visited, unlocked, cleared)
     */
    public static void updateLocationState(String locationId, String state) {
        if (locationId == null || locationId.isEmpty()) {
            return;
        }
        
        switch (state.toLowerCase()) {
            case "visited":
                visitedLocations.add(locationId);
                break;
            case "unlocked":
                unlockedLocations.add(locationId);
                visitedLocations.add(locationId);
                break;
            case "cleared":
                clearedLocations.add(locationId);
                visitedLocations.add(locationId);
                unlockedLocations.add(locationId);
                break;
        }
        
        locationStates.put(locationId, state);
        
        // Delegate to engine
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge != null && isBridgeAvailable()) {
                bridge.updateLocationState(locationId, state);
            }
        } catch (Exception e) {
            System.err.println("[WorldStateManager] Failed to update location state: " + e.getMessage());
        }
    }
    
    /**
     * Check if location has been visited
     */
    public static boolean hasLocationBeenVisited(String locationId) {
        if (locationId == null) {
            return false;
        }
        return visitedLocations.contains(locationId);
    }
    
    /**
     * Check if location is unlocked
     */
    public static boolean isLocationUnlocked(String locationId) {
        if (locationId == null) {
            return false;
        }
        return unlockedLocations.contains(locationId);
    }
    
    /**
     * Check if location has been cleared
     */
    public static boolean hasLocationBeenCleared(String locationId) {
        if (locationId == null) {
            return false;
        }
        return clearedLocations.contains(locationId);
    }
    
    /**
     * Get current state of a location
     */
    public static String getLocationState(String locationId) {
        if (locationId == null) {
            return "unknown";
        }
        return locationStates.getOrDefault(locationId, "unknown");
    }
    
    /**
     * Trigger a world-wide event
     */
    public static void triggerWorldEvent(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return;
        }
        
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge != null && isBridgeAvailable()) {
                bridge.triggerWorldEvent(eventId);
            }
        } catch (Exception e) {
            System.err.println("[WorldStateManager] Failed to trigger world event: " + e.getMessage());
        }
    }
    
    /**
     * Respawn NPCs in a location
     */
    public static void respawnNpcsInLocation(String locationId) {
        if (locationId == null || locationId.isEmpty()) {
            return;
        }
        
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge != null && isBridgeAvailable()) {
                bridge.respawnNpcsInLocation(locationId);
            }
        } catch (Exception e) {
            System.err.println("[WorldStateManager] Failed to respawn NPCs in location: " + e.getMessage());
        }
    }
    
    /**
     * Check if bridge is available
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
     * Get all visited locations
     */
    public static Set<String> getVisitedLocations() {
        return new HashSet<>(visitedLocations);
    }
    
    /**
     * Get all unlocked locations
     */
    public static Set<String> getUnlockedLocations() {
        return new HashSet<>(unlockedLocations);
    }
    
    /**
     * Get all cleared locations
     */
    public static Set<String> getClearedLocations() {
        return new HashSet<>(clearedLocations);
    }
    
    /**
     * Reset world state (for new game)
     */
    public static void resetWorldState() {
        visitedLocations.clear();
        unlockedLocations.clear();
        clearedLocations.clear();
        locationStates.clear();
        System.out.println("[WorldStateManager] World state reset");
    }
    
    /**
     * Get debugging information
     */
    public static String getWorldStateStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("[WorldStateManager Status]\n");
        sb.append("Visited Locations: ").append(visitedLocations.size()).append("\n");
        sb.append("Unlocked Locations: ").append(unlockedLocations.size()).append("\n");
        sb.append("Cleared Locations: ").append(clearedLocations.size()).append("\n");
        sb.append("Bridge Available: ").append(isBridgeAvailable()).append("\n");
        return sb.toString();
    }
}
