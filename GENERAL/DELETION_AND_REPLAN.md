# Files to Delete and Correct Architecture Plan

## 25 Files to Delete (All Incorrect Real-Time Graphics Architecture)

### Layers (7 files - DELETE)
```
src/com/lilithsthrone/ui/layers/DialogueLayer.java
src/com/lilithsthrone/ui/layers/EffectsLayer.java
src/com/lilithsthrone/ui/layers/HudLayer.java
src/com/lilithsthrone/ui/layers/MapLayer.java
src/com/lilithsthrone/ui/layers/MenuLayer.java
src/com/lilithsthrone/ui/layers/PauseLayer.java
src/com/lilithsthrone/ui/layers/UILayer.java
```

**Why:** These implement continuous rendering layers inappropriate for text-based game.

---

### Screens (4 files - DELETE)
```
src/com/lilithsthrone/ui/screens/AllScreens.java
src/com/lilithsthrone/ui/screens/GameScreen.java
src/com/lilithsthrone/ui/screens/MainMenuScreen.java
src/com/lilithsthrone/ui/screens/SaveLoadScreen.java
```

**Why:** LibGDX Screen interface inappropriate for text-based HTML rendering.

---

### Components (8 files - DELETE)
```
src/com/lilithsthrone/ui/components/UIButton.java
src/com/lilithsthrone/ui/components/UIComponent.java
src/com/lilithsthrone/ui/components/UIImage.java
src/com/lilithsthrone/ui/components/UIList.java
src/com/lilithsthrone/ui/components/UIPanel.java
src/com/lilithsthrone/ui/components/UIProgressBar.java
src/com/lilithsthrone/ui/components/UISlider.java
src/com/lilithsthrone/ui/components/UIText.java
```

**Why:** Component hierarchy model for real-time graphics, not text rendering.

---

### Graphics (1 file - DELETE)
```
src/com/lilithsthrone/ui/graphics/PixelDraw.java
```

**Why:** Pixel drawing utilities for graphics engine not needed for text.

---

### Input (1 file - DELETE)
```
src/com/lilithsthrone/ui/input/InputEvent.java
```

**Why:** Custom input event system; use InputManager instead.

---

### Utilities (2 files - DELETE)
```
src/com/lilithsthrone/utils/KeyCode.java
src/com/lilithsthrone/utils/colours/ColorRGB.java
```

**Why:** KeyCode duplicates GLFW keycodes; ColorRGB duplicates existing color system.

---

## Total: 24 Files to Delete

Plus any other LibGdxLayer.java, TextScreenRenderer.java, LibGdxUIManager.java from previous incorrect work.

---

## Correct Architecture: Event-Driven Text Rendering

### What Game Actually Does

```
Player clicks button/link
    ↓
MainController event listener fires
    ↓
Game.java updates internal state
    ↓
RenderingEngine.java generates HTML string
    ↓
Display system renders HTML as text
    ↓
Player sees update on screen
```

**No continuous rendering loop. No graphics layers. No sprite batches. No camera.**

---

### Files to KEEP (Core Game Engine)

```
src/com/lilithsthrone/game/Game.java (6,583 LOC) - UNMODIFIED
    ↓ Uses
src/com/lilithsthrone/rendering/RenderingEngine.java (3,361 LOC) - MOSTLY UNMODIFIED
src/com/lilithsthrone/controller/MainController.java (3,346 LOC) - MOSTLY UNMODIFIED
    ↑ Uses
src/com/lilithsthrone/game/dialogue/Colour (existing) - KEEP
src/com/lilithsthrone/utils/colours/PresetColour (existing) - KEEP
```

These **stay unchanged** and handle all game logic and HTML generation.

---

### New Files to CREATE (Simple Text Renderer Only)

1. **TextScreenRenderer.java** (NEW)
   - Parses HTML from RenderingEngine
   - Extracts text, colors, interactive regions
   - Renders with BitmapFont to screen
   - Tracks clickable areas

2. **LibGdxUIManager.java** (NEW)
   - Implements UIManager interface
   - Calls TextScreenRenderer to render
   - Routes mouse clicks to MainController
   - Acts as bridge between game and LibGDX

3. **Possibly update UIManager.java interface**
   - Remove JavaScript execution methods
   - Add text rendering methods
   - Keep click detection methods

---

## Why This Approach is Correct

### ✓ Event-Driven (Correct)
- Game only renders when state changes
- No wasted frames on static content
- Matches actual game design

### ✓ Text-Based Output (Correct)
- RenderingEngine already generates all text
- Just display text differently (LibGDX instead of WebView)
- Visual outcome looks similar to JavaFX

