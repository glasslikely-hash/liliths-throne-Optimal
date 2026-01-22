# STEP 3 UI REFACTORING: CORRECTED PLAN

**Date:** January 21, 2026  
**Status:** Phase 3 Planning Complete - Ready for Implementation  
**Architecture:** Event-Driven Text Rendering (NOT Real-Time Graphics)

---

## Executive Summary

### The Mistake
Created 25 files implementing a real-time graphics engine (layers, screens, components, continuous rendering loop) for a **text-based event-driven game**. This is fundamentally wrong architecture.

### The Correction
Delete all 25 files. Implement simple text renderer that:
1. Parses HTML from RenderingEngine (already generates text)
2. Renders text with BitmapFont (simple LibGDX)
3. Tracks click regions for interactive elements
4. Routes clicks back to MainController (existing)

### Expected Result
- Game plays identically to JavaFX version
- Visual layout looks virtually the same
- Code is much simpler (3 files vs 25 files)
- Architecture matches actual event-driven design
- No wasted CPU on continuous rendering

---

## Architecture Diagram

### Current Broken Architecture (25 Files)
```
┌─────────────────────────────────────────────────┐
│ LibGdxApp (Entry Point)                         │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌────────────────────────────────────────┐    │
│  │ GameScreen (Implements Screen)         │    │
│  ├────────────────────────────────────────┤    │
│  │ update() { Called every frame }        │    │
│  │ render() {                             │    │
│  │   ├─ DialogueLayer.render()            │    │
│  │   ├─ MapLayer.render()                 │    │
│  │   ├─ HudLayer.render()                 │    │
│  │   ├─ MenuLayer.render()                │    │
│  │   └─ EffectsLayer.render()             │    │
│  │ }                                      │    │
│  │                                        │    │
│  │ [Each Layer has components]            │    │
│  │ ├─ UIButton, UIPanel, UIText, ...      │    │
│  │ └─ Particles, Animations, Camera      │    │
│  └────────────────────────────────────────┘    │
│                                                 │
│  Problem: Game doesn't use any of this!       │
│  - No continuous update loop exists            │
│  - No layers in game architecture              │
│  - No components in rendering                  │
│  - Wastes frames rendering static content     │
│                                                 │
└─────────────────────────────────────────────────┘

Game State                 What Actually Happens
┌──────────────┐          ┌──────────────────────┐
│ Game.java    │   Click  │ MainController fires │
│ (6,583 LOC)  │────────→ │ event listener       │
└──────────────┘          └──────────────────────┘
                                    │
                                    ↓
                          ┌──────────────────────┐
                          │ Update game state    │
                          │ (internal logic)     │
                          └──────────────────────┘
                                    │
                                    ↓
                          ┌──────────────────────┐
                          │ RenderingEngine.java │
                          │ (3,361 LOC)          │
                          │ Generates HTML text  │
                          └──────────────────────┘
                                    │
                                    ↓
                          ┌──────────────────────┐
                          │ Display on screen    │
                          │ (WebView or LibGDX)  │
                          └──────────────────────┘
                                    │
                          Click happens again...

The 25-file architecture sits unused in the middle!
```

