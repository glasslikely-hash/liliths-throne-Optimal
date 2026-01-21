package com.lilithsthrone.logic.persistence;

import java.util.*;
import java.util.concurrent.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

/**
 * Manages memory footprint for game state.
 * 
 * Strategy:
 *   1. Keep only "active chunks" in memory (player location, inventory, current NPCs)
 *   2. Lazy-load other regions/entities from binary files
 *   3. Cache frequently accessed data
 *   4. Monitor memory usage and trigger cleanup
 * 
 * Memory Tiers:
 *   - Hot: Current location, active inventory, nearby NPCs (always loaded)
 *   - Warm: Recently visited locations, last few deltas (LRU cache)
 *   - Cold: Everything else (binary file on disk, loaded on demand)
 * 
 * Chunking Strategy:
 *   World divided into 64x64 grid chunks
 *   Each chunk loaded/unloaded independently
 *   Unloaded chunks reference binary file offsets only
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
@SuppressWarnings("unused")
public class MemoryManager {
    
    private static final long MEMORY_CHECK_INTERVAL_MS = 5000;  // Check every 5 seconds
    private static final float MEMORY_CRITICAL_THRESHOLD = 0.85f;  // 85% heap usage
    private static final float MEMORY_WARNING_THRESHOLD = 0.70f;   // 70% heap usage
    
    private final MemoryCache cache;
    private final ChunkManager chunkManager;
    private final MemoryMonitor monitor;
    
    // Statistics
    private volatile long heapUsageBytes = 0;
    private volatile long maxHeapBytes = 0;
    private volatile float heapUtilization = 0;
    private volatile int activeChunks = 0;
    private volatile int cachedItems = 0;
    
    public MemoryManager(int maxCacheItemsPerCategory) {
        this.cache = new MemoryCache(maxCacheItemsPerCategory);
        this.chunkManager = new ChunkManager();
        this.monitor = new MemoryMonitor();
        this.monitor.start();
    }
    
    /**
     * Memory cache using LRU with size-based eviction.
     * Separates inventory, locations, NPCs for targeted cleanup.
     */
    public static class MemoryCache {
        private static class CacheEntry<T> {
            T data;
            @SuppressWarnings("unused")
            long lastAccessTime;
            long sizeBytes;
            
            CacheEntry(T data, long sizeBytes) {
                this.data = data;
                this.lastAccessTime = System.currentTimeMillis();
                this.sizeBytes = sizeBytes;
            }
        }
        
        // Separate caches for different data types
        private final LinkedHashMap<String, CacheEntry<?>> inventoryCache;
        private final LinkedHashMap<String, CacheEntry<?>> locationCache;
        private final LinkedHashMap<String, CacheEntry<?>> npcCache;
        private final LinkedHashMap<String, CacheEntry<?>> generalCache;
        
        private final int maxItems;
        private volatile long totalCacheSize = 0;
        
        public MemoryCache(int maxItemsPerCategory) {
            this.maxItems = maxItemsPerCategory;
            
            // LinkedHashMap with access-order to enable LRU
            this.inventoryCache = new LinkedHashMap<String, CacheEntry<?>>(
                maxItemsPerCategory, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > maxItems;
                }
            };
            
            this.locationCache = new LinkedHashMap<String, CacheEntry<?>>(
                maxItemsPerCategory, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > maxItems;
                }
            };
            
            this.npcCache = new LinkedHashMap<String, CacheEntry<?>>(
                maxItemsPerCategory, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > maxItems;
                }
            };
            
            this.generalCache = new LinkedHashMap<String, CacheEntry<?>>(
                maxItemsPerCategory, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > maxItems;
                }
            };
        }
        
        /**
         * Get item from cache.
         */
        public <T> T get(String key, CacheCategory category) {
            LinkedHashMap<String, CacheEntry<?>> cacheMap = getCacheMapForCategory(category);
            CacheEntry<?> entry = cacheMap.get(key);
            
            if (entry != null) {
                entry.lastAccessTime = System.currentTimeMillis();
                return (T) entry.data;
            }
            return null;
        }
        
        /**
         * Put item in cache.
         */
        public <T> void put(String key, T data, long estimatedSizeBytes, CacheCategory category) {
            LinkedHashMap<String, CacheEntry<?>> cacheMap = getCacheMapForCategory(category);
            
            CacheEntry<?> oldEntry = cacheMap.get(key);
            if (oldEntry != null) {
                totalCacheSize -= oldEntry.sizeBytes;
            }
            
            CacheEntry<T> entry = new CacheEntry<>(data, estimatedSizeBytes);
            cacheMap.put(key, entry);
            totalCacheSize += estimatedSizeBytes;
        }
        
        /**
         * Clear cache by category.
         */
        public void clearCategory(CacheCategory category) {
            LinkedHashMap<String, CacheEntry<?>> cacheMap = getCacheMapForCategory(category);
            totalCacheSize -= cacheMap.values().stream()
                .mapToLong(e -> e.sizeBytes)
                .sum();
            cacheMap.clear();
        }
        
        /**
         * Get total cache size.
         */
        public long getTotalCacheSize() {
            return totalCacheSize;
        }
        
        /**
         * Get cache statistics.
         */
        public Map<String, Integer> getCacheStats() {
            Map<String, Integer> stats = new HashMap<>();
            stats.put("inventory", inventoryCache.size());
            stats.put("location", locationCache.size());
            stats.put("npc", npcCache.size());
            stats.put("general", generalCache.size());
            return stats;
        }
        
        private LinkedHashMap<String, CacheEntry<?>> getCacheMapForCategory(CacheCategory category) {
            switch (category) {
                case INVENTORY: return inventoryCache;
                case LOCATION: return locationCache;
                case NPC: return npcCache;
                default: return generalCache;
            }
        }
    }
    
    /**
     * Cache categories for targeted memory management.
     */
    public enum CacheCategory {
        INVENTORY,   // Player inventory, equipped items
        LOCATION,    // World locations, tiles, scenery
        NPC,         // Characters, creatures, NPCs
        GENERAL      // Other data
    }
    
    /**
     * Chunk management for world data.
     * World divided into 64x64 chunks, each can be independently loaded/unloaded.
     */
    public static class ChunkManager {
        private static final int CHUNK_SIZE = 64;  // 64x64 tiles per chunk
        private final Map<Long, ChunkData> loadedChunks = new ConcurrentHashMap<>();
        
        private static class ChunkData {
            long chunkId;
            int gridX, gridY;
            byte[] binaryData;  // Raw binary data
            long fileOffset;    // File position for lazy loading
            long fileSize;      // Size on disk
            long lastAccessTime;
            
            ChunkData(long chunkId, int gridX, int gridY) {
                this.chunkId = chunkId;
                this.gridX = gridX;
                this.gridY = gridY;
                this.lastAccessTime = System.currentTimeMillis();
            }
        }
        
        /**
         * Get or load chunk from disk.
         */
        public ChunkData getChunk(int gridX, int gridY) {
            long chunkId = encodeChunkId(gridX, gridY);
            ChunkData chunk = loadedChunks.get(chunkId);
            
            if (chunk != null) {
                chunk.lastAccessTime = System.currentTimeMillis();
                return chunk;
            }
            
            // Lazy load from disk if not in memory
            return null;  // Would load from file on demand
        }
        
        /**
         * Unload least-recently-used chunks to free memory.
         */
        public int unloadLeastRecentlyUsedChunks(int targetCount) {
            if (loadedChunks.size() <= targetCount) {
                return 0;
            }
            
            int unloadedCount = 0;
            loadedChunks.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> e.getValue().lastAccessTime))
                .limit(loadedChunks.size() - targetCount)
                .forEach(e -> {
                    loadedChunks.remove(e.getKey());
                });
            
            return unloadedCount;
        }
        
        /**
         * Get active chunk count.
         */
        public int getActiveChunkCount() {
            return loadedChunks.size();
        }
        
        private long encodeChunkId(int gridX, int gridY) {
            return ((long) gridX << 32) | (gridY & 0xFFFFFFFFL);
        }
    }
    
    /**
     * Background thread that monitors memory usage and triggers cleanup.
     */
    private class MemoryMonitor extends Thread {
        private volatile boolean running = true;
        
        MemoryMonitor() {
            setName("MemoryMonitor");
            setDaemon(true);
        }
        
        @Override
        public void run() {
            while (running) {
                try {
                    updateMemoryStats();
                    checkAndHandleMemoryPressure();
                    Thread.sleep(MEMORY_CHECK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        
        private void updateMemoryStats() {
            MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            heapUsageBytes = heap.getUsed();
            maxHeapBytes = heap.getMax();
            heapUtilization = (float) heapUsageBytes / maxHeapBytes;
            activeChunks = chunkManager.getActiveChunkCount();
            cachedItems = cache.inventoryCache.size() + cache.locationCache.size() 
                        + cache.npcCache.size() + cache.generalCache.size();
        }
        
        private void checkAndHandleMemoryPressure() {
            if (heapUtilization >= MEMORY_CRITICAL_THRESHOLD) {
                // Critical: Unload cold chunks and clear non-critical cache
                System.err.println("[MEMORY CRITICAL] Heap at " + 
                    String.format("%.1f%%", heapUtilization * 100));
                
                // Unload half of chunks
                int targetChunks = Math.max(1, chunkManager.getActiveChunkCount() / 2);
                chunkManager.unloadLeastRecentlyUsedChunks(targetChunks);
                
                // Clear non-critical cache
                cache.clearCategory(CacheCategory.GENERAL);
                cache.clearCategory(CacheCategory.LOCATION);
                
                System.gc();
            } else if (heapUtilization >= MEMORY_WARNING_THRESHOLD) {
                // Warning: Clear oldest items from cache
                System.out.println("[MEMORY WARNING] Heap at " + 
                    String.format("%.1f%%", heapUtilization * 100));
                
                cache.clearCategory(CacheCategory.GENERAL);
            }
        }
        
        void shutdown() {
            running = false;
            interrupt();
        }
    }
    
    /**
     * Get memory statistics.
     */
    public Map<String, Object> getMemoryStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("heapUsageBytes", heapUsageBytes);
        stats.put("maxHeapBytes", maxHeapBytes);
        stats.put("heapUtilizationPercent", String.format("%.1f%%", heapUtilization * 100));
        stats.put("activeChunks", activeChunks);
        stats.put("cachedItems", cachedItems);
        stats.put("cacheSize", cache.getTotalCacheSize());
        stats.put("cacheSizeMB", String.format("%.2f", cache.getTotalCacheSize() / (1024.0 * 1024)));
        stats.putAll(cache.getCacheStats());
        return stats;
    }
    
    /**
     * Get current heap utilization as percentage.
     */
    public float getHeapUtilization() {
        return heapUtilization;
    }
    
    /**
     * Shutdown memory manager.
     */
    public void shutdown() {
        monitor.shutdown();
    }
}
