package com.lilithsthrone.utils;

/**
 * StringBuilder helper for safe string concatenation in performance-critical code.
 * 
 * Reduces string allocation pressure in loops and frequently-called methods.
 * 
 * Benefits:
 *   - Single allocation for complex string building
 *   - Avoids intermediate String objects
 *   - Reusable across calls (thread-local)
 * 
 * Problem this solves:
 *   ❌ String concatenation in loops: "result" += item  // New String each iteration
 *   ✅ StringBuilder usage: sb.append(item)  // Single allocation
 * 
 * Usage in loops:
 *   StringBuilder sb = StringBuilderCache.get();
 *   for (Item item : items) {
 *       sb.append(item.toString()).append("\n");
 *   }
 *   String result = sb.toString();
 *   StringBuilderCache.release(sb);
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class StringBuilderCache {
    
    private static final ThreadLocal<StringBuilder> cache = ThreadLocal.withInitial(StringBuilder::new);
    
    /**
     * Get a StringBuilder from cache.
     * If the thread-local StringBuilder exists, it's cleared and reused.
     * Otherwise, a new one is created.
     * 
     * @return StringBuilder ready for use (empty)
     */
    public static StringBuilder get() {
        StringBuilder sb = cache.get();
        sb.setLength(0); // Clear it while keeping capacity
        return sb;
    }
    
    /**
     * Release StringBuilder back to cache.
     * Call this after you're done with the StringBuilder to free memory.
     * Optional - cache will clean up on thread termination anyway.
     * 
     * @param sb StringBuilder to release
     */
    public static void release(StringBuilder sb) {
        if (sb.capacity() > 8192) {
            // If StringBuilder grew very large, discard and create new one
            // This prevents memory waste from very large strings
            cache.set(new StringBuilder());
        } else {
            // Otherwise, keep it for reuse
            sb.setLength(0);
        }
    }
    
    /**
     * Quick convenience method for one-off string building
     * 
     * Usage: String result = StringBuilderCache.build(sb -> {
     *     sb.append("Hello ").append(name);
     * });
     */
    public static String build(StringBuilderAction action) {
        StringBuilder sb = get();
        try {
            action.apply(sb);
            return sb.toString();
        } finally {
            release(sb);
        }
    }
    
    /**
     * Functional interface for StringBuilder operations
     */
    @FunctionalInterface
    public interface StringBuilderAction {
        void apply(StringBuilder sb);
    }
}
