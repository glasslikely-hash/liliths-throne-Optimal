package com.lilithsthrone.utils.storage;

/**
 * Factory for creating platform-appropriate GameStorage implementations.
 * Provides unified access to file storage across different platforms.
 */
public class GameStorageFactory {

	private static GameStorage instance;

	/**
	 * Gets the singleton GameStorage instance for the current platform.
	 * Creates a new instance on first call, then returns the cached singleton.
	 * 
	 * @return Platform-appropriate GameStorage implementation
	 */
	public static synchronized GameStorage getGameStorage() {
		if (instance == null) {
			instance = createGameStorage();
		}
		return instance;
	}

	/**
	 * Creates a new GameStorage instance based on the current platform.
	 * 
	 * @return Platform-appropriate GameStorage implementation
	 */
	private static GameStorage createGameStorage() {
		String osName = System.getProperty("os.name");
		
		// Check if running on Android (future-proofing)
		// Android apps running in WebView will have specific system properties
		if (isAndroid()) {
			// TODO: Return AndroidGameStorage when Android support is added
			// return new AndroidGameStorage(context);
		}
		
		// Default to desktop implementation
		return new DesktopGameStorage();
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
