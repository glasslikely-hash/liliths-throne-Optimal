package com.lilithsthrone.ui.platform;

import com.badlogic.gdx.Gdx;

/**
 * Platform configuration and detection.
 *
 * Automatically detects whether running on desktop or mobile
 * and configures UI, input, and rendering accordingly.
 *
 * Usage:
 *  PlatformConfig.detectPlatform();  // Call during app initialization
 *  if (PlatformConfig.IS_MOBILE) {
 *      // Use mobile layout
 *  }
 */
public class PlatformConfig {

    /**
     * Detected platform type.
     */
    public enum Platform {
        WINDOWS,
        MACOS,
        LINUX,
        ANDROID,
        IOS
    }

    // ================== Configuration ==================

    /** Currently detected platform */
    public static Platform PLATFORM = Platform.WINDOWS;

    /** Whether running on mobile device */
    public static boolean IS_MOBILE = false;

    /** Whether touch input is available */
    public static boolean HAS_TOUCH = false;

    // ================== Desktop Specific ==================

    /** Desktop window width */
    public static int DESKTOP_WIDTH = 1200;

    /** Desktop window height */
    public static int DESKTOP_HEIGHT = 800;

    /** Minimum window width */
    public static int DESKTOP_MIN_WIDTH = 800;

    /** Minimum window height */
    public static int DESKTOP_MIN_HEIGHT = 600;

    /** Whether to use fullscreen (desktop) */
    public static boolean USE_FULLSCREEN = false;

    /** Whether to use vsync */
    public static boolean USE_VSYNC = true;

    // ================== Mobile Specific ==================

    /** Mobile screen DPI (default 160 for Android) */
    public static float MOBILE_DPI = 160f;

    /** Whether to support orientation change */
    public static boolean ALLOW_ORIENTATION_CHANGE = true;

    /** Safe area padding for notch/cutout (mobile) */
    public static float SAFE_AREA_TOP = 0f;
    public static float SAFE_AREA_BOTTOM = 0f;
    public static float SAFE_AREA_LEFT = 0f;
    public static float SAFE_AREA_RIGHT = 0f;

    // ================== Graphics Settings ==================

    /** Target framerate (0 = unlimited) */
    public static int TARGET_FPS = 60;

    /** Anti-aliasing enabled */
    public static boolean ANTI_ALIASING = true;

    /** Texture filtering (NEAREST or LINEAR) */
    public enum TextureFilter {
        NEAREST,
        LINEAR
    }
    public static TextureFilter TEXTURE_FILTER = TextureFilter.LINEAR;

    /** Font rendering quality */
    public enum FontQuality {
        LOW,        // 12pt, less memory
        MEDIUM,     // 16pt, balanced
        HIGH        // 20pt+, more detailed
    }
    public static FontQuality FONT_QUALITY = FontQuality.MEDIUM;

    // ================== Performance Settings ==================

    /** Maximum texture size (power of 2) */
    public static int MAX_TEXTURE_SIZE = 2048;

    /** Particle system max particles */
    public static int MAX_PARTICLES = 1000;

    /** Draw distance for rendering */
    public static float DRAW_DISTANCE = 500f;

    /** Memory budget for assets (MB) */
    public static int ASSET_MEMORY_MB = 256;

    // ================== Audio Settings ==================

    /** Master volume (0.0 = silent, 1.0 = full) */
    public static float MASTER_VOLUME = 0.8f;

    /** Music volume */
    public static float MUSIC_VOLUME = 0.7f;

    /** Sound effects volume */
    public static float SFX_VOLUME = 0.8f;

    /** Whether to stream large audio files */
    public static boolean STREAM_AUDIO = true;

    // ================== Logging ==================

    /** Enable debug logging */
    public static boolean DEBUG_MODE = true;

    /** Log frame times */
    public static boolean LOG_FRAME_TIMES = false;

    /** Log input events */
    public static boolean LOG_INPUT = false;