### Correct Architecture (3 Files)
```
┌─────────────────────────────────────────────────┐
│ LibGdxApp (Entry Point)                         │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌────────────────────────────────────────┐    │
│  │ LibGdxUIManager (Implements UIManager) │    │
│  ├────────────────────────────────────────┤    │
│  │ render() {                             │    │
│  │   ├─ Get current HTML from Game.java   │    │
│  │   └─ Pass to TextScreenRenderer        │    │
│  │ }                                      │    │
│  │ mousePressed(x, y) {                   │    │
│  │   └─ Route to MainController           │    │
│  │ }                                      │    │
│  └────────────────────────────────────────┘    │
│         │                        ↑             │
│         │ "Render HTML"    "Handle Click"      │
│         ↓                        │             │
│  ┌────────────────────────────────────────┐    │
│  │ TextScreenRenderer (NEW)               │    │
│  ├────────────────────────────────────────┤    │
│  │ parseHTML(htmlString) {                │    │
│  │   ├─ Extract text content              │    │
│  │   ├─ Parse colors from CSS             │    │
│  │   ├─ Track clickable regions           │    │
│  │   └─ Return render info                │    │
│  │ }                                      │    │
│  │ render(spriteBatch) {                  │    │
│  │   ├─ BitmapFont.draw(text)             │    │
│  │   ├─ Draw rectangles for status bars   │    │
│  │   └─ Highlight on hover                │    │
│  │ }                                      │    │
│  │ getClickedElement(x, y) {              │    │
│  │   └─ Return which button/choice        │    │
│  │ }                                      │    │
│  └────────────────────────────────────────┘    │
│         ↑                                      │
│         │ HTML string from RenderingEngine     │
│         │                                      │
│  ┌────────────────────────────────────────┐    │
│  │ Game.java + RenderingEngine (EXISTING) │    │
│  │ ✓ No changes needed                    │    │
│  │ ✓ Generates text for display           │    │
│  │ ✓ Handles all game logic               │    │
│  └────────────────────────────────────────┘    │
│                                                 │
└─────────────────────────────────────────────────┘

Simple, clean, matches actual game architecture!
```

---

## Phase 3: Implementation Details

### Phase 3.1: Cleanup & Analysis (3-4 hours)

#### 3.1.1: Delete 24 Incorrect Files
```
Layers:       DialogueLayer, EffectsLayer, HudLayer, MapLayer, 
              MenuLayer, PauseLayer, UILayer (7 files)

Screens:      GameScreen, MainMenuScreen, SaveLoadScreen, 
              AllScreens (4 files)

Components:   UIButton, UIComponent, UIImage, UIList, UIPanel, 
              UIProgressBar, UISlider, UIText (8 files)

Graphics:     PixelDraw (1 file)

Input:        InputEvent (1 file)

Utils:        KeyCode, ColorRGB (2 files)

Other:        LibGdxLayer if it exists (1 file)
```

**Verification:**
```
src/com/lilithsthrone/ui/layers/      → Should be EMPTY
src/com/lilithsthrone/ui/screens/     → Should be EMPTY
src/com/lilithsthrone/ui/components/  → Should be EMPTY
src/com/lilithsthrone/ui/graphics/    → Should be EMPTY
src/com/lilithsthrone/ui/input/       → Only InputManager.java
```

#### 3.1.2: Analyze RenderingEngine Output
**Goal:** Understand the exact HTML structure RenderingEngine generates

**Method:**
1. Read RenderingEngine.java (already done, 3,361 lines)
2. Find these key methods:
   - `getMainPanel()` - Main narrative content
   - `getStatusPanel()` - Health, mana, stamina bars
   - `getInventoryPanel()` - Inventory screen
   - `getCharacterPanel()` - Character sheet
   - `getMapPanel()` - Location/map display
   - `getResponseButtons()` - Dialogue choices
   - `getFooterHTML()` - Footer info

3. Document:
   - HTML structure (divs, spans, layout)
   - CSS classes used
   - Color values (hex codes)
   - Text content examples
   - Interactive element IDs

#### 3.1.3: Create Screen Layout Specification
**Goal:** Map visual layout to screen coordinates

**Output Document:** SCREEN_LAYOUT_SPECIFICATION.md

**Contents:**
```
Screen Resolution: 1024x768 (typical LibGDX default)

Region 1: Header (y: 0-60)
├─ Title text
├─ Version text
└─ Static colors and fonts

Region 2: Status Bars (y: 60-160)
├─ Health bar (y: 60-90)
│  ├─ Background: 16px tall
│  ├─ Current value: 0-1024px wide
│  ├─ Text: "Health: XXX/100"
│  └─ Color: Red (#cc0000)
├─ Mana bar (y: 92-122)
│  └─ Color: Blue (#0099ff)
├─ Stamina bar (y: 124-154)
│  └─ Color: Green (#00ff00)
└─ Corruption bar (y: 156-186)
   └─ Color: Purple (#ff00ff)

Region 3: Main Content (y: 160-600)
├─ Narrative text (left-aligned)
├─ Character names and dialogue
├─ Item descriptions
├─ Status information
└─ Dynamic height based on content

Region 4: Buttons/Choices (y: 600-700)
├─ Dialogue response buttons
├─ Navigation buttons (Back, Inventory, etc.)
├─ Each button is clickable rectangle
└─ Highlight on hover

Region 5: Footer (y: 700-768)
├─ Time display
├─ Location display
├─ Day/season/year
└─ Navigation info

Total: 1024x768 viewport
Font sizes:
- Title: 24px
- Body text: 14px
- Labels: 11px
```

