package com.lilithsthrone.ui;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized form data service.
 * Abstracts form value access away from direct WebEngine calls.
 * 
 * This service:
 * - Caches form values for performance
 * - Works across all platforms (Desktop, Android, etc.)
 * - Enables asynchronous form updates on non-synchronous platforms
 */
public class FormDataService {

	private static final UIManager uiManager = UIManagerFactory.getUIManager();
	
	// Cache for form values (enables async platforms to function)
	private static final Map<String, String> formCache = new HashMap<>();

	/**
	 * Gets a form value, checking both DOM and cache.
	 * Priority: DOM (Desktop) → Cache (Android)
	 * 
	 * @param elementId HTML form element ID
	 * @return Form value, or empty string if not found
	 */
	public static String getFormValue(String elementId) {
		// Try to get from UI (platform-specific)
		String value = uiManager.getFormValue(elementId);
		
		// If empty, try cache (for cross-platform support)
		if (value.isEmpty()) {
			value = formCache.getOrDefault(elementId, "");
		}
		
		return value;
	}

	/**
	 * Sets a form value in both UI and cache.
	 * 
	 * @param elementId HTML form element ID
	 * @param value Value to set
	 */
	public static void setFormValue(String elementId, String value) {
		// Set in UI (platform-specific)
		uiManager.setFormValue(elementId, value);
		
		// Also cache for cross-platform support
		formCache.put(elementId, value);
	}

	/**
	 * Gets a cached form value (doesn't touch UI).
	 * Useful for getting values set during current session.
	 * 
	 * @param elementId HTML form element ID
	 * @return Cached value, or empty string if not cached
	 */
	public static String getCachedFormValue(String elementId) {
		return formCache.getOrDefault(elementId, "");
	}

	/**
	 * Sets a cached form value (doesn't touch UI).
	 * Useful for session-temporary values.
	 * 
	 * @param elementId HTML form element ID
	 * @param value Value to cache
	 */
	public static void setCachedFormValue(String elementId, String value) {
		formCache.put(elementId, value);
	}

	/**
	 * Clears all cached form values.
	 * Call this when switching screens/dialogs.
	 */
	public static void clearCache() {
		formCache.clear();
	}

	/**
	 * Clears a specific cached form value.
	 * 
	 * @param elementId HTML form element ID
	 */
	public static void clearCachedValue(String elementId) {
		formCache.remove(elementId);
	}

	/**
	 * Synchronizes all cached values to the UI.
	 * Useful after major screen updates.
	 */
	public static void syncCacheToUI() {
		for (Map.Entry<String, String> entry : formCache.entrySet()) {
			uiManager.setFormValue(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Synchronizes all UI values to the cache.
	 * Useful after detecting external UI changes.
	 */
	public static void syncUIToCache() {
		// This is complex and platform-dependent, leaving for future implementation
		// For now, we rely on explicit cache updates
	}

}
