╔═══════════════════════════════════════════════════════════════════════════════╗
║              STEP 3 UI LAYER - QUICK REFERENCE GUIDE                           ║
║                   Implementation Patterns & API Usage                           ║
╚═══════════════════════════════════════════════════════════════════════════════╝

QUICK START
═════════════════════════════════════════════════════════════════════════════════

Minimal LibGDX App (Main.java):
────────────────────────────────────────────────────────────────────────────────

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.lilithsthrone.ui.LibGdxApp;
import com.lilithsthrone.ui.platform.PlatformConfig;

public class Main {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        config.setTitle("Lilith's Throne");
        config.setWindowedMode(1200, 800);
        config.setResizable(true);
        config.setVSync(true);
        config.setForegroundFPS(60);
        
        new Lwjgl3Application(new LibGdxApp(), config);
    }
}

Note: For mobile (Android), use different application class
  (AndroidApplication for Android, IOSApplication for iOS)


SCREEN MANAGEMENT EXAMPLES
═════════════════════════════════════════════════════════════════════════════════

Switching Screens (Instant):
────────────────────────────────────────────────────────────────────────────────
ScreenManager screenManager = libGdxApp.getScreenManager();
screenManager.setScreen(ScreenManager.ScreenType.GAME);

Switching with Transition (Fade):
────────────────────────────────────────────────────────────────────────────────
Transition fade = new Transition.Fade(0.5f);  // 0.5 second fade
screenManager.setScreenWithTransition(ScreenManager.ScreenType.GAME, fade);

Switching with Transition (Slide):
────────────────────────────────────────────────────────────────────────────────
Transition slide = new Transition.Slide(0.3f, Transition.Slide.Direction.LEFT);
screenManager.setScreenWithTransition(ScreenManager.ScreenType.MENU, slide);

Switching with Transition (Wipe):
────────────────────────────────────────────────────────────────────────────────
Transition wipe = new Transition.Wipe(0.4f, Transition.Wipe.Direction.HORIZONTAL);
screenManager.setScreenWithTransition(ScreenManager.ScreenType.INVENTORY, wipe);

Check Current Screen:
────────────────────────────────────────────────────────────────────────────────
ScreenManager.ScreenType current = screenManager.getCurrentScreenType();
if (current == ScreenManager.ScreenType.GAME) {
    // In gameplay
}

Check if Transitioning:
────────────────────────────────────────────────────────────────────────────────
if (screenManager.isTransitioning()) {
    // Disable input during transition
}


INPUT HANDLING EXAMPLES
═════════════════════════════════════════════════════════════════════════════════

In a Screen's update() method:
────────────────────────────────────────────────────────────────────────────────

private InputManager inputManager;  // Provided by BaseScreen

@Override
public void update(float delta) {
    // Update input state
    inputManager.update(delta);
    
    // Check keyboard (desktop)
    if (inputManager.isKeyPressed(Input.Keys.W)) {
        // W key held down
    }
    
    if (inputManager.isKeyJustPressed(Input.Keys.E)) {
        // E key pressed this frame only
    }
    
    // Check mouse (desktop)
    int mouseX = inputManager.getMouseX();
    int mouseY = inputManager.getMouseY();
    
    if (inputManager.isMouseButtonJustPressed(Input.Buttons.LEFT)) {
        // Left click
        onMouseClick(mouseX, mouseY);
    }
    
    // Check touch (mobile)
    if (inputManager.isTouchPressed(0)) {
        // Finger 0 is touching screen
        float touchX = inputManager.getTouchX(0);
        float touchY = inputManager.getTouchY(0);
    }
}

Check Multiple Keys (simulate movement):
────────────────────────────────────────────────────────────────────────────────

int dirX = 0;
int dirY = 0;

if (inputManager.isKeyPressed(Input.Keys.W)) dirY += 1;
if (inputManager.isKeyPressed(Input.Keys.S)) dirY -= 1;
if (inputManager.isKeyPressed(Input.Keys.A)) dirX -= 1;
if (inputManager.isKeyPressed(Input.Keys.D)) dirX += 1;

if (dirX != 0 || dirY != 0) {
    // Move player
    movePlayer(dirX, dirY);
}


LOGIC LAYER API EXAMPLES (Query Only)
═════════════════════════════════════════════════════════════════════════════════

Get from LibGdxApp:
────────────────────────────────────────────────────────────────────────────────
LogicLayerAPI api = libGdxApp.getLogicLayerAPI();

Query Player Health:
────────────────────────────────────────────────────────────────────────────────
int health = api.getPlayerHealth();
int maxHealth = api.getPlayerMaxHealth();
int percent = (health * 100) / maxHealth;
drawHealthBar(x, y, width, height, percent);

Query Player Location:
────────────────────────────────────────────────────────────────────────────────
String location = api.getPlayerLocation();
hud.setLocationText(location);

Query Inventory:
────────────────────────────────────────────────────────────────────────────────
List<Item> items = api.getInventoryItems();
for (Item item : items) {
    drawInventorySlot(item.id, item.name, item.iconTexture);
}

