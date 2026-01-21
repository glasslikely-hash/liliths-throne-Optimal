# Refactor Remediation Checklist
**Target Completion**: 31 hours across 4 implementation phases  
**Status**: Ready for development  

---

## PHASE 1: CRITICAL STABILITY (6.5 Hours)
**Target**: Fix blocking issues, enable testability  
**Owner**: Senior Developer  
**Dependencies**: None  

### [ ] P1-001: Remove Direct State Access Violations (2 hours)
**Files**:
- [ ] `src/com/lilithsthrone/logic/GameLoopAdapter.java:145`
  - [ ] Replace `Main.game.isInCombat()` with `logicLayerAPI.isInCombat()`
  - [ ] Replace `Main.world.getLocation()` with `logicLayerAPI.getCurrentLocation()`
  
- [ ] `src/com/lilithsthrone/logic/adapters/QuestDialogueAdapter.java:89`
  - [ ] Replace `Main.dialogue.getCurrentDialogue()` with `logicLayerAPI.getCurrentDialogue()`
  - [ ] Replace `Main.game.getPlayer()` with `logicLayerAPI.getPlayer()`

- [ ] `src/com/lilithsthrone/ui/controllers/CombatUIController.java:156`
  - [ ] Replace `Main.sex.getCharacterPerformingAction()` with `logicLayerAPI.getActiveCharacter()`

- [ ] `src/com/lilithsthrone/ui/controllers/InventoryUIController.java:201`
  - [ ] Replace `Main.game.getPlayer()` with `logicLayerAPI.getPlayer()`

**Verification**:
- [ ] Grep for `Main\.game\|Main\.sex\|Main\.world\|Main\.dialogue` returns 0 in logic layer
- [ ] All replacements compile
- [ ] Unit tests pass

---

### [ ] P1-002: Implement Null-Returning Methods (4 hours)
**Files**:

**Option A: Implement fallback behavior**
- [ ] `src/com/lilithsthrone/persistence/binary/BinaryAssetLoader.java:67`
  - Current: `return null;` in `loadWeaponBinary()`
  - [ ] Implement binary loading OR
  - [ ] Add fallback: `return loadWeaponFromText(weaponId);`
  - [ ] Add logging: `logger.info("Loading weapon {} from text", weaponId);`
  - Line count: ~15 LOC

- [ ] `src/com/lilithsthrone/persistence/binary/BinaryAssetLoader.java:94`
  - Current: `return new ArrayList<>();` in `loadClothingBinary()`
  - [ ] Implement or fallback to text
  - Line count: ~15 LOC

- [ ] `src/com/lilithsthrone/persistence/binary/BinaryAssetLoader.java:112`
  - Current: `return new ArrayList<>();` in `loadRaceBinary()`
  - [ ] Implement or fallback to text
  - Line count: ~15 LOC

**Option B: Throw exception**
- [ ] `src/com/lilithsthrone/logic/persistence/DeltaEngine.java:156`
  - Current: `return null;` in `applyDelta()`
  - [ ] Change to: `throw new NotImplementedException("Delta application not yet implemented");`
  - [ ] Create `NotImplementedException` class if needed
  - Line count: ~5 LOC

- [ ] `src/com/lilithsthrone/persistence/data/DataPipelineBuilder.java:143`
  - Current: `return null;` in converter
  - [ ] Implement type conversion or throw exception
  - Line count: ~10 LOC

**Verification**:
- [ ] No method returns null without documentation
- [ ] All placeholders either implemented or throw NotImplementedException
- [ ] Code compiles and unit tests pass

---

### [ ] P1-003: Extract Hardcoded Configuration to External File (1.5 hours)

**Create**: `src/main/resources/game.properties`
```properties
# Frame rate and timing
game.fps=60
game.max_delta_time_ms=50

# Memory management
memory.threshold.high=0.8
memory.threshold.critical=0.95

# Caching
cache.max_size=1000
cache.lru_load_factor=0.75

# Object pooling
pool.growth_factor=1.5
pool.initial_size=100

# UI dimensions
ui.portrait_size=150
ui.line_height=25
ui.response_height=35

# Persistence
persistence.binary_cache_dir=cache/binary
persistence.snapshot_interval_seconds=300
persistence.autosave_interval_seconds=60
```

