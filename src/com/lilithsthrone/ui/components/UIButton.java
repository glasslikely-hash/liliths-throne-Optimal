package com.lilithsthrone.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.lilithsthrone.ui.input.InputEvent;
import com.lilithsthrone.utils.logging.LogManager;

/**
 * Button UI component with label and click handling.
 * 
 * Features:
 * - Rectangular clickable area
 * - Text label with automatic centering
 * - Hover and pressed states
 * - Callback on click
 * - Color customization
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class UIButton extends UIComponent {
	
	private String label;
	private BitmapFont font;
	private Runnable onClickCallback;
	
	// Visual properties
	private Color normalColor = new Color(0.3f, 0.3f, 0.3f, 1f);
	private Color hoverColor = new Color(0.4f, 0.4f, 0.4f, 1f);
	private Color pressedColor = new Color(0.2f, 0.2f, 0.2f, 1f);
	private Color textColor = new Color(1f, 1f, 1f, 1f);
	private Color borderColor = new Color(0.5f, 0.5f, 0.5f, 1f);
	
	private boolean hovered = false;
	private boolean pressed = false;
	
	/**
	 * Constructor
	 */
	public UIButton(float x, float y, float width, float height, String label, BitmapFont font) {
		super(x, y, width, height);
		this.label = label;
		this.font = font;
	}
	
	@Override
	public void update(float delta) {
		// Button updates are handled by input
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible || !enabled) {
			return;
		}
		
		// Draw button background
		Color bgColor = pressed ? pressedColor : (hovered ? hoverColor : normalColor);
		drawRectangle(batch, x, y, width, height, bgColor);
		
		// Draw border
		drawRectangleOutline(batch, x, y, width, height, 2, borderColor);
		
		// Draw text centered
		if (font != null && label != null) {
			GlyphLayout layout = new GlyphLayout(font, label);
			float textX = x + (width - layout.width) / 2;
			float textY = y + (height + layout.height) / 2;
			
			font.setColor(textColor);
			font.draw(batch, label, textX, textY);
		}
	}
	
	@Override
	public void onInput(InputEvent event) {
		if (!enabled) {
			return;
		}
		
		Rectangle bounds = new Rectangle(x, y, width, height);
		boolean containsPoint = bounds.contains(event.getScreenX(), event.getScreenY());
		
		if (event.getType() == InputEvent.InputType.MOUSE_MOVE) {
			hovered = containsPoint;
		} else if (event.getType() == InputEvent.InputType.MOUSE_CLICK) {
			if (containsPoint && onClickCallback != null) {
				onClickCallback.run();
				event.consume();
			}
		}
	}
	
	/**
	 * Set callback function when button is clicked.
	 */
	public UIButton onClick(Runnable callback) {
		this.onClickCallback = callback;
		return this;
	}
	
	/**
	 * Set button label.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Set normal state color.
	 */
	public void setNormalColor(Color color) {
		this.normalColor = color;
	}
	
	/**
	 * Set hover state color.
	 */
	public void setHoverColor(Color color) {
		this.hoverColor = color;
	}
	
	/**
	 * Set pressed state color.
	 */
	public void setPressedColor(Color color) {
		this.pressedColor = color;
	}
	
	/**
	 * Set text color.
	 */
	public void setTextColor(Color color) {
		this.textColor = color;
	}
	
	/**
	 * Draw filled rectangle.
	 */
	private void drawRectangle(SpriteBatch batch, float x, float y, float width, float height, Color color) {
		batch.setColor(color);
		batch.draw(batch.getBlendingDisabledTexture(), x, y, width, height);
	}
	
	/**
	 * Draw rectangle outline.
	 */
	private void drawRectangleOutline(SpriteBatch batch, float x, float y, float width, float height, float thickness, Color color) {
		batch.setColor(color);
		// Top
		batch.draw(batch.getBlendingDisabledTexture(), x, y + height - thickness, width, thickness);
		// Bottom
		batch.draw(batch.getBlendingDisabledTexture(), x, y, width, thickness);
		// Left
		batch.draw(batch.getBlendingDisabledTexture(), x, y, thickness, height);
		// Right
		batch.draw(batch.getBlendingDisabledTexture(), x + width - thickness, y, thickness, height);
	}
}
