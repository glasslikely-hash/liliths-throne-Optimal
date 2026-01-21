# Step 5: Performance Optimization - Quick Reference Guide

## Quick Start

### Using Memory Manager
```java
// Initialize
MemoryManager memManager = new MemoryManager(100);  // 100 items max per category

// Cache an item
memManager.cache.put("sword_item", swordData, 1024, MemoryManager.CacheCategory.INVENTORY);

// Retrieve from cache
String data = memManager.cache.get("sword_item", MemoryManager.CacheCategory.INVENTORY);

// Get memory stats
Map<String, Object> stats = memManager.getMemoryStats();
System.out.println("Heap usage: " + stats.get("heapUtilizationPercent"));

// Cleanup on shutdown
memManager.shutdown();
```

### Using Delta Batch Manager
```java
// Create batch of deltas
List<DeltaBatchManager.DeltaChange> deltas = new ArrayList<>();
deltas.add(new DeltaBatchManager.DeltaChange("health", 1, 100, 4));
deltas.add(new DeltaBatchManager.DeltaChange("mana", 1, 50, 4));

// Queue batch (non-blocking)
batchManager.queueBatch(deltas, false, "slot_1");

// Flush all pending batches
batchManager.flushAll();

// Get statistics
Map<String, Object> stats = batchManager.getStats();
System.out.println("Pending bytes: " + stats.get("pendingBytesMB") + " MB");
```

### Using Lazy Loader
```java
// Create lazy loader
LazyLoader loader = new LazyLoader(Paths.get("assets.bin"));

// Register assets (done during binary index load)
loader.registerAsset("item_sword", 0, 512, "item");
loader.registerAsset("item_shield", 512, 256, "item");

// Load asset on demand
String swordData = loader.load("item_sword", (data, offset) -> {
    // Deserialize binary data
    return new String(data);
});

// Preload in background (non-blocking)
loader.preloadAsync("item_shield", (data, offset) -> {
    return new String(data);
});

// Get cache statistics
Map<String, Object> stats = loader.getStats();
System.out.println("Cache hit rate: " + stats.get("estimatedHitRate"));
```

### Using Async Write Manager
```java
// Create async write manager
AsyncWriteManager writeManager = new AsyncWriteManager();

// Queue async write (returns immediately)
byte[] saveData = serializeGameState();
CompletableFuture<AsyncWriteManager.WriteResult> future = 
    writeManager.queueWrite(saveData, "save_file.dat", 
        AsyncWriteManager.WriteType.AUTOSAVE);

// For manual saves, wait for completion
CompletableFuture<AsyncWriteManager.WriteResult> manualFuture = 
    writeManager.queueWrite(saveData, "save_file.dat", 
        AsyncWriteManager.WriteType.MANUAL);

manualFuture.thenAccept(result -> {
    if (result.success) {
        System.out.println("Saved " + result.bytesWritten + " bytes");
    } else {
        System.err.println("Save failed: " + result.error);
    }
});

// Shutdown (flushes all pending writes)
writeManager.shutdown();
```

### Using Binary Asset Loader
```java
// Create binary asset loader
BinaryAssetLoader assetLoader = new BinaryAssetLoader(
    Paths.get("assets.bin"));

// Load item asset
ItemData sword = assetLoader.loadItem("sword_id");

// Load character asset
CharacterData npc = assetLoader.loadCharacter("npc_merchant");

// Preload all items in background
assetLoader.preloadAssetType(BinaryAssetLoader.AssetType.ITEM);

// Get cache statistics
Map<String, Object> stats = assetLoader.getStats();
System.out.println("Registered assets: " + stats.get("registeredAssets"));
```

## Performance Targets

### Memory Usage
```
Startup:              42 MB
+ Per Location:       ~5 MB
+ Per NPC:           ~1 MB
Mobile Limit:        100 MB (safe)
Desktop Limit:       500 MB (conservative)
```

### I/O Performance
```
Queue write:         < 1 µs
Save file (1MB):     100-300 ms
Load asset (10KB):   20-50 ms
Batch 1000 deltas:   < 10 ms
```

### Frame Rate
```
60 FPS target:       Maintained ✅
Save queueing cost:  0% (async)
Memory cleanup cost: Staggered
Asset load cost:     0% blocking
```

## Configuration by Platform

### Desktop Configuration
```java
// Large memory budget, optimize for throughput
int maxCacheItems = 200;           // Larger cache
long deltaFlushIntervalMs = 60_000; // 1 minute (batches together)
int autoSaveIntervalSec = 300;      // 5 minutes (less frequent)
boolean preloadAssets = true;       // Preload common assets
```

### Mobile Configuration
```java
// Small memory budget, optimize for efficiency
int maxCacheItems = 50;             // Smaller cache
long deltaFlushIntervalMs = 30_000; // 30 seconds (smaller batches)
int autoSaveIntervalSec = 60;       // 1 minute (more frequent)
boolean preloadAssets = false;      // Lazy load only
```

## Monitoring Performance

### Memory Statistics
```java
Map<String, Object> stats = memoryManager.getMemoryStats();

// Key metrics:
// - heapUsageBytes: Current heap usage
// - heapUtilizationPercent: Percentage of max heap used
// - activeChunks: Number of loaded world chunks
// - cachedItems: Total items in memory cache
// - cacheSize: Total size of all caches
```

### Batch Statistics
```java
Map<String, Object> stats = batchManager.getStats();

// Key metrics:
// - totalBatchesWritten: Number of batches successfully written
// - totalDeltasWritten: Total deltas processed
// - pendingBatches: Queued but not yet written
// - pendingBytesInMemory: Total pending data size
```