**Update Files**:
- [ ] `src/com/lilithsthrone/optimization/FrameRateOptimizer.java:45`
  - [ ] Replace `targetFrameTime = 16.67f` with `GameConfig.getTargetFrameTime()`

- [ ] `src/com/lilithsthrone/optimization/MemoryManager.java:38-39`
  - [ ] Replace hardcoded `0.8f` and `0.95f` with config values

- [ ] `src/com/lilithsthrone/logic/GameLoopCoordinator.java:15`
  - [ ] Replace `TARGET_FPS = 60` with config value

- [ ] `src/com/lilithsthrone/optimization/CacheManager.java:52`
  - [ ] Replace `MAX_CACHE_SIZE = 1000` with config value

- [ ] `src/com/lilithsthrone/ui/controllers/DialogueUIController.java:53`
  - [ ] Replace `PORTRAIT_SIZE = 150f` with config value

- [ ] `src/com/lilithsthrone/optimization/ObjectPoolManager.java:88`
  - [ ] Replace `POOL_GROWTH_FACTOR = 1.5f` with config value

- [ ] `src/com/lilithsthrone/persistence/binary/StaticDataManager.java:28`
  - [ ] Replace `BINARY_CACHE_DIR = "cache/binary"` with config value

**Create Config Loader**:
- [ ] `src/com/lilithsthrone/config/GameConfig.java`
  - [ ] Static initializer loads game.properties
  - [ ] Getters for all configuration values
  - [ ] Fallback defaults if property not found
  - [ ] Logging when loading config

**Verification**:
- [ ] game.properties loads without errors
- [ ] All config values accessible via GameConfig
- [ ] Code compiles and runs
- [ ] Can modify game.properties and see changes without recompilation

---

## PHASE 2: HIGH-VALUE IMPROVEMENTS (11 Hours)
**Target**: Code quality, performance, maintainability  
**Owner**: Mid-level Developer  
**Dependencies**: Phase 1 complete  

### [ ] P2-001: Remove 13 Unused Constants (0.5 hours)

**GameStateAdapter.java** (5 constants)
- [ ] Line 45: `STATE_VERSION = "2.1"` - Search codebase, verify unused
  - [ ] If unused: DELETE
  - [ ] If used: KEEP but add comment about usage

- [ ] Line 46: `SNAPSHOT_MAGIC = 0xDEADBEEF` - Same process
- [ ] Line 47: `DELTA_MAGIC = 0xCAFEBABE` - Same process
- [ ] Line 48: `VERSION_NUMBER = 3` - Same process
- [ ] Line 49: `COMPATIBILITY_VERSION = 2` - Same process

**EventProcessor.java** (4 constants)
- [ ] Line 56: `PRIORITY_CRITICAL` - Check usage
- [ ] Line 57: `PRIORITY_HIGH` - Check usage
- [ ] Line 58: `PRIORITY_NORMAL` - Check usage
- [ ] Line 59: `PRIORITY_LOW` - Check usage

**DialogueCoordinator.java** (3 constants)
- [ ] Lines to identify and check...

**CombatCoordinator.java** (1 constant)
- [ ] Lines to identify and check...

**Method**:
```bash
for each constant:
  grep -r "CONSTANT_NAME" src/
  if no results: delete line
```

**Verification**:
- [ ] All 13 constants verified as unused
- [ ] Code still compiles
- [ ] No broken references

---

### [ ] P2-002: Implement 5 Placeholder Methods (2.5 hours)

Already partially done in P1-002. Verify completion:

