package com.lilithsthrone.ui.controllers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.input.InputManager;
import com.lilithsthrone.ui.utils.ColorCache;
import java.util.ArrayList;
import java.util.List;

/**
 * Event Log Controller
 * 
 * Displays history of game events and dialogue.
 * 
 * Features:
 *  - Recent dialogue exchanges
 *  - Combat log entries
 *  - Quest updates
 *  - System messages
 *  - Item pickups
 *  - Event timestamps
 *  - Log filtering (show all, combat only, dialogue only)
 * 
 * @since Step 3
 * @version 1.0
 */
public class EventLogController extends UIControllerBase {

	private static final String CONTROLLER_NAME = "EventLogController";

	// UI elements
	private ShapeRenderer shapeRenderer;
	private BitmapFont font;
	private BitmapFont fontSmall;

	// Log display
	private float logPanelX, logPanelY;
	private static final float LOG_PANEL_WIDTH = 600f;
	private static final float LOG_PANEL_HEIGHT = 400f;
	private static final float LOG_ENTRY_HEIGHT = 25f;
	private static final int MAX_VISIBLE_ENTRIES = 15;

	// Event log storage
	private List<LogEntry> eventLog = new ArrayList<>();
	private static final int MAX_LOG_ENTRIES = 100;
	private int logScrollOffset = 0;

	// Filter
	private enum LogFilter {
		ALL,
		DIALOGUE,
		COMBAT,
		QUEST,
		ITEMS
	}

	private LogFilter currentFilter = LogFilter.ALL;
	private float filterButtonStartX, filterButtonStartY;
	private static final float FILTER_BUTTON_WIDTH = 80f;
	private static final float FILTER_BUTTON_HEIGHT = 25f;

	// Inner class for log entry
	private static class LogEntry {
		String message;
		float timestamp;
		Color color;
		LogEntryType type;

		LogEntry(String message, Color color, LogEntryType type) {
			this.message = message;
			this.color = color;
			this.type = type;
			this.timestamp = System.currentTimeMillis() / 1000f;
		}
	}

	private enum LogEntryType {
		DIALOGUE,
		COMBAT,
		QUEST,
		ITEM,
		SYSTEM
	}

	/**
	 * Constructor for event log controller
	 */
	public EventLogController(SpriteBatch batch, OrthographicCamera camera,
	                          LogicLayerAPI logicLayerAPI, InputManager inputManager) {
		super(batch, camera, logicLayerAPI, inputManager);
		this.shapeRenderer = new ShapeRenderer();
		this.font = new BitmapFont();
		this.fontSmall = new BitmapFont();
	}

	@Override
	public void initialize() {
		System.out.println("[" + CONTROLLER_NAME + "] Initializing event log");
		logPanelX = 20f;
		logPanelY = screenHeight - 100f;

		filterButtonStartX = logPanelX;
		filterButtonStartY = screenHeight - 50f;

		// Add initial log entry
		addLogEntry("Game started!", Color.GREEN, LogEntryType.SYSTEM);
	}

	/**
	 * Add an entry to the event log
	 * 
	 * @param message The message text
	 * @param color The color for the text
	 * @param type The type of log entry
	 */
	public void addLogEntry(String message, Color color, LogEntryType type) {
		eventLog.add(new LogEntry(message, color, type));

		// Maintain max log size
		if (eventLog.size() > MAX_LOG_ENTRIES) {
			eventLog.remove(0);
		}

		// Auto-scroll to bottom
		logScrollOffset = 0;
	}

	/**
	 * Add a dialogue log entry
	 * 
	 * @param speaker The character speaking
	 * @param text The dialogue text
	 */
	public void logDialogue(String speaker, String text) {
		String message = speaker + ": " + text;
		addLogEntry(message, Color.CYAN, LogEntryType.DIALOGUE);
	}

	/**
	 * Add a combat log entry
	 * 
	 * @param message The combat message
	 */
	public void logCombat(String message) {
		addLogEntry(message, Color.RED, LogEntryType.COMBAT);
	}

	/**
	 * Add a quest log entry
	 * 
	 * @param message The quest message
	 */
	public void logQuest(String message) {
		addLogEntry(message, Color.GOLD, LogEntryType.QUEST);
	}

	/**
	 * Add an item log entry
	 * 
	 * @param message The item message
	 */
	public void logItem(String message) {
		addLogEntry(message, Color.YELLOW, LogEntryType.ITEM);
	}

	/**
	 * Get filtered log entries based on current filter
	 * 
	 * @return List of log entries matching current filter
	 */
	private List<LogEntry> getFilteredEntries() {
		List<LogEntry> filtered = new ArrayList<>();

		for (LogEntry entry : eventLog) {
			if (currentFilter == LogFilter.ALL) {
				filtered.add(entry);
			} else if (currentFilter == LogFilter.DIALOGUE && entry.type == LogEntryType.DIALOGUE) {
				filtered.add(entry);
			} else if (currentFilter == LogFilter.COMBAT && entry.type == LogEntryType.COMBAT) {
				filtered.add(entry);
			} else if (currentFilter == LogFilter.QUEST && entry.type == LogEntryType.QUEST) {
				filtered.add(entry);
			} else if (currentFilter == LogFilter.ITEMS && entry.type == LogEntryType.ITEM) {
				filtered.add(entry);
			}
		}

		return filtered;
	}

	@Override
	public void update(float deltaTime) {
		if (!isActive || !isVisible) {
			return;
		}

		// Update logic here if needed
	}

