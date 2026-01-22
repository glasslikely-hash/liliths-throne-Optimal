# Step 4 Preparation: Persistence Integration
**Status**: Ready to Begin  
**Prerequisite**: Step 3 Phase 2 ✅ COMPLETE  

---

## Overview

Step 4 connects the completed UI framework to the game's logic layer, enabling:
1. **State Binding**: UI components automatically reflect game state changes
2. **Manual Saving**: Player can save game via Save button in menu
3. **Auto-Saving**: Game saves on state changes (entering new area, level up, etc.)
4. **Load Game**: Continue button loads saved game state

---

## State Binding Examples

### Example 1: HudLayer ↔ Player Stats

**Current Code** (HudLayer.java):
```java
// Dummy update - hardcoded values
public void setHealth(float health) {
    healthBar.setProgress(health / 100f);
    healthText.setText("Health: " + (int)health);
}
```

**Step 4 Integration**:
```java
// Update HudLayer from player state
@Override
public void update(float delta) {
    if (!visible) return;
    
    // Get live player data from LogicLayerAPI
    Player player = logicApi.getPlayer();
    if (player != null) {
        setHealth(player.getCurrentHealth());
        setMana(player.getCurrentMana());
        setStamina(player.getCurrentStamina());
        setLocation(player.getCurrentLocation());
        setLevel(player.getLevel());
    }
    
    // ... rest of update
}
```

### Example 2: MapLayer ↔ World State

**Current Code** (MapLayer.java):
```java
// Dummy tile rendering - checkerboard pattern
private void renderTerrain(SpriteBatch batch) {
    for (int x = 0; x < GRID_WIDTH; x++) {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            boolean isWater = (x + y) % 2 == 0;
            Color color = isWater ? Color.BLUE : Color.GRAY;
            // ... draw tile
        }
    }
}
```

**Step 4 Integration**:
```java
// Render actual world terrain
private void renderTerrain(SpriteBatch batch) {
    WorldState world = logicApi.getWorldState();
    if (world == null) return;
    
    for (int x = 0; x < GRID_WIDTH; x++) {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            Terrain terrain = world.getTerrain(x, y);
            if (terrain != null) {
                // Render based on terrain type (grass, water, mountain, etc.)
                renderTerrainTile(batch, x, y, terrain.getType());
            }
        }
    }
}
```

### Example 3: MenuLayer ↔ Inventory

**Current Code** (MenuLayer.java):
```java
// Dummy inventory
public void setMenuItems(String[] items) {
    itemList.clearItems();
    for (String item : items) {
        itemList.addItem(item);
    }
}
```

**Step 4 Integration**:
```java
// Bind to live inventory
public void openInventory() {
    menuOpen = true;
    setVisible(true);
    
    Inventory inventory = logicApi.getInventory();
    if (inventory != null) {
        String[] itemNames = new String[inventory.getItemCount()];
        for (int i = 0; i < inventory.getItemCount(); i++) {
            Item item = inventory.getItem(i);
            itemNames[i] = item.getName() + " (x" + item.getQuantity() + ")";
        }
        setMenuItems(itemNames);
    }
}
```

### Example 4: DialogueLayer ↔ Quest System

**Current Code** (DialogueLayer.java):
```java
// Dummy dialogue
public void startDialogue(String npcName, String dialogueText, String[] choices) {
    dialogueActive = true;
    setVisible(true);
    
    speakerName.setText(npcName);
    this.fullDialogueText = dialogueText;
    // ... set choices
}
```

**Step 4 Integration**:
```java
// Load dialogue from quest system
public void startDialogueWithNPC(String npcId) {
    NPC npc = logicApi.getNPC(npcId);
    if (npc == null) return;
    
    DialogueTree tree = logicApi.getDialogueTree(npc, player);
    if (tree == null) return;
    
    DialogueNode currentNode = tree.getCurrentNode();
    startDialogue(
        npc.getName(),
        currentNode.getText(),
        currentNode.getChoiceTexts()
    );
}

// Handle choice selection
private void selectChoice(int choiceIndex) {
    DialogueTree tree = logicApi.getDialogueTree(currentNPC, player);
    DialogueNode nextNode = tree.selectChoice(choiceIndex);
    
    if (nextNode != null && nextNode.hasMoreChoices()) {
        // Continue dialogue
        startDialogueWithNPC(currentNPC.getId());
    } else {
        // Dialogue complete - trigger rewards/quest updates
        endDialogue();
        logicApi.completeDialogue(currentNPC, choiceIndex);
    }
}
```

---

## SaveManager Implementation (Step 4 Phase 2)

### Save Format (Binary)
```
[GameState Header]
  - Version: 1 (int)
  - Timestamp: (long)
  - PlayerName: (String)

[Player Data]
  - Position: X, Y, Z (float)
  - Stats: HP, Mana, Stamina, Level (int/float)
  - Inventory: Item IDs and quantities (serialized)
  - Equipment: Equipped item slots (serialized)

[World Data]
  - Map state: Terrain modifications (delta from golden copy)
  - NPC states: Visited flags, dialogue choices (serialized)
  - Quest progress: Completed tasks (serialized)

[Timestamp Footer]
  - Created: (long)
  - Playtime: (long)
```