- [ ] `BinaryAssetLoader.java:67` - `loadWeaponBinary()` - DONE
- [ ] `BinaryAssetLoader.java:94` - `loadClothingBinary()` - DONE
- [ ] `BinaryAssetLoader.java:112` - `loadRaceBinary()` - DONE
- [ ] `DeltaEngine.java:156` - `applyDelta()` - DONE
- [ ] `DataPipelineBuilder.java:143` - Type converter - DONE

**Additional Verification**:
- [ ] Search codebase for `TODO.*implement` - should return 0
- [ ] All placeholder methods have real implementations or throw exceptions
- [ ] No method returns `null` silently

---

### [ ] P2-003: Cache Colors for Rendering (1 hour)

**Create**: `src/com/lilithsthrone/rendering/ColorCache.java`
```java
public class ColorCache {
    private static final Map<String, Color> cache = new HashMap<>();
    
    static {
        // Pre-cache health colors (0-100%)
        for (int i = 0; i <= 100; i++) {
            float health = i / 100f;
            cache.put("health_" + i, 
                new Color(1f - health, health, 0f, 1f));
        }
        // Pre-cache mana/stamina colors similarly
        // ...
    }
    
    public static Color getHealthColor(float percent) {
        int index = Math.min(100, Math.max(0, (int)(percent * 100)));
        return cache.get("health_" + index);
    }
}
```

**Update Files**:
- [ ] `src/com/lilithsthrone/ui/controllers/GameplayUIController.java:142-167`
  - [ ] Replace color creation loop with cache lookups
  - [ ] OLD: `shapeRenderer.setColor(1f - health, health, 0f, 1f);`
  - [ ] NEW: `shapeRenderer.setColor(ColorCache.getHealthColor(healthPercent));`

- [ ] `src/com/lilithsthrone/ui/controllers/StatusPanelController.java:188-210`
  - [ ] Same pattern for resource bars

**Measurement**:
- [ ] Before: Profile shows 240 Color objects/sec
- [ ] After: Profile shows 0 Color objects created (using cache)
- [ ] Expected result: 8% GC reduction

**Verification**:
- [ ] ColorCache initializes without errors
- [ ] All health colors return consistent color objects
- [ ] No new Color objects created per frame
- [ ] Visual rendering unchanged

---

### [ ] P2-004: Extract Error Handling Utility (1.5 hours)

**Create**: `src/com/lilithsthrone/error/ErrorHandler.java`
```java
public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
    
    public static <T> T executeWithRecovery(String context, 
                                           Supplier<T> action, 
                                           T defaultValue) {
        try {
            return action.get();
        } catch (Exception e) {
            logger.error("Error in {}: {}", context, e.getMessage(), e);
            return defaultValue;
        }
    }
    
    public static void executeWithErrorLogging(String context, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            logger.error("Error in {}: {}", context, e.getMessage(), e);
        }
    }
}
```

**Find and Replace Duplicates**:
- [ ] `EventProcessor.java:89` - Extract try-catch, replace with `ErrorHandler.executeWithRecovery(...)`
- [ ] `EventProcessor.java:156` - Same pattern
- [ ] `EventProcessor.java:223` - Same pattern
- [ ] `DialogueAdapter.java:112` - Same pattern
- [ ] `DialogueAdapter.java:189` - Same pattern
- [ ] `DialogueAdapter.java:267` - Same pattern
- [ ] `CombatAdapter.java:134` - Same pattern
- [ ] `CombatAdapter.java:201` - Same pattern
- [ ] `CombatAdapter.java:278` - Same pattern
- [ ] `GameLoopAdapter.java:145` - Same pattern
- [ ] `GameLoopAdapter.java:212` - Same pattern
- [ ] `GameLoopAdapter.java:289` - Same pattern

**Verification**:
- [ ] All 12 duplicate try-catch blocks replaced
- [ ] Code compiles
- [ ] Error messages consistent
- [ ] Logging level configurable

---

### [ ] P2-005: Fix String Concatenation in Loops (1 hour)

