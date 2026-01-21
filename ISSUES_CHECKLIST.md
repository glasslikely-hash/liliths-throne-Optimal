# Quick Reference: Issues Checklist

## CRITICAL ISSUES - Fix First (4 issues, ~4 hours)

### [ ] 1. Remove Main.game access from GameLoopAdapter
- **File:** src/com/lilithsthrone/logic/GameLoopAdapter.java
- **Instances:** 9 methods, 10+ violations
- **What to remove:** All `Main.game.getLogicLayerAPI()` and `Main.game.getEventEngine()`
- **Replacement:** Add methods to LogicLayerAPI for event notification
- **Lines affected:** 31, 62, 88, 112, 136, 162, 190, 215, 242, 268
- **Time:** 1 hour

### [ ] 2. Remove Main.game access from QuestDialogueAdapter  
- **File:** src/com/lilithsthrone/logic/quest/QuestDialogueAdapter.java
- **Instances:** isBridgeAvailable() checks Main.game and Main.game.getEventEngine()
- **Lines affected:** 229-230
- **Time:** 30 minutes

### [ ] 3. Fix/Remove Placeholder Implementations
- **File:** src/com/lilithsthrone/logic/persistence/BinaryAssetLoader.java
  - [ ] loadCharacter() - line 105
  - [ ] loadDialogues() - line 115
  - [ ] loadCombatMoves() - line 125
  - [ ] loadWeapon() - line 135
  - [ ] loadOutfit() - line 145
- **Choose:** Implement OR mark as abstract/future OR throw UnsupportedOperationException
- **Time:** 1.5 hours

### [ ] 4. Fix DeltaEngine placeholder implementation
- **File:** src/com/lilithsthrone/logic/persistence/DeltaEngine.java
- **Method:** applyFieldChange() - line 135
- **Action:** Implement field change application
- **Time:** 1 hour

---

## HIGH PRIORITY - Complete This Week (10 issues, ~12 hours)

### [ ] 5. Extract Duplicate Code Pattern from Adapters
- **Files:** GameLoopAdapter.java, QuestDialogueAdapter.java
- **Pattern:** isBridgeAvailable() + try-catch + API access
- **Solution:** Create shared utility method or base class
- **Time:** 2 hours

### [ ] 6. Remove Unused Constants (13 fields)
- **File:** src/com/lilithsthrone/logic/GameStateAdapter.java
- **Lines:** 30-50
- **Constants to delete:** KEY_PLAYER_HEALTH, KEY_PLAYER_MANA, KEY_PLAYER_LEVEL, KEY_PLAYER_EXPERIENCE, KEY_PLAYER_LOCATION, KEY_ACTIVE_QUESTS, KEY_COMPLETED_QUESTS, KEY_ACTIVE_EFFECTS, KEY_ACTIVE_PERKS, KEY_NPC_STATES, KEY_VISITED_LOCATIONS, and 2 more
- **Time:** 15 minutes

### [ ] 7. Remove Unused Variable in CharacterEngine
- **File:** src/com/lilithsthrone/logic/engines/CharacterEngine.java
- **Line:** 116
- **Variable:** baseDamage
- **Action:** Either use in calculation or remove
- **Time:** 10 minutes

### [ ] 8. Cache Color Objects in UI Controller
- **File:** src/com/lilithsthrone/ui/controllers/StatusPanelController.java
- **Lines:** 218, 222, 226, 323
- **Action:** Move `new Color(...)` to static final fields
- **Static fields to add:**
  - COLOR_HP = new Color(1f, 0.2f, 0.2f, 1f)
  - COLOR_MANA = new Color(0.2f, 0.5f, 1f, 1f)
  - COLOR_STAMINA = new Color(0.2f, 1f, 0.2f, 1f)
  - COLOR_DEBUFF = new Color(1f, 0.5f, 0.5f, 1f)
- **Benefit:** -240 Color objects/second ≈ 8% GC reduction
- **Time:** 30 minutes

### [ ] 9. Replace String Concatenation with String.format()
- **File:** src/com/lilithsthrone/logic/quest/QuestDialogueAdapter.java
- **Lines:** 54, 88, 93, 121, 144, 190
- **Pattern to fix:** "quest_" + questId + "_dialogue_" + milestone
- **Replace with:** String.format("quest_%s_dialogue_%s", questId, milestone)
- **Time:** 30 minutes

### [ ] 10. Create GameConfig Class
- **New file:** src/com/lilithsthrone/config/GameConfig.java
- **Contents:** Consolidate all hardcoded values
- **Source from:**
  - GameLoopCoordinator.java (lines 18-28)
  - CharacterEngine.java (lines 32-33, 110-111)
  - WorldEngine.java (line 32)
  - GameIntegrationBridge.java (line 36)
  - LogicLayerAPI.java (lines 78-79)
  - PersistenceManager.java (line 75)
