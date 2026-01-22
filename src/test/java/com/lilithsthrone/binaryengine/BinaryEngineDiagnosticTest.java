package com.lilithsthrone.binaryengine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Diagnostic tests for binary engine read/write correctness.
 * Generates logs to verify serialization behavior and catch issues.
 */
@DisplayName("Binary Engine - Diagnostic Logging Tests")
public class BinaryEngineDiagnosticTest {

    private BinaryStream stream;
    private PrintStream originalOut;
    private ByteArrayOutputStream logCapture;

    @BeforeEach
    public void setUp() {
        stream = new BinaryStream();
        // Capture logs
        originalOut = System.out;
        logCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(logCapture));
    }

    // ==================== WRITE OPERATION VERIFICATION ====================

    @Test
    @DisplayName("Log and verify byte write operation")
    public void testByteWriteLogging() {
        // Arrange
        byte value = 42;
        BinaryStream.Writer writer = stream.new Writer();
        
        // Act
        System.out.println("[WRITE] Byte value: " + value);
        writer.writeByte(value);
        byte[] buffer = writer.getBuffer();
        System.out.println("[WRITE] Buffer size: " + buffer.length + " bytes");
        System.out.println("[WRITE] Buffer hex: " + bytesToHex(buffer));
        
        // Assert
        assertNotNull(buffer, "Buffer should not be null");
        assertTrue(buffer.length > 0, "Buffer should contain data");
        System.out.println("[PASS] Byte write successful");
    }

    @Test
    @DisplayName("Log and verify int write operation")
    public void testIntWriteLogging() {
        // Arrange
        int value = 1000000;
        BinaryStream.Writer writer = stream.new Writer();
        
        // Act
        System.out.println("[WRITE] Int value: " + value);
        writer.writeInt(value);
        byte[] buffer = writer.getBuffer();
        System.out.println("[WRITE] Buffer size: " + buffer.length + " bytes");
        System.out.println("[WRITE] Buffer hex: " + bytesToHex(buffer));
        
        // Assert
        assertNotNull(buffer, "Buffer should contain serialized int");
        System.out.println("[PASS] Int write successful");
    }

    @Test
    @DisplayName("Log and verify string write operation")
    public void testStringWriteLogging() {
        // Arrange
        String value = "Binary Engine Test";
        BinaryStream.Writer writer = stream.new Writer();
        
        // Act
        System.out.println("[WRITE] String value: " + value);
        System.out.println("[WRITE] String length: " + value.length() + " characters");
        writer.writeString(value);
        byte[] buffer = writer.getBuffer();
        System.out.println("[WRITE] Buffer size: " + buffer.length + " bytes");
        System.out.println("[WRITE] Buffer hex (first 32): " + 
                          bytesToHex(java.util.Arrays.copyOf(buffer, Math.min(32, buffer.length))));
        
        // Assert
        assertNotNull(buffer, "Buffer should contain serialized string");
        System.out.println("[PASS] String write successful");
    }

    // ==================== READ OPERATION VERIFICATION ====================

    @Test
    @DisplayName("Log and verify byte read operation")
    public void testByteReadLogging() {
        // Arrange: Write a byte
        byte originalValue = 42;
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeByte(originalValue);
        byte[] data = writer.getBuffer();
        
        System.out.println("[WRITE] Original byte: " + originalValue);
        System.out.println("[WRITE] Serialized to: " + bytesToHex(data));
        
        // Act: Read it back
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        System.out.println("[READ] Reading from buffer...");
        byte readValue = reader.readByte();
        System.out.println("[READ] Read byte: " + readValue);
        
        // Assert
        assertEquals(originalValue, readValue, "Read value should match written value");
        System.out.println("[PASS] Byte read successful");
    }

    @Test
    @DisplayName("Log and verify round-trip serialization")
    public void testRoundTripLogging() {
        // Arrange: Create complex data
        int intVal = 12345;
        String strVal = "Test Data";
        double doubleVal = 3.14159;
        boolean boolVal = true;
        
        BinaryStream.Writer writer = stream.new Writer();
        
        // Act: Write all values
        System.out.println("[WRITE] ===== Round-Trip Write =====");
        System.out.println("[WRITE] Int: " + intVal);
        writer.writeInt(intVal);
        
        System.out.println("[WRITE] String: " + strVal);
        writer.writeString(strVal);
        
        System.out.println("[WRITE] Double: " + doubleVal);
        writer.writeDouble(doubleVal);
        
        System.out.println("[WRITE] Boolean: " + boolVal);
        writer.writeBoolean(boolVal);
        
        byte[] data = writer.getBuffer();
        System.out.println("[WRITE] Total buffer size: " + data.length + " bytes");
        
        // Read values back
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        
        System.out.println("[READ] ===== Round-Trip Read =====");
        int readInt = reader.readInt();
        System.out.println("[READ] Int: " + readInt + " (expected: " + intVal + ")");
        
        String readStr = reader.readString();
        System.out.println("[READ] String: " + readStr + " (expected: " + strVal + ")");
        
        double readDouble = reader.readDouble();
        System.out.println("[READ] Double: " + readDouble + " (expected: " + doubleVal + ")");
        
        boolean readBool = reader.readBoolean();
        System.out.println("[READ] Boolean: " + readBool + " (expected: " + boolVal + ")");
        
        // Assert
        assertEquals(intVal, readInt, "Int should match");
        assertEquals(strVal, readStr, "String should match");
        assertEquals(doubleVal, readDouble, 0.0001, "Double should match");
        assertEquals(boolVal, readBool, "Boolean should match");
        System.out.println("[PASS] Round-trip serialization successful");
    }

    // ==================== BUFFER INTEGRITY TESTS ====================

    @Test
    @DisplayName("Log buffer integrity check")
    public void testBufferIntegrityLogging() {
        // Arrange
        BinaryStream.Writer writer = stream.new Writer();
        
        // Write sequence of values
        System.out.println("[WRITE] Writing sequence of values...");
        for (int i = 0; i < 10; i++) {
            writer.writeInt(i);
        }
        
        byte[] buffer = writer.getBuffer();
        System.out.println("[WRITE] Buffer size: " + buffer.length + " bytes");
        System.out.println("[WRITE] Estimated size per int: ~" + (buffer.length / 10) + " bytes");
        
        // Read and verify
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(buffer);
        
        System.out.println("[READ] Reading sequence back...");
        boolean allMatch = true;
        for (int i = 0; i < 10; i++) {
            int readValue = reader.readInt();
            boolean matches = (readValue == i);
            System.out.println("[READ] Position " + i + ": " + readValue + " " + 
                             (matches ? "✓" : "✗ MISMATCH"));
            allMatch = allMatch && matches;
        }
        
        assertTrue(allMatch, "All buffer positions should match");
        System.out.println("[PASS] Buffer integrity verified");
    }

    // ==================== EDGE CASE LOGGING ====================

    @Test
    @DisplayName("Log handling of maximum values")
    public void testMaximumValuesLogging() {
        System.out.println("[TEST] ===== Maximum Values =====");
        
        BinaryStream.Writer writer = stream.new Writer();
        
        // Test max values
        System.out.println("[WRITE] Integer.MAX_VALUE: " + Integer.MAX_VALUE);
        writer.writeInt(Integer.MAX_VALUE);
        
        System.out.println("[WRITE] Long.MAX_VALUE: " + Long.MAX_VALUE);
        writer.writeLong(Long.MAX_VALUE);
        
        System.out.println("[WRITE] Byte.MAX_VALUE: " + Byte.MAX_VALUE);
        writer.writeByte(Byte.MAX_VALUE);
        
        byte[] buffer = writer.getBuffer();
        System.out.println("[WRITE] Total buffer size: " + buffer.length + " bytes");
        
        // Read back
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(buffer);
        
        System.out.println("[READ] Reading maximum values...");
        int readInt = reader.readInt();
        long readLong = reader.readLong();
        byte readByte = reader.readByte();
        
        System.out.println("[READ] Integer: " + readInt + 
                         (readInt == Integer.MAX_VALUE ? " ✓" : " ✗"));
        System.out.println("[READ] Long: " + readLong + 
                         (readLong == Long.MAX_VALUE ? " ✓" : " ✗"));
        System.out.println("[READ] Byte: " + readByte + 
                         (readByte == Byte.MAX_VALUE ? " ✓" : " ✗"));
        
        assertEquals(Integer.MAX_VALUE, readInt);
        assertEquals(Long.MAX_VALUE, readLong);
        assertEquals(Byte.MAX_VALUE, readByte);
        System.out.println("[PASS] Maximum values handled correctly");
    }

    @Test
    @DisplayName("Log handling of unicode strings")
    public void testUnicodeLogging() {
        String[] testStrings = {
            "ASCII only",
            "Émojis: 🎮 🎉 🔥",
            "中文字符",
            "Русский язык",
            "العربية"
        };
        
        System.out.println("[TEST] ===== Unicode String Tests =====");
        
        for (String testStr : testStrings) {
            BinaryStream.Writer writer = stream.new Writer();
            System.out.println("[WRITE] String: " + testStr + " (length: " + 
                             testStr.length() + ")");
            
            writer.writeString(testStr);
            byte[] buffer = writer.getBuffer();
            System.out.println("[WRITE] Serialized to: " + buffer.length + " bytes");
            
            // Read back
            BinaryStream readStream = new BinaryStream();
            BinaryStream.Reader reader = readStream.new Reader(buffer);
            String readStr = reader.readString();
            
            boolean matches = testStr.equals(readStr);
            System.out.println("[READ] " + (matches ? "✓" : "✗") + " Read: " + readStr);
            
            assertEquals(testStr, readStr, "Unicode string should be preserved");
        }
        
        System.out.println("[PASS] All unicode strings handled correctly");
    }

    // ==================== PERFORMANCE LOGGING ====================

    @Test
    @DisplayName("Log performance metrics")
    public void testPerformanceMetricsLogging() {
        System.out.println("[PERF] ===== Performance Metrics =====");
        
        // Write performance
        long startTime = System.nanoTime();
        BinaryStream.Writer writer = stream.new Writer();
        
        for (int i = 0; i < 1000; i++) {
            writer.writeInt(i);
            writer.writeString("Test " + i);
        }
        
        long writeTime = System.nanoTime() - startTime;
        byte[] buffer = writer.getBuffer();
        
        System.out.println("[PERF] Written 1000 int+string pairs");
        System.out.println("[PERF] Total time: " + (writeTime / 1_000_000.0) + " ms");
        System.out.println("[PERF] Total size: " + buffer.length + " bytes");
        System.out.println("[PERF] Time per write: " + (writeTime / 2000.0) + " ns");
        System.out.println("[PERF] Avg size per pair: " + (buffer.length / 1000) + " bytes");
        
        // Read performance
        startTime = System.nanoTime();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(buffer);
        
        for (int i = 0; i < 1000; i++) {
            reader.readInt();
            reader.readString();
        }
        
        long readTime = System.nanoTime() - startTime;
        System.out.println("[PERF] Read time: " + (readTime / 1_000_000.0) + " ms");
        System.out.println("[PERF] Time per read: " + (readTime / 2000.0) + " ns");
        
        assertTrue(writeTime < 100_000_000, "1000 writes should complete quickly");
        assertTrue(readTime < 100_000_000, "1000 reads should complete quickly");
        System.out.println("[PASS] Performance acceptable");
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Convert byte array to hex string for logging
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x ", b));
        }
        return sb.toString().trim();
    }
}
