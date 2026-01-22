package com.lilithsthrone.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * TextScreenRenderer: Converts HTML from RenderingEngine into LibGDX text rendering.
 * 
 * This class:
 * 1. Parses HTML content from RenderingEngine
 * 2. Extracts text, colors, and interactive element positions
 * 3. Renders text using BitmapFont
 * 4. Tracks clickable regions for detecting user input
 * 
 * The game's RenderingEngine generates HTML strings containing:
 * - Narrative text with color markup
 * - Status bars with progress indicators
 * - Clickable dialogue choices
 * - Navigation buttons
 * - Inventory items
 * 
 * TextScreenRenderer converts this HTML into simple text rendered with LibGDX's
 * BitmapFont, maintaining visual similarity to the original JavaFX/WebView output.
 * 
 * @version 1.0.0
 * @author Step 3 UI Refactoring
 */
public class TextScreenRenderer {

	// Screen dimensions
	private int screenWidth = 1024;
	private int screenHeight = 768;
	
	// Rendering
	private BitmapFont font;
	private ShapeRenderer shapeRenderer;
	
	// Content storage
	private List<ScreenElement> elements;
	private List<ClickableRegion> clickableRegions;
	private Map<String, Color> colorMap;
	
	// Current state
	private String currentHTML;
	private int hoveredElementIndex = -1;
	
	// Layout constants
	private static final float HEADER_HEIGHT = 60;
	private static final float STATUS_BAR_HEIGHT = 100;
	private static final float FOOTER_HEIGHT = 40;
	private static final float CONTENT_START_Y = HEADER_HEIGHT + STATUS_BAR_HEIGHT;
	private float CONTENT_END_Y;  // Calculated based on screenHeight in constructor
	
	private static final int TITLE_FONT_SIZE = 24;
	private static final int BODY_FONT_SIZE = 14;
	private static final int LABEL_FONT_SIZE = 11;
	
	private static final float LINE_HEIGHT = 16;
	private static final float PADDING = 10;
	
	// Background colors
	private static final Color BACKGROUND_DARK = new Color(0.13f, 0.13f, 0.13f, 1f);  // #222222
	private static final Color BACKGROUND = new Color(0.2f, 0.2f, 0.2f, 1f);           // #333333
	private static final Color TEXT_COLOR = new Color(0.8f, 0.8f, 0.8f, 1f);           // #cccccc
	private static final Color HOVER_COLOR = new Color(1f, 1f, 0f, 1f);               // #ffff00 (gold)
	
	/**
	 * Constructor
	 */
	public TextScreenRenderer() {
		this.elements = new ArrayList<>();
		this.clickableRegions = new ArrayList<>();
		this.colorMap = new HashMap<>();
		this.CONTENT_END_Y = screenHeight - FOOTER_HEIGHT;
		initializeColorMap();
	}
	
	/**
	 * Initialize color mapping from hex to LibGDX Color
	 * These colors match PresetColour values used in RenderingEngine
	 */
	private void initializeColorMap() {
		// Status bar colors
		colorMap.put("cc0000", Color.RED);                    // Health - red
		colorMap.put("ff0000", Color.RED);                    // Explicit red
		colorMap.put("0099ff", Color.BLUE);                   // Mana - blue
		colorMap.put("00ff00", Color.GREEN);                  // Stamina - green
		colorMap.put("ff00ff", Color.MAGENTA);                // Arousal - magenta
		colorMap.put("ffff00", Color.YELLOW);                 // Gold/Corruption - yellow
		colorMap.put("cccccc", TEXT_COLOR);                   // Default text
		colorMap.put("ffffff", Color.WHITE);                  // White text
		colorMap.put("222222", BACKGROUND_DARK);              // Dark background
		colorMap.put("333333", BACKGROUND);                   // Standard background
		
		// Ensure lowercase matching
		Map<String, Color> lowerCaseMap = new HashMap<>();
		for (Map.Entry<String, Color> entry : colorMap.entrySet()) {
			lowerCaseMap.put(entry.getKey().toLowerCase(), entry.getValue());
		}
		colorMap = lowerCaseMap;
	}
	
	/**
	 * Parse HTML content from RenderingEngine and extract display information
	 * 
	 * @param htmlContent HTML string from RenderingEngine.getMainPanel() etc.
	 */
	public void parseHTML(String htmlContent) {
		this.currentHTML = htmlContent;
		elements.clear();
		clickableRegions.clear();
		
		// Remove HTML tags
		String plainText = htmlContent.replaceAll("<[^>]*>", "");
		
		// Split into lines
		String[] lines = plainText.split("\n");
		
		float currentY = CONTENT_START_Y;
		
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty()) {
				currentY += LINE_HEIGHT / 2;  // Half line for spacing
				continue;
			}
			