- **Time:** 1.5 hours

### [ ] 11. Create Shared Adapter Utility or Base Class
- **New file:** src/com/lilithsthrone/logic/adapters/AdapterUtil.java (or BaseAdapter.java)
- **Consolidate:**
  - isBridgeAvailable() - eliminate duplicate in 2 files
  - Error handling pattern - eliminate duplicate in 10 methods
  - Safe API execution pattern
- **Time:** 1.5 hours

### [ ] 12. Replace System.out.println() with Logger (All Core Files)
- **Files to update:** (approx 30+ locations)
  - All Engine classes (BuffEngine, CharacterEngine, EventEngine, QuestEngine, WorldEngine, GameEngines, RemainingEngines)
  - GameStateAdapter.java
  - GameLoopAdapter.java
  - GameLoopCoordinator.java
- **Add to each file:**
  ```java
  private static final Logger logger = LoggerFactory.getLogger(ClassName.class);
  ```
- **Replace:** System.out.println("[ClassName] msg") → logger.info("msg")
- **Replace:** System.err.println("[ClassName] Error msg") → logger.error("Error msg")
- **Time:** 3 hours

### [ ] 13. Add Null Validation to Methods
- **File:** src/com/lilithsthrone/logic/GameStateAdapter.java
- **Method:** getAdaptedValue() - line 70
- **Add:** if (fieldName == null) throw new IllegalArgumentException("fieldName cannot be null")
- **Time:** 15 minutes

### [ ] 14. Implement Specific Exception Handling
- **Files:** GameLoopAdapter.java, other adapters
- **Change:** From generic `catch (Exception e)` to specific exception types
- **Time:** 2 hours

---

## MEDIUM PRIORITY - Complete This Sprint (16 issues, ~10 hours)

### [ ] 15. Replace Wildcard Imports with Specific Imports
- **Files:** All Engine classes
  - BuffEngine.java
  - CharacterEngine.java
  - EventEngine.java
  - QuestEngine.java
  - WorldEngine.java
  - GameEngines.java
  - RemainingEngines.java
- **Change:** `import java.util.*;` → specific imports
- **Time:** 1 hour

### [ ] 16. Remove Redundant Null Checks in GameLoopAdapter
- **Lines:** All delegate methods
- **Issue:** Double null checking
- **Fix:** Remove inner `if (logicAPI != null)` since isBridgeAvailable() already checks
- **Time:** 30 minutes

### [ ] 17. Add @Nullable Annotation to Placeholder Methods
- **File:** src/com/lilithsthrone/logic/persistence/BinaryAssetLoader.java
- **Add:** `@Nullable` annotation from javax.annotation
- **Methods:** All load* methods
- **Time:** 20 minutes

### [ ] 18. Cache String Formatting Results
- **File:** src/com/lilithsthrone/ui/controllers/StatusPanelController.java
- **Lines:** 206, 263, 285
- **Option 1:** Cache last formatted strings and only update when values change
- **Option 2:** Use StringBuilder instead of String.format()
- **Time:** 1 hour

### [ ] 19. Add Configuration Validation
- **File:** src/com/lilithsthrone/config/GameConfig.java
- **Add:** Static initializer that validates all configuration values
- **Check:** No negative numbers for time intervals, positive attributes, etc.
- **Time:** 1 hour

### [ ] 20. Document Incomplete Implementations
- **Action:** Add @Deprecated or @Future annotation to placeholder methods
- **Example:** 
  ```java
  @Deprecated(forRemoval = true)
  @Future("Implement binary asset loading")
  public Character loadCharacter(String id) {
      throw new UnsupportedOperationException("Asset loading not yet implemented");
  }
  ```
- **Time:** 30 minutes

### [ ] 21. Extract Common Logging Pattern
- **Consolidate:** Create LoggingUtil class for consistent format
- **Pattern:** [ComponentName] message
- **Time:** 1.5 hours

### [ ] 22. Add Null Checks to GameStateAdapter Methods
- **Methods to update:**
  - getAdaptedValue() - check fieldName
  - adaptGameStateToLegacy() - check fieldName
  - validateConsistency() - check gameState
- **Time:** 1 hour

### [ ] 23. Review and Fix NPC Respawn Time Hardcoding
- **File:** src/com/lilithsthrone/logic/engines/WorldEngine.java
- **Line:** 32
- **Move to:** GameConfig.java as NPC_RESPAWN_TIME_SECONDS
- **Time:** 15 minutes

### [ ] 24. Fix Conflicting Autosave Intervals
- **Issue:** Different files have different intervals (30s vs 300s)
- **Files:**
  - GameLoopCoordinator.java: 30 seconds
  - PersistenceManager.java: 300 seconds (5 minutes)