#### 3.1.4: List All Interactive Elements
**Goal:** Document every clickable area in game

**Output Format:**
```
Dialogue Choice Buttons:
├─ ID: "dialogue_choice_0"
├─ Text: "I'm interested in what you're selling"
├─ Position: (50, 615)
├─ Size: 900x25
├─ Action: SelectDialogueChoice(0)
└─ Color: Gold (#ffff00) on hover

Navigation Buttons:
├─ "btn_back" - Go to previous screen
├─ "btn_inventory" - Open inventory
├─ "btn_character" - Open character sheet
├─ "btn_map" - Open map
└─ etc.

Inventory Items (Clickable):
├─ Item slot 0: (50, 200)
├─ Item slot 1: (50, 230)
└─ etc.

Equipment Slots (Clickable):
├─ Head slot: (700, 100)
├─ Chest slot: (700, 130)
└─ etc.
```

---

### Phase 3.2: TextScreenRenderer Implementation (4-6 hours)

#### 3.2.1: Create TextScreenRenderer.java

**Purpose:** Bridge between HTML generation and LibGDX rendering

**Key Methods:**

```java
public class TextScreenRenderer {
    
    // Input: HTML string from RenderingEngine
    public void parseHTML(String htmlContent) {
        // Extract all text with positions
        // Extract all colors from CSS
        // Track clickable areas
    }
    
    // Output: Render to LibGDX batch
    public void render(SpriteBatch batch, BitmapFont font) {
        // Draw background rectangles
        // Draw all text with correct colors
        // Draw status bars with fills
        // Draw button highlights
    }
    
    // Input handling
    public int getClickedElementIndex(float x, float y) {
        // Check which button/choice was clicked
        // Return index or -1 if none
    }
    
    public void setHoveredElement(float x, float y) {
        // Track which element mouse is over
        // Used for highlighting
    }
    
    // Screen management
    public void setScreenSize(int width, int height) {
        // Recalculate all positions on resize
    }
    
    public void setCurrentScreen(ScreenType type) {
        // Render different layouts for:
        // - Main game screen
        // - Inventory screen
        // - Character sheet
        // - Map screen
        // - Save/Load screen
    }
}
```

**Data Structures:**

```java
class ScreenElement {
    String id;           // "dialogue_choice_0", "btn_inventory", etc.
    String text;         // Display text
    float x, y;          // Position on screen
    float width, height; // Size of clickable area
    Color color;         // Text color
    Color hoverColor;    // Color on hover
    Color bgColor;       // Background color
    ElementType type;    // "button", "text", "bar", etc.
    Object data;         // Extra data (index, item reference, etc.)
}

enum ElementType {
    TEXT, BUTTON, PROGRESS_BAR, IMAGE, CONTAINER
}
```

#### 3.2.2: HTML Parsing Strategy

**Input:** HTML string from RenderingEngine (example below)
```html
<div style="background-color: #222222;">
  <h1 style="color: #ffff00;">Game Title</h1>
  <div class="status">
    <span>Health:</span>
    <div style="width: 80%; background-color: #cc0000;">80/100</div>
  </div>
  <div class="content">
    <p>You are standing in a plaza...</p>
    <p>"Welcome!" the merchant says.</p>
  </div>
  <div class="choices">
    <button id="choice_0">I'm interested in what you're selling</button>
    <button id="choice_1">I don't have time for this</button>
  </div>
</div>
```

**Parsing Approach (Simple Regex + String Operations):**

