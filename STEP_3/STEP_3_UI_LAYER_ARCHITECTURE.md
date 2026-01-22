╔═══════════════════════════════════════════════════════════════════════════════╗
║                      STEP 3: UI LAYER REFACTORING                             ║
║                   LibGDX Rendering Architecture                               ║
╚═══════════════════════════════════════════════════════════════════════════════╝

OVERVIEW
═════════════════════════════════════════════════════════════════════════════════

Objective: Replace JavaFX/WebView with LibGDX cross-platform rendering engine

KEY REQUIREMENTS:
  ✓ Remove all JavaFX/WebView dependencies
  ✓ LibGDX rendering and input handling
  ✓ Standardized UI components (buttons, menus, effects, animations)
  ✓ Query-only access to LogicLayerAPI (read data, no modifications)
  ✓ Platform-specific layout support (desktop vs mobile)
  ✓ Fully functional UI rendering gameplay state

CONSTRAINTS:
  ✓ UI layer is READ-ONLY for LogicLayerAPI
  ✓ No direct state modification (all through API)
  ✓ No UI-specific data in logic layer
  ✓ Support resolution scaling (800x600 to 4K+)
  ✓ Touch input for mobile, mouse/keyboard for desktop


ARCHITECTURE LAYERS
═════════════════════════════════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────┐
│ Application Layer (Main.java)                       │
│  - Entry point initialization                       │
│  - Desktop/Mobile platform detection                │
│  - LibGDX app creation                              │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│ Game Screen (GameScreen.java)                       │
│  - Core game loop (update → render)                 │
│  - Screen management                                │
│  - Input delegation                                 │
└─────────────────────────────────────────────────────┘
                          ↓
┌──────────────────────────────────────────────────────────────┐
│ UI Layers (HUD, Menus, Effects)                              │
│  ├─ HudLayer.java       (health, mana, inventory bars)       │
│  ├─ MenuLayer.java      (pause menu, inventory, character)   │
│  ├─ EffectsLayer.java   (particles, animations, transitions) │
│  └─ DialogueLayer.java  (NPC dialogue, choices)              │
└──────────────────────────────────────────────────────────────┘
                          ↓
┌──────────────────────────────────────────────────────────────┐
│ Rendering Components (Buttons, Panels, Text)                 │
│  ├─ UIButton.java      (clickable button with states)        │
│  ├─ UIPanel.java       (container with background)           │
│  ├─ UIText.java        (text rendering with effects)         │
│  ├─ UIImage.java       (sprite rendering)                    │
│  ├─ UIProgressBar.java (progress/health/mana bar)            │
│  ├─ UISlider.java      (value selection)                     │
│  └─ UIList.java        (scrollable lists)                    │
└──────────────────────────────────────────────────────────────┘
                          ↓
┌──────────────────────────────────────────────────────────────┐
│ Asset Management (Textures, Fonts, Sounds)                   │
│  ├─ AssetManager.java  (load/cache resources)                │
│  ├─ TextureAtlas.java  (sprite sheet management)             │
│  ├─ FontCache.java     (bitmap font caching)                 │
│  └─ SoundPlayer.java   (audio with volume control)           │
└──────────────────────────────────────────────────────────────┘
                          ↓
┌──────────────────────────────────────────────────────────────┐
│ Platform Abstraction (Desktop vs Mobile)                     │
│  ├─ PlatformConfig.java  (platform detection)                │
│  ├─ LayoutManager.java   (resolution-aware layouts)          │
│  ├─ InputHandler.java    (mouse/touch unified)               │
│  └─ PerformanceConfig.java (quality settings)                │
└──────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│ Logic Layer API (Query-Only Interface)              │
│  - getPlayerHealth(), getPlayerLocation()           │
│  - getInventoryItems(), getActiveEffects()          │
│  - NO MODIFICATIONS from UI layer                   │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│ Persistence & Game Engines (Steps 1 & 2)           │
└─────────────────────────────────────────────────────┘


DIRECTORY STRUCTURE
═════════════════════════════════════════════════════════════════════════════════

