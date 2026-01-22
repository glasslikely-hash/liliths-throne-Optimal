# Phase 3.1 Analysis Complete: RenderingEngine Output Format

**Date:** January 22, 2026  
**Status:** Analysis complete, ready for Phase 3.2 implementation  

---

## Game Content Flow Confirmed

### Complete Data Flow Path

```
1. PLAYER INTERACTION
   └─ Clicks dialogue choice, button, or navigation element
   
2. MAINCONTROLLER EVENT LISTENERS
   └─ ButtonCharactersEventListener
   └─ ButtonInventoryEventHandler
   └─ ButtonMainMenuEventListener
   └─ ButtonMoveNorthEventListener, etc.
   └─ TooltipResponseDescriptionEventListener
   
3. GAME.SETCONTENT()
   └─ Main.game.setContent(new Response(...))
   └─ Main.game.setContent(node)
   └─ Triggers dialogue node change
   
4. GAME.CURRENTDIALOGUENODE UPDATE
   └─ Game.java maintains currentDialogueNode
   └─ Calls node.getContent() to get narrative text
   └─ Node can be from any dialogue package:
      ├─ dialogue.places.dominion.*
      ├─ dialogue.utils.*
      ├─ dialogue.npcDialogue.*
      ├─ dialogue.story.*
      └─ dialogue.companions.*
   
5. RENDERINGENGINE HTML GENERATION
   └─ RenderingEngine.ENGINE generates HTML content
   └─ Key public methods found:
      ├─ getInventoryPanel(character, buyback) → Returns HTML for inventory screen
      ├─ renderedHTMLMap() → Returns HTML for map screen
      └─ Helper methods for character panels, status displays, etc.
   
6. MAINCONTROLLER DISPLAY
   └─ webViewMain loads HTML content
   └─ webViewAttributes displays stats/status
   └─ webViewRight displays right panel
   └─ webViewButtonsLeft/Right display buttons
   └─ Tooltip webview displays hints
   
7. LIBGDX TEXT RENDERER (NEW)
   └─ TextScreenRenderer.parseHTML(htmlString)
   └─ Extract text content from HTML
   └─ Extract colors from CSS style attributes
   └─ Track clickable regions and their indices
   └─ Render text with BitmapFont
```

---

## RenderingEngine Key Methods Found

### HTML Generation Methods

**Located:** `src/com/lilithsthrone/rendering/RenderingEngine.java` (3,361 LOC)

1. **getInventoryPanel(GameCharacter, boolean)**
   - Line: 132
   - Output: Full inventory screen HTML
   - Contains: Equipped items, inventory grid, item counts, gold amount
   - Structure:
     ```html
     <div class='container-full-width' style='background: [color];'>
       [Inventory for player]
       [Inventory for character]
     </div>
     ```

2. **getInventoryEquippedPanel(GameCharacter)**
   - Renders equipped items in slots
   - Returns HTML divs with inventory-item-slot classes
   - Slot IDs: "EYES", "HEAD", "HAIR", "HORNS", "MOUTH", etc.
   - Each slot: `<div class='inventory-item-slot' id='[SLOT]Slot'></div>`

3. **renderedHTMLMap()**
   - Line: 2198
   - Returns HTML for map/location screen
   - Shows world grid, travel options, location information

4. **getCharacterPanelDiv(boolean, String, GameCharacter)**
   - Line: 2706
   - Character sheet rendering
   - Shows attributes, stats, perks

5. **getCharacterPanelSexDiv(boolean, String, GameCharacter)**
   - Line: 3041
   - Sex-related attributes display

### HTML Structure Patterns Found

All methods use consistent HTML patterns:

```java
// Background colors use:
PresetColour.BACKGROUND_DARK.toWebHexString()
PresetColour.BACKGROUND.toWebHexString()

// Text colors use inline CSS:
style='color: [hex color]'

// ID attributes for interactivity:
id='[SLOT_NAME]Slot'
id='choice_[number]'
id='[action]_button'
```

---

## Color System Analysis