### SaveManager API
```java
public class SaveManager {
    // Manual saves
    public static boolean saveGame(String slotName) throws IOException;
    public static GameState loadGame(String slotName) throws IOException;
    
    // Auto-saves (called by LogicLayer on state changes)
    public static void autoSave();
    public static void autoSaveIfNeeded();
    
    // Save slots
    public static List<SaveSlot> listSaveSlots();
    public static boolean deleteSaveSlot(String slotName);
    
    // Metadata
    public static SaveSlot getSaveSlotInfo(String slotName);
}
```

---

## Integration Checklist for Step 4

### Phase 4.1: State Binding (HudLayer, MapLayer)
- [ ] Read LogicLayerAPI interface methods
- [ ] Update HudLayer.update() to pull player stats
- [ ] Update MapLayer.update() to pull world state
- [ ] Update MapLayer.render() to display actual terrain
- [ ] Test with dummy LogicLayerAPI returning test data

### Phase 4.2: Menu & Dialogue Integration
- [ ] Update MenuLayer.openInventory() to load inventory
- [ ] Update DialogueLayer.startDialogueWithNPC() for quest trees
- [ ] Implement choice callbacks to LogicLayerAPI
- [ ] Test menu and dialogue with dummy data

### Phase 4.3: SaveManager Creation
- [ ] Create SaveManager class with binary persistence
- [ ] Implement GameState serialization
- [ ] Implement save slot management
- [ ] Create save/load UI screens

### Phase 4.4: Full Integration Test
- [ ] Start game, load main menu
- [ ] Create new game → verify state binding
- [ ] Change stats → verify HUD updates
- [ ] Save game → verify file creation
- [ ] Load game → verify state restoration
- [ ] Test inventory, dialogue, quest progress

---

## Files Already Prepared for Step 4

### UI Classes Ready for Integration
- HudLayer.java: `setHealth()`, `setMana()`, `setStamina()` methods ready
- MapLayer.java: Camera positioning ready, terrain rendering framework ready
- MenuLayer.java: `openInventory()` method ready
- DialogueLayer.java: `startDialogue()`, `selectChoice()` methods ready

### LogicLayerAPI Ready
- `getPlayer()` - returns Player entity
- `getWorldState()` - returns WorldState with terrain/NPCs
- `getInventory()` - returns Inventory with items
- `getDialogueTree()` - returns DialogueTree for NPC
- (All other methods documented in LogicLayerAPI.java)

### Missing Classes (To Be Created in Step 4)
- SaveManager.java - Binary save/load system
- SaveSlot.java - Save file metadata
- SaveLoadScreen.java - UI for selecting save slots
- (All support by LogicLayerAPI delegation)

---

## Key Architecture Points for Step 4

### Data Flow
```
GameScreen (main)
  ↓ update(delta) each frame
  ├─ HudLayer.update() → queries LogicLayerAPI.getPlayer()
  ├─ MapLayer.update() → queries LogicLayerAPI.getWorldState()
  ├─ MenuLayer.update() → queries LogicLayerAPI.getInventory() (if open)
  └─ DialogueLayer.update() → queries LogicLayerAPI.getDialogueState() (if active)

User Interaction (input)
  ├─ MenuLayer.onInput() → calls openInventory()/closeMenu()
  ├─ DialogueLayer.onInput() → calls selectChoice(index)
  └─ MainMenuScreen.onInput() → calls SaveManager.saveGame() or loadGame()
```

### State Consistency
- All reads from LogicLayerAPI are **read-only** (query methods)
- All writes to game state go through **LogicLayerAPI methods** (not UI directly)
- Saves are triggered by **state change events** from LogicLayer, not UI
- Loads restore entire game state atomically (no partial loads)

---

## Recommended Reading for Step 4

1. **LogicLayerAPI.java** - Full interface for logic layer queries
2. **Player.java** - Player entity with stats
3. **WorldState.java** - World data structure
4. **Inventory.java** - Item management
5. **Existing SaveManager** (if present from earlier work)

---

## Success Criteria for Step 4

### Full Integration Complete
- ✅ HudLayer displays live player stats
- ✅ MapLayer displays live world terrain
- ✅ MenuLayer displays live inventory
- ✅ DialogueLayer loads live NPC dialogue trees
- ✅ Save button creates binary save file
- ✅ Load button restores game from file
- ✅ All tests pass without errors

### Code Quality
- ✅ Zero compilation errors
- ✅ All methods documented
- ✅ Error handling for missing state
- ✅ Graceful fallback for null values

---

**Next Command**: `proceed. check the LogicLayerAPI to understand available state methods.`
