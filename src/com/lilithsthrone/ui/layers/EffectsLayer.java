package com.lilithsthrone.ui.layers;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Effects Layer - Renders particles, animations, and visual effects.
 * 
 * Handles:
 * - Particle systems (combat effects, spells)
 * - Screen transitions (fade, wipe)
 * - Floating damage numbers
 * - Item drop animations
 * - Status effect indicators
 * - Screen shakes and camera effects
 * 
 * Replaces JavaFX-based effect rendering.
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class EffectsLayer extends UILayer {
	
	private static final String LAYER_NAME = "EffectsLayer";
	
	private List<ParticleEffect> activeEffects = new ArrayList<>();
	private ScreenTransition currentTransition;
	private List<FloatingText> floatingTexts = new ArrayList<>();
	
	private BitmapFont effectFont;
	
	public EffectsLayer(LogicLayerAPI logicApi) {
		super(logicApi);
		effectFont = new BitmapFont();
	}
	
	@Override
	public void update(float delta) {
		if (!visible) {
			return;
		}
		
		// Update all active particles
		for (int i = activeEffects.size() - 1; i >= 0; i--) {
			ParticleEffect effect = activeEffects.get(i);
			effect.update(delta);
			if (effect.isFinished()) {
				activeEffects.remove(i);
			}
		}
		
		// Update floating text
		for (int i = floatingTexts.size() - 1; i >= 0; i--) {
			FloatingText text = floatingTexts.get(i);
			text.update(delta);
			if (text.isFinished()) {
				floatingTexts.remove(i);
			}
		}
		
		// Update transition
		if (currentTransition != null) {
			currentTransition.update(delta);
			if (currentTransition.isFinished()) {
				currentTransition = null;
			}
		}
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible) {
			return;
		}
		
		// Render all particles
		for (ParticleEffect effect : activeEffects) {
			effect.render(batch);
		}
		
		// Render floating text
		for (FloatingText text : floatingTexts) {
			text.render(batch);
		}
		
		// Render transition overlay (should be on top)
		if (currentTransition != null) {
			currentTransition.render(batch);
		}
	}
	
	@Override
	public void onInput(InputEvent event) {
		// Effects layer typically doesn't handle input
		// unless we want to click through effects
	}
	
	/**
	 * Add a particle effect
	 */
	public void addParticles(String effectType, float x, float y) {
		// TODO: Create and add particle emitter based on effect type
		// For now, simple implementation
	}
	
	/**
	 * Show floating text effect (damage numbers, healing, etc)
	 */
	public void showFloatingText(String text, float x, float y, Color color, float duration) {
		FloatingText floatText = new FloatingText(text, x, y, color, duration, effectFont);
		floatingTexts.add(floatText);
	}
	
	/**
	 * Play screen transition
	 */
	public void playTransition(String transitionType, float duration) {
		currentTransition = new ScreenTransition(transitionType, duration);
	}
	
	/**
	 * Particle effect interface
	 */
	public interface ParticleEffect {
		void update(float delta);
		void render(SpriteBatch batch);
		boolean isFinished();
	}
	
	/**
	 * Floating text effect
	 */
	public static class FloatingText {
		private String text;
		private float x, y;
		private Color color;
		private float duration;
		private float elapsed = 0f;
		private BitmapFont font;
		
		public FloatingText(String text, float x, float y, Color color, float duration, BitmapFont font) {
			this.text = text;
			this.x = x;
			this.y = y;
			this.color = color;
			this.duration = duration;
			this.font = font;
		}
		
		public void update(float delta) {
			elapsed += delta;
			y += 20 * delta;  // Float upward
		}
		
		public void render(SpriteBatch batch) {
			// Fade out as time progresses
			float alpha = 1f - (elapsed / duration);
			Color drawColor = new Color(color.r, color.g, color.b, alpha);
			font.setColor(drawColor);
			font.draw(batch, text, x, y);
		}
		
		public boolean isFinished() {
			return elapsed >= duration;
		}
	}
	
	/**
	 * Screen transition effect
	 */
	public static class ScreenTransition {
		private String type;
		private float duration;
		private float elapsed = 0f;
		
		public ScreenTransition(String type, float duration) {
			this.type = type;
			this.duration = duration;
		}
		
		public void update(float delta) {
			elapsed += delta;
		}
		
		public void render(SpriteBatch batch) {
			// Render fade transition
			if ("fade".equals(type)) {
				float alpha = elapsed / duration;
				Color fadeColor = new Color(0f, 0f, 0f, alpha);
				com.lilithsthrone.ui.graphics.PixelDraw.drawRectangle(batch, 0, 0, 800, 600, fadeColor);
			}
			// TODO: Implement other transition types (wipe, slide, etc)
		}
		
		public boolean isFinished() {
			return elapsed >= duration;
		}
	}
	
	@Override
	public void dispose() {
		if (effectFont != null) {
			effectFont.dispose();
		}
	}
}
