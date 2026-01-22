package com.lilithsthrone.game.combat;

import com.lilithsthrone.main.Main;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.logic.GameIntegrationBridge;

/**
 * Adapter for Combat ↔ EventEngine delegation
 * 
 * Bridges combat system with EventEngine for centralized damage tracking,
 * combat event recording, and victory/defeat coordination. Enables combat
 * outcomes to drive narrative consequences and state changes.
 * 
 * @since Phase 2.5
 * @version 1.0
 */
public class CombatEventAdapter {
	
	/**
	 * Delegates combat initiation to event engine
	 * Records combat start for state tracking
	 * 
	 * @param playerEnemies List of enemies player faces
	 * @param playerAllies List of allies fighting with player
	 * @return true if combat initialized successfully
	 */
	public static boolean delegateCombatInitiation(java.util.List<GameCharacter> playerEnemies, 
		java.util.List<GameCharacter> playerAllies) {
		
		if (isBridgeAvailable()) {
			try {
				String combatId = "combat_" + System.currentTimeMillis();
				Main.game.getEventEngine().triggerEvent("combat_started_" + combatId);
				System.out.println("[CombatEventAdapter] Combat initiated: " + combatId);
				return true;
			} catch (Exception e) {
				System.err.println("[CombatEventAdapter] Error initiating combat");
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates turn execution in combat
	 * Records each turn for combat progression tracking
	 * 
	 * @param turnNumber Current turn count
	 * @param actor Character taking the turn
	 */
	public static void delegateCombatTurn(int turnNumber, GameCharacter actor) {
		if (isBridgeAvailable()) {
			try {
				String characterId = actor != null ? actor.getName() : "unknown";
				Main.game.getEventEngine().triggerEvent("combat_turn_" + turnNumber + "_" + characterId);
			} catch (Exception e) {
				System.err.println("[CombatEventAdapter] Error recording combat turn");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates damage application
	 * Records all damage events for combat analysis
	 * 
	 * @param attacker Character dealing damage
	 * @param target Character taking damage
	 * @param damageAmount Damage value applied
	 * @param damageType Type of damage (physical, fire, etc.)
	 */
	public static void delegateDamageApplication(GameCharacter attacker, GameCharacter target, 
		int damageAmount, String damageType) {
		
		if (isBridgeAvailable()) {
			try {
				String attackerId = attacker != null ? attacker.getName() : "unknown";
				String targetId = target != null ? target.getName() : "unknown";
				
				Main.game.getEventEngine().triggerEvent(
					"combat_damage_" + damageType + "_from_" + attackerId + "_to_" + targetId);
				
				System.out.println("[CombatEventAdapter] Damage recorded: " + attackerId + 
					" dealt " + damageAmount + " " + damageType + " to " + targetId);
			} catch (Exception e) {
				System.err.println("[CombatEventAdapter] Error recording damage");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates critical hit detection
	 * Records critical strikes for special effect handling
	 * 
	 * @param attacker Character delivering critical
	 * @param target Character hit critically
	 * @param damageMultiplier Critical damage multiplier
	 */
	public static void delegateCriticalHit(GameCharacter attacker, GameCharacter target, 
		float damageMultiplier) {
		
		if (isBridgeAvailable()) {
			try {
				String attackerId = attacker != null ? attacker.getName() : "unknown";
				String targetId = target != null ? target.getName() : "unknown";
				
				Main.game.getEventEngine().triggerEvent(
					"combat_critical_hit_" + attackerId + "_vs_" + targetId);
				
				System.out.println("[CombatEventAdapter] Critical hit recorded: " + attackerId + 
					" -> " + targetId + " (multiplier: " + damageMultiplier + ")");
			} catch (Exception e) {
				System.err.println("[CombatEventAdapter] Error recording critical hit");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates combat victory
	 * Records when player defeats enemies
	 * 
	 * @param defeatedEnemies Enemies defeated
	 * @param rewards Experience/items gained
	 */
	public static void delegateCombatVictory(java.util.List<GameCharacter> defeatedEnemies, String rewards) {
		if (isBridgeAvailable()) {
			try {
				Main.game.getEventEngine().triggerEvent("combat_victory");
				
				if (defeatedEnemies != null) {
					for (GameCharacter enemy : defeatedEnemies) {
						Main.game.getEventEngine().triggerEvent("enemy_defeated_" + enemy.getName());
					}
				}
				
				System.out.println("[CombatEventAdapter] Combat victory recorded: " + 
					(defeatedEnemies != null ? defeatedEnemies.size() : 0) + " enemies defeated");
			} catch (Exception e) {
				System.err.println("[CombatEventAdapter] Error recording combat victory");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates combat defeat
	 * Records when player loses in combat
	 * 
	 * @param victor Winning opponent
	 */
	public static void delegateCombatDefeat(GameCharacter victor) {
		if (isBridgeAvailable()) {
			try {
				Main.game.getEventEngine().triggerEvent("combat_defeat");
				
				if (victor != null) {
					Main.game.getEventEngine().triggerEvent("defeated_by_" + victor.getName());
				}
				
				System.out.println("[CombatEventAdapter] Combat defeat recorded");
			} catch (Exception e) {
				System.err.println("[CombatEventAdapter] Error recording combat defeat");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates combat escape
	 * Records successful or failed escape attempts
	 * 
	 * @param successful Whether escape succeeded
	 */
	public static void delegateCombatEscape(boolean successful) {
		if (isBridgeAvailable()) {
			try {
				String eventId = successful ? "combat_escape_success" : "combat_escape_failure";
				Main.game.getEventEngine().triggerEvent(eventId);
				
				System.out.println("[CombatEventAdapter] Combat escape recorded: " + 
					(successful ? "success" : "failure"));
			} catch (Exception e) {
				System.err.println("[CombatEventAdapter] Error recording combat escape");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates combat move execution
	 * Records special combat actions
	 * 
	 * @param moveId Combat move identifier
	 * @param actor Character performing move
	 * @param target Target of move
	 */
	public static void delegateCombatMoveExecution(String moveId, GameCharacter actor, GameCharacter target) {
		if (isBridgeAvailable()) {
			try {
				String actorId = actor != null ? actor.getName() : "unknown";
				String targetId = target != null ? target.getName() : "unknown";
				
				Main.game.getEventEngine().triggerEvent(
					"combat_move_" + moveId + "_" + actorId + "_vs_" + targetId);
				
				System.out.println("[CombatEventAdapter] Combat move recorded: " + moveId);
			} catch (Exception e) {
				System.err.println("[CombatEventAdapter] Error recording combat move");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates special combat event
	 * Records unique combat situations (shield break, status immunity, etc.)
	 * 
	 * @param eventType Type of special event
	 * @param character Character involved
	 */
	public static void delegateSpecialCombatEvent(String eventType, GameCharacter character) {
		if (isBridgeAvailable()) {
			try {
				String characterId = character != null ? character.getName() : "unknown";
				Main.game.getEventEngine().triggerEvent("combat_special_" + eventType + "_" + characterId);
				
				System.out.println("[CombatEventAdapter] Special combat event: " + eventType);
			} catch (Exception e) {
				System.err.println("[CombatEventAdapter] Error recording special combat event");
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
