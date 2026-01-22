package com.lilithsthrone.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;

/**
 * Manages screen transitions and active screen.
 *
 * Responsibilities:
 *  - Track current active screen
 *  - Handle screen transitions (dispose old, show new)
 *  - Update and render current screen
 *  - Manage lifecycle events (pause/resume/resize)
 *
 * Usage:
 *  screenManager.setScreen(ScreenManager.ScreenType.GAME);
 *  // Or with transition:
 *  screenManager.setScreenWithTransition(ScreenType.INVENTORY, Transition.FADE_IN);
 */
public class ScreenManager {

    /**
     * Available screen types in the game.
     */
    public enum ScreenType {
        MAIN_MENU,
        NEW_GAME_MENU,
        LOAD_GAME_MENU,
        SETTINGS_MENU,
        GAME,
        INVENTORY,
        CHARACTER,
        MAP,
        PAUSE_MENU,
        SAVE_GAME_MENU
    }

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private LogicLayerAPI logicLayerAPI;

    private BaseScreen currentScreen;
    private BaseScreen pendingScreen;
    private ScreenType currentScreenType;

    // Transition effect (null = instant)
    private Transition transition;
    private float transitionTime;
    private float transitionDuration;

    /**
     * Constructor for screen manager.
     */
    public ScreenManager(SpriteBatch batch, OrthographicCamera camera, LogicLayerAPI logicLayerAPI) {
        this.batch = batch;
        this.camera = camera;
        this.logicLayerAPI = logicLayerAPI;
        this.currentScreen = null;
        this.pendingScreen = null;
        this.transition = null;
    }

    /**
     * Set the active screen immediately (no transition).
     */
    public void setScreen(ScreenType screenType) {
        setScreenWithTransition(screenType, null);
    }

    /**
     * Set the active screen with a transition effect.
     */
    public void setScreenWithTransition(ScreenType screenType, Transition transition) {
        if (this.currentScreenType == screenType) {
            return; // Already on this screen
        }

        // Dispose old screen
        if (currentScreen != null) {
            currentScreen.hide();
            currentScreen.dispose();
        }

        // Create new screen based on type
        this.pendingScreen = createScreen(screenType);
        this.transition = transition;
        this.transitionTime = 0f;
        this.transitionDuration = transition != null ? transition.getDuration() : 0f;
        this.currentScreenType = screenType;
    }

    /**
     * Update current screen.
     * Handles transition logic and delegates to screen.
     */
    public void update(float delta) {
        // Handle screen transition
        if (transition != null) {
            transitionTime += delta;
            if (transitionTime >= transitionDuration) {
                // Transition complete
                currentScreen = pendingScreen;
                currentScreen.show();
                pendingScreen = null;
                transition = null;
            }
        }

        // Update current screen
        if (currentScreen != null) {
            currentScreen.update(delta);
        }
    }

    /**
     * Render current screen.
     * Batch is already begun by LibGdxApp.
     */
    public void render(SpriteBatch batch) {
        if (currentScreen != null) {
            currentScreen.render(batch);
        }

        // Render transition effect overlay (if active)
        if (transition != null && transitionDuration > 0) {
            transition.render(batch, transitionTime / transitionDuration);
        }
    }

    /**
     * Handle screen resize.
     */
    public void resize(int width, int height) {
        if (currentScreen != null) {
            currentScreen.resize(width, height);
        }
        if (pendingScreen != null) {
            pendingScreen.resize(width, height);
        }
    }

    /**
     * Handle app pause.
     */
    public void pause() {
        if (currentScreen != null) {
            currentScreen.hide();
        }
    }

    /**
     * Handle app resume.
     */
    public void resume() {
        if (currentScreen != null) {
            currentScreen.show();
        }
    }

    /**
     * Dispose all resources.
     */
    public void dispose() {
        if (currentScreen != null) {
            currentScreen.dispose();
        }
        if (pendingScreen != null) {
            pendingScreen.dispose();
        }
    }

    /**
     * Create a screen instance based on type.
     * Subclasses of BaseScreen are instantiated here.
     */
    private BaseScreen createScreen(ScreenType screenType) {
        switch (screenType) {
            case MAIN_MENU:
                return new MainMenuScreen(batch, camera, logicLayerAPI, this);
            case NEW_GAME_MENU:
                return new NewGameMenuScreen(batch, camera, logicLayerAPI, this);
            case LOAD_GAME_MENU:
                return new LoadGameMenuScreen(batch, camera, logicLayerAPI, this);
            case SETTINGS_MENU:
                return new SettingsMenuScreen(batch, camera, logicLayerAPI, this);
            case GAME:
                return new GameScreen(batch, camera, logicLayerAPI, this);
            case INVENTORY:
                return new InventoryScreen(batch, camera, logicLayerAPI, this);
            case CHARACTER:
                return new CharacterScreen(batch, camera, logicLayerAPI, this);
            case MAP:
                return new MapScreen(batch, camera, logicLayerAPI, this);
            case PAUSE_MENU:
                return new PauseMenuScreen(batch, camera, logicLayerAPI, this);
            case SAVE_GAME_MENU:
                return new SaveGameMenuScreen(batch, camera, logicLayerAPI, this);
            default:
                throw new IllegalArgumentException("Unknown screen type: " + screenType);
        }
    }

    // ================== Getters ==================

    public BaseScreen getCurrentScreen() {
        return currentScreen;
    }

    public ScreenType getCurrentScreenType() {
        return currentScreenType;
    }

    public boolean isTransitioning() {
        return transition != null;
    }

    public float getTransitionProgress() {
        if (transition == null) {
            return 0f;
        }
        return transitionTime / transitionDuration;
    }
}
