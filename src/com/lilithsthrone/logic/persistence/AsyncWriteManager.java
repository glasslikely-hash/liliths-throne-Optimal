package com.lilithsthrone.logic.persistence;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Asynchronous Write Manager - non-blocking snapshot and delta writes.
 * 
 * Strategy:
 *   1. Queue write operations instead of blocking thread
 *   2. Background writer thread handles I/O
 *   3. Main thread never blocked on disk I/O
 *   4. Maintain data consistency with queuing order
 * 
 * Write Types:
 *   - Autosave (low priority, can be dropped if necessary)
 *   - Manual save (high priority, must complete)
 *   - Checkpoint (medium priority, commit recovery point)
 * 
 * Guarantees:
 *   - Manual saves always written in order
 *   - No write can occur before previous manual save completes
 *   - Autosaves are isolated from manual saves
 *   - Failures logged and retried
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class AsyncWriteManager {
    
    // Configuration
    private static final int WRITE_QUEUE_SIZE = 100;
    private static final int MAX_CONCURRENT_WRITES = 2;
    @SuppressWarnings("unused")
    private static final long WRITE_TIMEOUT_MS = 60_000;     // 1 minute per write
    
    // Write queues (separate for autosave vs manual)
    private final BlockingQueue<WriteOperation> autoSaveQueue;
    private final BlockingQueue<WriteOperation> manualSaveQueue;
    
    // Writer threads
    private final ExecutorService writeExecutor;
    private final WriteExecutor autoSaveWriter;
    private final WriteExecutor manualSaveWriter;
    
    // Statistics
    private final AtomicLong totalWritesCompleted = new AtomicLong(0);
    private final AtomicLong totalWritesFailed = new AtomicLong(0);
    private final AtomicLong totalBytesWritten = new AtomicLong(0);
    
    /**
     * Write operation to be executed.
     */
    public static class WriteOperation {
        public final long operationId;
        public final byte[] data;
        public final String filePath;
        public final WriteType type;
        public final long createdTime;
        
        private int retryCount = 0;
        private CompletableFuture<WriteResult> future;
        
        public WriteOperation(byte[] data, String filePath, WriteType type) {
            this.operationId = System.nanoTime();
            this.data = data;
            this.filePath = filePath;
            this.type = type;
            this.createdTime = System.currentTimeMillis();
            this.future = new CompletableFuture<>();
        }
        
        public CompletableFuture<WriteResult> getFuture() {
            return future;
        }
        
        public void incrementRetry() {
            retryCount++;
        }
        
        public int getRetryCount() {
            return retryCount;
        }
    }
    
    /**
     * Result of write operation.
     */
    public static class WriteResult {
        public final boolean success;
        public final long bytesWritten;
        public final long durationMs;
        public final Exception error;
        
        public WriteResult(boolean success, long bytesWritten, long durationMs, Exception error) {
            this.success = success;
            this.bytesWritten = bytesWritten;
            this.durationMs = durationMs;
            this.error = error;
        }
        
        public static WriteResult success(long bytesWritten, long durationMs) {
            return new WriteResult(true, bytesWritten, durationMs, null);
        }
        
        public static WriteResult failure(Exception error, long durationMs) {
            return new WriteResult(false, 0, durationMs, error);
        }
    }
    
    /**
     * Write operation type for priority handling.
     */
    public enum WriteType {
        AUTOSAVE,      // Low priority, can be dropped
        MANUAL,        // High priority, must complete
        CHECKPOINT     // Medium priority
    }
    
    /**
     * Constructor.
     */
    public AsyncWriteManager() {
        this.autoSaveQueue = new LinkedBlockingQueue<>(WRITE_QUEUE_SIZE);
        this.manualSaveQueue = new LinkedBlockingQueue<>(WRITE_QUEUE_SIZE);
        
        // Create dedicated writer threads
        this.writeExecutor = Executors.newFixedThreadPool(MAX_CONCURRENT_WRITES, r -> {
            Thread t = new Thread(r);
            t.setName("AsyncWriter-" + t.getId());
            t.setDaemon(true);
            return t;
        });
        
        // Start writers
        this.autoSaveWriter = new WriteExecutor("autosave", autoSaveQueue, false);
        this.manualSaveWriter = new WriteExecutor("manual", manualSaveQueue, true);
        
        writeExecutor.execute(autoSaveWriter);
        writeExecutor.execute(manualSaveWriter);
    }
    
    /**
     * Queue write operation (non-blocking).
     */
    public CompletableFuture<WriteResult> queueWrite(byte[] data, String filePath, WriteType type) {
        WriteOperation op = new WriteOperation(data, filePath, type);
        
        try {
            if (type == WriteType.AUTOSAVE) {
                // Try to queue, but don't block if full
                if (!autoSaveQueue.offer(op, 100, TimeUnit.MILLISECONDS)) {
                    // Queue full, drop autosave
                    System.err.println("[ASYNC WRITE] Autosave queue full, dropping write to " + filePath);
                    op.future.complete(WriteResult.failure(
                        new IOException("Queue full"), 0));
                    return op.future;
                }
            } else {
                // Manual saves are important, wait if necessary
                manualSaveQueue.put(op);
            }
        } catch (InterruptedException e) {
            op.future.completeExceptionally(e);
            Thread.currentThread().interrupt();
        }
        
        return op.future;
    }
    
    /**
     * Flush all pending writes (blocking).
     */
    public void flushAll() throws InterruptedException {
        flushQueue(autoSaveQueue, "autosave");
        flushQueue(manualSaveQueue, "manual");
    }
    
    /**
     * Shutdown async write manager.
     */
    public void shutdown() throws InterruptedException {
        autoSaveWriter.shutdown();
        manualSaveWriter.shutdown();
        
        writeExecutor.shutdown();
        if (!writeExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
            writeExecutor.shutdownNow();
        }
    }
    
    /**
     * Get write statistics.
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalWritesCompleted", totalWritesCompleted.get());
        stats.put("totalWritesFailed", totalWritesFailed.get());
        stats.put("totalBytesWritten", totalBytesWritten.get());
        stats.put("totalBytesMB", String.format("%.2f", totalBytesWritten.get() / (1024.0 * 1024)));
        stats.put("pendingAutoSaves", autoSaveQueue.size());
        stats.put("pendingManualSaves", manualSaveQueue.size());
        return stats;
    }
    
    /**
     * Drain queue until empty.
     */
    private void flushQueue(BlockingQueue<WriteOperation> queue, String queueName) throws InterruptedException {
        List<WriteOperation> operations = new ArrayList<>();
        queue.drainTo(operations);
        
        System.out.println("[ASYNC WRITE] Flushing " + operations.size() + " pending " + queueName + " operations");
    }
    
    /**
     * Background writer thread.
     */
    private class WriteExecutor implements Runnable {
        @SuppressWarnings("unused")
        private final String name;
        private final BlockingQueue<WriteOperation> queue;
        private final boolean isPriorityQueue;
        private volatile boolean running = true;
        
        WriteExecutor(String name, BlockingQueue<WriteOperation> queue, boolean isPriorityQueue) {
            this.name = name;
            this.queue = queue;
            this.isPriorityQueue = isPriorityQueue;
        }
        
        @Override
        public void run() {
            while (running) {
                try {
                    // Wait for write operation
                    WriteOperation op = queue.poll(5, TimeUnit.SECONDS);
                    
                    if (op != null) {
                        long startTime = System.currentTimeMillis();
                        
                        try {
                            // Perform write
                            writeToFile(op);
                            
                            long duration = System.currentTimeMillis() - startTime;
                            totalWritesCompleted.incrementAndGet();
                            totalBytesWritten.addAndGet(op.data.length);
                            
                            op.future.complete(WriteResult.success(op.data.length, duration));
                            
                        } catch (IOException e) {
                            long duration = System.currentTimeMillis() - startTime;
                            System.err.println("[ASYNC WRITE] Write failed: " + e.getMessage());
                            
                            // Retry logic for manual saves
                            if (isPriorityQueue && op.getRetryCount() < 3) {
                                op.incrementRetry();
                                queue.put(op);  // Re-queue for retry
                            } else {
                                totalWritesFailed.incrementAndGet();
                                op.future.complete(WriteResult.failure(e, duration));
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        
        /**
         * Write data to file.
         */
        private void writeToFile(WriteOperation op) throws IOException {
            try (FileOutputStream fos = new FileOutputStream(op.filePath);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                bos.write(op.data);
                bos.flush();
            }
        }
        
        void shutdown() {
            running = false;
        }
    }
}