List<Item> equipped = api.getEquippedItems();
for (Item item : equipped) {
    drawEquippedSlot(item.slot, item.iconTexture);
}

Query Active Effects:
────────────────────────────────────────────────────────────────────────────────
List<StatusEffect> effects = api.getActiveEffects();
for (StatusEffect effect : effects) {
    drawStatusIcon(effect.iconTexture, effect.duration);
}

Action: Move Player:
────────────────────────────────────────────────────────────────────────────────
// When player presses direction key
if (inputManager.isKeyJustPressed(Input.Keys.W)) {
    try {
        api.moveToLocation("north");
        // Engine updates game state, next render will show new location
    } catch (Exception e) {
        hud.showError("Cannot move: " + e.getMessage());
    }
}

Action: Start Combat:
────────────────────────────────────────────────────────────────────────────────
if (inputManager.isKeyJustPressed(Input.Keys.SPACE)) {
    try {
        api.startCombat("npc_goblin_001");
        // Switch to combat screen
        screenManager.setScreen(ScreenManager.ScreenType.GAME);
    } catch (Exception e) {
        hud.showError("Cannot attack: " + e.getMessage());
    }
}

Action: Use Item:
────────────────────────────────────────────────────────────────────────────────
void onInventoryItemSelected(String itemId) {
    try {
        api.useItem(itemId);
        // Item consumed, next render shows updated inventory
        refreshInventoryUI();
    } catch (Exception e) {
        menu.showError("Cannot use item: " + e.getMessage());
    }
}

Action: Save Game:
────────────────────────────────────────────────────────────────────────────────
void onQuickSave() {
    try {
        api.saveGame("quicksave");
        hud.showNotification("Game saved!");
    } catch (Exception e) {
        hud.showError("Save failed: " + e.getMessage());
    }
}

CRITICAL CONSTRAINT:
  ├─ NEVER modify GameState directly
  ├─ NEVER call engine methods directly
  ├─ ONLY use LogicLayerAPI methods
  ├─ Query methods are safe (return copies)
  └─ Action methods go through proper validation


PLATFORM-SPECIFIC CODE
═════════════════════════════════════════════════════════════════════════════════

Check if Mobile:
────────────────────────────────────────────────────────────────────────────────
if (PlatformConfig.IS_MOBILE) {
    // Use larger fonts and buttons
    // Use touch-friendly layouts
    // Disable right-click menus
    // Use virtual D-pad instead of keyboard
} else {
    // Desktop layout
    // Use keyboard shortcuts
    // Support mouse right-click
}

Get Platform:
────────────────────────────────────────────────────────────────────────────────
switch (PlatformConfig.PLATFORM) {
    case ANDROID:
        // Android-specific code
        break;
    case IOS:
        // iOS-specific code
        break;
    case WINDOWS:
    case MACOS:
    case LINUX:
        // Desktop code
        break;
}

Responsive Layout (by screen size):
────────────────────────────────────────────────────────────────────────────────
LayoutManager.Breakpoint bp = LayoutManager.getBreakpoint();
switch (bp) {
    case SMALL:     // 800x600 - minimal UI
        fontSize = 12;
        buttonWidth = 60;
        break;
    case MEDIUM:    // 1024x768 - standard
        fontSize = 14;
        buttonWidth = 80;
        break;
    case LARGE:     // 1280x720 - expanded
        fontSize = 16;
        buttonWidth = 100;
        break;
    case XLARGE:    // 1920x1080+ - full
        fontSize = 18;
        buttonWidth = 120;
        break;
}

Safe Area (mobile notches):
────────────────────────────────────────────────────────────────────────────────
float safeLeft = LayoutManager.getSafeAreaLeft();
float safeRight = LayoutManager.getSafeAreaRight();
float safeTop = LayoutManager.getSafeAreaTop();
float safeBottom = LayoutManager.getSafeAreaBottom();

// Draw HUD only within safe area
drawHUD(safeLeft, safeBottom, safeRight - safeLeft, safeTop - safeBottom);

Scale UI Elements:
────────────────────────────────────────────────────────────────────────────────
float buttonWidth = LayoutManager.getScaledWidth(80f);  // Base 80px
float buttonHeight = LayoutManager.getScaledHeight(40f);
float fontSize = PlatformConfig.getScaledFontSize(14);

Center UI Elements:
────────────────────────────────────────────────────────────────────────────────
float panelWidth = 600f;
float panelHeight = 400f;
float panelX = LayoutManager.getCenteredX(panelWidth);
float panelY = LayoutManager.getCenteredY(panelHeight);
drawPanel(panelX, panelY, panelWidth, panelHeight);


ASSET MANAGEMENT EXAMPLES
═════════════════════════════════════════════════════════════════════════════════

In LibGdxApp.create():
────────────────────────────────────────────────────────────────────────────────
AssetManager.initialize(Gdx.files);
AssetManager.loadAllAssets();  // Block until loaded

