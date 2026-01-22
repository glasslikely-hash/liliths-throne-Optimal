package com.lilithsthrone.ui;

/**
 * Factory for creating platform-appropriate UIManager implementations.
 * Provides unified access to UI operations across different platforms.
 */
public class UIManagerFactory {

	private static UIManager instance;

	/**
	 * Gets the singleton UIManager instance for the current platform.
	 * Creates a new instance on first call, then returns the cached singleton.
	 * 
	 * @return Platform-appropriate UIManager implementation
	 */
	public static synchronized UIManager getUIManager() {
		if (instance == null) {
			instance = createUIManager();
		}
		return instance;
	}

	/**
	 * Creates a new UIManager instance based on the current platform.
	 * 
	 * @return Platform-appropriate UIManager implementation
	 */
	private static UIManager createUIManager() {
		String osName = System.getProperty("os.name");
		
		// Check if running on Android
		if (isAndroid()) {
			// TODO: Return AndroidUIManager when Android support is added
			// return new AndroidUIManager();
		}
		
		// Default to desktop implementation
		return new DesktopUIManager();
	}

	/**
	 * Detects if the code is running on Android.
	 * This is a heuristic check; can be enhanced as needed.
	 * 
	 * @return true if running on Android, false otherwise
	 */
	private static boolean isAndroid() {
		try {
			Class.forName("android.app.Activity");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * Resets the singleton instance (useful for testing).
	 */
	protected static void resetInstance() {
		instance = null;
	}

}
