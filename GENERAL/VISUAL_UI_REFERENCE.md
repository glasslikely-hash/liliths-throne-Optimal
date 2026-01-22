# Visual UI Reference: JavaFX to LibGDX Equivalent

## Understanding the Current UI Layout

Based on RenderingEngine.java and MainController.java analysis, the game has these **screen regions**:

---

## Screen 1: Main Game Screen (Dialogue/Narrative)

```
╔════════════════════════════════════════════════════════════╗
║                  LILITH'S THRONE v0.4.9                    ║
╠════════════════════════════════════════════════════════════╣
║                                                            ║
║  [STATUS SECTION]                                          ║
║  Health: ████████░░ 80/100 | Mana: ██████░░░░ 60/100     ║
║  Stamina: ███████░░░ 70/100 | Arousal: ░░░░░░░░░░ 0/100  ║
║  Corruption: ████░░░░░░ 40/100                            ║
║                                                            ║
╠════════════════════════════════════════════════════════════╣
║  [MAIN CONTENT AREA - Dialogue/Narrative Text]             ║
║                                                            ║
║  You are standing in the Dominion Plaza. The afternoon    ║
║  sun beats down on the cobblestones. A merchant waves      ║
║  you over from behind a stall displaying exotic wares.    ║
║                                                            ║
║  "Welcome! I have some wonderful items for someone of      ║
║   your... talents."                                        ║
║                                                            ║
║  What do you do?                                           ║
║                                                            ║
║  [◄ Back]  [Inventory]  [Character]  [Map]                ║
║                                                            ║
║  [Click Here] "I'm interested in what you're selling"     ║
║  [Click Here] "I don't have time for this"                ║
║  [Click Here] "Get away from me, you creep"               ║
║                                                            ║
║  [Wait]  [Rest]  [Leave Area]                             ║
║                                                            ║
║  Time: 2:45 PM | Location: Dominion City                  ║
║  Day: 3 | Season: Summer | Year: 1                        ║
║                                                            ║
╚════════════════════════════════════════════════════════════╝
```

### Regions to Render:

1. **Header** (Static)
   - Game title
   - Version number

2. **Status Bars** (Dynamic)
   - 5 progress bars (Health, Mana, Stamina, Arousal, Corruption)
   - Current/Max values
   - Color-coded backgrounds

3. **Main Content Area** (Dynamic)
   - Narrative text from RenderingEngine
   - Character dialogue (in quotes)
   - Player descriptions
   - Interactive elements

4. **Choice Buttons** (Dynamic)
   - Clickable dialogue responses
   - Text color changes on hover
   - Numbered or styled

5. **Footer Navigation** (Context-dependent)
   - Back button
   - Context buttons (Inventory, Character, Map)
   - Wait/Rest/Leave buttons
   - Location and time display

---

## Screen 2: Inventory Screen

```
╔════════════════════════════════════════════════════════════╗
║                        INVENTORY                           ║
╠════════════════════════════════════════════════════════════╣
║  [Carrying: 15/50 items]  [Gold: 2,450]                   ║
╠════════════════════════════════════════════════════════════╣
║                                                            ║
║  Weapons:                    Clothing:                     ║
║  • Iron Sword [Equip]        • Leather Armor [Equip]      ║
║  • Wooden Staff              • Silk Dress                  ║
║                              • Healing Potion x5           ║
║  Accessories:                                              ║
║  • Gold Ring [Equip]         Page 1 of 5  [◄] [►]         ║
║  • Amulet of Protection                                    ║
║  • Enchanted Bracelet x2                                   ║
║                                                            ║
║  [Sell] [Drop] [Use] [Close]                              ║
║                                                            ║
╚════════════════════════════════════════════════════════════╝
```

### Regions:
- Header with carrying capacity
- Item list with categories
- Item actions (Equip, Sell, Drop, Use)
- Pagination
- Close button

---

## Screen 3: Character Sheet

```
╔════════════════════════════════════════════════════════════╗
║                    CHARACTER SHEET                         ║
╠════════════════════════════════════════════════════════════╣
║  [Avatar Image]              Name: [Custom Name]           ║
║                              Level: 5                      ║
║  [Status Bars]               Exp: ████░░░░░░ (45%)        ║
║                                                            ║
║  ATTRIBUTES:                 COMBAT STATS:                 ║
║  Strength: 8                 Attack: 12                    ║
║  Constitution: 7             Defense: 8                    ║
║  Dexterity: 6                Dodge: 6                      ║
║  Intelligence: 9             Magic Power: 14               ║
║  Wisdom: 5                   Resistance: 7                 ║
║  Charisma: 10                Critical: 5%                  ║
║                                                            ║
║  [View Body]  [View Spells]  [View Perks]  [Close]        ║
║                                                            ║
╚════════════════════════════════════════════════════════════╝
```

---

## Screen 4: Map/Location Screen