src/com/lilithsthrone/ui/
├── LibGdxApp.java                  # Entry point (implements ApplicationListener)
├── GameScreen.java                 # Main game screen/loop
├── ScreenManager.java              # Screen state management
├── InputManager.java               # Unified input handling
│
├── components/
│   ├── UIComponent.java            # Base class for all UI elements
│   ├── UIButton.java               # Clickable button
│   ├── UIPanel.java                # Container/background
│   ├── UIText.java                 # Text rendering
│   ├── UIImage.java                # Sprite rendering
│   ├── UIProgressBar.java          # Health/mana/etc bars
│   ├── UISlider.java               # Value selection
│   └── UIList.java                 # Scrollable lists
│
├── layers/
│   ├── UILayer.java                # Base layer class
│   ├── HudLayer.java               # Status bars, quick stats
│   ├── MenuLayer.java              # Menus and panels
│   ├── EffectsLayer.java           # Particles, animations
│   ├── DialogueLayer.java          # Dialogue and choices
│   └── MapLayer.java               # Game world rendering
│
├── assets/
│   ├── AssetManager.java           # Load/cache resources
│   ├── TextureCache.java           # Texture atlas management
│   ├── FontCache.java              # Bitmap font cache
│   ├── SoundPlayer.java            # Audio playback
│   └── AnimationPlayer.java        # Animation playback
│
├── platform/
│   ├── PlatformConfig.java         # Platform detection
│   ├── LayoutManager.java          # Resolution-aware layouts
│   ├── InputHandler.java           # Platform-specific input
│   └── PerformanceConfig.java      # Quality settings
│
└── effects/
    ├── Particle.java               # Single particle
    ├── ParticleEmitter.java        # Particle system
    ├── Transition.java             # Screen transitions
    └── AnimationState.java         # Animation state tracking


DATA FLOW: Event → Input → Action
═════════════════════════════════════════════════════════════════════════════════

1. USER INPUT (Touch/Mouse/Keyboard)
   │
   └─→ InputManager.handleInput()
       ├─ Detect input type (touch/mouse/keyboard)
       ├─ Find target UI element (button, menu, etc)
       └─ Emit InputEvent

2. INPUT EVENT PROCESSING
   │
   └─→ UIComponent.onInput(event)
       ├─ Button click → emit ButtonClickEvent
       ├─ Text field → emit TextInputEvent
       ├─ Menu item → emit MenuSelectEvent
       └─ Game hotkey → emit GameActionEvent

3. EVENT ROUTING
   │
   ├─→ GameAction
   │   ├─ moveUp/moveDown/moveLeft/moveRight
   │   ├─ interact/attack
   │   ├─ openInventory/openCharacterScreen
   │   ├─ pauseGame/unpauseGame
   │   └─ quickSave/quickLoad
   │
   └─→ MenuAction
       ├─ startGame/loadGame/saveGame
       ├─ changeSettings
       ├─ selectInventoryItem
       └─ selectDialogueChoice

4. ACTION EXECUTION (Via GameScreen.update())
   │
   └─→ Validate action (is player in menu? can move?)
       └─→ Call LogicLayerAPI.actionXxx()
           ├─ api.moveToLocation(direction)
           ├─ api.startCombat(npcId)
           ├─ api.equipItem(itemId)
           └─ api.progressDialogue(nodeId, choiceId)

5. STATE QUERY & RENDER
   │
   └─→ GameScreen.render(delta)
       ├─ Query api.getPlayerHealth()
       ├─ Query api.getPlayerLocation()
       ├─ Query api.getInventoryItems()
       ├─ Query api.getActiveEffects()
       └─ Render UI layers with current state

6. VISUAL UPDATE
   │
   └─→ UILayer.render(batch, camera)
       ├─ Draw background/world
       ├─ HudLayer renders health bar, mana, location
       ├─ EffectsLayer renders particles/animations
       ├─ MenuLayer renders open menus
       └─ DialogueLayer renders dialogue box


COMPONENT INHERITANCE HIERARCHY
═════════════════════════════════════════════════════════════════════════════════

UIComponent (Abstract Base)
  │
  ├─ UIButton
  │   ├─ SimpleButton
  │   ├─ ToggleButton
  │   └─ IconButton
  │
  ├─ UIPanel
  │   ├─ StatusPanel
  │   ├─ MenuPanel
  │   ├─ InventoryPanel
  │   └─ CharacterPanel
  │
  ├─ UIText
  │   ├─ Label
  │   ├─ ValueDisplay
  │   └─ TextInput
  │
  ├─ UIImage
  │   ├─ CharacterSprite
  │   ├─ ItemIcon
  │   └─ EnvironmentSprite
  │
  ├─ UIProgressBar
  │   ├─ HealthBar
  │   ├─ ManaBar
  │   └─ ExperienceBar
  │
  ├─ UISlider
  │   ├─ VolumeSlider
  │   └─ BrightnessSlider
  │
  └─ UIList
      ├─ InventoryList
      ├─ SpellList
      └─ DialogueChoiceList


