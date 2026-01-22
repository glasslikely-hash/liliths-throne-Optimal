package com.lilithsthrone.ui.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Utility class for drawing primitive shapes using LibGDX SpriteBatch.
 * 
 * Provides simple rectangle and line drawing without requiring ShapeRenderer.
 * Uses a 1x1 white pixel texture for efficient drawing.
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class PixelDraw {
	
	private static Texture whitePixel;
	
	/**
	 * Initialize the white pixel texture (should be called once at startup)
	 */
	public static void initialize() {
		if (whitePixel == null) {
			Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
			pixmap.setColor(Color.WHITE);
			pixmap.fillRectangle(0, 0, 1, 1);
			whitePixel = new Texture(pixmap);
			pixmap.dispose();
		}
	}
	
	/**
	 * Draw a filled rectangle
	 */
	public static void drawRectangle(SpriteBatch batch, int x, int y, int width, int height, Color color) {
		if (whitePixel == null) {
			initialize();
		}
		
		Color oldColor = batch.getColor();
		batch.setColor(color);
		batch.draw(whitePixel, x, y, width, height);
		batch.setColor(oldColor);
	}
	
	/**
	 * Draw a rectangle outline
	 */
	public static void drawRectangleOutline(SpriteBatch batch, int x, int y, int width, int height, Color color, int thickness) {
		if (whitePixel == null) {
			initialize();
		}
		
		Color oldColor = batch.getColor();
		batch.setColor(color);
		
		// Top edge
		batch.draw(whitePixel, x, y + height - thickness, width, thickness);
		
		// Bottom edge
		batch.draw(whitePixel, x, y, width, thickness);
		
		// Left edge
		batch.draw(whitePixel, x, y, thickness, height);
		
		// Right edge
		batch.draw(whitePixel, x + width - thickness, y, thickness, height);
		
		batch.setColor(oldColor);
	}
	
	/**
	 * Draw a horizontal line
	 */
	public static void drawLineHorizontal(SpriteBatch batch, int x, int y, int width, Color color, int thickness) {
		if (whitePixel == null) {
			initialize();
		}
		
		Color oldColor = batch.getColor();
		batch.setColor(color);
		batch.draw(whitePixel, x, y, width, thickness);
		batch.setColor(oldColor);
	}
	
	/**
	 * Draw a vertical line
	 */
	public static void drawLineVertical(SpriteBatch batch, int x, int y, int height, Color color, int thickness) {
		if (whitePixel == null) {
			initialize();
		}
		
		Color oldColor = batch.getColor();
		batch.setColor(color);
		batch.draw(whitePixel, x, y, thickness, height);
		batch.setColor(oldColor);
	}
	
	/**
	 * Dispose resources
	 */
	public static void dispose() {
		if (whitePixel != null) {
			whitePixel.dispose();
			whitePixel = null;
		}
	}
}
