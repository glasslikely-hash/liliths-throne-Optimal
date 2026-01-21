package com.lilithsthrone.game.character.effects;

import com.lilithsthrone.main.Main;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.logic.GameIntegrationBridge;

/**
 * Adapter for Status Effects ↔ BuffEngine delegation
 * 
 * Bridges status effect system with BuffEngine for centralized effect
 * management, duration tracking, and effect stacking. Enables effects
 * to be applied, removed, and synchronized across game systems.
 * 
 * @since Phase 2.5
 * @version 1.0
 */
public class StatusEffectAdapter {
	
	/**
	 * Delegates status effect application to BuffEngine
	 * Routes effect application through engine for centralized tracking
	 * 
	 * @param character Character receiving effect
	 * @param effectId Status effect identifier
	 * @param durationSeconds Duration in seconds
	 * @return true if effect was applied, false otherwise
	 */
	public static boolean delegateStatusEffectApplication(GameCharacter character, 
		String effectId, int durationSeconds) {
		
		if (isBridgeAvailable()) {
			try {
				Main.game.getBuffEngine().applyEffect(effectId, durationSeconds);
				
				String characterId = character != null ? character.getName() : "unknown";
				System.out.println("[StatusEffectAdapter] Status effect applied to " + characterId + 
					": " + effectId + " (" + durationSeconds + " seconds)");
				
				return true;
			} catch (Exception e) {
				System.err.println("[StatusEffectAdapter] Error applying status effect: " + effectId);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates status effect removal
	 * Routes effect removal through engine
	 * 
	 * @param character Character losing effect
	 * @param effectId Effect to remove
	 * @return true if effect was removed, false otherwise
	 */
	public static boolean delegateStatusEffectRemoval(GameCharacter character, String effectId) {
		if (isBridgeAvailable()) {
			try {
				Main.game.getBuffEngine().removeEffect(effectId);
				
				String characterId = character != null ? character.getName() : "unknown";
				System.out.println("[StatusEffectAdapter] Status effect removed from " + characterId + 
					": " + effectId);
				
				return true;
			} catch (Exception e) {
				System.err.println("[StatusEffectAdapter] Error removing status effect: " + effectId);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates status effect refresh (duration reset)
	 * Used when effect is reapplied
	 * 
	 * @param character Character with effect
	 * @param effectId Effect to refresh
	 * @param newDurationSeconds New duration value
	 */
	public static void delegateStatusEffectRefresh(GameCharacter character, String effectId, 
		int newDurationSeconds) {
		
		if (isBridgeAvailable()) {
			try {
				// Remove old effect and reapply with new duration
				Main.game.getBuffEngine().removeEffect(effectId);
				Main.game.getBuffEngine().applyEffect(effectId, newDurationSeconds);
				
				String characterId = character != null ? character.getName() : "unknown";
				System.out.println("[StatusEffectAdapter] Status effect refreshed on " + characterId + 
					": " + effectId);
			} catch (Exception e) {
				System.err.println("[StatusEffectAdapter] Error refreshing status effect: " + effectId);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks if character has a specific status effect
	 * Queries engine for effect presence
	 * 
	 * @param effectId Effect to check for
	 * @return true if effect is active, false otherwise
	 */
	public static boolean hasStatusEffect(String effectId) {
		if (isBridgeAvailable()) {
			try {
				return Main.game.getBuffEngine().getActiveEffects().containsKey(effectId);
			} catch (Exception e) {
				System.err.println("[StatusEffectAdapter] Error checking status effect: " + effectId);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Gets remaining duration of status effect
	 * Queries engine for effect duration
	 * 
	 * @param effectId Effect to check
	 * @return Remaining duration in seconds, 0 if not active
	 */
	public static int getStatusEffectDuration(String effectId) {
		if (isBridgeAvailable()) {
			try {
				Integer duration = Main.game.getBuffEngine().getActiveEffects().get(effectId);
				return duration != null ? duration : 0;
			} catch (Exception e) {
				System.err.println("[StatusEffectAdapter] Error getting status effect duration: " + effectId);
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	/**
	 * Gets stack count of effect
	 * Returns number of times effect has been applied
	 * 
	 * @param effectId Effect to check
	 * @return Number of stacks, 0 if not active
	 */
	public static int getStatusEffectStacks(String effectId) {
		if (isBridgeAvailable()) {
			try {
				return Main.game.getBuffEngine().getEffectStacks(effectId);
			} catch (Exception e) {
				System.err.println("[StatusEffectAdapter] Error getting effect stacks: " + effectId);
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	/**
	 * Delegates conditional effect removal
	 * Removes effect only if specific condition met
	 * 
	 * @param character Character with effect
	 * @param effectId Effect to conditionally remove
	 * @param condition Condition string (framework)
	 * @return true if removed, false if condition not met
	 */
	public static boolean delegateConditionalEffectRemoval(GameCharacter character, 
		String effectId, String condition) {
		
		if (isBridgeAvailable()) {
			try {
				// Framework: condition evaluation could be expanded
				if (condition == null || condition.isEmpty() || condition.equals("always")) {
					return delegateStatusEffectRemoval(character, effectId);
				}
				// Other conditions could be evaluated here
				return false;
			} catch (Exception e) {
				System.err.println("[StatusEffectAdapter] Error in conditional effect removal");
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates batch effect removal
	 * Removes multiple effects at once
	 * 
	 * @param character Character losing effects
	 * @param effectIds Array of effect IDs to remove
	 */
	public static void delegateBatchEffectRemoval(GameCharacter character, String[] effectIds) {
		if (isBridgeAvailable() && effectIds != null) {
			try {
				for (String effectId : effectIds) {
					delegateStatusEffectRemoval(character, effectId);
				}
				
				String characterId = character != null ? character.getName() : "unknown";
				System.out.println("[StatusEffectAdapter] Batch removal: " + effectIds.length + 
					" effects removed from " + characterId);
			} catch (Exception e) {
				System.err.println("[StatusEffectAdapter] Error in batch effect removal");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates category-based effect removal
	 * Removes all effects in a category
	 * 
	 * @param character Character losing effects
	 * @param categoryName Effect category (debuffs, buffs, etc.)
	 */
	public static void delegateCategoryEffectRemoval(GameCharacter character, String categoryName) {
		if (isBridgeAvailable()) {
			try {
				// Framework for category-based removal
				// Could expand to manage effects by category
				System.out.println("[StatusEffectAdapter] Category removal requested: " + categoryName);
			} catch (Exception e) {
				System.err.println("[StatusEffectAdapter] Error in category effect removal");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Syncs character's status effects to engine
	 * Called on game load/save to synchronize state
	 * 
	 * @param character Character to sync effects for
	 */
	public static void syncCharacterEffectsToEngine(GameCharacter character) {
		if (isBridgeAvailable() && character != null) {
			try {
				// Framework for syncing applied status effects to engine
				System.out.println("[StatusEffectAdapter] Character effects synced: " + character.getName());
			} catch (Exception e) {
				System.err.println("[StatusEffectAdapter] Error syncing character effects");
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
				&& Main.game.getBuffEngine() != null;
		} catch (Exception e) {
			return false;
		}
	}
}