**Locations** (4 methods):
- [ ] `src/com/lilithsthrone/optimization/PerformanceMonitor.java:134`
  - OLD: `String report = ""; for (...) report += metric + "\n";`
  - NEW: `StringBuilder report = new StringBuilder(); for (...) report.append(metric).append("\n");`

- [ ] `src/com/lilithsthrone/ui/controllers/EventLogController.java:201`
  - [ ] Same pattern

- [ ] `src/com/lilithsthrone/logic/EventProcessor.java:98`
  - [ ] Same pattern

- [ ] `src/com/lilithsthrone/ui/controllers/DialogueUIController.java:243`
  - [ ] Same pattern

**Measurement**:
- [ ] Before: Profile shows 180 String objects/sec
- [ ] After: Profile shows ~5 String objects/sec (only StringBuilder allocation)
- [ ] Expected result: 4% GC reduction

**Verification**:
- [ ] All 4 locations fixed
- [ ] Output strings identical
- [ ] Performance profiler shows reduced allocations

---

### [ ] P2-006: Add Null Checks Uniformly (2 hours)

**Files with gaps** (13 methods):

**GameIntegrationBridge.java** (3 methods):
- [ ] Line 145: `executeGameAction(String actionId)` 
  - [ ] Add: `if (actionId == null || actionId.isEmpty()) throw new IllegalArgumentException(...);`

- [ ] Line 201: `getCharacterState(String characterId)`
  - [ ] Add: null/empty check

- [ ] Line 267: `updateCharacterState(String id, GameCharacter state)`
  - [ ] Add: null check for both parameters

**LogicLayerAPI.java** (5 methods):
- [ ] Implement consistent validation for all parameters
- [ ] Throw `IllegalArgumentException` with descriptive message

**UIControllerBase.java** (2 methods):
- [ ] `initialize()` - Check batch, camera, logicLayerAPI not null
- [ ] `dispose()` - Check resources exist before disposing

**EventProcessor.java** (3 methods):
- [ ] `processEvent(Event event)` - Check event not null
- [ ] `registerListener(String eventType, EventListener listener)` - Check both params
- [ ] `unregisterListener(String eventType, String listenerId)` - Check params

**Method Template**:
```java
public void methodName(String param) {
    if (param == null || param.isEmpty()) {
        throw new IllegalArgumentException("Parameter cannot be null or empty");
    }
    // ... rest of method
}
```

**Verification**:
- [ ] 13 methods validated
- [ ] All null checks in place
- [ ] Descriptive error messages
- [ ] Code compiles and tests pass

---

### [ ] P2-007: Consolidate Logging Framework (1.5 hours)

**Goal**: Replace 34 System.out.println with SLF4J

**Add Dependency**: (if not already present)
```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.0</version>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.4.0</version>
</dependency>
```

**Find all println statements**:
```bash
grep -r "System\.out\.println" src/ | wc -l
# Expected output: 34
```

**Replace Pattern**:
```java
// OLD
System.out.println("[GameLoopCoordinator] Frame: " + frameCount);

// NEW - Add to class:
private static final Logger logger = LoggerFactory.getLogger(GameLoopCoordinator.class);

// Then use:
logger.debug("Frame: {}", frameCount); // or info(), warn(), error()
```

**Create**: `src/main/resources/logback.xml`
```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>
    
    <!-- Set DEBUG level for specific packages during development -->
    <logger name="com.lilithsthrone.logic" level="DEBUG" />
    <logger name="com.lilithsthrone.persistence" level="DEBUG" />
</configuration>
```

**Verification**:
- [ ] All 34 println statements replaced or verified as necessary
- [ ] Logger field added to each class
- [ ] logback.xml created and loads without errors
- [ ] Can adjust log levels in logback.xml without recompiling
- [ ] Console output cleaner and more structured

---

### [ ] P2-008: Fix Circular Dependencies (1 hour)

**Current**:
```
Main.java 
  → GameLoopCoordinator 
    → GameIntegrationBridge 
      → EventEngine 
        → Main.game (CIRCULAR)
```

**Solution**: Introduce interface-based dependencies

