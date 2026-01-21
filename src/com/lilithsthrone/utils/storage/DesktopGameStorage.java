package com.lilithsthrone.utils.storage;

import java.io.File;
import java.io.InputStream;

/**
 * Desktop implementation of GameStorage.
 * Provides access to game files in standard directories on Windows, macOS, and Linux.
 */
public class DesktopGameStorage implements GameStorage {

	private static final String GAME_DATA_DIR = "Lilith's Throne";
	private final File baseGameDirectory;
	private final File persistentSaveDirectory;
	private final File cacheDirectory;

	public DesktopGameStorage() {
		// Determine base directory based on OS
		String userHome = System.getProperty("user.home");
		String osName = System.getProperty("os.name").toLowerCase();
		
		File documentsDir;
		if (osName.contains("win")) {
			// Windows: Documents folder
			documentsDir = new File(userHome, "Documents");
		} else if (osName.contains("mac")) {
			// macOS: Documents folder
			documentsDir = new File(userHome, "Documents");
		} else {
			// Linux and others: home directory
			documentsDir = new File(userHome);
		}
		
		this.baseGameDirectory = new File(documentsDir, GAME_DATA_DIR);
		this.persistentSaveDirectory = new File(baseGameDirectory, "saves");
		this.cacheDirectory = new File(baseGameDirectory, "autosaves");
		
		// Ensure directories exist
		ensureDirectories();
	}

	private void ensureDirectories() {
		baseGameDirectory.mkdirs();
		persistentSaveDirectory.mkdirs();
		cacheDirectory.mkdirs();
	}

	@Override
	public File getPersistentSaveDirectory() {
		return persistentSaveDirectory;
	}

	@Override
	public File getCacheDirectory() {
		return cacheDirectory;
	}

	@Override
	public File getResourceDirectory(String type) {
		// Bundled resources are in res/ subdirectory
		File resDir = new File("res/" + type);
		return resDir;
	}

	@Override
	public File getModDirectory(String type) {
		// User-created mods are in the game data directory
		File modsDir = new File(baseGameDirectory, "mods");
		File typeModDir = new File(modsDir, type);
		typeModDir.mkdirs();
		return typeModDir;
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		// Load from classpath (JAR resources)
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream is = classLoader.getResourceAsStream(path);
		
		// If not found in classpath, try as a file
		if (is == null) {
			try {
				File file = new File(path);
				if (file.exists()) {
					return new java.io.FileInputStream(file);
				}
			} catch (Exception e) {
				// Fall through to return null
			}
		}
		
		return is;
	}

	@Override
	public boolean resourceExists(String path) {
		// Check classpath first
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream is = classLoader.getResourceAsStream(path)) {
			if (is != null) {
				return true;
			}
		} catch (Exception e) {
			// Continue
		}
		
		// Check as file
		File file = new File(path);
		return file.exists();
	}

	@Override
	public File getBaseGameDirectory() {
		return baseGameDirectory;
	}

}
