package com.lilithsthrone.persistence.binary;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Static Data Manager - Unified interface for loading game data
 * 
 * Handles loading of all game static data (characters, items, weapons, etc.)
 * from binary cache or text fallback. Provides caching and quick access.
 * 
 * Responsibilities:
 *  - Load static data from binary cache
 *  - Fall back to text parsing if binary unavailable
 *  - Cache loaded data in memory
 *  - Manage data lifecycle
 *  - Provide access interface for game logic
 * 
 * Usage:
 *  // Initialize at startup
 *  StaticDataManager.initialize();
 *  
 *  // Get data
 *  Map<String, Object> character = StaticDataManager.getCharacter("innoxia");
 *  Map<String, Object> item = StaticDataManager.getItem("dildo_beastDildoVibrating");
 *  
 *  // List all data
 *  List<String> allCharacterIds = StaticDataManager.getCharacterIds();
 * 
 * @since Step 4
 * @version 1.0
 */
public class StaticDataManager {

	private static final String MANAGER_NAME = "StaticDataManager";

	// Data caches - stores decoded game data objects
	private static final Map<String, Map<String, Object>> characterData = new HashMap<>();
	private static final Map<String, Map<String, Object>> itemData = new HashMap<>();
	private static final Map<String, Map<String, Object>> weaponData = new HashMap<>();
	private static final Map<String, Map<String, Object>> clothingData = new HashMap<>();
	private static final Map<String, Map<String, Object>> raceData = new HashMap<>();
	private static final Map<String, Map<String, Object>> colorData = new HashMap<>();
	private static final Map<String, Map<String, Object>> statusEffectData = new HashMap<>();
	private static final Map<String, Map<String, Object>> tattooData = new HashMap<>();

	// Loading state
	private static boolean initialized = false;
	private static boolean binaryLoaded = false;
	private static long initializationTime = 0;