### ✓ Simple Implementation (Correct)
- No layers, no screens, no continuous loops
- Just: parse HTML → extract text → render text
- Minimal code, maximum clarity

### ✗ Real-Time Graphics (WRONG)
- Game doesn't need continuous rendering
- No camera, no sprites, no particles
- Only wastes CPU cycles

### ✗ Component Hierarchy (WRONG)
- Game doesn't compose UI from reusable components
- HTML generation is monolithic (full page at once)
- Creating component tree would be artificial

---

## Visual Outcome Similarity Requirement

User stated: "i also want the end screens to look similar to the javafx ones, ONLY IN OUTCOME, not a 1:1 implemetation in libgdx"

### What This Means

**SAME (Outcome):**
- Text content layout
- Color scheme
- Status bars visual appearance
- Button/link positions
- Information organization
- User interaction flow

**DIFFERENT (Implementation):**
- Not using JavaFX components
- Not using CSS/HTML rendering
- Not using WebView
- Using simple BitmapFont text rendering
- Using LibGDX rectangles for bars/buttons

**Result:** Player sees virtually identical UI with different underlying technology.

---

## Implementation Checklist

### Phase 3.1: Analysis (3-4 hours)
- [ ] Delete 24 incorrect files
- [ ] Examine RenderingEngine output examples
- [ ] Document HTML structure
- [ ] Map UI regions to screen coordinates
- [ ] List all interactive elements

### Phase 3.2: TextScreenRenderer (4-6 hours)
- [ ] Create TextScreenRenderer.java
- [ ] Parse HTML to extract text
- [ ] Extract CSS colors and convert to LibGDX Color
- [ ] Position text on screen
- [ ] Track clickable regions
- [ ] Implement hover highlighting

### Phase 3.3: UI Interface (2-3 hours)
- [ ] Review UIManager interface
- [ ] Remove JavaScript/HTML-specific methods
- [ ] Add text rendering methods
- [ ] Keep click event routing

### Phase 3.4: LibGDX Manager (4-5 hours)
- [ ] Create LibGdxUIManager implementing UIManager
- [ ] Integrate TextScreenRenderer
- [ ] Implement mouse click handler
- [ ] Route clicks to MainController
- [ ] Handle screen resize

### Phase 3.5: Integration (3-4 hours)
- [ ] Update LibGdxApp.java to use new TextScreenRenderer
- [ ] Test game flow: click → state change → display update
- [ ] Verify all screens render correctly
- [ ] Test all interactive elements work
- [ ] Compare visual output to JavaFX version

### Phase 3.6: Validation (2-3 hours)
- [ ] Play through game sequence
- [ ] Verify no console errors
- [ ] Check compilation succeeds
- [ ] Validate file count (all 24 deleted, new 2-3 created)

---

## File Summary After Refactoring

### Deleted (24 files)
```
Layers: 7
Screens: 4
Components: 8
Graphics: 1
Input: 1
Utils: 2
Other: 1 (if LibGdxLayer.java exists)
```

### Created (3 files)
```
TextScreenRenderer.java (500-700 LOC)
LibGdxUIManager.java (300-400 LOC)
Updated UIManager.java (interface additions)
```

### Kept (All existing game code)
```
All persistence/ files
All logic/ files
All data/ files
All rendering/ files (RenderingEngine)
All controller/ files (MainController)
All game/ files (Game.java)
All utils/ files (except 2 deleted)
All colours/ files (except ColorRGB.java)
```

---

## Success Criteria

1. **Compilation succeeds** - No compiler errors
2. **Game starts** - LibGdxApp.java launches without crashes
3. **Text renders** - Game display shows narrative text clearly
4. **Interactions work** - Clicking dialogue choices updates game state
5. **Visual match** - Screen layout matches JavaFX version
6. **Performance** - No CPU waste on unused rendering

---

## Commands to Execute (Manually or Script)

```bash
#!/bin/bash
BASE="/workspaces/liliths-throne-Optimal/src/com/lilithsthrone"

# Delete layers
rm -f "$BASE/ui/layers/"*.java

# Delete screens
rm -f "$BASE/ui/screens/"*.java

# Delete components
rm -f "$BASE/ui/components/"*.java

# Delete graphics
rm -f "$BASE/ui/graphics/PixelDraw.java"

# Delete input event
rm -f "$BASE/ui/input/InputEvent.java"

# Delete utils
rm -f "$BASE/utils/KeyCode.java"
rm -f "$BASE/utils/colours/ColorRGB.java"

echo "✓ Cleanup complete"
```

This removes the real-time graphics architecture and prepares codebase for simple event-driven text renderer.
