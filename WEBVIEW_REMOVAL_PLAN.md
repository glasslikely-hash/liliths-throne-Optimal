# WebView/JS Removal & Android UI Abstraction - Implementation Plan

## Current State Analysis

**WebEngine Usage**: 150+ active calls across the codebase
- Direct DOM manipulation via `executeScript()`
- Document access via `getDocument()`
- Form input retrieval via JavaScript execution
- HTML content manipulation

**Files with WebEngine Dependencies** (Most Critical):
1. CharacterCreation.java - 10+ calls
2. OptionsDialogue.java - 8+ calls
3. EnchantmentDialogue.java - 20+ calls
4. CosmeticsDialogue.java - 6+ calls
5. DebugDialogue.java - 4+ calls
6. Game.java - 6+ calls
7. FileController.java - 4+ calls
8. MainController.java - 50+ calls (registration, event handling)
9. Multiple dialogue files - 50+ calls

## Solution Architecture

### Phase 1: Create UI Abstraction Layer (2-3 hours)

**Core Pattern**: Extract all UI operations into platform-independent interfaces

```
GameLogic
    ↓
UIManager (Interface & Factory)
    ├─ DesktopUIManager (JavaFX WebView - current platform)
    ├─ LibGDXUIManager (Cross-platform rendering - future)
    └─ AndroidUIManager (Android native - future)
```

### Files to Create

1. **UIManager.java** (Interface)
   - `executeScript(String script, UICallback callback)`
   - `getDocument() -> Document`
   - `setContent(String html)`
   - `getFormValue(String elementId) -> String`
   - `setFormValue(String elementId, String value)`
   - `getElementById(String id) -> UIElement`

2. **DesktopUIManager.java** (Desktop Implementation)
   - Wraps WebEngine calls
   - Maintains existing functionality

3. **UICallbackAdapter.java** (Pattern)
   - Converts sync WebEngine calls to async callbacks

4. **FormDataService.java** (Utility)
   - Centralized form data management
   - Replaces scattered getWebEngine() calls

### Phase 2: Refactor DialogueNodes (2-3 hours)

Extract WebEngine operations from 20+ dialogue files:
- Replace direct DOM manipulation
- Use FormDataService for form access
- Use UIManager callbacks for asynchronous operations

### Phase 3: Refactor MainController (2-3 hours)

Core controller refactoring:
- Event listener registration via UIManager
- Content updates via UIManager callbacks
- Document manipulation abstraction

### Phase 4: Complete Android Implementation (4-6 hours)

Once abstraction is complete, Android support is straightforward:
- Create AndroidUIManager
- No changes needed to game logic

---

## Implementation Order

1. **Step 1**: Create UIManager interface + DesktopUIManager (1 hour)
2. **Step 2**: Create FormDataService (30 min)
3. **Step 3**: Refactor highest-impact files first:
   - FileController.java (4 calls)
   - Game.java (6 calls)
   - CosmeticsDialogue.java (6 calls)
4. **Step 4**: Refactor DialogueUtils (EnchantmentDialogue, etc.)
5. **Step 5**: Refactor CharacterCreation (10+ calls)
6. **Step 6**: Refactor OptionsDialogue (8+ calls)
7. **Step 7**: Complete MainController refactoring
8. **Step 8**: Remove remaining direct WebEngine references

---

## Success Criteria

✅ Zero direct WebEngine references in game logic
✅ All form operations go through FormDataService
✅ All DOM operations go through UIManager
✅ Desktop build still works perfectly
✅ Android build can now proceed

---

## Effort Estimate

| Phase | Task | Time | Status |
|-------|------|------|--------|
| 1 | Create UIManager interface | 45 min | Not Started |
| 1 | Create DesktopUIManager | 30 min | Not Started |
| 2 | Create FormDataService | 30 min | Not Started |
| 3 | Refactor high-impact files (6) | 1.5 hours | Not Started |
| 4 | Refactor DialogueUtils (6 files) | 1 hour | Not Started |
| 5 | Refactor CharacterCreation | 45 min | Not Started |
| 6 | Refactor OptionsDialogue | 45 min | Not Started |
| 7 | Complete MainController | 1 hour | Not Started |
| 8 | Final cleanup & testing | 30 min | Not Started |
| | **TOTAL** | **~7 hours** | |

---

## Next Steps

1. User confirmation of approach
2. Create UIManager interface
3. Implement DesktopUIManager
4. Begin systematic refactoring