- **Fix:** Use single value from GameConfig
- **Time:** 30 minutes

### [ ] 25. Update FrameRateOptimizer Integration
- **Review:** Match timing constants with GameConfig
- **File:** src/com/lilithsthrone/optimization/FrameRateOptimizer.java
- **Time:** 1 hour

### [ ] 26. Add Asset Loading Abstract Base
- **Create:** BaseAssetLoader abstract class
- **File:** src/com/lilithsthrone/logic/persistence/BaseAssetLoader.java
- **Purpose:** Define contract for asset loaders, mark methods as abstract
- **Time:** 1.5 hours

### [ ] 27. Implement Proper Error Codes
- **Create:** enum GameError or ErrorCode
- **Use in:** Exception handling throughout logic layer
- **Time:** 2 hours

### [ ] 28. Add Try-Finally for Resource Cleanup
- **Review:** BinaryStream usage
- **Ensure:** Files are closed properly even on exception
- **Time:** 1.5 hours

### [ ] 29. Document API Contracts
- **Add:** @throws documentation to all methods
- **Example:** "Throws UnsupportedOperationException if asset loading not implemented"
- **Time:** 2 hours

### [ ] 30. Performance Profiling Prep
- **Add:** Performance counters to GameConfig
- **Enable:** JMH benchmarks for critical paths
- **Time:** 2 hours

---

## LOW PRIORITY - Polish (8 issues, ~5 hours)

### [ ] 31. Replace forEach with Iterator where needed
- **File:** src/com/lilithsthrone/logic/engines/WorldEngine.java
- **Line:** 57
- **Status:** Very minor, low priority
- **Time:** 10 minutes

### [ ] 32. Add Javadoc Comments
- **Add:** @param, @return, @throws to all public methods
- **Time:** 3 hours

### [ ] 33. Create Unit Tests
- **Test files to create:**
  - GameStateAdapterTest
  - BinaryAssetLoaderTest
  - DeltaEngineTest
  - GameConfigTest
- **Time:** 4 hours

### [ ] 34. Update README with Architecture Diagram
- **Include:** Proper layer separation after refactoring
- **Time:** 1 hour

### [ ] 35. Create Code Style Guide Document
- **Establish:** Standards for new code
- **Include:** Logging, error handling, null safety
- **Time:** 1 hour

### [ ] 36. Add SonarQube/Checkstyle Configuration
- **File:** pom.xml
- **Automate:** Code quality checks
- **Time:** 1.5 hours

### [ ] 37. Optimize HashMap Usage
- **Review:** All Map instantiations
- **Consider:** Capacity hints for large maps
- **Time:** 30 minutes

### [ ] 38. Document Future Work Items
- **Create:** FUTURE_WORK.md
- **Include:** Asset loader implementation, complete sync, etc.
- **Time:** 30 minutes

---

## Implementation Timeline

**Total Effort:** ~27 hours across 3 phases

### Phase 1: Critical Fixes (4 hours)
- [ ] Issues 1-4
- **Duration:** 1 day

### Phase 2: High Priority (12 hours)  
- [ ] Issues 5-14
- **Duration:** 2 days

### Phase 3: Medium Priority (10 hours)
- [ ] Issues 15-30
- **Duration:** 2 days

### Phase 4: Low Priority (5 hours)
- [ ] Issues 31-38
- **Duration:** 1 day

**Total:** 4-5 days, 1 developer

---

## Verification Checklist

After completing all fixes:

- [ ] All tests pass (unit + integration)
- [ ] Zero compiler warnings
- [ ] Zero SonarQube critical/blocker issues
- [ ] Performance profiling shows GC reduction
- [ ] Code review approval from team lead
- [ ] Architecture validation (proper layer separation)
- [ ] No direct Main.game access in logic layer
- [ ] All hardcoded values in GameConfig
- [ ] All logging uses SLF4J Logger
- [ ] All placeholder implementations marked or completed
- [ ] Documentation updated

---

## Quick Links to Issues

- Critical Issues: Lines 287-293 (GameLoopAdapter.isBridgeAvailable), 229-230 (QuestDialogueAdapter), 105-145 (BinaryAssetLoader), 135 (DeltaEngine)
- Dead Code: Lines 30-50 (GameStateAdapter constants), 116 (CharacterEngine variable)
- Duplicate Code: Lines 28-278 (GameLoopAdapter methods), 287 (GameLoopAdapter), 226 (QuestDialogueAdapter)
- Performance: Lines 218, 222, 226, 323 (StatusPanelController colors), 206, 263, 285 (StatusPanelController strings)
- Hardcoding: Lines 18-28 (GameLoopCoordinator), 32-33 (CharacterEngine), 78-79 (LogicLayerAPI), 75 (PersistenceManager), 32 (WorldEngine), 36 (GameIntegrationBridge)
