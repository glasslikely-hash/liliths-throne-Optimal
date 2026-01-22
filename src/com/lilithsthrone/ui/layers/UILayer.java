package com.lilithsthrone.ui.layers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Base class for UI rendering layers.
 * 
 * The game renders in layers:
 * 1. MapLayer - World, terrain, characters
 * 2. HudLayer - Status bars, quick info
 * 3. MenuLayer - Open menus/panels
 * 4. EffectsLayer - Particles, animations
 * 5. DialogueLayer - NPC dialogue
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public abstract class UILayer {
	
	protected LogicLayerAPI logicApi;
	protected boolean visible = true;
	protected float alpha = 1.0f;
	
	/**
	 * Constructor
	 */
	public UILayer(LogicLayerAPI logicApi) {
		this.logicApi = logicApi;
	}
	
	/**
	 * Update layer logic
	 */
	public abstract void update(float delta);
	
	/**
	 * Render layer
	 */
	public abstract void render(SpriteBatch batch);
	
	/**
	 * Handle input
	 */
	public abstract void onInput(InputEvent event);
	
	/**
	 * Show layer
	 */
	public void show() {
		visible = true;
	}
	
	/**
	 * Hide layer
	 */
	public void hide() {
		visible = false;
	}
	
	/**
	 * Check if visible
	 */
	public boolean isVisible() {
		return visible;
	}
	
	/**
	 * Set visibility
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	/**
	 * Get alpha (transparency)
	 */
	public float getAlpha() {
		return alpha;
	}
	
	/**
	 * Set alpha (transparency)
	 */
	public void setAlpha(float alpha) {
		this.alpha = Math.max(0, Math.min(1, alpha));
	}
}