**Create**: `src/com/lilithsthrone/logic/GameCoordinatorInterface.java`
```java
public interface GameCoordinator {
    void update(float deltaTime);
    GameState getCurrentState();
    void saveState();
}
```

**Create**: `src/com/lilithsthrone/event/EventDispatcherInterface.java`
```java
public interface EventDispatcher {
    void dispatch(GameEvent event);
    void subscribe(String eventType, EventListener listener);
}
```

**Refactor**:
- [ ] GameLoopCoordinator accepts EventDispatcher in constructor
- [ ] GameIntegrationBridge accepts GameCoordinator in constructor
- [ ] Remove static singleton pattern for GameLoopCoordinator
- [ ] Use constructor injection instead

**Before**:
```java
public class GameLoopCoordinator {
    public static GameLoopCoordinator instance;
    public static void initialize() { ... }
}
```

**After**:
```java
public class GameLoopCoordinator implements GameCoordinator {
    private final EventDispatcher dispatcher;
    
    public GameLoopCoordinator(EventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
}
```

**Verification**:
- [ ] No static getInstance() patterns
- [ ] Dependencies injected via constructor
- [ ] Circular reference broken
- [ ] Unit tests can mock dependencies
- [ ] Code compiles

---

## PHASE 3: CODE QUALITY (8 Hours)
**Target**: Cleanliness, maintainability, documentation  
**Owner**: Junior Developer  
**Dependencies**: Phase 1-2 complete  

### [ ] P3-001: Remove Unused Imports (0.5 hours)

**Files** (8 total):
- [ ] `GameLoopCoordinator.java:15` - Remove `import java.nio.file.*`
- [ ] `MemoryManager.java:8` - Fix `import java.lang.management.*` (keep only RuntimeMXBean)
- [ ] `CacheManager.java:5` - Replace wildcard `import java.util.*` with specific imports
- [ ] `StaticDataManager.java:10` - Replace wildcard with specific imports
- [ ] `DialogueUIController.java:18` - Remove unused GDX imports
- [ ] `InventoryUIController.java:12` - Replace wildcard with specific
- [ ] `EventLogController.java:8` - Remove `import java.nio.file.*`
- [ ] `MainUIController.java:24` - Remove unused utils import

**Method**:
```bash
for each file:
  1. Open file
  2. For each import: search if it's used in the code
  3. If not used: delete the import line
```

**Verification**:
- [ ] All 8 files have clean imports (no unused imports)
- [ ] Code still compiles
- [ ] Wildcard imports replaced with specific ones

---

### [ ] P3-002: Extract Magic Numbers to Named Constants (1 hour)

**Files** (6 with additional magic numbers beyond Phase 1):

**ObjectPoolManager.java**:
- [ ] Line 45: `new float[8]` → `POOL_METRICS_ARRAY_SIZE = 8`
- [ ] With comment: `// Tracks: created, acquired, released, pooled, discarded, reset, time_spent, errors`

**CacheManager.java**:
- [ ] Line 78: `0.75f` load factor → `CACHE_LOAD_FACTOR = 0.75f`
- [ ] Comment: `// HashMap growth threshold`

**FrameRateOptimizer.java**:
- [ ] Line 89: `50f` max delta → Already done in Phase 1 config
- [ ] Verify it's in GameConfig

**MemoryManager.java**:
- [ ] Line 67: `1024 * 1024` → `BYTES_PER_MB = 1024 * 1024`

**DialogueUIController.java**:
- [ ] Line 200: `25f` line height → Already done in Phase 1 config
- [ ] Verify it's in GameConfig

**StatusPanelController.java**:
- [ ] Line 145: `0.2f` update interval → Extract to constant with comment

**Method**:
```java
// BEFORE
if (height > 1024 * 1024) { ... }

// AFTER
private static final int BYTES_PER_MB = 1024 * 1024;
if (height > BYTES_PER_MB) { ... }
```