```java
private void parseHTML(String html) {
    // 1. Remove all HTML tags
    String text = html.replaceAll("<[^>]*>", "");
    
    // 2. Split by line breaks
    String[] lines = text.split("\n");
    
    // 3. Track Y position and add elements
    float y = startY;
    for (String line : lines) {
        if (line.isEmpty()) continue;
        
        // Extract color from previous tag
        Color color = extractColor(html, line);
        
        // Create text element
        ScreenElement elem = new ScreenElement();
        elem.text = line;
        elem.y = y;
        elem.color = color;
        
        elements.add(elem);
        y += fontHeight + spacing;
    }
    
    // 4. Extract clickable buttons
    Pattern buttonPattern = Pattern.compile(
        "<button[^>]*id=\"([^\"]*)[^>]*>([^<]*)</button>"
    );
    Matcher m = buttonPattern.matcher(html);
    
    while (m.find()) {
        ScreenElement button = new ScreenElement();
        button.id = m.group(1);
        button.text = m.group(2);
        button.type = ElementType.BUTTON;
        // ... position calculations
        elements.add(button);
    }
}
```

#### 3.2.3: Color Extraction

**Strategy:** Build lookup table of CSS colors used in RenderingEngine

```java
private static final Map<String, Color> COLOR_MAP = new HashMap<>();
static {
    COLOR_MAP.put("#ffff00", Color.YELLOW);
    COLOR_MAP.put("#cc0000", Color.RED);
    COLOR_MAP.put("#0099ff", Color.BLUE);
    COLOR_MAP.put("#00ff00", Color.GREEN);
    COLOR_MAP.put("#ff00ff", Color.MAGENTA);
    // ... add all colors from RenderingEngine
}

private Color extractColor(String htmlLine) {
    // Look for style="color: #XXXXXX"
    Pattern p = Pattern.compile("color:\\s*#([0-9a-f]+)", 
        Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(htmlLine);
    
    if (m.find()) {
        String hex = "#" + m.group(1);
        return COLOR_MAP.getOrDefault(hex, Color.WHITE);
    }
    return Color.WHITE;
}
```

#### 3.2.4: Progress Bar Rendering

**Strategy:** Detect progress bar HTML and render as rectangle

```html
<div class="progress-bar">
  <div style="width: 80%; background-color: #cc0000;">
    <span>Health: 80/100</span>
  </div>
</div>
```

**Rendering:**
```java
private void renderProgressBar(ScreenElement bar, SpriteBatch batch, 
                               ShapeRenderer shapes) {
    // Draw background
    shapes.begin(ShapeRenderer.ShapeType.Filled);
    shapes.setColor(Color.DARK_GRAY);
    shapes.rect(bar.x, bar.y, bar.width, bar.height);
    
    // Draw filled portion
    float fillWidth = bar.width * bar.fillPercent;
    shapes.setColor(bar.bgColor);
    shapes.rect(bar.x, bar.y, fillWidth, bar.height);
    
    shapes.end();
    
    // Draw text on top
    font.draw(batch, bar.text, bar.x + 10, bar.y + 15);
}
```

---

### Phase 3.3: UIManager Interface Update (2-3 hours)

#### 3.3.1: Review Current UIManager

**Location:** `src/com/lilithsthrone/ui/UIManager.java`

**Current Methods (Likely):**
```java
public interface UIManager {
    void setContent(String htmlContent);        // Set display content
    void executeScript(String javascript);      // Run JS
    String getElement(String elementId);        // Query element
    void addEventListener(String elementId, 
                         String event, 
                         String handler);       // Add listener
}
```

#### 3.3.2: Update Interface

**New Methods for Text Rendering:**
```java
public interface UIManager {
    // Existing (keep if DesktopUIManager still uses)
    void setContent(String htmlContent);
    
    // New (for LibGDX text renderer)
    void render(SpriteBatch batch);         // Render screen
    void setScreenSize(int width, int height);
    void mousePressed(float x, float y);    // Handle click
    void mouseMoved(float x, float y);      // Handle hover
}
```

#### 3.3.3: Keep Click Routing
```java
// MainController already has these listeners:
// - ButtonCharactersEventListener
// - ButtonInventoryEventListener
// - ButtonMapEventListener
// - SelectDialogueChoice(index)
// etc.

// LibGdxUIManager will route clicks to these:
public void mousePressed(float x, float y) {
    int clickedIndex = textRenderer.getClickedElementIndex(x, y);
    
    if (clickedIndex >= 0 && isDialogueChoice(clickedIndex)) {
        MainController.selectDialogueChoice(clickedIndex);
    } else if (clickedId.equals("btn_inventory")) {
        MainController.switchToInventory();
    } else if (clickedId.equals("btn_character")) {
        MainController.switchToCharacter();
    }
    // ... etc
}
```