LAYER RESPONSIBILITIES
═════════════════════════════════════════════════════════════════════════════════

MapLayer (Game World Rendering)
  ├─ Render game world (cells, terrain, objects)
  ├─ Render character sprites
  ├─ Render NPCs
  ├─ Render lighting/fog of war
  └─ Handle map camera/zoom
  
  Methods:
  ├─ render(batch, logicApi)
  ├─ update(delta, logicApi)
  ├─ handleMapInput(inputEvent)
  └─ setViewport(width, height)

HudLayer (Status Information)
  ├─ Health bar (player)
  ├─ Mana bar (player)
  ├─ Experience bar
  ├─ Location name
  ├─ Current time
  ├─ Quick inventory slots (top-right)
  └─ Buff/effect icons
  
  Methods:
  ├─ render(batch, logicApi)
  ├─ update(delta, logicApi)
  └─ showFloatingDamage(amount, x, y, color)

MenuLayer (UI Panels/Menus)
  ├─ Open/close menus
  ├─ Inventory screen
  ├─ Character stat screen
  ├─ Spell screen
  ├─ Map screen
  ├─ Settings menu
  ├─ Pause menu
  └─ Save/load menu
  
  Methods:
  ├─ render(batch, logicApi)
  ├─ update(delta, logicApi)
  ├─ openMenu(menuType)
  ├─ closeMenu()
  ├─ handleMenuInput(inputEvent)
  └─ isMenuOpen(): boolean

DialogueLayer (NPC Interaction)
  ├─ Dialogue box (text)
  ├─ Character portrait
  ├─ Dialogue choices
  ├─ Typewriter effect
  └─ Choice selection
  
  Methods:
  ├─ render(batch, logicApi)
  ├─ update(delta, logicApi)
  ├─ startDialogue(npcId, logicApi)
  ├─ selectChoice(choiceIndex, logicApi)
  ├─ advance()
  └─ isDialogueActive(): boolean

EffectsLayer (Animations & Particles)
  ├─ Particle systems (combat effects, spells)
  ├─ Screen transitions (fade, wipe)
  ├─ Damage numbers (floating text)
  ├─ Item drop animations
  ├─ Character death animation
  └─ Status effect indicators
  
  Methods:
  ├─ render(batch)
  ├─ update(delta)
  ├─ addParticles(emitterType, x, y, params)
  ├─ playTransition(transitionType)
  └─ showFloatingText(text, x, y, color, duration)


PLATFORM-SPECIFIC BEHAVIOR
═════════════════════════════════════════════════════════════════════════════════

DESKTOP (1200x800 minimum, keyboard/mouse)
  ├─ Layout: Centered UI, large fonts (18pt+)
  ├─ Controls:
  │   ├─ WASD or Arrow Keys: Movement
  │   ├─ Mouse Click: Interact, menu navigation
  │   ├─ Right Click: Context menu
  │   ├─ E: Interact with NPC
  │   ├─ I: Inventory
  │   ├─ C: Character screen
  │   ├─ M: Map
  │   ├─ Esc: Pause/close menu
  │   └─ F5/F9: Quick save/load
  ├─ Viewport: Fixed 1200x800 with pillarboxing
  ├─ Font size: Scales with resolution
  └─ Button size: 80px × 40px minimum

MOBILE (500x800 to 1080x1920, touch)
  ├─ Layout: Full-screen, larger touch targets
  ├─ Controls:
  │   ├─ D-pad or gesture: Movement
  │   ├─ Tap: Select menu, interact
  │   ├─ Long-press: Context menu
  │   ├─ Swipe: Scroll menus/inventory
  │   ├─ Pinch: Zoom map
  │   ├─ Tab buttons at bottom: Inventory, character, map
  │   └─ Virtual D-pad (left side of screen)
  ├─ Viewport: Full screen with safe-area padding
  ├─ Font size: 16pt minimum for readability
  └─ Button size: 60px × 60px minimum

RESPONSIVE SCALING
  ├─ 800x600:   Title screen, menus only
  ├─ 1024x768:  Full game playable
  ├─ 1280x720:  Standard HD
  ├─ 1920x1080: Full HD
  ├─ 2560x1440: 2K
  └─ 3840x2160: 4K


