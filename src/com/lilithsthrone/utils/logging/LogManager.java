package com.lilithsthrone.utils.logging;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Unified logging system for Lilith's Throne.
 * 
 * Replaces scattered System.out.println() calls with proper logging framework.
 * Supports different log levels: FINEST, FINE, INFO, WARNING, SEVERE
 * 
 * Benefits:
 *   - Centralized log output
 *   - Log level control (can suppress verbose debug logs)
 *   - Consistent formatting
 *   - Integration with logging framework
 *   - Easy to redirect to file/console
 * 
 * Usage:
 *   LogManager.debug("Starting game");
 *   LogManager.info("Player loaded: " + playerName);
 *   LogManager.warning("Low memory detected");
 *   LogManager.error("Failed to load: " + filename, exception);
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class LogManager {
    
    private static final Logger mainLogger = Logger.getLogger("LilithsThrone");
    private static final Logger uiLogger = Logger.getLogger("LilithsThrone.UI");
    private static final Logger logicLogger = Logger.getLogger("LilithsThrone.Logic");
    private static final Logger persistenceLogger = Logger.getLogger("LilithsThrone.Persistence");
    private static final Logger gameLogger = Logger.getLogger("LilithsThrone.Game");
    
    // Default log level (can be changed at runtime)
    private static Level defaultLevel = Level.INFO;
    
    /**
     * Set global log level
     */
    public static void setLogLevel(Level level) {
        defaultLevel = level;
        mainLogger.setLevel(level);
        uiLogger.setLevel(level);
        logicLogger.setLevel(level);
        persistenceLogger.setLevel(level);
        gameLogger.setLevel(level);
    }
    
    // ===== MAIN LOGGER =====
    
    public static void debug(String message) {
        mainLogger.log(Level.FINE, message);
    }
    
    public static void info(String message) {
        mainLogger.log(Level.INFO, message);
    }
    
    public static void warning(String message) {
        mainLogger.log(Level.WARNING, message);
    }
    
    public static void error(String message) {
        mainLogger.log(Level.SEVERE, message);
    }
    
    public static void error(String message, Throwable throwable) {
        mainLogger.log(Level.SEVERE, message, throwable);
    }
    
    // ===== UI LOGGER =====
    
    public static void uiDebug(String message) {
        uiLogger.log(Level.FINE, message);
    }
    
    public static void uiInfo(String message) {
        uiLogger.log(Level.INFO, message);
    }
    
    public static void uiWarning(String message) {
        uiLogger.log(Level.WARNING, message);
    }
    
    public static void uiError(String message) {
        uiLogger.log(Level.SEVERE, message);
    }
    
    public static void uiError(String message, Throwable throwable) {
        uiLogger.log(Level.SEVERE, message, throwable);
    }
    
    // ===== LOGIC LOGGER =====
    
    public static void logicDebug(String message) {
        logicLogger.log(Level.FINE, message);
    }
    
    public static void logicInfo(String message) {
        logicLogger.log(Level.INFO, message);
    }
    
    public static void logicWarning(String message) {
        logicLogger.log(Level.WARNING, message);
    }
    
    public static void logicError(String message) {
        logicLogger.log(Level.SEVERE, message);
    }
    
    public static void logicError(String message, Throwable throwable) {
        logicLogger.log(Level.SEVERE, message, throwable);
    }
    
    // ===== PERSISTENCE LOGGER =====
    
    public static void persistenceDebug(String message) {
        persistenceLogger.log(Level.FINE, message);
    }
    
    public static void persistenceInfo(String message) {
        persistenceLogger.log(Level.INFO, message);
    }
    
    public static void persistenceWarning(String message) {
        persistenceLogger.log(Level.WARNING, message);
    }
    
    public static void persistenceError(String message) {
        persistenceLogger.log(Level.SEVERE, message);
    }
    
    public static void persistenceError(String message, Throwable throwable) {
        persistenceLogger.log(Level.SEVERE, message, throwable);
    }
    
    // ===== GAME LOGGER =====
    
    public static void gameDebug(String message) {
        gameLogger.log(Level.FINE, message);
    }
    
    public static void gameInfo(String message) {
        gameLogger.log(Level.INFO, message);
    }
    
    public static void gameWarning(String message) {
        gameLogger.log(Level.WARNING, message);
    }
    
    public static void gameError(String message) {
        gameLogger.log(Level.SEVERE, message);
    }
    
    public static void gameError(String message, Throwable throwable) {
        gameLogger.log(Level.SEVERE, message, throwable);
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Get logger for a specific module
     */
    public static Logger getModuleLogger(String moduleName) {
        return Logger.getLogger("LilithsThrone." + moduleName);
    }
    
    /**
     * Format message with parameters (reduces string concatenation in calls)
     * 
     * Usage: LogManager.info(LogManager.format("Player {} loaded with {} items", name, count));
     */
    public static String format(String template, Object... args) {
        String result = template;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{}", String.valueOf(args[i]));
        }
        return result;
    }
    
    /**
     * Convenience method for formatted logging
     */
    public static void infof(String template, Object... args) {
        info(format(template, args));
    }
    
    public static void debugf(String template, Object... args) {
        debug(format(template, args));
    }
    
    public static void warningf(String template, Object... args) {
        warning(format(template, args));
    }
    
    public static void errorf(String template, Object... args) {
        error(format(template, args));
    }
    
    // ===== FILE LOADING ERROR HANDLER =====
    
    /**
     * Unified error handler for file loading failures.
     * Replaces the duplicate pattern:
     *   System.err.println("Loading X failed at 'Y'. File path: " + path);
     *   System.err.println("Actual exception: ");
     *   ex.printStackTrace(System.err);
     * 
     * Usage:
     *   try {
     *       loadFile(file);
     *   } catch(Exception ex) {
     *       LogManager.logFileLoadingError("MyType", file.getAbsolutePath(), ex);
     *   }
     * 
     * @param context Class or type name (e.g., "ItemType", "ClothingType")
     * @param filePath File path that failed to load
     * @param exception The exception that occurred
     */
    public static void logFileLoadingError(String context, String filePath, Exception exception) {
        String message = "Loading " + context + " failed. File path: " + filePath;
        gameLogger.log(Level.SEVERE, message, exception);
    }
}