	@Override
	public void render(float deltaTime) {
		if (!isVisible) {
			return;
		}

		batch.begin();

		// Render filter buttons
		renderFilterButtons();

		// Render log panel
		renderLogPanel();

		batch.end();
	}

	/**
	 * Render filter selection buttons
	 */
	private void renderFilterButtons() {
		float buttonX = filterButtonStartX;
		LogFilter[] filters = LogFilter.values();

		for (LogFilter filter : filters) {
			boolean isSelected = (filter == currentFilter);

			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			Color bgColor = isSelected ? ColorCache.UI_SELECTED : ColorCache.UI_UNSELECTED_ALT;
			shapeRenderer.setColor(bgColor);
			shapeRenderer.rect(buttonX, filterButtonStartY, FILTER_BUTTON_WIDTH, FILTER_BUTTON_HEIGHT);

			shapeRenderer.setColor(isSelected ? Color.CYAN : Color.WHITE);
			shapeRenderer.rect(buttonX, filterButtonStartY, FILTER_BUTTON_WIDTH, FILTER_BUTTON_HEIGHT);
			shapeRenderer.end();

			// Button label
			font.setColor(isSelected ? Color.CYAN : Color.WHITE);
			String label = filter.toString().substring(0, 1) + filter.toString().substring(1).toLowerCase();
			font.draw(batch, label, buttonX + 5, filterButtonStartY + 17);

			buttonX += (FILTER_BUTTON_WIDTH + 5);
		}
	}

	/**
	 * Render the event log panel
	 */
	private void renderLogPanel() {
		// Panel background
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
		shapeRenderer.rect(logPanelX, logPanelY - LOG_PANEL_HEIGHT, LOG_PANEL_WIDTH, LOG_PANEL_HEIGHT);

		// Panel border
		shapeRenderer.setColor(0.8f, 0.6f, 0.2f, 1f);
		shapeRenderer.rect(logPanelX, logPanelY - LOG_PANEL_HEIGHT, LOG_PANEL_WIDTH, LOG_PANEL_HEIGHT);
		shapeRenderer.end();

		// Render log entries
		List<LogEntry> filteredEntries = getFilteredEntries();
		float entryY = logPanelY - 20;
		int startIndex = Math.max(0, filteredEntries.size() - MAX_VISIBLE_ENTRIES - logScrollOffset);
		int endIndex = Math.max(0, filteredEntries.size() - logScrollOffset);

		for (int i = startIndex; i < endIndex && i < filteredEntries.size(); i++) {
			LogEntry entry = filteredEntries.get(i);

			// Alternate background for readability
			if ((i - startIndex) % 2 == 0) {
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 0.5f);
				shapeRenderer.rect(logPanelX + 2, entryY - LOG_ENTRY_HEIGHT, LOG_PANEL_WIDTH - 4, LOG_ENTRY_HEIGHT);
				shapeRenderer.end();
			}

			// Entry text
			fontSmall.setColor(entry.color);
			fontSmall.draw(batch, entry.message, logPanelX + 10, entryY - 5);

			entryY -= LOG_ENTRY_HEIGHT;
		}

		// Scroll indicator
		if (filteredEntries.size() > MAX_VISIBLE_ENTRIES) {
			float scrollPercent = logScrollOffset / (float) (filteredEntries.size() - MAX_VISIBLE_ENTRIES);
			fontSmall.setColor(Color.GRAY);
			fontSmall.draw(batch, "↑ ↓", logPanelX + LOG_PANEL_WIDTH - 30, logPanelY - 15);
		}
	}

	/**
	 * Scroll log up (show older entries)
	 */
	public void scrollUp() {
		List<LogEntry> filtered = getFilteredEntries();
		if (filtered.size() > MAX_VISIBLE_ENTRIES) {
			logScrollOffset = Math.min(logScrollOffset + 1, filtered.size() - MAX_VISIBLE_ENTRIES);
		}
	}

	/**
	 * Scroll log down (show newer entries)
	 */
	public void scrollDown() {
		logScrollOffset = Math.max(logScrollOffset - 1, 0);
	}

	/**
	 * Switch log filter
	 * 
	 * @param filter The filter to apply
	 */
	public void setFilter(LogFilter filter) {
		this.currentFilter = filter;
		logScrollOffset = 0; // Reset scroll when changing filter
	}

	@Override
	public boolean handleInput() {
		if (!isActive || !isVisible) {
			return false;
		}

		// Log scrolling
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.PAGE_UP)) {
			scrollUp();
			return true;
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.PAGE_DOWN)) {
			scrollDown();
			return true;
		}

		// Filter switching
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.NUM_1)) {
			setFilter(LogFilter.ALL);
			return true;
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.NUM_2)) {
			setFilter(LogFilter.DIALOGUE);
			return true;
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.NUM_3)) {
			setFilter(LogFilter.COMBAT);
			return true;
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.NUM_4)) {
			setFilter(LogFilter.QUEST);
			return true;
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.NUM_5)) {
			setFilter(LogFilter.ITEMS);
			return true;
		}

		return false;
	}

	@Override
	protected void onResize() {
		// Adjust log panel position on resize
		logPanelX = 20f;
		logPanelY = screenHeight - 100f;
		filterButtonStartY = screenHeight - 50f;
	}

	@Override
	public void dispose() {
		if (shapeRenderer != null) {
			shapeRenderer.dispose();
		}
		if (font != null) {
			font.dispose();
		}
		if (fontSmall != null) {
			fontSmall.dispose();
		}
	}
}