			// Extract color from HTML for this line
			Color color = extractColorFromHTML(htmlContent, line);
			if (color == null) {
				color = TEXT_COLOR;
			}
			
			// Create screen element
			ScreenElement element = new ScreenElement();
			element.text = line;
			element.x = PADDING;
			element.y = currentY;
			element.color = color;
			element.type = ElementType.TEXT;
			element.width = line.length() * 8;  // Rough estimate for monospace
			element.height = LINE_HEIGHT;
			
			elements.add(element);
			currentY += LINE_HEIGHT;
			
			if (currentY > CONTENT_END_Y) {
				break;  // Stop rendering if we exceed screen height
			}
		}
		
		// Extract clickable elements (buttons, choices, etc.)
		extractClickableElements(htmlContent);
		
		// Extract status bar information
		extractStatusBars(htmlContent);
	}
	
	/**
	 * Extract color from HTML style attributes
	 * Looks for patterns like: style="color: #ff0000" or style="color: #ff0000ff"
	 */
	private Color extractColorFromHTML(String html, String lineText) {
		// Look backwards in HTML to find the closest style attribute before this text
		int textPos = html.indexOf(lineText);
		if (textPos < 0) {
			return null;
		}
		
		// Search backwards from text position for a style attribute
		int searchStart = Math.max(0, textPos - 500);  // Look back 500 chars
		String context = html.substring(searchStart, textPos);
		
		// Match pattern: color:\s*#[0-9a-f]+
		Pattern colorPattern = Pattern.compile("color:\\s*#([0-9a-f]+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = colorPattern.matcher(context);
		
		Color color = null;
		while (matcher.find()) {
			String hex = matcher.group(1).toLowerCase();
			// Take only last 6 digits if it's longer (RRGGBB)
			if (hex.length() > 6) {
				hex = hex.substring(hex.length() - 6);
			}
			color = colorMap.getOrDefault(hex, null);
		}
		
		return color;
	}
	
	/**
	 * Extract clickable button regions from HTML
	 * Looks for button tags with IDs or onclick attributes
	 */
	private void extractClickableElements(String html) {
		// Pattern: <button...id='([^']*)'...>([^<]*)</button>
		Pattern buttonPattern = Pattern.compile(
			"<button[^>]*id=['\"]([^'\"]*)['\"][^>]*>([^<]*)</button>",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL
		);
		
		Matcher matcher = buttonPattern.matcher(html);
		int choiceIndex = 0;
		
		while (matcher.find()) {
			String id = matcher.group(1);
			String text = matcher.group(2).trim();
			
			// Find this text in our elements list to get position
			for (ScreenElement elem : elements) {
				if (elem.text.contains(text)) {
					ClickableRegion region = new ClickableRegion();
					region.id = id;
					region.x = elem.x;
					region.y = elem.y;
					region.width = elem.width;
					region.height = elem.height;
					region.dataIndex = choiceIndex;
					region.text = text;
					
					clickableRegions.add(region);
					choiceIndex++;
					break;
				}
			}
		}
	}
	
	/**
	 * Extract and store status bar information from HTML
	 * Status bars have special rendering (filled rectangles)
	 */
	private void extractStatusBars(String html) {
		// Look for patterns like: Health: 80/100
		// These are followed by visual progress bars in the original
		
		Pattern statusPattern = Pattern.compile(
			"(Health|Mana|Stamina|Arousal|Corruption):\\s*(\\d+)/(\\d+)",
			Pattern.CASE_INSENSITIVE
		);
		
		Matcher matcher = statusPattern.matcher(html);
		float barY = HEADER_HEIGHT + 10;
		int barIndex = 0;
		
		while (matcher.find() && barIndex < 5) {
			String statusName = matcher.group(1);
			int current = Integer.parseInt(matcher.group(2));
			int max = Integer.parseInt(matcher.group(3));
			
			ScreenElement barElement = new ScreenElement();
			barElement.text = statusName + ": " + current + "/" + max;
			barElement.x = PADDING;
			barElement.y = barY;
			barElement.type = ElementType.STATUS_BAR;
			barElement.color = getStatusBarColor(statusName);
			barElement.currentValue = current;
			barElement.maxValue = max;
			barElement.width = 200;  // Bar width
			barElement.height = 16;
			
			elements.add(barElement);
			barY += 20;
			barIndex++;
		}
	}
	
	/**
	 * Get color for a status bar based on type
	 */
	private Color getStatusBarColor(String statusName) {
		switch (statusName.toLowerCase()) {
			case "health": return Color.RED;
			case "mana": return Color.BLUE;
			case "stamina": return Color.GREEN;
			case "arousal": return Color.MAGENTA;
			case "corruption": return Color.YELLOW;
			default: return TEXT_COLOR;
		}
	}
	
	/**
	 * Render all content to the screen
	 */
	public void render(SpriteBatch batch, BitmapFont font, ShapeRenderer shapeRenderer) {
		if (batch == null || font == null) {
			return;
		}
		
		// Draw background
		batch.begin();
		drawBackground(batch);
		batch.end();
		
		// Draw shapes (status bars, borders)
		if (shapeRenderer != null) {
			shapeRenderer.begin(ShapeType.Filled);
			drawStatusBars(shapeRenderer);
			shapeRenderer.end();
		}
		
		// Draw text
		batch.begin();
		drawText(batch, font);
		batch.end();
	}
	
	/**
	 * Draw background rectangles
	 */
	private void drawBackground(SpriteBatch batch) {
		// This would typically draw using a simple colored rectangle
		// For now, just set clear color in LibGdxApp
	}
	
	/**
	 * Draw status bar rectangles with fills
	 */
	private void drawStatusBars(ShapeRenderer shapeRenderer) {
		for (ScreenElement elem : elements) {
			if (elem.type != ElementType.STATUS_BAR) {
				continue;
			}
			
			float fillPercent = elem.currentValue / (float) elem.maxValue;
			float fillWidth = elem.width * fillPercent;
			
			// Draw background
			shapeRenderer.setColor(BACKGROUND);
			shapeRenderer.rect(elem.x, elem.y, elem.width, elem.height);
			
			// Draw fill
			shapeRenderer.setColor(elem.color);
			shapeRenderer.rect(elem.x, elem.y, fillWidth, elem.height);
		}
	}
	
	/**
	 * Draw all text elements
	 */
	private void drawText(SpriteBatch batch, BitmapFont font) {
		font.setColor(TEXT_COLOR);
		
		for (int i = 0; i < elements.size(); i++) {
			ScreenElement elem = elements.get(i);
			
			if (elem.type == ElementType.STATUS_BAR) {
				// Status bars drawn separately
				continue;
			}
			
			// Set color for this element
			if (i == hoveredElementIndex) {
				font.setColor(HOVER_COLOR);
			} else {
				font.setColor(elem.color != null ? elem.color : TEXT_COLOR);
			}
			
			// Draw text
			font.draw(batch, elem.text, elem.x, elem.y + elem.height);
		}
		
		// Reset color
		font.setColor(Color.WHITE);
	}
	
	/**
	 * Handle mouse movement for hover detection
	 */
	public void setHoveredElement(float x, float y) {
		hoveredElementIndex = -1;
		
		for (int i = 0; i < elements.size(); i++) {
			ScreenElement elem = elements.get(i);
			
			if (elem.x <= x && x <= elem.x + elem.width &&
				elem.y <= y && y <= elem.y + elem.height) {
				hoveredElementIndex = i;
				break;
			}
		}
	}
	
	/**
	 * Get the clicked element at position (x, y)
	 * Returns the clickable region if a button/choice was clicked, null otherwise
	 */
	public ClickableRegion getClickedElement(float x, float y) {
		for (ClickableRegion region : clickableRegions) {
			if (region.x <= x && x <= region.x + region.width &&
				region.y <= y && y <= region.y + region.height) {
				return region;
			}
		}
		return null;
	}
	
	/**
	 * Set screen dimensions (for resize handling)
	 */
	public void setScreenSize(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;
	}
	
	/**
	 * Get current HTML content
	 */
	public String getCurrentHTML() {
		return currentHTML;
	}
	
	/**
	 * Get all rendered elements (for debugging)
	 */
	public List<ScreenElement> getElements() {
		return elements;
	}
	
	/**
	 * Get all clickable regions (for debugging)
	 */
	public List<ClickableRegion> getClickableRegions() {
		return clickableRegions;
	}
	
	/**
	 * Inner class representing a rendered element on screen
	 */
	public static class ScreenElement {
		public String text;
		public float x, y;
		public float width, height;
		public Color color;
		public ElementType type;
		public int currentValue;
		public int maxValue;
	}
	
	/**
	 * Inner class representing a clickable region on screen
	 */
	public static class ClickableRegion {
		public String id;
		public String text;
		public float x, y;
		public float width, height;
		public int dataIndex;  // Index into responses/choices
	}
	
	/**
	 * Element type enumeration
	 */
	public enum ElementType {
		TEXT,
		BUTTON,
		STATUS_BAR,
		LABEL,
		CONTAINER
	}
}
