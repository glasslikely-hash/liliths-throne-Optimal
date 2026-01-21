package com.lilithsthrone.ui.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Asset manager - loads and caches game resources.
 *
 * Responsibilities:
 *  - Load textures, fonts, sounds
 *  - Cache assets in memory
 *  - Handle memory management
 *  - Provide asset access API
 *
 * Usage:
 *  AssetManager.initialize(Gdx.files);
 *  Texture sprite = AssetManager.getTexture("characters");
 *  BitmapFont font = AssetManager.getFont("ui_regular");
 */
public class AssetManager {

    private static com.badlogic.gdx.assets.AssetManager assetManager;
    private static boolean initialized = false;

    /**
     * Initialize asset manager.
     * Call this during app startup.
     */
    public static void initialize(FileHandle fileHandle) {
        if (initialized) {
            return;
        }

        assetManager = new com.badlogic.gdx.assets.AssetManager();

        // Queue loading of essential assets
        // In a real implementation, these would be loaded from res/ui/
        // For now, just initialize the manager

        Gdx.app.log("AssetManager", "Initialized (real asset loading to be implemented)");
        initialized = true;
    }

    /**
     * Load all assets synchronously.
     * Blocks until all assets are loaded.
     */
    public static void loadAllAssets() {
        if (!initialized) {
            throw new RuntimeException("AssetManager not initialized");
        }

        // Load essential assets first
        // Textures, fonts, sounds would be queued and loaded here

        // Block until all loaded
        assetManager.finishLoading();
        Gdx.app.log("AssetManager", "All assets loaded");
    }

    /**
     * Get a texture by name.
     */
    public static com.badlogic.gdx.graphics.Texture getTexture(String name) {
        if (!initialized) {
            throw new RuntimeException("AssetManager not initialized");
        }

        try {
            return assetManager.get("textures/" + name + ".png", com.badlogic.gdx.graphics.Texture.class);
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to load texture: " + name);
            // Return a placeholder white texture
            return null;
        }
    }

    /**
     * Get a bitmap font by name.
     */
    public static com.badlogic.gdx.graphics.g2d.BitmapFont getFont(String name) {
        if (!initialized) {
            throw new RuntimeException("AssetManager not initialized");
        }

        try {
            return assetManager.get("fonts/" + name + ".fnt", com.badlogic.gdx.graphics.g2d.BitmapFont.class);
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to load font: " + name);
            return null;
        }
    }

    /**
     * Get a sound by name.
     */
    public static com.badlogic.gdx.audio.Sound getSound(String name) {
        if (!initialized) {
            throw new RuntimeException("AssetManager not initialized");
        }

        try {
            return assetManager.get("sounds/" + name + ".wav", com.badlogic.gdx.audio.Sound.class);
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to load sound: " + name);
            return null;
        }
    }

    /**
     * Get background music by name.
     */
    public static com.badlogic.gdx.audio.Music getMusic(String name) {
        if (!initialized) {
            throw new RuntimeException("AssetManager not initialized");
        }

        try {
            return assetManager.get("music/" + name + ".ogg", com.badlogic.gdx.audio.Music.class);
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to load music: " + name);
            return null;
        }
    }

    /**
     * Check if an asset is loaded.
     */
    public static boolean isAssetLoaded(String name, Class<?> type) {
        if (!initialized) {
            return false;
        }
        return assetManager.isLoaded(name, type);
    }

    /**
     * Unload a specific asset.
     */
    public static void unloadAsset(String name) {
        if (!initialized) {
            return;
        }
        assetManager.unload(name);
    }

    /**
     * Get loading progress (0.0 = not loaded, 1.0 = all loaded).
     */
    public static float getLoadProgress() {
        if (!initialized) {
            return 0f;
        }
        return assetManager.getProgress();
    }

    /**
     * Dispose all assets and clean up.
     * Call this during app shutdown.
     */
    public static void dispose() {
        if (!initialized) {
            return;
        }

        assetManager.dispose();
        initialized = false;
        Gdx.app.log("AssetManager", "Disposed");
    }

    /**
     * Get memory usage statistics.
     */
    public static String getMemoryStats() {
        if (!initialized) {
            return "Not initialized";
        }

        return "Loaded assets: " + assetManager.getAssetNames().size;
    }
}