INPUT HANDLING MODEL
═════════════════════════════════════════════════════════════════════════════════

InputManager tracks:
  ├─ Mouse position (always)
  ├─ Active touch points (mobile)
  ├─ Keyboard state (desktop)
  └─ Detected swipes/gestures (mobile)

Input Events cascade to:
  1. MenuLayer (if menu open) - consumes input
  2. DialogueLayer (if dialogue active) - consumes input
  3. MapLayer (if in game) - handles movement
  4. Fallback to hotkeys (F5=save, I=inventory, etc)

InputEvent data:
  ├─ type: MOUSE_CLICK, TOUCH, KEY_DOWN, SWIPE
  ├─ button: LEFT, RIGHT, MIDDLE (mouse)
  ├─ key: W, A, S, D, E, ESC, etc (keyboard)
  ├─ x, y: Position in world coordinates
  ├─ screenX, screenY: Position in screen coordinates
  └─ timestamp: Event time for double-click detection


ASSET MANAGEMENT STRATEGY
═════════════════════════════════════════════════════════════════════════════════

Resources in res/ directory:

res/ui/
├── atlas/
│   ├── buttons.atlas        (button sprites)
│   ├── panels.atlas         (panel backgrounds)
│   ├── icons.atlas          (item/status icons)
│   ├── effects.atlas        (particle sprites)
│   └── characters.atlas     (character portraits)
│
├── fonts/
│   ├── ui_regular.fnt       (main UI font)
│   ├── ui_bold.fnt          (headings)
│   ├── ui_small.fnt         (labels)
│   └─── dialogue.fnt        (dialogue text)
│
└── animations/
    ├── damage_flash.anim    (red flash on damage)
    ├── heal_sparkle.anim    (green sparkles)
    ├── level_up.anim        (level up effect)
    └── transition_fade.anim (screen fade)

AssetManager loads on startup:
  ├─ All texture atlases (parallel load)
  ├─ All fonts (parallel load)
  ├─ Sound effects (lazy load)
  └─ Background music (streamed)

Caching strategy:
  ├─ Textures: Keep in memory (8-16 MB)
  ├─ Fonts: Keep in memory (2-4 MB)
  ├─ Sounds: Stream large audio
  └─ Cleanup: On memory warning


SAVE/LOAD UI FLOW
═════════════════════════════════════════════════════════════════════════════════

Main Menu
  ├─ New Game → StartGameMenu
  ├─ Load Game → LoadGameMenu
  ├─ Settings → SettingsMenu
  └─ Quit → Exit

StartGameMenu (Class/difficulty selection)
  ├─ Select class (warrior/mage/rogue)
  ├─ Select difficulty (easy/normal/hard)
  ├─ Name character
  └─ Create → LogicLayerAPI.newGame()

LoadGameMenu (Save slot selection)
  ├─ List save slots (with timestamp/character info)
  ├─ Select slot
  └─ Load → LogicLayerAPI.loadGame(slotName)

InGame Menu (Esc key)
  ├─ Continue Game
  ├─ Save Game (to current slot or new)
  ├─ Load Game → LoadGameMenu
  ├─ Settings
  └─ Quit to Main Menu

SaveGameMenu (Explicit save)
  ├─ List existing slots
  ├─ Option to overwrite
  ├─ Option to create new slot
  └─ Confirm → LogicLayerAPI.saveGame(slotName)


ANIMATION & EFFECTS SYSTEM
═════════════════════════════════════════════════════════════════════════════════

ParticleEmitter (reusable effect template)
  ├─ Position (x, y, z layer)
  ├─ Sprite (from texture atlas)
  ├─ Duration (how long effect plays)
  ├─ Count (number of particles)
  ├─ Velocity (direction/speed/spread)
  ├─ Rotation (spin angle)
  ├─ Scale (grow/shrink during lifetime)
  ├─ Alpha (fade in/out)
  ├─ Color (tint or gradient)
  └─ Lifetime (how long each particle exists)

Built-in Effects:
  ├─ DAMAGE_HIT (red flash on hit)
  ├─ HEAL_GLOW (green sparkles)
  ├─ LEVEL_UP (stars and text)
  ├─ SPELL_CAST (magical aura)
  ├─ ITEM_DROP (floating pickup)
  ├─ DEATH_EXPLODE (puff/dissolve)
  ├─ CRITICAL_HIT (yellow burst)
  └─ BUFF_APPLY (swirling effect)