### PresetColour Enum (Existing)
**Location:** `src/com/lilithsthrone/utils/colours/PresetColour.java`

RenderingEngine uses colors like:
- `PresetColour.BACKGROUND_DARK` - Dark background
- `PresetColour.BACKGROUND` - Standard background
- `PresetColour.AROUSAL_STAGE_ZERO` through `FIVE` - Status bar colors
- `PresetColour.GENERIC_ARCANE` - Spell/magic colors

Each PresetColour has:
- `.toWebHexString()` method
- Returns format: `#RRGGBB` (e.g., `#ff0000` for red)

### Color Extraction Strategy

For TextScreenRenderer, we need to:
1. Extract `style="color: #XXXXXX"` from HTML
2. Map hex to LibGDX Color enum
3. Apply color to BitmapFont before drawing text

Example:
```java
// From HTML: <span style="color: #ff0000">Red text</span>
Pattern colorPattern = Pattern.compile("color:\\s*#([0-9a-f]+)", 
                                      Pattern.CASE_INSENSITIVE);
Matcher m = colorPattern.matcher(htmlLine);
if (m.find()) {
    String hex = m.group(1);  // "ff0000"
    Color color = Color.valueOf(hex + "ff");  // Add alpha channel
    font.setColor(color);
    font.draw(batch, "Red text", x, y);
}
```

---

## Screen Layout Analysis

### Content Areas Identified

Based on RenderingEngine output patterns, screens have these areas:

**Main Game Screen:**
1. **Header** - Title, version (static)
2. **Status Section** - Health, Mana, Stamina, Arousal, Corruption bars (dynamic)
3. **Main Content** - Narrative text, dialogue (dynamic, scrollable)
4. **Response Buttons** - Clickable dialogue choices (dynamic)
5. **Navigation Buttons** - Back, Inventory, Character, Map, Wait, Rest, Leave (dynamic)
6. **Footer** - Time, Location, Date info (dynamic)

**Inventory Screen:**
1. Header with carrying capacity
2. Grid of items with categories
3. Action buttons (Sell, Drop, Use)
4. Pagination controls

**Character Sheet:**
1. Character name and level
2. Avatar image placeholder
3. Status bars (health, mana, experience)
4. Attributes table (Strength, Dexterity, etc.)
5. Combat stats (Attack, Defense, etc.)
6. Tab buttons (View Body, View Spells, View Perks)

**Map Screen:**
1. Location name header
2. ASCII/text map representation
3. Direction buttons (← → ↑)
4. Available actions (Explore, Rest, Leave)

---

## Interactive Element Mapping

### Clickable Elements Found in HTML

RenderingEngine creates elements with these ID patterns:

```
Dialogue Choices:
  id="choice_0", id="choice_1", id="choice_2", etc.
  
Inventory Items:
  id="[INVENTORY_SLOT_NAME]Slot"  e.g., "EYESSLot", "HEADSlot"
  
Equipment Slots:
  id="WEAPON_MAIN_1Slot"
  id="WEAPON_OFFHAND_1Slot"
  id="PIERCING_EARSlot", etc.
  
Navigation Buttons:
  Class-based (HTML buttons, not IDs)
  
Direction Buttons:
  id="moveNorth", id="moveSouth", id="moveEast", id="moveWest"
```

---

## TextScreenRenderer Implementation Strategy

### Phase 3.2 Task Breakdown

#### 3.2.1: HTML Parser
```java
public class TextScreenRenderer {
    private List<ScreenElement> elements;
    private List<ClickableRegion> clickableRegions;
    
    public void parseHTML(String html) {
        // 1. Remove HTML tags: html.replaceAll("<[^>]*>", "")
        // 2. Split by lines and track positions
        // 3. Extract colors from style attributes
        // 4. Track clickable IDs and their positions
        // 5. Store all in ScreenElement list
    }
}

class ScreenElement {
    String text;
    float x, y;
    Color color;
    int lineHeight;
    String type; // "text", "button", "bar", etc.
}

class ClickableRegion {
    String id;           // e.g., "choice_0"
    float x, y;
    float width, height;
    int dataIndex;       // Which response/choice this is
}
```

