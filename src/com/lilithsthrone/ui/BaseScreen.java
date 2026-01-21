package com.lilithsthrone.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.input.InputManager;

/**
 * Base class for all screen types (main menu, game world, inventory, etc).
 *
 * Responsibilities:
 *  - Update game/menu state each frame
 *  - Render current screen
 *  - Handle input for this screen
 *  - Manage lifecycle (show/hide/dispose)
 *
 * Subclasses:
 *  - MainMenuScreen (main menu UI)
 *  - GameScreen (gameplay world rendering)
 *  - InventoryScreen (inventory UI)
 *  - CharacterScreen (character stats)
 *  - SettingsScreen (settings/options)
 *  - PauseMenuScreen (pause overlay)
 */
public abstract class BaseScreen {

    protected SpriteBatch batch;
    protected OrthographicCamera camera;
    protected LogicLayerAPI logicLayerAPI;
    protected InputManager inputManager;

    /**
     * Constructor for screen.
     */
    public BaseScreen(SpriteBatch batch, OrthographicCamera camera, LogicLayerAPI logicLayerAPI) {
        this.batch = batch;
        this.camera = camera;
        this.logicLayerAPI = logicLayerAPI;
        this.inputManager = new InputManager();
    }

    /**
     * Called when screen becomes active.
     */
    public abstract void show();

    /**
     * Called when screen is hidden.
     */
    public abstract void hide();

    /**
     * Update screen logic.
     * Called once per frame BEFORE render().
     *
     * @param delta Time since last frame in seconds
     */
    public abstract void update(float delta);

    /**
     * Render screen.
     * Called once per frame AFTER update().
     * Batch is already begun, camera is ready.
     *
     * @param batch Sprite batch for rendering
     */
    public abstract void render(SpriteBatch batch);

    /**
     * Handle screen resize.
     *
     * @param width New width in pixels
     * @param height New height in pixels
     */
    public abstract void resize(int width, int height);

    /**
     * Clean up resources when screen is disposed.
     */
    public abstract void dispose();

    /**
     * Get the input manager for this screen.
     */
    public InputManager getInputManager() {
        return inputManager;
    }
}