```
╔════════════════════════════════════════════════════════════╗
║                     DOMINION CITY                          ║
╠════════════════════════════════════════════════════════════╣
║                                                            ║
║   Current Location: Dominion Plaza                         ║
║                                                            ║
║   [City Area Map showing regions]                          ║
║   ┌──────────────────────────┐                            ║
║   │ Theater │  Slaver's  │    │                            ║
║   │         │   Alley    │    │                            ║
║   │─────────┼────────────┤ S  │                            ║
║   │ Plaza   │  Shopping  │ E  │                            ║
║   │ (HERE)  │  Arcade    │ A  │                            ║
║   └──────────────────────────┘                            ║
║                                                            ║
║   [← Theater] [→ Slaver's Alley] [↑ Shopping Arcade]      ║
║                                                            ║
║   You can go:                                              ║
║   • North to Shopping Arcade                              ║
║   • East to Slaver's Alley                                ║
║   • West to Theater                                       ║
║                                                            ║
║   [Explore] [Rest] [Close]                                ║
║                                                            ║
╚════════════════════════════════════════════════════════════╝
```

---

## Key LibGDX Implementation Points

### 1. Text Rendering
- Use `BitmapFont` for all text
- Create font sizes for:
  - Title (24px)
  - Body text (14px)
  - Small labels (11px)

### 2. Status Bars
- Draw as rectangles:
  - Background: dark color
  - Fill: color-coded (red=health, blue=mana, green=stamina, pink=arousal, purple=corruption)
  - Border: thin outline

### 3. Buttons/Clickable Areas
- Render text on screen
- Track rectangular regions for click detection
- Highlight on hover (change text color)
- Call MainController on click

### 4. Color Scheme
From RenderingEngine's `PresetColour`:
- Background: dark gray (#222)
- Text: light gray (#ccc)
- Accent colors: gold, red, blue, green, pink, purple
- Highlights: lighter versions of accent colors

### 5. Layout Sections
```
┌─ Header (fixed height, ~60px)
├─ Status Bars (fixed height, ~100px)
├─ Main Content (flexible, scrollable)
├─ Choices/Navigation (variable height)
└─ Footer (fixed height, ~40px)
```

---

## Mapping RenderingEngine Methods to UI Output

| RenderingEngine Method | Output Type | Screen Section |
|---|---|---|
| `getMainPanel()` | HTML | Main Content Area |
| `getStatusPanel()` | HTML | Status Bars |
| `getInventoryPanel()` | HTML | Inventory Screen |
| `getCharacterPanel()` | HTML | Character Sheet |
| `getResponseButtons()` | HTML | Choice Buttons |
| `getMapPanel()` | HTML | Map Screen |
| `getFooterHTML()` | HTML | Footer/Info Bar |

**Action:** These methods return HTML. Our TextScreenRenderer will:
1. Parse HTML for text content
2. Extract colors from CSS/HTML tags
3. Position text on screen
4. Render with BitmapFont

---

## CSS to LibGDX Color Conversion

RenderingEngine uses inline CSS with colors like:
```html
<span style="color: #ff0000">Red Text</span>
<div style="background-color: #00ff00">Green Background</div>
```

Conversion:
```java
// Extract hex color #ff0000
Color renderColor = Color.valueOf("ff0000ff");  // Add alpha

// Apply to BitmapFont
font.setColor(renderColor);
font.draw(batch, "Red Text", x, y);

// Reset to white after
font.setColor(Color.WHITE);
```

---

## Input Handling

### JavaFX Version:
- HTML button IDs mapped in FXML
- JavaScript onclick events
- MainController methods triggered

### LibGDX Version:
```java
public void mousePressed(MouseEvent event) {
    float x = event.getX();
    float y = event.getY();
    
    // Check each choice button region
    for (ButtonRegion choice : choiceButtons) {
        if (choice.bounds.contains(x, y)) {
            MainController.selectDialogueChoice(choice.index);
            break;
        }
    }
    
    // Check inventory, character, map buttons
    // etc...
}
```

---

## Summary: What TextScreenRenderer Must Do

**Input:** HTML string from RenderingEngine  
**Output:** Rendered text + clickable regions on screen

```
RenderingEngine.getMainPanel() 
    → HTML with text, colors, elements
    ↓
TextScreenRenderer.parseHTML()
    → Extract text, colors, positions
    ↓
TextScreenRenderer.render(batch)
    → Draw text with BitmapFont
    → Draw rectangles for buttons/bars
    → Track click regions
    ↓
LibGDX screen shows game
    ↓
Player clicks region
    ↓
TextScreenRenderer.getClickedElement()
    → Return which choice/button clicked
    ↓
MainController.handleClick()
    → Update game state (SAME AS BEFORE)
    ↓
RenderingEngine.render()
    → Generate new HTML
    ↓
Loop back to TextScreenRenderer.render()
```

**The visual outcome looks the same to the player.**
**The architecture is now correct for a text-based, event-driven game.**
