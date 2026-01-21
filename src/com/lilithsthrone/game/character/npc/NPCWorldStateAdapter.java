package com.lilithsthrone.game.character.npc;

import com.lilithsthrone.logic.GameIntegrationBridge;
import com.lilithsthrone.game.character.GameCharacter;
import java.util.*;

/**
 * NPCWorldStateAdapter - Integrates NPC system with new WorldEngine
 * 
 * This adapter ensures NPCs properly delegate to WorldEngine for:
 * - Respawn scheduling and timers
 * - Location state management
 * - NPC state tracking (alive, dead, sleeping, etc.)
 * - World event coordination
 * 
 * Delegation Pattern:
 * - NPC Death: scheduleNpcRespawn() → WorldEngine
 * - Location Tracking: updateNpcState() → WorldEngine
 * - World Events: triggerWorldEvent() → WorldEngine
 * - NPC Spawning: respawnNpc() → WorldEngine
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class NPCWorldStateAdapter {
    
    /**
     * Delegate: Schedule NPC respawn when they die
     * Routes to: WorldEngine via GameIntegrationBridge
     */
    public static void delegateNpcRespawnScheduling(NPC npc) {
        if (npc == null || npc.isPlayer()) {
            return;
        }
        
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge != null && isBridgeAvailable()) {
                String npcId = getNpcIdentifier(npc);
                bridge.scheduleNpcRespawn(npcId);
            }
        } catch (Exception e) {
            System.err.println("[NPCWorldStateAdapter] Failed to delegate NPC respawn scheduling: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Delegate: Update NPC state (alive, dead, sleeping, etc.)
     * Routes to: WorldEngine via GameIntegrationBridge
     */
    public static void delegateNpcStateUpdate(NPC npc, String state) {
        if (npc == null) {
            return;
        }
        
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge != null && isBridgeAvailable()) {
                String npcId = getNpcIdentifier(npc);
                bridge.updateNpcState(npcId, state);
            }
        } catch (Exception e) {
            System.err.println("[NPCWorldStateAdapter] Failed to delegate NPC state update: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Delegate: Update location state when NPC moves
     * Routes to: WorldEngine via GameIntegrationBridge
     */
    public static void delegateLocationStateUpdate(String locationId, String state) {
        if (locationId == null || locationId.isEmpty()) {
            return;
        }
        
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge != null && isBridgeAvailable()) {
                bridge.updateLocationState(locationId, state);
            }
        } catch (Exception e) {
            System.err.println("[NPCWorldStateAdapter] Failed to delegate location state update: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Delegate: Trigger a world event that might affect NPCs
     * Routes to: WorldEngine via GameIntegrationBridge
     */
    public static void delegateWorldEvent(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return;
        }
        
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge != null && isBridgeAvailable()) {
                bridge.triggerWorldEvent(eventId);
            }
        } catch (Exception e) {
            System.err.println("[NPCWorldStateAdapter] Failed to delegate world event: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Delegate: Respawn NPCs in a location
     * Routes to: WorldEngine via GameIntegrationBridge
     */
    public static void delegateLocationNpcRespawn(String locationId) {
        if (locationId == null || locationId.isEmpty()) {
            return;
        }
        
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge != null && isBridgeAvailable()) {
                bridge.respawnNpcsInLocation(locationId);
            }
        } catch (Exception e) {
            System.err.println("[NPCWorldStateAdapter] Failed to delegate location NPC respawn: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if bridge is available (for conditional delegation)
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
     * Get unique NPC identifier for engine tracking
     * Combines NPC name and ID for uniqueness
     */
    private static String getNpcIdentifier(NPC npc) {
        if (npc == null) {
            return "unknown";
        }
        
        try {
            // Use NPC's name as identifier (most unique within game)
            String name = npc.getName("");
            if (name != null && !name.isEmpty()) {
                return name.toLowerCase().replaceAll("\\s+", "_");
            }
        } catch (Exception e) {
            // Fallback
        }
        
        // Fallback: use toString()
        return npc.toString();
    }
    
    /**
     * Sync NPC state from legacy to WorldEngine
     * Called during NPC creation/load
     */
    public static void syncNpcStateToEngine(NPC npc) {
        if (npc == null) {
            return;
        }
        
        try {
            GameIntegrationBridge bridge = GameIntegrationBridge.getInstance();
            if (bridge == null) {
                return;
            }
            
            // Determine NPC state
            String npcId = getNpcIdentifier(npc);
            String state = "alive";  // Default state
            
            // Update engine with NPC state
            delegateNpcStateUpdate(npc, state);
            
            System.out.println("[NPCWorldStateAdapter] Synced NPC state - ID: " + npcId + ", State: " + state);
            
        } catch (Exception e) {
            System.err.println("[NPCWorldStateAdapter] Failed to sync NPC state: " + e.getMessage());
        }
    }
    
    /**
     * Get all NPCs in a location
     * For inventory/management purposes
     */
    public static List<String> getNpcsInLocation(String locationId) {
        // This would be implemented to query GameState
        return new ArrayList<>();
    }
    
    /**
     * Check if NPC respawn is scheduled
     */
    public static boolean isNpcRespawnScheduled(NPC npc) {
        if (npc == null) {
            return false;
        }
        
        // This would query WorldEngine via bridge
        // For now, return false (future implementation)
        return false;
    }
}