---

### Phase 3.4: LibGdxUIManager Implementation (4-5 hours)

#### 3.4.1: Create LibGdxUIManager.java

**Purpose:** Implements UIManager, bridges Game/RenderingEngine to TextScreenRenderer

**Structure:**
```java
public class LibGdxUIManager implements UIManager {
    private Game game;                    // Reference to game logic
    private TextScreenRenderer renderer;  // Text rendering
    private BitmapFont font;              // Text font
    private SpriteBatch batch;            // LibGDX rendering
    private ShapeRenderer shapes;         // Rectangles, bars
    
    public LibGdxUIManager(Game game) {
        this.game = game;
        this.renderer = new TextScreenRenderer();
        // Initialize fonts and rendering
    }
    
    @Override
    public void setContent(String htmlContent) {
        // Called when game generates new display
        renderer.parseHTML(htmlContent);
    }
    
    @Override
    public void render(SpriteBatch batch) {
        // Called every frame to display
        renderer.render(batch, font);
    }
    
    @Override
    public void mousePressed(float x, float y) {
        // Player clicked at (x, y)
        int elementIndex = renderer.getClickedElementIndex(x, y);
        routeClick(elementIndex, x, y);
    }
    
    @Override
    public void mouseMoved(float x, float y) {
        // Update hover highlighting
        renderer.setHoveredElement(x, y);
    }
    
    private void routeClick(int index, float x, float y) {
        // Route click to appropriate MainController method
        // OR call game.selectResponse(index)
        // Depends on which controller handles event routing
    }
}
```

#### 3.4.2: Font Loading

**Strategy:** Load BitmapFont from existing assets or create new one

```java
public LibGdxUIManager(Game game) {
    // Option 1: Load from TTF font
    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
        Gdx.files.internal("fonts/arial.ttf"));
    FreeTypeFontGenerator.FreeTypeFontParameter parameter = 
        new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = 14;
    parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + 
                          "éàùèêôûâäöñíçß"; // Extended chars
    
    this.font = generator.generateFont(parameter);
    generator.dispose();
    
    // Option 2: Use built-in font (less pretty but works)
    // this.font = new BitmapFont();
}
```

#### 3.4.3: Input Handling Integration

```java
public class LibGdxApp implements ApplicationListener, InputProcessor {
    private LibGdxUIManager uiManager;
    
    @Override
    public void create() {
        game = new Game();
        uiManager = new LibGdxUIManager(game);
        Gdx.input.setInputProcessor(this);
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, 
                            int button) {
        // Convert screen coords to game coords if needed
        uiManager.mousePressed(screenX, screenY);
        return true;
    }
    
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        uiManager.mouseMoved(screenX, screenY);
        return false;
    }
    
    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        spriteBatch.begin();
        uiManager.render(spriteBatch);
        spriteBatch.end();
    }
}
```

---

### Phase 3.5: Integration Testing (3-4 hours)

#### 3.5.1: Compile and Fix Errors
- Verify TextScreenRenderer.java compiles
- Verify LibGdxUIManager.java compiles
- Check for missing imports

#### 3.5.2: Start Application
```bash
mvn clean compile exec:java@run
```

**Expected:** Window opens, game screen displays

#### 3.5.3: Verify Text Rendering
- [ ] Title appears at top
- [ ] Narrative text displays in main area
- [ ] Status bars show with correct colors
- [ ] Dialogue choices list appears
- [ ] Navigation buttons visible

#### 3.5.4: Test Interactions
```
Test sequence:
1. Click dialogue choice
   → Verify: Game state updates, new text renders
2. Click inventory button
   → Verify: Switch to inventory screen, show items
3. Click character button
   → Verify: Switch to character sheet, show stats
4. Click back button
   → Verify: Return to main game screen
5. Hover over dialogue choice
   → Verify: Text changes color/highlights
6. Click item in inventory
   → Verify: Item action menu appears
```

