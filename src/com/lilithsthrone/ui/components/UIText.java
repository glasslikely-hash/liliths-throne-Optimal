package com.lilithsthrone.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Text UI component for displaying static or dynamic text.
 * 
 * Features:
 * - Configurable font and color
 * - Text alignment (left, center, right)
 * - Word wrapping
 * - Dynamic text updates
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class UIText extends UIComponent {
	
	public enum TextAlignment {
		LEFT, CENTER, RIGHT
	}
	
	private String text;
	private BitmapFont font;
	private Color textColor = new Color(1f, 1f, 1f, 1f);
	private TextAlignment alignment = TextAlignment.LEFT;
	private boolean wordWrap = false;
	private float lineSpacing = 1.2f;
	
	/**
	 * Constructor
	 */
	public UIText(float x, float y, float width, float height, String text, BitmapFont font) {
		super(x, y, width, height);
		this.text = text;
		this.font = font;
	}
	
	@Override
	public void update(float delta) {
		// Text components don't need updates
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible || font == null || text == null) {
			return;
		}
		
		font.setColor(textColor);
		
		if (wordWrap && width > 0) {
			renderWrappedText(batch);
		} else {
			renderSingleLine(batch);
		}
	}
	
	@Override
	public void onInput(InputEvent event) {
		// Text components don't handle input
	}
	
	/**
	 * Render text as single line.
	 */
	private void renderSingleLine(SpriteBatch batch) {
		GlyphLayout layout = new GlyphLayout(font, text);
		
		float textX = x;
		switch (alignment) {
			case CENTER:
				textX = x + (width - layout.width) / 2;
				break;
			case RIGHT:
				textX = x + width - layout.width;
				break;
			case LEFT:
			default:
				textX = x;
				break;
		}
		
		float textY = y + height - font.getCapHeight();
		font.draw(batch, text, textX, textY);
	}
	
	/**
	 * Render text with word wrapping.
	 */
	private void renderWrappedText(SpriteBatch batch) {
		// Simple word wrapping implementation
		String[] words = text.split(" ");
		StringBuilder line = new StringBuilder();
		float currentY = y + height - font.getCapHeight();
		
		for (String word : words) {
			String testLine = line.length() == 0 ? word : line.toString() + " " + word;
			GlyphLayout layout = new GlyphLayout(font, testLine);
			
			if (layout.width > width && line.length() > 0) {
				// Draw current line and start new one
				drawLine(batch, line.toString(), currentY);
				currentY -= font.getCapHeight() * lineSpacing;
				line = new StringBuilder(word);
			} else {
				if (line.length() > 0) {
					line.append(" ");
				}
				line.append(word);
			}
		}
		
		// Draw remaining line
		if (line.length() > 0) {
			drawLine(batch, line.toString(), currentY);
		}
	}
	
	/**
	 * Draw single line of text with alignment.
	 */
	private void drawLine(SpriteBatch batch, String line, float y) {
		GlyphLayout layout = new GlyphLayout(font, line);
		
		float textX = x;
		switch (alignment) {
			case CENTER:
				textX = x + (width - layout.width) / 2;
				break;
			case RIGHT:
				textX = x + width - layout.width;
				break;
			case LEFT:
			default:
				textX = x;
				break;
		}
		
		font.draw(batch, line, textX, y);
	}
	
	/**
	 * Set text content.
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * Get text content.
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Set text color.
	 */
	public void setColor(Color color) {
		this.textColor = color;
	}
	
	/**
	 * Set text alignment.
	 */
	public void setAlignment(TextAlignment alignment) {
		this.alignment = alignment;
	}
	
	/**
	 * Enable/disable word wrapping.
	 */
	public void setWordWrap(boolean wrap) {
		this.wordWrap = wrap;
	}
	
	/**
	 * Set line spacing for wrapped text.
	 */
	public void setLineSpacing(float spacing) {
		this.lineSpacing = spacing;
	}
}