In a Screen's render():
────────────────────────────────────────────────────────────────────────────────
Texture characterAtlas = AssetManager.getTexture("characters");
if (characterAtlas != null) {
    batch.draw(characterAtlas, x, y);
}

BitmapFont uiFont = AssetManager.getFont("ui_regular");
if (uiFont != null) {
    uiFont.draw(batch, "Health: " + health, x, y);
}

Load Sound:
────────────────────────────────────────────────────────────────────────────────
Sound damageSound = AssetManager.getSound("impact");
if (damageSound != null) {
    damageSound.play(PlatformConfig.SFX_VOLUME);
}

Load Music:
────────────────────────────────────────────────────────────────────────────────
Music battleMusic = AssetManager.getMusic("battle_theme");
if (battleMusic != null) {
    battleMusic.setLooping(true);
    battleMusic.setVolume(PlatformConfig.MUSIC_VOLUME);
    battleMusic.play();
}

Check Loading Progress:
────────────────────────────────────────────────────────────────────────────────
float progress = AssetManager.getLoadProgress();
if (progress < 1.0f) {
    drawLoadingBar(progress);
} else {
    // All loaded, hide loading screen
}


COMMON PATTERNS
═════════════════════════════════════════════════════════════════════════════════

Game Loop Pattern (in GameScreen.update):
────────────────────────────────────────────────────────────────────────────────
@Override
public void update(float delta) {
    // 1. Update input
    inputManager.update(delta);
    
    // 2. Handle player input
    handlePlayerMovement();
    handlePlayerActions();
    handleMenuInput();
    
    // 3. Query game state
    int health = logicLayerAPI.getPlayerHealth();
    String location = logicLayerAPI.getPlayerLocation();
    List<Item> inventory = logicLayerAPI.getInventoryItems();
    
    // 4. Update UI state based on game state
    hud.updateHealthBar(health);
    hud.updateLocationLabel(location);
    inventoryPanel.updateItems(inventory);
    
    // 5. Update game logic (time-based animations, etc)
    updateParticles(delta);
    updateAnimations(delta);
}

Render Pattern:
────────────────────────────────────────────────────────────────────────────────
@Override
public void render(SpriteBatch batch) {
    // 1. Draw background/map
    mapLayer.render(batch);
    
    // 2. Draw game objects
    drawCharacters(batch);
    drawNPCs(batch);
    drawItems(batch);
    
    // 3. Draw HUD
    hud.render(batch);
    
    // 4. Draw menus/overlays
    if (menuOpen) {
        menuLayer.render(batch);
    }
    
    // 5. Draw effects/particles
    effectsLayer.render(batch);
}

Screen Lifecycle Pattern:
────────────────────────────────────────────────────────────────────────────────
@Override
public void show() {
    // Load assets specific to this screen
    // Initialize UI state
    // Start music/sounds
    AssetManager.getMusic("main_theme").play();
}

@Override
public void hide() {
    // Stop music
    // Save UI state if needed
    AssetManager.getMusic("main_theme").stop();
}

@Override
public void dispose() {
    // No explicit disposal needed - AssetManager handles it
}

Error Handling Pattern:
────────────────────────────────────────────────────────────────────────────────
try {
    logicLayerAPI.moveToLocation("north");
} catch (IllegalArgumentException e) {
    hud.showError("Cannot move in that direction");
} catch (IllegalStateException e) {
    hud.showError("Cannot move while in combat");
} catch (Exception e) {
    hud.showError("Move failed: " + e.getMessage());
}

Check State Before Action:
────────────────────────────────────────────────────────────────────────────────
boolean isInCombat = logicLayerAPI.isInCombat();
if (!isInCombat) {
    api.moveToLocation("north");  // OK
} else {
    hud.showError("Cannot move during combat");
}


EXPECTED DIRECTORY STRUCTURE
═════════════════════════════════════════════════════════════════════════════════

src/com/lilithsthrone/ui/
├── LibGdxApp.java              ✓ Core app
├── BaseScreen.java             ✓ Base class
├── ScreenManager.java          ✓ Screen management
├── Transition.java             ✓ Transition effects
│
├── input/
│   └── InputManager.java       ✓ Input handling
│
├── platform/
│   ├── PlatformConfig.java     ✓ Platform detection
│   └── LayoutManager.java      ✓ Responsive layouts
│
├── assets/
│   └── AssetManager.java       ✓ Asset loading
│
├── components/        (Phase 3.3)
│   ├── UIComponent.java
│   ├── UIButton.java
│   ├── UIPanel.java
│   ├── UIText.java
│   ├── UIImage.java
│   ├── UIProgressBar.java
│   └── UISlider.java
│
├── layers/            (Phase 3.4)
│   ├── UILayer.java
│   ├── MapLayer.java
│   ├── HudLayer.java
│   ├── MenuLayer.java
│   └── DialogueLayer.java
│
└── effects/           (Phase 3.5)
    ├── ParticleEmitter.java
    └── AnimationPlayer.java

═════════════════════════════════════════════════════════════════════════════════
