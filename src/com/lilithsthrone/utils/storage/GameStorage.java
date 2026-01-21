package com.lilithsthrone.utils.storage;

import java.io.File;
import java.io.InputStream;

/**
 * Abstract interface for game file storage across different platforms.
 * Provides unified access to persistent data, cache, and resource directories
 * regardless of the underlying platform (Desktop, Android, etc).
 */
public interface GameStorage {

	/**
	 * Gets the directory for persistent game saves.
	 * On desktop: user's documents/Lilith's Throne/saves/
	 * On Android: app's internal/external files directory
	 * 
	 * @return File object representing the persistent save directory
	 */
	File getPersistentSaveDirectory();

	/**
	 * Gets the directory for autosave and cache files.
	 * On desktop: user's documents/Lilith's Throne/autosaves/
	 * On Android: app's cache directory
	 * 
	 * @return File object representing the cache/autosave directory
	 */
	File getCacheDirectory();

	/**
	 * Gets the directory for a specific type of game resource.
	 * Handles both res/ (bundled) and mods/ directories.
	 * 
	 * @param type Resource type (e.g., "clothing", "items", "weapons")
	 * @return File object representing the resource directory
	 */
	File getResourceDirectory(String type);

	/**
	 * Gets the resource directory for modded content.
	 * 
	 * @param type Resource type (e.g., "clothing", "items", "weapons")
	 * @return File object representing the mod resource directory
	 */
	File getModDirectory(String type);

	/**
	 * Gets an input stream for a classpath/bundled resource.
	 * On desktop: loads from JAR/resources
	 * On Android: loads from APK assets
	 * 
	 * @param path Resource path (e.g., "res/clothing/basic.xml")
	 * @return InputStream for the resource, or null if not found
	 */
	InputStream getResourceAsStream(String path);

	/**
	 * Checks if a resource file exists.
	 * 
	 * @param path Resource path to check
	 * @return true if the resource exists
	 */
	boolean resourceExists(String path);

	/**
	 * Gets the base directory for all game data.
	 * This is typically the user's documents directory or app-specific directory.
	 * 
	 * @return File object representing the base game data directory
	 */
	File getBaseGameDirectory();

}
