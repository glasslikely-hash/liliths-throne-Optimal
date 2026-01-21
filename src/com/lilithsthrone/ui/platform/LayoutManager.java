package com.lilithsthrone.ui.platform;

import com.lilithsthrone.ui.LibGdxApp;

/**
 * Responsive layout management - scales UI for different screen sizes.
 *
 * Responsibilities:
 *  - Track screen dimensions
 *  - Scale UI elements responsively
 *  - Manage safe areas (mobile notches)
 *  - Provide layout helper methods
 *
 * Usage:
 *  LayoutManager.updateScreenSize(1920, 1080);
 *  float buttonSize = LayoutManager.getScaledWidth(80);
 */
public class LayoutManager {

    // Screen dimensions
    private static int screenWidth = 1200;
    private static int screenHeight = 800;
    private static float virtualWidth = LibGdxApp.getVirtualWidth();
    private static float virtualHeight = LibGdxApp.getVirtualHeight();

    // Scale factors
    private static float scaleX = 1f;
    private static float scaleY = 1f;

    /**
     * Update screen dimensions when window is resized.
     */
    public static void updateScreenSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        recalculateScale();
    }

    /**
     * Set virtual viewport size (called by LibGdxApp after camera setup).
     */
    public static void setVirtualViewport(float width, float height) {
        virtualWidth = width;
        virtualHeight = height;
        recalculateScale();
    }

    /**
     * Recalculate scale factors.
     */
    private static void recalculateScale() {
        scaleX = virtualWidth / LibGdxApp.getVirtualWidth();
        scaleY = virtualHeight / LibGdxApp.getVirtualHeight();
    }

    /**
     * Scale a width value for responsive design.
     */
    public static float getScaledWidth(float baseWidth) {
        return baseWidth * scaleX;
    }

    /**
     * Scale a height value for responsive design.
     */
    public static float getScaledHeight(float baseHeight) {
        return baseHeight * scaleY;
    }

    /**
     * Get safe area bounds (mobile notches, etc).
     * Returns absolute pixel values.
     */
    public static float getSafeAreaLeft() {
        return PlatformConfig.SAFE_AREA_LEFT;
    }

    public static float getSafeAreaRight() {
        return screenWidth - PlatformConfig.SAFE_AREA_RIGHT;
    }

    public static float getSafeAreaTop() {
        return screenHeight - PlatformConfig.SAFE_AREA_TOP;
    }

    public static float getSafeAreaBottom() {
        return PlatformConfig.SAFE_AREA_BOTTOM;
    }

    /**
     * Center a UI element horizontally.
     */
    public static float getCenteredX(float elementWidth) {
        return (virtualWidth - elementWidth) / 2f;
    }

    /**
     * Center a UI element vertically.
     */
    public static float getCenteredY(float elementHeight) {
        return (virtualHeight - elementHeight) / 2f;
    }

    /**
     * Get breakpoint layout type based on screen size.
     */
    public enum Breakpoint {
        SMALL,      // 800x600 - limited layout
        MEDIUM,     // 1024x768 - standard layout
        LARGE,      // 1280x720+ - expanded layout
        XLARGE      // 1920x1080+ - full layout
    }

    public static Breakpoint getBreakpoint() {
        float minDimension = Math.min(virtualWidth, virtualHeight);

        if (minDimension < 700) {
            return Breakpoint.SMALL;
        } else if (minDimension < 900) {
            return Breakpoint.MEDIUM;
        } else if (minDimension < 1200) {
            return Breakpoint.LARGE;
        } else {
            return Breakpoint.XLARGE;
        }
    }

    // ================== Getters ==================

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static float getVirtualWidth() {
        return virtualWidth;
    }

    public static float getVirtualHeight() {
        return virtualHeight;
    }

    public static float getScaleX() {
        return scaleX;
    }

    public static float getScaleY() {
        return scaleY;
    }
}
