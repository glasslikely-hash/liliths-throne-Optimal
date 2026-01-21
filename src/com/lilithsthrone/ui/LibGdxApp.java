package com.lilithsthrone.ui;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.platform.PlatformConfig;
import com.lilithsthrone.ui.platform.LayoutManager;
import com.lilithsthrone.ui.assets.AssetManager;

/**
 * LibGDX Application entry point. Replaces old JavaFX/WebView rendering.
 *
 * Responsibilities:
 *  - Initialize LibGDX rendering pipeline
 *  - Manage main game loop (update → render)
 *  - Handle screen transitions
 *  - Manage resource lifecycle
 *  - Detect platform (desktop/mobile) and apply settings
 *
 * Architecture:
 *  LibGdxApp (main loop) → GameScreen (game state rendering)
 *                      → ScreenManager (screen transitions)
 *
 * Data Flow:
 *  create() → show() → render(delta) → [update + render] → dispose()
 */
public class LibGdxApp implements ApplicationListener {

    // Core rendering
    private SpriteBatch batch;
    private OrthographicCamera camera;

    // Screen management
    private ScreenManager screenManager;

    // Platform configuration
    private static final float VIRTUAL_WIDTH = 1200f;
    private static final float VIRTUAL_HEIGHT = 800f;

    // Logic layer API (query-only for UI)
    private LogicLayerAPI logicLayerAPI;

    // Timing
    private float deltaTime;
    private long frameCounter;

    @Override
    public void create() {
        // Initialize platform configuration FIRST
        PlatformConfig.detectPlatform();
        Gdx.app.log("LibGdxApp", "Platform detected: " + 
            (PlatformConfig.IS_MOBILE ? "MOBILE" : "DESKTOP"));

        // Initialize rendering pipeline
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        updateCamera();

        // Set up asset manager
        AssetManager.initialize(Gdx.files);

        // Create logic layer API
        try {
            this.logicLayerAPI = new LogicLayerAPI();
            Gdx.app.log("LibGdxApp", "LogicLayerAPI initialized successfully");
        } catch (Exception e) {
            Gdx.app.error("LibGdxApp", "Failed to initialize LogicLayerAPI: " + e.getMessage());
            throw new RuntimeException("Cannot start game without logic layer", e);
        }

        // Initialize screen manager
        this.screenManager = new ScreenManager(this.batch, this.camera, this.logicLayerAPI);

        // Load main menu screen
        screenManager.setScreen(ScreenManager.ScreenType.MAIN_MENU);

        this.frameCounter = 0;
        Gdx.app.log("LibGdxApp", "Initialization complete");
    }

    @Override
    public void render() {
        // Calculate delta time (capped at 1/30 FPS to prevent large jumps)
        this.deltaTime = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        this.frameCounter++;

        // Clear screen with black
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Update camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Update current screen
        screenManager.update(deltaTime);

        // Render current screen
        batch.begin();
        screenManager.render(batch);
        batch.end();

        // Handle debug keys
        handleDebugInput();
    }

    @Override
    public void resize(int width, int height) {
        // Update camera when window resizes
        updateCamera();
        LayoutManager.updateScreenSize(width, height);
        screenManager.resize(width, height);
        Gdx.app.log("LibGdxApp", "Resized to: " + width + "x" + height);
    }

    @Override
    public void pause() {
        screenManager.pause();
        Gdx.app.log("LibGdxApp", "Application paused");
    }

    @Override
    public void resume() {
        screenManager.resume();
        Gdx.app.log("LibGdxApp", "Application resumed");
    }

    @Override
    public void dispose() {
        Gdx.app.log("LibGdxApp", "Disposing application resources");

        // Shutdown logic layer (triggers save if needed)
        if (logicLayerAPI != null) {
            try {
                logicLayerAPI.shutdown();
            } catch (Exception e) {
                Gdx.app.error("LibGdxApp", "Error shutting down logic layer: " + e.getMessage());
            }
        }

        // Dispose screens
        if (screenManager != null) {
            screenManager.dispose();
        }

        // Dispose asset manager
        AssetManager.dispose();

        // Dispose rendering resources
        if (batch != null) {
            batch.dispose();
        }

        Gdx.app.log("LibGdxApp", "Disposal complete");
    }

    /**
     * Update camera projection based on screen size and virtual viewport.
     * Maintains 1200x800 virtual resolution with pillarboxing on desktop.
     */
    private void updateCamera() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Calculate aspect ratios
        float screenAspect = (float) screenWidth / screenHeight;
        float virtualAspect = VIRTUAL_WIDTH / VIRTUAL_HEIGHT;

        float newViewportWidth = VIRTUAL_WIDTH;
        float newViewportHeight = VIRTUAL_HEIGHT;

        // Adjust viewport to maintain virtual resolution
        if (screenAspect > virtualAspect) {
            // Screen is wider - use pillarboxing
            newViewportWidth = VIRTUAL_HEIGHT * screenAspect;
        } else if (screenAspect < virtualAspect) {
            // Screen is taller - use letterboxing
            newViewportHeight = VIRTUAL_WIDTH / screenAspect;
        }

        camera.setToOrtho(false, newViewportWidth, newViewportHeight);
        camera.position.set(newViewportWidth / 2f, newViewportHeight / 2f, 0f);

        LayoutManager.setVirtualViewport(newViewportWidth, newViewportHeight);
    }

    /**
     * Handle debug input keys (F-keys, etc).
     * Remove in production.
     */
    private void handleDebugInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F10)) {
            // F10: Quick save
            try {
                logicLayerAPI.saveGame("quicksave");
                Gdx.app.log("LibGdxApp", "Quick save successful");
            } catch (Exception e) {
                Gdx.app.error("LibGdxApp", "Quick save failed: " + e.getMessage());
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            // F11: Quick load
            try {
                logicLayerAPI.loadGame("quicksave");
                Gdx.app.log("LibGdxApp", "Quick load successful");
            } catch (Exception e) {
                Gdx.app.error("LibGdxApp", "Quick load failed: " + e.getMessage());
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            // F12: Toggle fullscreen (desktop only)
            if (!PlatformConfig.IS_MOBILE) {
                boolean isFullscreen = Gdx.graphics.isFullscreen();
                Gdx.graphics.setFullscreenMode(
                    isFullscreen ? Gdx.graphics.getDisplayMode() : null
                );
            }
        }
    }

    // ================== Public API ==================

    /**
     * Get the current delta time (seconds since last frame).
     */
    public float getDeltaTime() {
        return deltaTime;
    }

    /**
     * Get the sprite batch for rendering.
     */
    public SpriteBatch getBatch() {
        return batch;
    }

    /**
     * Get the orthographic camera.
     */
    public OrthographicCamera getCamera() {
        return camera;
    }

    /**
     * Get the logic layer API (query-only interface).
     */
    public LogicLayerAPI getLogicLayerAPI() {
        return logicLayerAPI;
    }

    /**
     * Get the screen manager.
     */
    public ScreenManager getScreenManager() {
        return screenManager;
    }

    /**
     * Get current frame number.
     */
    public long getFrameCounter() {
        return frameCounter;
    }

    /**
     * Get virtual viewport width.
     */
    public static float getVirtualWidth() {
        return VIRTUAL_WIDTH;
    }

    /**
     * Get virtual viewport height.
     */
    public static float getVirtualHeight() {
        return VIRTUAL_HEIGHT;
    }
}