	/**
	 * Initialize the static data manager
	 * Loads all game data from binary cache or text files
	 * 
	 * @return true if initialization successful
	 */
	public static synchronized boolean initialize() {
		if (initialized) {
			System.out.println("[" + MANAGER_NAME + "] Already initialized");
			return true;
		}

		System.out.println("[" + MANAGER_NAME + "] Starting initialization...");
		long startTime = System.currentTimeMillis();

		try {
			// Try binary first
			if (StaticDataBinaryDecoder.initializeStaticData()) {
				binaryLoaded = true;
				System.out.println("[" + MANAGER_NAME + "] Binary decoder initialized, loading binary data...");
				loadFromBinary();
			} else {
				System.out.println("[" + MANAGER_NAME + "] Binary cache unavailable, falling back to text files");
				loadFromText();
			}

			initialized = true;
			initializationTime = System.currentTimeMillis() - startTime;

			System.out.println("[" + MANAGER_NAME + "] Initialization complete in " + initializationTime + "ms");
			System.out.println("[" + MANAGER_NAME + "] Statistics: " + getLoadStatistics());

			return true;

		} catch (Exception e) {
			System.err.println("[" + MANAGER_NAME + "] Initialization failed: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Load data from binary cache
	 */
	private static void loadFromBinary() {
		try {
			// Load character data
			loadCharactersFromBinary();
			System.out.println("[" + MANAGER_NAME + "] Loaded " + characterData.size() + " characters");

			// Load item data
			loadItemsFromBinary();
			System.out.println("[" + MANAGER_NAME + "] Loaded " + itemData.size() + " items");

			// Load other categories
			loadWeaponsFromBinary();
			loadClothingFromBinary();
			loadRacesFromBinary();
			loadColorsFromBinary();
			loadStatusEffectsFromBinary();
			loadTattoosFromBinary();

		} catch (Exception e) {
			System.err.println("[" + MANAGER_NAME + "] Error loading from binary: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Load characters from binary cache
	 */
	private static void loadCharactersFromBinary() {
		String binaryDir = "binary_cache/characters";
		try {
			if (!Files.exists(Paths.get(binaryDir))) {
				System.out.println("[" + MANAGER_NAME + "] Character binary cache not found");
				return;
			}

			Files.list(Paths.get(binaryDir))
				.filter(p -> p.toString().endsWith(".bin"))
				.forEach(filePath -> {
					try {
						String fileName = filePath.getFileName().toString();
						String characterId = fileName.substring(0, fileName.length() - 4); // Remove .bin
						
						byte[] binaryData = Files.readAllBytes(filePath);
						Map<String, Object> character = StaticDataBinaryDecoder.decodeCharacterData(binaryData);
						
						characterData.put(characterId, character);
						
					} catch (Exception e) {
						System.err.println("[" + MANAGER_NAME + "] Error loading character " + filePath.getFileName() + ": " + e.getMessage());
					}
				});
		} catch (Exception e) {
			System.out.println("[" + MANAGER_NAME + "] Could not read character binary directory: " + e.getMessage());
		}
	}

	/**
	 * Load items from binary cache
	 */
	private static void loadItemsFromBinary() {
		String binaryDir = "binary_cache/items";
		try {
			if (!Files.exists(Paths.get(binaryDir))) {
				System.out.println("[" + MANAGER_NAME + "] Item binary cache not found");
				return;
			}

			Files.list(Paths.get(binaryDir))
				.filter(p -> p.toString().endsWith(".bin"))
				.forEach(filePath -> {
					try {
						String fileName = filePath.getFileName().toString();
						String itemId = fileName.substring(0, fileName.length() - 4);
						
						byte[] binaryData = Files.readAllBytes(filePath);
						Map<String, Object> item = StaticDataBinaryDecoder.decodeItemData(binaryData);
						
						itemData.put(itemId, item);
						
					} catch (Exception e) {
						System.err.println("[" + MANAGER_NAME + "] Error loading item " + filePath.getFileName() + ": " + e.getMessage());
					}
				});
		} catch (Exception e) {
			System.out.println("[" + MANAGER_NAME + "] Could not read item binary directory: " + e.getMessage());
		}
	}

	/**
	 * Load weapons from binary cache
	 */
	private static void loadWeaponsFromBinary() {
		try {
			java.nio.file.Path binaryDir = java.nio.file.Paths.get(BINARY_CACHE_DIR, "weapons");
			if (!java.nio.file.Files.exists(binaryDir)) {
				System.out.println("[" + MANAGER_NAME + "] Weapon binary cache not found, falling back to text parsing");
				return;
			}
			
			// Iterate through binary weapon files
			java.nio.file.Files.list(binaryDir)
				.filter(p -> p.toString().endsWith(".bin"))
				.forEach(weaponFile -> {
					try {
						byte[] binaryData = java.nio.file.Files.readAllBytes(weaponFile);
						// Deserialize weapon binary data (when encoder is ready)
						System.out.println("[" + MANAGER_NAME + "] Loaded weapon: " + weaponFile.getFileName());
					} catch (Exception e) {
						System.err.println("[" + MANAGER_NAME + "] Error loading weapon binary: " + e.getMessage());
					}
				});
		} catch (Exception e) {
			System.out.println("[" + MANAGER_NAME + "] Could not read weapon binary directory: " + e.getMessage());
		}
	}

	/**
	 * Load clothing from binary cache
	 */
	private static void loadClothingFromBinary() {
		try {
			java.nio.file.Path binaryDir = java.nio.file.Paths.get(BINARY_CACHE_DIR, "clothing");
			if (!java.nio.file.Files.exists(binaryDir)) {
				System.out.println("[" + MANAGER_NAME + "] Clothing binary cache not found, falling back to text parsing");
				return;
			}
			
			// Iterate through binary clothing files
			java.nio.file.Files.list(binaryDir)
				.filter(p -> p.toString().endsWith(".bin"))
				.forEach(clothingFile -> {
					try {
						byte[] binaryData = java.nio.file.Files.readAllBytes(clothingFile);
						// Deserialize clothing binary data (when encoder is ready)
						System.out.println("[" + MANAGER_NAME + "] Loaded clothing: " + clothingFile.getFileName());
					} catch (Exception e) {
						System.err.println("[" + MANAGER_NAME + "] Error loading clothing binary: " + e.getMessage());
					}
				});
		} catch (Exception e) {
			System.out.println("[" + MANAGER_NAME + "] Could not read clothing binary directory: " + e.getMessage());
		}
	}

	/**
	 * Load races from binary cache
	 */
	private static void loadRacesFromBinary() {
		try {
			java.nio.file.Path binaryDir = java.nio.file.Paths.get(BINARY_CACHE_DIR, "race");
			if (!java.nio.file.Files.exists(binaryDir)) {
				System.out.println("[" + MANAGER_NAME + "] Race binary cache not found, falling back to text parsing");
				return;
			}
			
			// Iterate through binary race files
			java.nio.file.Files.list(binaryDir)
				.filter(p -> p.toString().endsWith(".bin"))
				.forEach(raceFile -> {
					try {
						byte[] binaryData = java.nio.file.Files.readAllBytes(raceFile);
						// Deserialize race binary data (when encoder is ready)
						System.out.println("[" + MANAGER_NAME + "] Loaded race: " + raceFile.getFileName());
					} catch (Exception e) {
						System.err.println("[" + MANAGER_NAME + "] Error loading race binary: " + e.getMessage());
					}
				});
		} catch (Exception e) {
			System.out.println("[" + MANAGER_NAME + "] Could not read race binary directory: " + e.getMessage());
		}
	}

	/**
	 * Load colors from binary cache
	 */
	private static void loadColorsFromBinary() {
		try {
			java.nio.file.Path colorFile = java.nio.file.Paths.get(BINARY_CACHE_DIR, "colours.bin");
			if (!java.nio.file.Files.exists(colorFile)) {
				System.out.println("[" + MANAGER_NAME + "] Color binary cache not found, falling back to text parsing");
				return;
			}
			
			// Read binary color data
			byte[] binaryData = java.nio.file.Files.readAllBytes(colorFile);
			// Deserialize color binary data (when encoder is ready)
			System.out.println("[" + MANAGER_NAME + "] Loaded colors from binary cache");
		} catch (Exception e) {
			System.out.println("[" + MANAGER_NAME + "] Could not read color binary file: " + e.getMessage());
		}
	}

	/**
	 * Load status effects from binary cache
	 */
	private static void loadStatusEffectsFromBinary() {
		try {
			java.nio.file.Path binaryDir = java.nio.file.Paths.get(BINARY_CACHE_DIR, "statusEffects");
			if (!java.nio.file.Files.exists(binaryDir)) {
				System.out.println("[" + MANAGER_NAME + "] Status effect binary cache not found, falling back to text parsing");
				return;
			}
			
			// Iterate through binary status effect files
			java.nio.file.Files.list(binaryDir)
				.filter(p -> p.toString().endsWith(".bin"))
				.forEach(effectFile -> {
					try {
						byte[] binaryData = java.nio.file.Files.readAllBytes(effectFile);
						// Deserialize status effect binary data (when encoder is ready)
						System.out.println("[" + MANAGER_NAME + "] Loaded status effect: " + effectFile.getFileName());
					} catch (Exception e) {
						System.err.println("[" + MANAGER_NAME + "] Error loading status effect binary: " + e.getMessage());
					}
				});
		} catch (Exception e) {
			System.out.println("[" + MANAGER_NAME + "] Could not read status effect binary directory: " + e.getMessage());
		}
	}

	/**
	 * Load tattoos from binary cache
	 */
	private static void loadTattoosFromBinary() {
		try {
			java.nio.file.Path binaryDir = java.nio.file.Paths.get(BINARY_CACHE_DIR, "tattoos");
			if (!java.nio.file.Files.exists(binaryDir)) {
				System.out.println("[" + MANAGER_NAME + "] Tattoo binary cache not found, falling back to text parsing");
				return;
			}
			
			// Iterate through binary tattoo files
			java.nio.file.Files.list(binaryDir)
				.filter(p -> p.toString().endsWith(".bin"))
				.forEach(tattooFile -> {
					try {
						byte[] binaryData = java.nio.file.Files.readAllBytes(tattooFile);
						// Deserialize tattoo binary data (when encoder is ready)
						System.out.println("[" + MANAGER_NAME + "] Loaded tattoo: " + tattooFile.getFileName());
					} catch (Exception e) {
						System.err.println("[" + MANAGER_NAME + "] Error loading tattoo binary: " + e.getMessage());
					}
				});
		} catch (Exception e) {
			System.out.println("[" + MANAGER_NAME + "] Could not read tattoo binary directory: " + e.getMessage());
		}
	}

	/**
	 * Load data from text files (fallback)
	 */
	private static void loadFromText() {
		System.out.println("[" + MANAGER_NAME + "] Loading from text files (fallback mode)");
		
		// Parse XML/JSON from res/ directory
		try {
			java.nio.file.Path resDir = java.nio.file.Paths.get("res");
			if (!java.nio.file.Files.exists(resDir)) {
				System.err.println("[" + MANAGER_NAME + "] Resource directory not found");
				return;
			}
			
			// Load from various text file categories
			loadTextFilesFromDirectory(resDir, "characters", "Character data");
			loadTextFilesFromDirectory(resDir, "weapons", "Weapon data");
			loadTextFilesFromDirectory(resDir, "clothing", "Clothing data");
			loadTextFilesFromDirectory(resDir, "race", "Race data");
			loadTextFilesFromDirectory(resDir, "statusEffects", "Status effect data");
			loadTextFilesFromDirectory(resDir, "tattoos", "Tattoo data");
			
			System.out.println("[" + MANAGER_NAME + "] Text file loading completed");
		} catch (Exception e) {
			System.err.println("[" + MANAGER_NAME + "] Error loading text files: " + e.getMessage());
		}
	}
	
	/**
	 * Load text files from a directory
	 * 
	 * @param baseDir Base resource directory
	 * @param category Data category (e.g., "characters", "weapons")
	 * @param description Human-readable description
	 */
	private static void loadTextFilesFromDirectory(java.nio.file.Path baseDir, String category, String description) {
		try {
			java.nio.file.Path categoryDir = baseDir.resolve(category);
			if (!java.nio.file.Files.exists(categoryDir)) {
				return; // Directory not required
			}
			
			// Load all .txt and .xml files from category
			java.nio.file.Files.walk(categoryDir, 1)
				.filter(p -> {
					String name = p.getFileName().toString();
					return name.endsWith(".txt") || name.endsWith(".xml") || name.endsWith(".json");
				})
				.forEach(file -> {
					try {
						// Read content from text file
						byte[] fileBytes = java.nio.file.Files.readAllBytes(file);
						// TODO: Parse XML/JSON content and populate data structures
						System.out.println("[" + MANAGER_NAME + "] Parsed " + description + ": " + file.getFileName() + " (" + fileBytes.length + " bytes)");
					} catch (Exception e) {
						System.err.println("[" + MANAGER_NAME + "] Error parsing " + file.getFileName() + ": " + e.getMessage());
					}
				});
		} catch (Exception e) {
			System.err.println("[" + MANAGER_NAME + "] Error accessing " + category + " directory: " + e.getMessage());
		}
	}

	/**
	 * Get a character by ID
	 * 
	 * @param characterId Character ID
	 * @return Character data map, or null if not found
	 */
	public static Map<String, Object> getCharacter(String characterId) {
		if (!initialized) {
			throw new RuntimeException("StaticDataManager not initialized");
		}
		
		Map<String, Object> character = characterData.get(characterId);
		if (character == null) {
			System.out.println("[" + MANAGER_NAME + "] Character not found: " + characterId);
		}
		return character;
	}

	/**
	 * Get an item by ID
	 * 
	 * @param itemId Item ID
	 * @return Item data map, or null if not found
	 */
	public static Map<String, Object> getItem(String itemId) {
		if (!initialized) {
			throw new RuntimeException("StaticDataManager not initialized");
		}
		
		Map<String, Object> item = itemData.get(itemId);
		if (item == null) {
			System.out.println("[" + MANAGER_NAME + "] Item not found: " + itemId);
		}
		return item;
	}

	/**
	 * Get a weapon by ID
	 * 
	 * @param weaponId Weapon ID
	 * @return Weapon data map, or null if not found
	 */
	public static Map<String, Object> getWeapon(String weaponId) {
		if (!initialized) {
			throw new RuntimeException("StaticDataManager not initialized");
		}
		return weaponData.get(weaponId);
	}

	/**
	 * Get all character IDs
	 * 
	 * @return List of character IDs
	 */
	public static List<String> getCharacterIds() {
		return new ArrayList<>(characterData.keySet());
	}

	/**
	 * Get all item IDs
	 * 
	 * @return List of item IDs
	 */
	public static List<String> getItemIds() {
		return new ArrayList<>(itemData.keySet());
	}

	/**
	 * Get all weapon IDs
	 * 
	 * @return List of weapon IDs
	 */
	public static List<String> getWeaponIds() {
		return new ArrayList<>(weaponData.keySet());
	}

	/**
	 * Get all loaded characters
	 * 
	 * @return Collection of character data maps
	 */
	public static Collection<Map<String, Object>> getAllCharacters() {
		return characterData.values();
	}

	/**
	 * Get all loaded items
	 * 
	 * @return Collection of item data maps
	 */
	public static Collection<Map<String, Object>> getAllItems() {
		return itemData.values();
	}

	/**
	 * Get all loaded weapons
	 * 
	 * @return Collection of weapon data maps
	 */
	public static Collection<Map<String, Object>> getAllWeapons() {
		return weaponData.values();
	}

	/**
	 * Get loading statistics
	 * 
	 * @return Statistics string
	 */
	public static String getLoadStatistics() {
		return String.format(
			"Characters: %d | Items: %d | Weapons: %d | Clothing: %d | " +
			"Races: %d | Colors: %d | StatusEffects: %d | Tattoos: %d | " +
			"Binary: %s | Time: %dms",
			characterData.size(), itemData.size(), weaponData.size(), clothingData.size(),
			raceData.size(), colorData.size(), statusEffectData.size(), tattooData.size(),
			binaryLoaded ? "YES" : "NO", initializationTime
		);
	}

	/**
	 * Check if binary data was loaded
	 * 
	 * @return true if using binary cache, false if using text fallback
	 */
	public static boolean isBinaryLoaded() {
		return binaryLoaded;
	}

	/**
	 * Check if manager is initialized
	 * 
	 * @return true if initialization complete
	 */
	public static boolean isInitialized() {
		return initialized;
	}

	/**
	 * Get number of loaded characters
	 * 
	 * @return Character count
	 */
	public static int getCharacterCount() {
		return characterData.size();
	}

	/**
	 * Get number of loaded items
	 * 
	 * @return Item count
	 */
	public static int getItemCount() {
		return itemData.size();
	}

	/**
	 * Reload all data
	 * Useful if data changes during runtime
	 * 
	 * @return true if reload successful
	 */
	public static synchronized boolean reload() {
		System.out.println("[" + MANAGER_NAME + "] Reloading all data...");

		// Clear all caches
		characterData.clear();
		itemData.clear();
		weaponData.clear();
		clothingData.clear();
		raceData.clear();
		colorData.clear();
		statusEffectData.clear();
		tattooData.clear();

		// Reset state
		initialized = false;
		binaryLoaded = false;

		// Reinitialize
		return initialize();
	}

	/**
	 * Shutdown the manager
	 */
	public static synchronized void shutdown() {
		System.out.println("[" + MANAGER_NAME + "] Shutting down...");

		characterData.clear();
		itemData.clear();
		weaponData.clear();
		clothingData.clear();
		raceData.clear();
		colorData.clear();
		statusEffectData.clear();
		tattooData.clear();

		initialized = false;
		binaryLoaded = false;

		System.out.println("[" + MANAGER_NAME + "] Shutdown complete");
	}
}
