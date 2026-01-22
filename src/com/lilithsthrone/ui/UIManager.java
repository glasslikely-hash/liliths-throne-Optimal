package com.lilithsthrone.ui;

import org.w3c.dom.Document;

/**
 * Platform-independent UI management interface.
 * Abstracts all UI operations away from platform-specific implementations.
 * 
 * This interface enables seamless support for:
 * - Desktop (JavaFX WebView)
 * - Android (native rendering)
 * - iOS (future)
 * - Web (future)
 */
public interface UIManager {

	/**
	 * Executes JavaScript code and returns the result.
	 * On Desktop: Runs on WebEngine
	 * On Android: No-op (uses FormDataService instead)
	 * 
	 * @param script JavaScript code to execute
	 * @return The result of script execution (usually String or Boolean)
	 */
	Object executeScript(String script);

	/**
	 * Sets HTML content to display.
	 * 
	 * @param html HTML content to display
	 */
	void setContent(String html);

	/**
	 * Appends HTML content to the current content.
	 * 
	 * @param html HTML content to append
	 */
	void appendContent(String html);

	/**
	 * Gets the current DOM document.
	 * Returns null on Android/cross-platform.
	 * 
	 * @return W3C Document object, or null if not available
	 */
	Document getDocument();

	/**
	 * Gets the value of an HTML form element by ID.
	 * Uses cached data on Android, DOM on Desktop.
	 * 
	 * @param elementId HTML element ID
	 * @return Element value, or empty string if not found
	 */
	String getFormValue(String elementId);

	/**
	 * Sets the value of an HTML form element by ID.
	 * 
	 * @param elementId HTML element ID
	 * @param value Value to set
	 */
	void setFormValue(String elementId, String value);

	/**
	 * Gets text content of an HTML element by ID.
	 * 
	 * @param elementId HTML element ID
	 * @return Text content, or empty string if not found
	 */
	String getElementText(String elementId);

	/**
	 * Sets HTML content of an element by ID.
	 * 
	 * @param elementId HTML element ID
	 * @param html HTML content to set
	 */
	void setElementHTML(String elementId, String html);

	/**
	 * Sets text content of an element by ID.
	 * 
	 * @param elementId HTML element ID
	 * @param text Text content to set
	 */
	void setElementText(String elementId, String text);

	/**
	 * Checks if an element exists in the DOM.
	 * 
	 * @param elementId HTML element ID
	 * @return true if element exists
	 */
	boolean elementExists(String elementId);

	/**
	 * Gets the scroll position of a container element.
	 * 
	 * @param elementId HTML element ID
	 * @return Current scroll position in pixels
	 */
	int getScrollPosition(String elementId);

	/**
	 * Sets the scroll position of a container element.
	 * 
	 * @param elementId HTML element ID
	 * @param position Scroll position in pixels
	 */
	void setScrollPosition(String elementId, int position);

	/**
	 * Updates element class list (add/remove CSS classes).
	 * 
	 * @param elementId HTML element ID
	 * @param className CSS class name
	 * @param add true to add class, false to remove
	 */
	void updateElementClass(String elementId, String className, boolean add);

	/**
	 * Checks if platform supports synchronous DOM operations.
	 * Desktop (WebView) = true
	 * Android = false (use async callbacks instead)
	 * 
	 * @return true if synchronous operations are available
	 */
	boolean supportsSynchronousDOMOperations();

	/**
	 * Gets the platform name for debugging/logging.
	 * 
	 * @return Platform identifier (e.g., "Desktop", "Android")
	 */
	String getPlatformName();

	/**
	 * Render current content to screen.
	 * Called every frame to display the game.
	 * For text-based rendering, only renders when content changes.
	 * 
	 * Default: Does nothing (used by LibGdxUIManager)
	 */
	default void render() {
		// Override in text-based implementations
	}

	/**
	 * Handle mouse click input.
	 * Routes clicks to the appropriate game logic handler.
	 * 
	 * @param screenX X coordinate of click (in screen space)
	 * @param screenY Y coordinate of click (in screen space)
	 */
	default void mousePressed(float screenX, float screenY) {
		// Override in implementations that handle direct mouse input
	}

	/**
	 * Handle mouse movement for hover effects.
	 * 
	 * @param screenX X coordinate of mouse
	 * @param screenY Y coordinate of mouse
	 */
	default void mouseMoved(float screenX, float screenY) {
		// Override in implementations that handle hover
	}

	/**
	 * Set screen dimensions (for window resize handling).
	 * 
	 * @param width Screen width in pixels
	 * @param height Screen height in pixels
	 */
	default void setScreenSize(int width, int height) {
		// Override in text-based implementations
	}

}