    /**
     * Detect current platform and configure accordingly.
     * Call this during app initialization.
     */
    public static void detectPlatform() {
        com.badlogic.gdx.Application.ApplicationType appType = Gdx.app.getType();

        switch (appType) {
            case Desktop:
                detectDesktopPlatform();
                IS_MOBILE = false;
                HAS_TOUCH = false;
                break;

            case Android:
                PLATFORM = Platform.ANDROID;
                IS_MOBILE = true;
                HAS_TOUCH = true;
                configureAndroid();
                break;

            case iOS:
                PLATFORM = Platform.IOS;
                IS_MOBILE = true;
                HAS_TOUCH = true;
                configureIOS();
                break;

            case WebGL:
                // Treat WebGL like desktop
                PLATFORM = Platform.LINUX;
                IS_MOBILE = false;
                HAS_TOUCH = false;
                break;

            default:
                PLATFORM = Platform.LINUX;
                IS_MOBILE = false;
                HAS_TOUCH = false;
        }

        Gdx.app.log("PlatformConfig", "Platform: " + PLATFORM + ", Mobile: " + IS_MOBILE + ", Touch: " + HAS_TOUCH);
    }

    /**
     * Detect desktop OS (Windows/Mac/Linux).
     */
    private static void detectDesktopPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            PLATFORM = Platform.WINDOWS;
        } else if (osName.contains("mac")) {
            PLATFORM = Platform.MACOS;
        } else {
            PLATFORM = Platform.LINUX;
        }

        Gdx.app.log("PlatformConfig", "Desktop OS: " + PLATFORM);
    }

    /**
     * Configure settings for Android.
     */
    private static void configureAndroid() {
        // Mobile-optimized defaults
        TARGET_FPS = 60;
        ANTI_ALIASING = false;
        TEXTURE_FILTER = TextureFilter.NEAREST;
        FONT_QUALITY = FontQuality.MEDIUM;
        MAX_TEXTURE_SIZE = 1024;
        MAX_PARTICLES = 500;
        DRAW_DISTANCE = 300f;
        ASSET_MEMORY_MB = 128;

        Gdx.app.log("PlatformConfig", "Android configuration applied");
    }

    /**
     * Configure settings for iOS.
     */
    private static void configureIOS() {
        // iOS-optimized defaults
        TARGET_FPS = 60;
        ANTI_ALIASING = true;  // iOS usually has good hardware
        TEXTURE_FILTER = TextureFilter.LINEAR;
        FONT_QUALITY = FontQuality.HIGH;
        MAX_TEXTURE_SIZE = 2048;
        MAX_PARTICLES = 1000;
        DRAW_DISTANCE = 500f;
        ASSET_MEMORY_MB = 256;

        Gdx.app.log("PlatformConfig", "iOS configuration applied");
    }

    /**
     * Get visual scale factor based on screen DPI.
     * Used for responsive text and UI sizing.
     */
    public static float getDPIScale() {
        if (IS_MOBILE) {
            // Mobile: scale based on DPI (typical 160-480)
            return MOBILE_DPI / 160f;
        } else {
            // Desktop: no scaling
            return 1f;
        }
    }

    /**
     * Get font size adjusted for platform.
     */
    public static int getScaledFontSize(int baseSize) {
        float scale = getDPIScale();
        if (IS_MOBILE) {
            // Increase font size on mobile for readability
            return Math.round(baseSize * scale * 1.5f);
        } else {
            return Math.round(baseSize * scale);
        }
    }

    /**
     * Get button size adjusted for platform.
     * Mobile buttons should be larger (60px+) for touch, desktop (40px+) for mouse.
     */
    public static float getScaledButtonSize(float baseSize) {
        if (IS_MOBILE) {
            // Larger touch targets (at least 48dp = ~72px at 160dpi)
            return baseSize * 1.5f;
        } else {
            // Smaller mouse targets are OK
            return baseSize;
        }
    }

    /**
     * Log configuration state (for debugging).
     */
    public static void logConfiguration() {
        if (!DEBUG_MODE) return;

        Gdx.app.log("PlatformConfig", "=== PLATFORM CONFIG ===");
        Gdx.app.log("PlatformConfig", "Platform: " + PLATFORM);
        Gdx.app.log("PlatformConfig", "Is Mobile: " + IS_MOBILE);
        Gdx.app.log("PlatformConfig", "Has Touch: " + HAS_TOUCH);
        Gdx.app.log("PlatformConfig", "Target FPS: " + TARGET_FPS);
        Gdx.app.log("PlatformConfig", "Max Texture: " + MAX_TEXTURE_SIZE);
        Gdx.app.log("PlatformConfig", "Font Quality: " + FONT_QUALITY);
        Gdx.app.log("PlatformConfig", "Asset Memory: " + ASSET_MEMORY_MB + "MB");
        Gdx.app.log("PlatformConfig", "=======================");
    }
}