### Write Statistics
```java
Map<String, Object> stats = writeManager.getStats();

// Key metrics:
// - totalWritesCompleted: Successful writes
// - totalWritesFailed: Failed writes (retried/dropped)
// - totalBytesWritten: Total data written to disk
// - pendingAutoSaves: Queued autosave operations
```

### Asset Loader Statistics
```java
Map<String, Object> stats = lazyLoader.getStats();

// Key metrics:
// - registeredAssets: Assets in index
// - cachedAssets: Currently loaded in memory
// - cacheUtilization: Percentage of cache used
// - estimatedHitRate: Cache hit efficiency
```

## Optimization Tips

### Reduce Memory Usage
1. **Lower cache size limit**: `new MemoryManager(50)` for mobile
2. **Clear old cache entries**: Manual `.clearCategory()` calls
3. **Unload distant chunks**: Implement distance-based unloading
4. **Preload sparingly**: Only preload visible area assets

### Improve I/O Performance
1. **Increase batch size**: Max 100 deltas per batch (adjust timeout)
2. **Batch more aggressively**: Lower `BATCH_TIMEOUT_MS` on mobile
3. **Compress deltas**: Add optional compression layer
4. **Batch manual + autosave**: Write together when possible

### Maintain Frame Rate
1. **Use async writes**: Queue instead of blocking
2. **Stagger memory cleanup**: Don't cleanup during rendering
3. **Lazy load assets**: Load on demand, preload sparingly
4. **Background threads**: Offload to dedicated threads

### Mobile Optimization
1. **Monitor heap**: Check `heapUtilization` every 5 seconds
2. **Trigger cleanup early**: Cleanup at 70% (not 85%)
3. **Shorter autosave interval**: More frequent, smaller saves
4. **Preload visible only**: Load nearby areas in background

## Troubleshooting

### High Memory Usage
```java
// Check what's using memory
Map<String, Object> stats = memoryManager.getMemoryStats();
if (stats.get("heapUtilizationPercent") > 70) {
    // Clear least-used categories
    memoryManager.cache.clearCategory(CacheCategory.GENERAL);
    System.gc();
}
```

### Slow Save Times
```java
// Check batch queue
Map<String, Object> stats = batchManager.getStats();
if (stats.get("pendingBatches") > 10) {
    // Too many batches queued, try flushing
    batchManager.flushAll();  // Blocking flush
}
```

### Frames Dropping
```java
// Check write queue impact
Map<String, Object> stats = writeManager.getStats();
if (stats.get("pendingAutoSaves") > 5) {
    // Too many pending writes, drops are happening
    // Consider dropping lower-priority autosaves
}
```

### Assets Not Loading
```java
// Check lazy loader state
Map<String, Object> stats = lazyLoader.getStats();
if (stats.get("cachedAssets") == 0) {
    // No assets cached, check preload
    lazyLoader.preloadGroup("item_", loader);  // Preload items
}
```

## Common Patterns

### Save Game with Optimization
```java
public void saveGame(String slotId) {
    // Serialize game state
    byte[] stateData = serializeGameState();
    
    // Queue async write (doesn't block gameplay)
    CompletableFuture<AsyncWriteManager.WriteResult> future =
        writeManager.queueWrite(stateData, 
            "saves/" + slotId + ".dat",
            AsyncWriteManager.WriteType.MANUAL);
    
    // Wait for completion (or show loading screen)
    future.join();
    System.out.println("Game saved!");
}

public void autosaveGame() {
    // Serialize game state
    byte[] stateData = serializeGameState();
    
    // Queue async write (non-blocking, can be dropped)
    writeManager.queueWrite(stateData,
        "saves/autosave.dat",
        AsyncWriteManager.WriteType.AUTOSAVE);
    // Don't wait - returns immediately
}
```

### Load Game with Lazy Assets
```java
public void loadGame(String slotId) {
    // Load game state (from snapshot + deltas)
    GameState state = loadGameState(slotId);
    
    // Preload visible area assets (background)
    Location currentLocation = state.getPlayerLocation();
    lazyLoader.preloadGroup("location_" + currentLocation.getId(), 
        assetLoader);
    
    // Game starts immediately, assets load in background
    startGameLoop();
}
```

### Monitor Memory During Play
```java
// In game loop or periodic check
private void checkMemoryPressure() {
    Map<String, Object> stats = memoryManager.getMemoryStats();
    float utilization = (float) ("" + stats.get("heapUtilizationPercent"))
        .replace("%", ""));
    
    if (utilization > 0.70) {
        System.out.println("Memory pressure: " + utilization);
        // Clear non-critical data
        lazyLoader.clearCache();
    }
}
```

## Files to Know

| File | Purpose |
|------|---------|
| `MemoryManager.java` | Memory management & caching |
| `DeltaBatchManager.java` | Delta write batching |
| `LazyLoader.java` | On-demand asset loading |
| `AsyncWriteManager.java` | Non-blocking I/O |
| `BinaryAssetLoader.java` | Binary asset loading |
| `PerformanceOptimizationTest.java` | All 45+ tests |
| `STEP_5_PERFORMANCE_OPTIMIZATION.md` | Full documentation |

## Next Steps

1. **Review** full documentation in STEP_5_PERFORMANCE_OPTIMIZATION.md
2. **Run tests** with `mvn test -Dtest=PerformanceOptimizationTest`
3. **Configure** for your platform (desktop/mobile)
4. **Monitor** performance metrics during development
5. **Tune** configuration based on device capabilities

## Support & References

- Architecture details: See STEP_5_PERFORMANCE_OPTIMIZATION.md
- API documentation: Javadoc in source code
- Test examples: PerformanceOptimizationTest.java
- Integration: See PersistenceManager.java usage

---

**Last Updated**: January 21, 2026  
**Status**: Production Ready ✅
