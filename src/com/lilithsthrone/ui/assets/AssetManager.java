package com.lilithsthrone.ui.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;

/**
 * Asset manager - loads and caches game resources.
 *
 * Responsibilities:
 *  - Load textures, fonts, sounds from res/ directory
 *  - Cache assets in memory with LibGDX asset manager
 *  - Handle memory management and cleanup
 *  - Provide convenient asset access API
 *  - Generate bitmap fonts from TTF files on demand
 *
 * Usage:
 *  AssetManager.initialize(Gdx.files);
 *  BitmapFont font = AssetManager.getFont(AssetManager.FONT_UI_REGULAR, 24);
 *  Texture texture = AssetManager.getTexture("button_normal");
 *  Sound sound = AssetManager.getSound("click");
 */
public class AssetManager {

    // Font asset identifiers
    public static final String FONT_UI_REGULAR = "ui_regular";
    public static final String FONT_UI_TITLE = "ui_title";
    public static final String FONT_DIALOGUE = "dialogue";
    public static final String FONT_HUD = "hud";

    private static com.badlogic.gdx.assets.AssetManager assetManager;
    private static FreeTypeFontGenerator fontGenerator;
    private static boolean initialized = false;

    // Asset directories
    private static final String TEXTURES_DIR = "res/ui/textures/";
    private static final String FONTS_DIR = "res/fonts/";
    private static final String SOUNDS_DIR = "res/sounds/";
    private static final String MUSIC_DIR = "res/music/";

    /**
     * Initialize asset manager.
     * Call this during app startup (from LibGdxApp.create()).
     */
    public static void initialize(FileHandle fileHandle) {
        if (initialized) {
            return;
        }

        try {
            assetManager = new com.badlogic.gdx.assets.AssetManager();

            // Set up texture loader
            assetManager.setLoader(Texture.class, new com.badlogic.gdx.assets.loaders.TextureLoader(
                new com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter()));

            Gdx.app.log("AssetManager", "Initialized");
            initialized = true;
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Initialization failed: " + e.getMessage());
            throw new RuntimeException("Failed to initialize AssetManager", e);
        }
    }

    /**
     * Load all essential assets synchronously.
     * Should be called after initialize() and before game starts.
     */
    public static void loadEssentialAssets() {
        if (!initialized) {
            throw new RuntimeException("AssetManager not initialized");
        }

        try {
            // Queue essential fonts
            queueFontLoading(FONT_UI_REGULAR, "res/fonts/DejaVu Sans/DejaVuSans.ttf", 16);
            queueFontLoading(FONT_UI_TITLE, "res/fonts/DejaVu Sans/DejaVuSans-Bold.ttf", 32);
            queueFontLoading(FONT_DIALOGUE, "res/fonts/DejaVu Sans/DejaVuSans.ttf", 20);
            queueFontLoading(FONT_HUD, "res/fonts/DejaVu Sans/DejaVuSans.ttf", 14);

            // Block until all loaded
            assetManager.finishLoading();
            Gdx.app.log("AssetManager", "Essential assets loaded successfully");
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to load essential assets: " + e.getMessage());
            // Don't crash - continue with default fonts
        }
    }