#### 3.2.2: Position Calculation
- Start at Y position 100 (below header/status)
- Increment Y by line height (14 pixels) after each line
- Track X position for horizontal centering
- Handle status bars as special rectangles (16px height each)
- Button regions are calculated based on text width

#### 3.2.3: Render Pipeline
```java
public void render(SpriteBatch batch, BitmapFont font) {
    // 1. Draw background
    // 2. Draw status bar rectangles
    // 3. Draw all text with correct colors
    // 4. Draw button highlights
    // 5. Draw borders/separators
}
```

#### 3.2.4: Click Detection
```java
public ClickableRegion getClickedElement(float x, float y) {
    for (ClickableRegion region : clickableRegions) {
        if (region.x <= x && x <= region.x + region.width &&
            region.y <= y && y <= region.y + region.height) {
            return region;
        }
    }
    return null;
}
```

---

## Integration Points with Existing Code

### Game.java Integration
- **Current:** Game.setContent() triggers rendering in MainController
- **New:** Game.setContent() will also update TextScreenRenderer via UIManager
- **No changes needed:** Game.java logic remains identical

### MainController Integration
- **Current:** Uses WebView to display HTML
- **New:** LibGdxUIManager will display the same HTML via TextScreenRenderer
- **No changes needed:** Event listeners remain identical
- **Click routing:** MainController event listeners are called from LibGdxUIManager

### RenderingEngine Integration
- **Current:** Generates HTML strings
- **New:** Same HTML generation, just parsed differently by TextScreenRenderer
- **No changes needed:** All content generation remains identical

### UIManager Interface Integration
```java
public interface UIManager {
    // Old methods (if still used):
    void setContent(String htmlContent);
    
    // New methods:
    void render(SpriteBatch batch);
    void mousePressed(float x, float y);
    void mouseMoved(float x, float y);
}
```

---

## Summary: What TextScreenRenderer Will Receive

### Input
HTML strings from RenderingEngine methods:
```html
<div class='container-full-width' style='background:#222222; margin-top:0;'>
  <span style='color: #ffff00; font-size: 24px;'>Game Title</span>
  <div style='color: #ff0000;'>Health: 80/100</div>
  <div style='color: #cccccc;'>You are standing in the plaza...</div>
  <button id='choice_0' style='color: #ffff00;'>I'm interested</button>
  <button id='choice_1' style='color: #ffff00;'>I don't have time</button>
</div>
```

### Processing
1. Strip HTML tags
2. Extract text content
3. Parse style colors
4. Calculate screen positions
5. Track clickable regions
6. Store in ScreenElement list

### Output
BitmapFont-rendered text on screen:
```
═══════════════════════════════════════════
      LILITH'S THRONE v0.4.9
═══════════════════════════════════════════
Health: ████████░░ 80/100

You are standing in the plaza. A merchant
waves at you.

► I'm interested in what you're selling
► I don't have time for this

[◄ Back]  [Inventory]  [Map]
═══════════════════════════════════════════
```

---

## Next Steps: Phase 3.2

Ready to create TextScreenRenderer.java with:
1. HTML parsing logic
2. Color extraction and mapping
3. Position calculation
4. Click detection
5. Text rendering with BitmapFont

**Estimated time:** 4-6 hours for full implementation and testing

---

## Files Confirmed

### No Changes Needed
- ✅ `Game.java` - Content generation unchanged
- ✅ `RenderingEngine.java` - HTML generation unchanged
- ✅ `MainController.java` - Event listeners unchanged
- ✅ All dialogue node files
- ✅ All colour/PresetColour files

### To Create/Modify
- 🆕 `TextScreenRenderer.java` - New text renderer
- 🆕 `LibGdxUIManager.java` - New UI manager implementation
- ✏️ `UIManager.java` - Interface update (add text rendering methods)

---

**Phase 3.1 Complete. Ready to proceed to Phase 3.2: TextScreenRenderer Implementation**