**Verification**:
- [ ] All magic numbers extracted to named constants
- [ ] Each constant has a comment explaining its purpose
- [ ] Code is more readable

---

### [ ] P3-003: Add JavaDoc to Complex Methods (2 hours)

**Files** (7 classes with missing documentation):

**GameIntegrationBridge.java**:
- [ ] `executeLogicUpdate()` - Document what it does, what exceptions it throws
- [ ] `synchronizeUIState()` - Document UI synchronization logic
- [ ] `handlePersistenceEvent()` - Document event handling flow

**EventProcessor.java**:
- [ ] `processEvent()` - Explain event dispatch mechanism
- [ ] `registerListener()` - Document listener registration
- [ ] `unregisterListener()` - Document cleanup

**GameStateAdapter.java**:
- [ ] Class-level JavaDoc explaining purpose
- [ ] `toSnapshot()` - Document snapshot creation logic
- [ ] `fromSnapshot()` - Document snapshot restoration
- [ ] `toDelta()` - Document delta generation

**StaticDataManager.java**:
- [ ] Class-level JavaDoc
- [ ] `initialize()` - Explain startup sequence
- [ ] `loadFromBinary()` vs `loadFromText()` - Explain fallback logic

**DataMigrationValidator.java**:
- [ ] `validate()` - Explain validation process
- [ ] Document what constitutes a valid snapshot

**MemoryManager.java**:
- [ ] `getMemoryStatistics()` - Document memory measurement
- [ ] `getOptimizationTips()` - Explain optimization recommendations

**PerformanceMonitor.java**:
- [ ] Class-level JavaDoc on performance tracking
- [ ] `detectBottlenecks()` - Explain bottleneck detection algorithm

**Template**:
```java
/**
 * [One-line summary of what method does]
 *
 * [Detailed explanation of how it works and why]
 * 
 * [Example usage if applicable]
 *
 * @param param1 Description of first parameter
 * @param param2 Description of second parameter
 * @return Description of return value
 * @throws IOException If file I/O fails
 * @throws IllegalArgumentException If parameters invalid
 * 
 * Example:
 *   GameState state = adapter.fromSnapshot(snapshotData);
 *   // state is now restored from checkpoint
 */
```

**Verification**:
- [ ] All 7 classes have complete JavaDoc
- [ ] Each public method documented
- [ ] Each parameter documented
- [ ] Return values documented
- [ ] Exceptions documented
- [ ] Examples provided where helpful

---

### [ ] P3-004: Standardize Exception Handling (1.5 hours)

**Current Issue**: 
- Some methods throw checked exceptions
- Others catch and log silently
- Custom exceptions inconsistent

**Solution**: Define exception hierarchy

**Create**: `src/com/lilithsthrone/exception/GameException.java`
```java
public class GameException extends RuntimeException {
    public GameException(String message) {
        super(message);
    }
    
    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class PersistenceException extends GameException { ... }
public class DataException extends GameException { ... }
public class LogicException extends GameException { ... }
```

**Update Methods** (8 methods):
- [ ] `StaticDataManager.loadFromBinary()` - Throw `DataException`
- [ ] `DeltaEngine.applyDelta()` - Throw `PersistenceException`
- [ ] `GameIntegrationBridge.executeLogicUpdate()` - Throw `LogicException`
- [ ] etc.

**Pattern**:
```java
// OLD: Throws multiple checked exceptions
public byte[] encodeBinary() throws IOException, SAXException { ... }

// NEW: Throws unchecked custom exception
public byte[] encodeBinary() {
    try {
        // ...
    } catch (IOException | SAXException e) {
        throw new PersistenceException("Failed to encode binary data", e);
    }
}
```

**Verification**:
- [ ] All custom exceptions extend GameException or subclass
- [ ] No raw IOException/SAXException thrown
- [ ] Error messages descriptive and actionable
- [ ] Code compiles

---

### [ ] P3-005: Add Input Validation Consistently (2 hours)

Already partially addressed in P2-006. Complete remaining 11 methods.

