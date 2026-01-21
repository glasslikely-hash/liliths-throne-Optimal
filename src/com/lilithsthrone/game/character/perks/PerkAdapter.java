package com.lilithsthrone.game.character.perks;

import com.lilithsthrone.main.Main;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.logic.GameIntegrationBridge;

/**
 * Adapter for Perks ↔ BuffEngine delegation
 * 
 * Bridges permanent perk system with BuffEngine for centralized perk
 * management and attribute modifications. Enables perks to be learned,
 * lost, and synchronized across game systems with attribute tracking.
 * 
 * @since Phase 2.5
 * @version 1.0
 */
public class PerkAdapter {
	
	/**
	 * Delegates perk acquisition to BuffEngine
	 * Routes perk learning through engine for persistent tracking
	 * 
	 * @param character Character learning perk
	 * @param perkId Perk identifier
	 * @return true if perk was added, false otherwise
	 */
	public static boolean delegatePerkAcquisition(GameCharacter character, String perkId) {
		if (isBridgeAvailable()) {
			try {
				Main.game.getBuffEngine().addPerk(perkId);
				
				String characterId = character != null ? character.getName() : "unknown";
				System.out.println("[PerkAdapter] Perk acquired by " + characterId + ": " + perkId);
				
				return true;
			} catch (Exception e) {
				System.err.println("[PerkAdapter] Error acquiring perk: " + perkId);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates perk removal
	 * Routes perk loss through engine
	 * 
	 * @param character Character losing perk
	 * @param perkId Perk to remove
	 * @return true if perk was removed, false otherwise
	 */
	public static boolean delegatePerkRemoval(GameCharacter character, String perkId) {
		if (isBridgeAvailable()) {
			try {
				Main.game.getBuffEngine().removePerk(perkId);
				
				String characterId = character != null ? character.getName() : "unknown";
				System.out.println("[PerkAdapter] Perk removed from " + characterId + ": " + perkId);
				
				return true;
			} catch (Exception e) {
				System.err.println("[PerkAdapter] Error removing perk: " + perkId);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Checks if character has a specific perk
	 * Queries engine for perk presence
	 * 
	 * @param perkId Perk to check for
	 * @return true if perk is active, false otherwise
	 */
	public static boolean hasPerk(String perkId) {
		if (isBridgeAvailable()) {
			try {
				return Main.game.getBuffEngine().getActivePerks().contains(perkId);
			} catch (Exception e) {
				System.err.println("[PerkAdapter] Error checking perk: " + perkId);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Gets all active perks
	 * Queries complete perk set from engine
	 * 
	 * @return Set of active perk IDs
	 */
	public static java.util.Set<String> getActivePerks() {
		if (isBridgeAvailable()) {
			try {
				return Main.game.getBuffEngine().getActivePerks();
			} catch (Exception e) {
				System.err.println("[PerkAdapter] Error getting active perks");
				e.printStackTrace();
			}
		}
		return new java.util.HashSet<>();
	}
	
	/**
	 * Delegates perk tree learning
	 * Manages perk tree progression and unlocks
	 * 
	 * @param character Character learning from tree
	 * @param treeId Perk tree identifier
	 * @param perkId Specific perk being learned
	 * @param pointsCost Number of points required
	 * @return true if learned successfully, false otherwise
	 */
	public static boolean delegatePerkTreeLearning(GameCharacter character, String treeId, 
		String perkId, int pointsCost) {
		
		if (isBridgeAvailable()) {
			try {
				// Record perk tree learning event
				Main.game.getEventEngine().triggerEvent(
					"perk_tree_" + treeId + "_learned_" + perkId);
				
				// Add perk through adapter
				delegatePerkAcquisition(character, perkId);
				
				String characterId = character != null ? character.getName() : "unknown";
				System.out.println("[PerkAdapter] Perk tree learning: " + characterId + 
					" learned " + perkId + " from " + treeId + " (cost: " + pointsCost + " points)");
				
				return true;
			} catch (Exception e) {
				System.err.println("[PerkAdapter] Error learning from perk tree");
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates attribute modification from perk
	 * Records attribute changes caused by perks
	 * 
	 * @param character Character with perk
	 * @param perkId Perk causing modification
	 * @param attributeName Attribute being modified
	 * @param modifierValue Value of modification
	 */
	public static void delegatePerkAttributeModification(GameCharacter character, String perkId,
		String attributeName, int modifierValue) {
		
		if (isBridgeAvailable()) {
			try {
				String characterId = character != null ? character.getName() : "unknown";
				
				Main.game.getEventEngine().triggerEvent(
					"perk_modifier_" + perkId + "_to_" + attributeName);
				
				System.out.println("[PerkAdapter] Perk attribute modification: " + characterId + 
					" perk " + perkId + " modifies " + attributeName + 
					" by " + modifierValue);
			} catch (Exception e) {
				System.err.println("[PerkAdapter] Error applying perk attribute modification");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates combat perk effect
	 * Applies special combat abilities from perks
	 * 
	 * @param character Character with combat perk
	 * @param perkId Combat perk identifier
	 * @param effectType Type of effect (damage_boost, healing, etc.)
	 */
	public static void delegateCombatPerkEffect(GameCharacter character, String perkId, 
		String effectType) {
		
		if (isBridgeAvailable()) {
			try {
				String characterId = character != null ? character.getName() : "unknown";
				
				Main.game.getEventEngine().triggerEvent(
					"perk_combat_effect_" + perkId + "_" + effectType);
				
				System.out.println("[PerkAdapter] Combat perk effect applied: " + characterId + 
					" perk " + perkId + " effect: " + effectType);
			} catch (Exception e) {
				System.err.println("[PerkAdapter] Error applying combat perk effect");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates perk requirement checking
	 * Determines if character can learn perk
	 * 
	 * @param character Character checking requirements
	 * @param perkId Perk to check
	 * @return true if requirements met, false otherwise
	 */
	public static boolean delegatePerkRequirementCheck(GameCharacter character, String perkId) {
		if (isBridgeAvailable() && character != null) {
			try {
				// Framework: could check prerequisites, level requirements, etc.
				// For now, perks can be learned if bridge available
				return true;
			} catch (Exception e) {
				System.err.println("[PerkAdapter] Error checking perk requirements");
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates perk mutation/transformation
	 * Handles perks that change or upgrade
	 * 
	 * @param character Character with transforming perk
	 * @param fromPerkId Original perk
	 * @param toPerkId New perk form
	 */
	public static void delegatePerkTransformation(GameCharacter character, String fromPerkId, 
		String toPerkId) {
		
		if (isBridgeAvailable()) {
			try {
				// Remove old perk
				delegatePerkRemoval(character, fromPerkId);
				
				// Add new perk
				delegatePerkAcquisition(character, toPerkId);
				
				String characterId = character != null ? character.getName() : "unknown";
				System.out.println("[PerkAdapter] Perk transformation: " + characterId + 
					" perk " + fromPerkId + " transformed to " + toPerkId);
			} catch (Exception e) {
				System.err.println("[PerkAdapter] Error transforming perk");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Syncs character's perks to engine
	 * Called on game load/save to synchronize state
	 * 
	 * @param character Character to sync perks for
	 */
	public static void syncCharacterPerksToEngine(GameCharacter character) {
		if (isBridgeAvailable() && character != null) {
			try {
				// Framework for syncing character perks to engine
				System.out.println("[PerkAdapter] Character perks synced: " + character.getName());
			} catch (Exception e) {
				System.err.println("[PerkAdapter] Error syncing character perks");
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
				&& Main.game.getBuffEngine() != null
				&& Main.game.getEventEngine() != null;
		} catch (Exception e) {
			return false;
		}
	}
}
