package com.lilithsthrone.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Centralized game configuration loader.
 * 
 * Loads all configurable game parameters from game.properties at startup.
 * Provides type-safe getters for configuration values with sensible defaults.
 * 
 * Benefits:
 *   - No hardcoded values scattered across codebase
 *   - Easy tuning: modify properties and restart (no recompilation)
 *   - Clear audit trail of what parameters exist
 *   - Easy to add new configurable parameters
 * 
 * Usage:
 *   int fps = GameConfig.getGameFPS();
 *   float threshold = GameConfig.getMemoryThresholdHigh();
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class GameConfig {
    private static final Logger LOGGER = Logger.getLogger(GameConfig.class.getName());
    private static final String CONFIG_FILE = "game.properties";
    private static final Properties config = new Properties();
    private static boolean isInitialized = false;

    /**
     * Static initializer - loads configuration on first use
     */
    static {
        loadConfiguration();
    }

    /**
     * Load configuration from game.properties file
     */
    private static void loadConfiguration() {
        if (isInitialized) {
            return;
        }

        try (InputStream input = GameConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                LOGGER.warning("Configuration file not found: " + CONFIG_FILE + ". Using defaults.");
                isInitialized = true;
                return;
            }

            config.load(input);
            LOGGER.info("Loaded game configuration from " + CONFIG_FILE);
            isInitialized = true;

        } catch (IOException e) {
            LOGGER.severe("Failed to load configuration: " + e.getMessage());
            isInitialized = true;
        }
    }

    /**
     * Reload configuration (for dev/testing)
     */
    public static void reload() {
        config.clear();
        isInitialized = false;
        loadConfiguration();
        LOGGER.info("Game configuration reloaded");
    }

    // ===== FRAME RATE AND TIMING =====

    /**
     * Target frame rate (frames per second)
     * Default: 60
     */
    public static int getGameFPS() {
        return getIntProperty("game.fps", 60);
    }

    /**
     * Maximum delta time per frame (milliseconds)
     * Prevents huge jumps if frame rate drops
     * Default: 50
     */
    public static float getMaxDeltaTimeMs() {
        return getFloatProperty("game.max_delta_time_ms", 50.0f);
    }

    /**
     * Frame skip threshold (consecutive frames below this FPS triggers skip)
     * Default: 3
     */
    public static int getFrameSkipThreshold() {
        return getIntProperty("game.frame_skip_threshold", 3);
    }

    // ===== MEMORY MANAGEMENT =====

    /**
     * High memory threshold (0-1, percentage of max heap)
     * Triggers aggressive garbage collection
     * Default: 0.8
     */
    public static float getMemoryThresholdHigh() {
        return getFloatProperty("memory.threshold.high", 0.8f);
    }

    /**
     * Critical memory threshold (0-1)
     * Triggers emergency memory cleanup
     * Default: 0.95
     */
    public static float getMemoryThresholdCritical() {
        return getFloatProperty("memory.threshold.critical", 0.95f);
    }

    /**
     * Garbage collection check interval (milliseconds)
     * Default: 5000
     */
    public static long getMemoryCollectIntervalMs() {
        return getLongProperty("memory.collect_interval_ms", 5000L);
    }

    // ===== CACHING =====

    /**
     * Maximum cache size (number of entries)
     * Default: 1000
     */
    public static int getCacheMaxSize() {
        return getIntProperty("cache.max_size", 1000);
    }

    /**
     * LRU load factor (evict when cache reaches this % of max)
     * Default: 0.75
     */
    public static float getCacheLRULoadFactor() {
        return getFloatProperty("cache.lru_load_factor", 0.75f);
    }

    /**
     * Cache entry TTL (time to live in seconds)
     * Default: 3600
     */
    public static long getCacheTTLSeconds() {
        return getLongProperty("cache.ttl_seconds", 3600L);
    }

    // ===== OBJECT POOLING =====

    /**
     * Object pool growth factor
     * Multiplied by pool size when growth needed
     * Default: 1.5
     */
    public static float getPoolGrowthFactor() {
        return getFloatProperty("pool.growth_factor", 1.5f);
    }

    /**
     * Initial pool size for most object types
     * Default: 100
     */
    public static int getPoolInitialSize() {
        return getIntProperty("pool.initial_size", 100);
    }

    /**
     * Maximum pool size before growth stops
     * Default: 10000
     */
    public static int getPoolMaxSize() {
        return getIntProperty("pool.max_size", 10000);
    }

    // ===== UI DIMENSIONS =====

    /**
     * Character portrait size (pixels)
     * Default: 150
     */
    public static float getUIPortraitSize() {
        return getFloatProperty("ui.portrait_size", 150.0f);
    }

    /**
     * Line height for text display (pixels)
     * Default: 25
     */
    public static float getUILineHeight() {
        return getFloatProperty("ui.line_height", 25.0f);
    }

    /**
     * Response button height (pixels)
     * Default: 35
     */
    public static float getUIResponseHeight() {
        return getFloatProperty("ui.response_height", 35.0f);
    }

    /**
     * Default window width (pixels)
     * Default: 1280
     */
    public static int getUIDefaultWidth() {
        return getIntProperty("ui.default_width", 1280);
    }

    /**
     * Default window height (pixels)
     * Default: 720
     */
    public static int getUIDefaultHeight() {
        return getIntProperty("ui.default_height", 720);
    }

    // ===== PERSISTENCE =====

    /**
     * Directory for binary asset cache
     * Default: cache/binary
     */
    public static String getPersistenceBinaryCacheDir() {
        return getStringProperty("persistence.binary_cache_dir", "cache/binary");
    }

    /**
     * Snapshot creation interval (seconds)
     * Default: 300
     */
    public static long getPersistenceSnapshotIntervalSeconds() {
        return getLongProperty("persistence.snapshot_interval_seconds", 300L);
    }

    /**
     * Autosave interval (seconds)
     * Default: 60
     */
    public static long getPersistenceAutosaveIntervalSeconds() {
        return getLongProperty("persistence.autosave_interval_seconds", 60L);
    }

    /**
     * Max deltas before merging into new snapshot
     * Default: 10
     */
    public static int getPersistenceMaxDeltaCount() {
        return getIntProperty("persistence.max_delta_count", 10);
    }

    // ===== DIALOGUE SYSTEM =====

    /**
     * Maximum dialogue history (number of messages)
     * Default: 100
     */
    public static int getDialogueMaxHistory() {
        return getIntProperty("dialogue.max_history", 100);
    }

    /**
     * Dialogue choice timeout (seconds)
     * Default: 300
     */
    public static long getDialogueChoiceTimeoutSeconds() {
        return getLongProperty("dialogue.choice_timeout_seconds", 300L);
    }

    // ===== COMBAT SYSTEM =====

    /**
     * Combat turn timeout (seconds)
     * Default: 60
     */
    public static long getCombatTurnTimeoutSeconds() {
        return getLongProperty("combat.turn_timeout_seconds", 60L);
    }

    /**
     * Combat animation speed multiplier
     * Default: 1.0
     */
    public static float getCombatAnimationSpeedFactor() {
        return getFloatProperty("combat.animation_speed_factor", 1.0f);
    }

    // ===== PERFORMANCE MONITORING =====

    /**
     * Enable performance monitoring
     * Default: true
     */
    public static boolean isMonitorEnabled() {
        return getBooleanProperty("monitor.enabled", true);
    }

    /**
     * Performance log interval (seconds)
     * Default: 10
     */
    public static long getMonitorLogIntervalSeconds() {
        return getLongProperty("monitor.log_interval_seconds", 10L);
    }

    /**
     * GC threshold for alerts (percent of total heap)
     * Default: 5
     */
    public static int getMonitorGCThresholdPercent() {
        return getIntProperty("monitor.gc_threshold_percent", 5);
    }

    // ===== DEBUG SETTINGS =====

    /**
     * Debug log level (INFO, DEBUG, TRACE)
     * Default: INFO
     */
    public static String getDebugLogLevel() {
        return getStringProperty("debug.log_level", "INFO");
    }

    /**
     * Show frame time in UI
     * Default: false
     */
    public static boolean isDebugShowFrameTime() {
        return getBooleanProperty("debug.show_frame_time", false);
    }

    /**
     * Show memory statistics in UI
     * Default: false
     */
    public static boolean isDebugShowMemoryStats() {
        return getBooleanProperty("debug.show_memory_stats", false);
    }

    // ===== HELPER METHODS =====

    private static String getStringProperty(String key, String defaultValue) {
        String value = config.getProperty(key);
        if (value == null || value.isEmpty()) {
            LOGGER.fine("Using default value for " + key + ": " + defaultValue);
            return defaultValue;
        }
        return value.trim();
    }

    private static int getIntProperty(String key, int defaultValue) {
        try {
            String value = config.getProperty(key);
            if (value == null || value.isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid integer for " + key + ", using default: " + defaultValue);
            return defaultValue;
        }
    }

    private static long getLongProperty(String key, long defaultValue) {
        try {
            String value = config.getProperty(key);
            if (value == null || value.isEmpty()) {
                return defaultValue;
            }
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid long for " + key + ", using default: " + defaultValue);
            return defaultValue;
        }
    }

    private static float getFloatProperty(String key, float defaultValue) {
        try {
            String value = config.getProperty(key);
            if (value == null || value.isEmpty()) {
                return defaultValue;
            }
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid float for " + key + ", using default: " + defaultValue);
            return defaultValue;
        }
    }

    private static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = config.getProperty(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value.trim().equalsIgnoreCase("true") || value.trim().equals("1");
    }

    /**
     * Get raw property value (advanced use)
     */
    public static String getRawProperty(String key) {
        return config.getProperty(key);
    }

    /**
     * Get all configured properties
     */
    public static Properties getAllProperties() {
        return (Properties) config.clone();
    }
}