**Pattern**:
```java
public void methodName(String id, List<String> items) {
    // Validate all inputs
    if (id == null || id.trim().isEmpty()) {
        throw new IllegalArgumentException("ID cannot be null or empty");
    }
    if (items == null) {
        throw new IllegalArgumentException("Items list cannot be null");
    }
    if (items.isEmpty()) {
        throw new IllegalArgumentException("Items list cannot be empty");
    }
    if (items.size() > MAX_ITEMS) {
        throw new IllegalArgumentException("Too many items: " + items.size() + 
                                         " (max: " + MAX_ITEMS + ")");
    }
    
    // Now safe to use parameters
    // ...
}
```

**Verification**:
- [ ] All 11 methods have input validation
- [ ] Error messages are descriptive
- [ ] Code compiles and tests pass

---

### [ ] P3-006: Organize Folder Structure (1 hour)

**Current**:
```
logic/
├── engines/
├── adapters/
├── coordinators/
└── persistence/
```

**Proposed**:
```
logic/
├── engines/
│   ├── GameLoopEngine.java (moved from coordinators/)
│   ├── EventEngine.java (moved from coordinators/)
│   └── [other engines]
├── adapters/
│   └── [all adapters]
└── persistence/
    └── [all persistence]
```

**Changes**:
- [ ] Move `coordinators/GameLoopCoordinator.java` → `engines/GameLoopEngine.java`
- [ ] Rename class from `GameLoopCoordinator` to `GameLoopEngine`
- [ ] Update all references to new name and package
- [ ] Delete now-empty `coordinators/` directory

**Utils Reorganization** (if comprehensive refactor wanted):
```
utils/
├── collections/
│   ├── Node.java
│   ├── TreeNode.java
│   └── SizedStack.java
├── text/
│   ├── TextUtil.java
│   └── TextFormatting.java
├── time/
│   ├── DateAndTime.java
│   └── [other time classes]
├── comparators/
│   └── [all comparators]
└── core/
    ├── Util.java
    ├── Builder.java
    └── Pathing.java
```

**Verification**:
- [ ] All files moved successfully
- [ ] All imports updated
- [ ] Code compiles
- [ ] Folder structure cleaner and more discoverable

---

## PHASE 4: POLISH (4 Hours)
**Target**: Production readiness, final cleanup  
**Owner**: Code Review Team  
**Dependencies**: Phase 1-3 complete  

### [ ] P4-001: Add Runtime Assertions (0.5 hours)

**Locations** (5 methods):

- [ ] `ObjectPoolManager.java:156`
  ```java
  assert pool != null : "Pool should exist before accessing";
  ```

- [ ] `CacheManager.java:201`
  ```java
  assert hitRatio >= 0 && hitRatio <= 1 : "Hit ratio must be 0-1";
  ```

- [ ] `FrameRateOptimizer.java:145`
  ```java
  assert frameTime > 0 : "Frame time must be positive";
  ```

- [ ] `MemoryManager.java:89`
  ```java
  assert currentMemory >= 0 && currentMemory <= maxMemory : "Memory bounds invalid";
  ```

- [ ] `PerformanceMonitor.java:201`
  ```java
  assert sectionTime >= 0 : "Section time cannot be negative";
  ```

**Verify Assertions Enabled**:
```bash
# Add to JVM args: -ea
```

**Verification**:
- [ ] 5 assertions added
- [ ] All assertions make sense
- [ ] Code compiles with assertions enabled

---

### [ ] P4-002: Fix Naming Convention Issues (0.5 hours)

**Issues** (2 total):
- [ ] Decide: "Controller" vs "Manager" vs "Adapter" vs "Engine"
  - ✅ UI = "Controller" (GameplayUIController)
  - ✅ Logic = "Engine" (EventEngine)
  - ✅ Integration = "Adapter" (DialogueAdapter)
  - Document decision and stick to it

- [ ] Check variable naming for consistency
  - [ ] Ensure all camelCase (no snake_case in Java code)
  - [ ] Ensure abbreviations consistent (e.g., `npc` not `npc`, `char` not `character`)