#### 3.5.5: Visual Verification
- [ ] Layout matches VISUAL_UI_REFERENCE.md
- [ ] Colors are accurate
- [ ] Text is readable
- [ ] Buttons are clearly clickable
- [ ] No visual glitches or missing elements

#### 3.5.6: Performance Check
```java
// In render() method
if (Gdx.input.isKeyPressed(Input.Keys.D)) {
    Gdx.app.log("FPS", String.valueOf(Gdx.graphics.getFramesPerSecond()));
}
```

**Expected:** 60 FPS (no stuttering)

---

## Success Criteria

### Compilation
- ✓ All 25 files deleted successfully
- ✓ TextScreenRenderer.java compiles
- ✓ LibGdxUIManager.java compiles
- ✓ No compiler errors in entire project

### Runtime
- ✓ LibGdxApp starts without crashes
- ✓ Game window opens with correct resolution
- ✓ Text renders on screen
- ✓ Colors are correct
- ✓ Layout matches reference

### Functionality
- ✓ Clicking dialogue choice triggers game logic
- ✓ Game state updates correctly
- ✓ New content renders after state change
- ✓ Inventory screen displays items
- ✓ Character sheet shows stats
- ✓ Map screen shows locations
- ✓ All navigation buttons work

### Visual Quality
- ✓ Text is clear and readable
- ✓ Status bars display correctly with fills
- ✓ Colors match original JavaFX version
- ✓ Layout is organized and not cluttered
- ✓ Interactive elements are obvious
- ✓ Hover highlighting works

### Code Quality
- ✓ TextScreenRenderer < 1,000 LOC
- ✓ LibGdxUIManager < 500 LOC
- ✓ No Game.java modifications
- ✓ No RenderingEngine.java modifications
- ✓ MainController integration unchanged

---

## Remaining Work After Step 3

### Step 4: Persistence (Already Planned)
- Save/load game state
- Autosave system
- Multiple save slots

### Step 5: Optimization
- Asset loading
- Text caching
- Performance profiling

### Step 6: Testing
- Unit tests for game logic
- Integration tests for UI
- Platform testing (desktop, web, mobile)

---

## Timeline Summary

| Phase | Estimated Time | Status |
|-------|---|---|
| 3.1: Cleanup & Analysis | 3-4 hours | Ready |
| 3.2: TextScreenRenderer | 4-6 hours | Ready |
| 3.3: UIManager Update | 2-3 hours | Ready |
| 3.4: LibGdxUIManager | 4-5 hours | Ready |
| 3.5: Integration & Test | 3-4 hours | Ready |
| **Total Step 3** | **18-22 hours** | **In Plan** |

---

## Key Files Reference

### Files to Delete
```
src/com/lilithsthrone/ui/layers/*          (7 files)
src/com/lilithsthrone/ui/screens/*         (4 files)
src/com/lilithsthrone/ui/components/*      (8 files)
src/com/lilithsthrone/ui/graphics/*        (1 file)
src/com/lilithsthrone/ui/input/InputEvent* (1 file)
src/com/lilithsthrone/utils/KeyCode*       (1 file)
src/com/lilithsthrone/utils/colours/ColorRGB* (1 file)
```

### Files to Create
```
src/com/lilithsthrone/ui/rendering/TextScreenRenderer.java
src/com/lilithsthrone/ui/LibGdxUIManager.java
```

### Files to Keep Unchanged
```
src/com/lilithsthrone/game/Game.java
src/com/lilithsthrone/rendering/RenderingEngine.java
src/com/lilithsthrone/controller/MainController.java
src/com/lilithsthrone/ui/LibGdxApp.java
src/com/lilithsthrone/ui/UIManager.java (interface - may add methods)
```

---

## Next Steps

1. **Delete 24 incorrect files** using provided bash script
2. **Create SCREEN_LAYOUT_SPECIFICATION.md** documenting visual layout
3. **Implement TextScreenRenderer.java** with HTML parsing
4. **Implement LibGdxUIManager.java** with input routing
5. **Update UIManager.java interface** with text rendering methods
6. **Test game flow** with new text renderer
7. **Verify visual output** matches JavaFX version

All phases are planned. Ready to execute when you give the green light.