    /**
     * Queue a font for loading with specific size.
     */
    private static void queueFontLoading(String name, String ttfPath, int size) {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(ttfPath));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = size;
            BitmapFont font = generator.generateFont(parameter);
            assetManager.setLoaded(name, BitmapFont.class);
            Gdx.app.log("AssetManager", "Queued font: " + name);
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to queue font " + name + ": " + e.getMessage());
        }
    }

    /**
     * Get a font by name and size. Generates from TTF if not cached.
     * Falls back to default bitmap font if generation fails.
     */
    public static BitmapFont getFont(String name, int size) {
        if (!initialized) {
            Gdx.app.error("AssetManager", "Not initialized");
            return new BitmapFont();  // Default font
        }

        try {
            // Try to get cached font
            String cacheKey = name + "_" + size;
            if (assetManager.isLoaded(cacheKey, BitmapFont.class)) {
                return assetManager.get(cacheKey, BitmapFont.class);
            }

            // Generate new font from TTF
            String ttfPath = getTTFPathForFont(name);
            if (ttfPath != null && Gdx.files.internal(ttfPath).exists()) {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(ttfPath));
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = size;
                BitmapFont font = generator.generateFont(parameter);
                generator.dispose();
                
                // Cache for future use
                assetManager.setLoaded(cacheKey, BitmapFont.class);
                Gdx.app.log("AssetManager", "Generated font: " + cacheKey);
                return font;
            }
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to load font " + name + ": " + e.getMessage());
        }

        // Fallback to default font
        return new BitmapFont();
    }

    /**
     * Get font with default size (16px).
     */
    public static BitmapFont getFont(String name) {
        return getFont(name, 16);
    }

    /**
     * Map font names to TTF file paths.
     */
    private static String getTTFPathForFont(String name) {
        switch (name) {
            case FONT_UI_REGULAR:
            case FONT_DIALOGUE:
            case FONT_HUD:
                return "res/fonts/DejaVu Sans/DejaVuSans.ttf";
            case FONT_UI_TITLE:
                return "res/fonts/DejaVu Sans/DejaVuSans-Bold.ttf";
            default:
                return "res/fonts/DejaVu Sans/DejaVuSans.ttf";
        }
    }

    /**
     * Get a texture by name from res/ui/textures/ directory.
     */
    public static Texture getTexture(String name) {
        if (!initialized) {
            Gdx.app.error("AssetManager", "Not initialized");
            return createWhitePixelTexture();
        }

        try {
            String texturePath = TEXTURES_DIR + name + ".png";
            
            if (!assetManager.isLoaded(texturePath, Texture.class)) {
                if (Gdx.files.internal(texturePath).exists()) {
                    assetManager.load(texturePath, Texture.class);
                    assetManager.finishLoadingAsset(texturePath);
                    Gdx.app.log("AssetManager", "Loaded texture: " + name);
                } else {
                    Gdx.app.error("AssetManager", "Texture not found: " + texturePath);
                    return createWhitePixelTexture();
                }
            }

            return assetManager.get(texturePath, Texture.class);
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to load texture: " + name + " (" + e.getMessage() + ")");
            return createWhitePixelTexture();
        }
    }

    /**
     * Get a sound by name from res/sounds/ directory.
     */
    public static Sound getSound(String name) {
        if (!initialized) {
            Gdx.app.error("AssetManager", "Not initialized");
            return null;
        }

        try {
            String soundPath = SOUNDS_DIR + name + ".wav";
            
            if (!assetManager.isLoaded(soundPath, Sound.class)) {
                if (Gdx.files.internal(soundPath).exists()) {
                    assetManager.load(soundPath, Sound.class);
                    assetManager.finishLoadingAsset(soundPath);
                    Gdx.app.log("AssetManager", "Loaded sound: " + name);
                } else {
                    Gdx.app.error("AssetManager", "Sound not found: " + soundPath);
                    return null;
                }
            }

            return assetManager.get(soundPath, Sound.class);
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to load sound: " + name);
            return null;
        }
    }

    /**
     * Get background music by name from res/music/ directory.
     */
    public static Music getMusic(String name) {
        if (!initialized) {
            Gdx.app.error("AssetManager", "Not initialized");
            return null;
        }

        try {
            String musicPath = MUSIC_DIR + name + ".ogg";
            
            if (!assetManager.isLoaded(musicPath, Music.class)) {
                if (Gdx.files.internal(musicPath).exists()) {
                    assetManager.load(musicPath, Music.class);
                    assetManager.finishLoadingAsset(musicPath);
                    Gdx.app.log("AssetManager", "Loaded music: " + name);
                } else {
                    Gdx.app.error("AssetManager", "Music not found: " + musicPath);
                    return null;
                }
            }

            return assetManager.get(musicPath, Music.class);
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to load music: " + name);
            return null;
        }
    }

    /**
     * Create a white pixel texture for fallback/placeholder use.
     */
    private static Texture createWhitePixelTexture() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, 
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fillRectangle(0, 0, 1, 1);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
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
        try {
            assetManager.unload(name);
            Gdx.app.log("AssetManager", "Unloaded: " + name);
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Failed to unload asset: " + name);
        }
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
     * Call this during app shutdown (from LibGdxApp.dispose()).
     */
    public static void dispose() {
        if (!initialized) {
            return;
        }

        try {
            if (assetManager != null) {
                assetManager.dispose();
            }
            if (fontGenerator != null) {
                fontGenerator.dispose();
            }
            Gdx.app.log("AssetManager", "Disposed");
        } catch (Exception e) {
            Gdx.app.error("AssetManager", "Error during disposal: " + e.getMessage());
        }

        initialized = false;
    }

    /**
     * Get memory usage statistics.
     */
    public static String getMemoryStats() {
        if (!initialized) {
            return "Not initialized";
        }

        return "Loaded assets: " + assetManager.getAssetNames().size + 
               " | Memory: " + assetManager.getReferenceCount() + " references";
    }

    /**
     * Check if asset manager is initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