**Verification**:
- [ ] Naming conventions documented
- [ ] All code follows conventions
- [ ] Code review passes

---

### [ ] P4-003: Remove Test Code from Production (1 hour)

**Find test-only code**:
```bash
grep -r "test\|debug\|TODO.*test\|XXX\|FIXME" src/com/lilithsthrone/ | grep -v "persistence.*Test"
```

**Locations** (estimate: 3-5 methods):
- [ ] Methods like `debugGetState()` - Move to test class or remove
- [ ] Methods like `testOnlyReset()` - Move to test class or remove
- [ ] Methods like `forceStateCorruption()` - Move to test class or remove

**Pattern**:
```java
// ❌ IN PRODUCTION CODE: Remove or move to test
public void debugPrintState() {
    System.out.println("DEBUG: " + internalState);
}

// ✅ IN TEST CLASS: Keep here
@Test
public void testStateInitialization() {
    coordinator.debugPrintState(); // Call from test
}
```

**Verification**:
- [ ] All test-only code moved to test classes
- [ ] No `DEBUG`, `TEST`, or `FIXME` constants in production
- [ ] Production code is clean

---

### [ ] P4-004: Final Review and Documentation (2 hours)

**Code Review Checklist**:
- [ ] All Phase 1-3 tasks completed
- [ ] No compilation errors
- [ ] All unit tests pass
- [ ] No performance regressions

**Documentation Updates**:
- [ ] Update README.md with architecture diagram
- [ ] Create REFACTORING_GUIDE.md for future developers
- [ ] Document all custom exceptions
- [ ] Document all configuration options
- [ ] Update ARCHITECTURE.md if exists

**Create**: `REFACTORING_GUIDE.md`
```markdown
# Refactoring Guide

## Architecture Overview
[Describe layer separation, data flow, etc.]

## Adding New Features
[Step-by-step guide for adding new adapters, engines, etc.]

## Performance Considerations
[Explain caching, pooling, optimization best practices]

## Common Pitfalls
[Document what NOT to do, lessons learned]

## Testing Strategy
[Explain how to test each layer independently]
```

**Verification**:
- [ ] All documentation complete
- [ ] Architecture diagram accurate
- [ ] Code review team signs off
- [ ] Ready for production deployment

---

## Final Verification Checklist

### Code Quality
- [ ] 0 compilation errors
- [ ] 0 compilation warnings (besides deprecation notices)
- [ ] Code coverage >80% for critical paths
- [ ] All code formatted consistently

### Architecture
- [ ] No direct state access violations
- [ ] No circular dependencies
- [ ] Clear separation of concerns
- [ ] All layers properly isolated

### Performance
- [ ] No object creation in hot loops
- [ ] Color caching implemented (8% GC reduction)
- [ ] String concatenation optimized (4% GC reduction)
- [ ] Memory pressure < 10% during typical gameplay

### Documentation
- [ ] All public methods have JavaDoc
- [ ] All exceptions documented
- [ ] Architecture diagram exists
- [ ] Configuration options documented

### Testing
- [ ] All unit tests pass
- [ ] Integration tests pass
- [ ] Performance benchmarks captured (for regression testing)
- [ ] Error recovery tested

---

## Success Criteria

**Phase 1 Complete** ✓
- System is stable and testable
- All null-returning methods fixed
- Configuration externalized

**Phase 2 Complete** ✓
- Code quality significantly improved
- Performance improved (12% GC reduction)
- Error handling consistent

**Phase 3 Complete** ✓
- Code is well-documented
- Maintainability greatly improved
- Structure is clean

**Phase 4 Complete** ✓
- Production ready
- All best practices applied
- Future developers have clear guidance

---

**Estimated Total Time**: 31 hours (4-5 days, single developer)  
**Estimated Team Time**: 15-20 hours (with parallelization)

This checklist serves as the implementation roadmap. Check off items as completed.