Transition Effects:
  ├─ FADE_IN (fade from black)
  ├─ FADE_OUT (fade to black)
  ├─ SLIDE_LEFT (slide in from left)
  ├─ SLIDE_RIGHT (slide in from right)
  ├─ WIPE_HORIZONTAL (curtain wipe)
  └─ ZOOM_IN (zoom to center)


CODE PATTERNS & BEST PRACTICES
═════════════════════════════════════════════════════════════════════════════════

1. UI Component Pattern:
   ├─ Extend UIComponent
   ├─ Implement update(delta, logicApi)
   ├─ Implement render(batch, camera)
   ├─ Implement onInput(event)
   └─ Store state, query API in update()

2. Layer Pattern:
   ├─ Extend UILayer
   ├─ Manage list of UIComponents
   ├─ Override render() to iterate components
   ├─ Override update() to query API and update components
   └─ Delegate input to components

3. Asset Loading Pattern:
   ├─ Load all assets on startup (AssetManager)
   ├─ Store references in static cache
   ├─ Retrieve via AssetManager.getTexture(name)
   ├─ Never load assets in render loop
   └─ Dispose assets on shutdown

4. Platform Detection Pattern:
   ├─ Check Gdx.app.getType() at startup
   ├─ Set PlatformConfig.PLATFORM and PlatformConfig.IS_MOBILE
   ├─ Use in LayoutManager for responsive design
   ├─ Different input handlers for desktop/mobile
   └─ Different font sizes based on platform

5. Input Event Pattern:
   ├─ InputManager detects input (mouse/touch/key)
   ├─ Create InputEvent with type, position, button/key
   ├─ Dispatch to active layer
   ├─ Component consumes event (return true)
   ├─ If unconsumed, check hotkeys
   └─ Example:
       ```java
       if (inputEvent.type == InputEvent.TOUCH) {
           UIComponent target = hud.getComponentAt(inputEvent.screenX, inputEvent.screenY);
           if (target != null && target.onInput(inputEvent)) {
               return; // consumed
           }
       }
       ```

6. Query-Only API Usage:
   ├─ WRONG:  gameState.playerState.health = 100
   ├─ RIGHT:  int health = logicApi.getPlayerHealth()
   ├─ WRONG:  gameState.playerState.location = newLocation
   ├─ RIGHT:  logicApi.moveToLocation(direction)
   ├─ ALWAYS call api.getXxx() before rendering
   ├─ NEVER modify GameState directly from UI
   └─ All modifications go through LogicLayerAPI.actionXxx()


IMPLEMENTATION ROADMAP
═════════════════════════════════════════════════════════════════════════════════

Phase 3.1: Core Framework (500 LOC)
  ├─ LibGdxApp - entry point
  ├─ GameScreen - main loop
  ├─ ScreenManager - screen transitions
  ├─ InputManager - unified input
  └─ Compile & verify zero errors

Phase 3.2: Asset System (400 LOC)
  ├─ AssetManager - resource loading
  ├─ TextureCache - texture atlas
  ├─ FontCache - font management
  └─ SoundPlayer - audio playback

Phase 3.3: UI Components (600 LOC)
  ├─ UIComponent - base class
  ├─ UIButton - clickable
  ├─ UIPanel - container
  ├─ UIText - text display
  ├─ UIImage - sprite display
  └─ UIProgressBar - health/mana bars

Phase 3.4: UI Layers (700 LOC)
  ├─ UILayer - base layer
  ├─ MapLayer - world rendering
  ├─ HudLayer - status display
  ├─ MenuLayer - menus
  └─ DialogueLayer - NPC dialogue

Phase 3.5: Platform & Effects (400 LOC)
  ├─ PlatformConfig - platform detection
  ├─ LayoutManager - responsive layouts
  ├─ InputHandler - platform input
  ├─ ParticleEmitter - effects system
  └─ Transition - screen transitions

Phase 3.6: Integration (300 LOC)
  ├─ Main.java update
  ├─ LogicLayerAPI wiring
  ├─ Save/load UI
  ├─ Settings persistence
  └─ Full testing

Total: ~3000 LOC for complete UI layer

═══════════════════════════════════════════════════════════════════════════════

Next step: Implement Phase 3.1 (Core Framework)
