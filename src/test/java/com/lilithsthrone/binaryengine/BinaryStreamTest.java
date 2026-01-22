package com.lilithsthrone.binaryengine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BinaryStream serialization/deserialization.
 * Validates correct encoding of all data types and edge cases.
 */
@DisplayName("Binary Engine - Stream Serialization Tests")
public class BinaryStreamTest {

    private BinaryStream stream;

    @BeforeEach
    public void setUp() {
        stream = new BinaryStream();
    }

    // ==================== PRIMITIVE TYPE TESTS ====================

    @Test
    @DisplayName("Write and read byte value")
    public void testByteRoundTrip() {
        byte value = 42;
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeByte(value);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        byte result = reader.readByte();
        
        assertEquals(value, result, "Byte value should match after round trip");
    }

    @Test
    @DisplayName("Write and read short value")
    public void testShortRoundTrip() {
        short value = 1024;
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeShort(value);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        short result = reader.readShort();
        
        assertEquals(value, result, "Short value should match after round trip");
    }

    @Test
    @DisplayName("Write and read int value")
    public void testIntRoundTrip() {
        int value = 1000000;
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeInt(value);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        int result = reader.readInt();
        
        assertEquals(value, result, "Int value should match after round trip");
    }

    @Test
    @DisplayName("Write and read long value")
    public void testLongRoundTrip() {
        long value = 9223372036854775L; // Large long
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeLong(value);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        long result = reader.readLong();
        
        assertEquals(value, result, "Long value should match after round trip");
    }

    @Test
    @DisplayName("Write and read float value")
    public void testFloatRoundTrip() {
        float value = 3.14159f;
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeFloat(value);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        float result = reader.readFloat();
        
        assertEquals(value, result, 0.0001f, "Float value should match after round trip");
    }

    @Test
    @DisplayName("Write and read double value")
    public void testDoubleRoundTrip() {
        double value = 2.718281828;
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeDouble(value);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        double result = reader.readDouble();
        
        assertEquals(value, result, 0.0001, "Double value should match after round trip");
    }

    @Test
    @DisplayName("Write and read boolean value")
    public void testBooleanRoundTrip() {
        boolean value = true;
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeBoolean(value);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        boolean result = reader.readBoolean();
        
        assertEquals(value, result, "Boolean value should match after round trip");
    }

    @Test
    @DisplayName("Write and read string value")
    public void testStringRoundTrip() {
        String value = "Hello, Binary Engine!";
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeString(value);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        String result = reader.readString();
        
        assertEquals(value, result, "String value should match after round trip");
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Handle zero values")
    public void testZeroValues() {
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeInt(0);
        writer.writeLong(0L);
        writer.writeFloat(0.0f);
        writer.writeDouble(0.0);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        
        assertEquals(0, reader.readInt());
        assertEquals(0L, reader.readLong());
        assertEquals(0.0f, reader.readFloat());
        assertEquals(0.0, reader.readDouble());
    }

    @Test
    @DisplayName("Handle negative values")
    public void testNegativeValues() {
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeInt(-42);
        writer.writeLong(-1000L);
        writer.writeFloat(-3.14f);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        
        assertEquals(-42, reader.readInt());
        assertEquals(-1000L, reader.readLong());
        assertEquals(-3.14f, reader.readFloat(), 0.01f);
    }

    @Test
    @DisplayName("Handle maximum values")
    public void testMaximumValues() {
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeInt(Integer.MAX_VALUE);
        writer.writeLong(Long.MAX_VALUE);
        writer.writeByte(Byte.MAX_VALUE);
        writer.writeShort(Short.MAX_VALUE);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        
        assertEquals(Integer.MAX_VALUE, reader.readInt());
        assertEquals(Long.MAX_VALUE, reader.readLong());
        assertEquals(Byte.MAX_VALUE, reader.readByte());
        assertEquals(Short.MAX_VALUE, reader.readShort());
    }

    @Test
    @DisplayName("Handle empty string")
    public void testEmptyString() {
        String value = "";
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeString(value);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        String result = reader.readString();
        
        assertEquals(value, result, "Empty string should be preserved");
    }

    @Test
    @DisplayName("Handle unicode strings")
    public void testUnicodeString() {
        String value = "你好世界 🎮 Привет мир";
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeString(value);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        String result = reader.readString();
        
        assertEquals(value, result, "Unicode string should be preserved");
    }

    // ==================== MULTIPLE VALUES TESTS ====================

    @Test
    @DisplayName("Write and read multiple values in sequence")
    public void testMultipleValuesSequence() {
        BinaryStream.Writer writer = stream.new Writer();
        writer.writeInt(42);
        writer.writeString("test");
        writer.writeDouble(3.14);
        writer.writeBoolean(true);
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        
        assertEquals(42, reader.readInt());
        assertEquals("test", reader.readString());
        assertEquals(3.14, reader.readDouble(), 0.01);
        assertTrue(reader.readBoolean());
    }

    @Test
    @DisplayName("Verify buffer consistency")
    public void testBufferConsistency() {
        BinaryStream.Writer writer = stream.new Writer();
        for (int i = 0; i < 100; i++) {
            writer.writeInt(i);
        }
        
        byte[] data = writer.getBuffer();
        BinaryStream readStream = new BinaryStream();
        BinaryStream.Reader reader = readStream.new Reader(data);
        
        for (int i = 0; i < 100; i++) {
            assertEquals(i, reader.readInt(), "Value at position " + i + " should match");
        }
    }

    // ==================== BINARY SIZE TESTS ====================

    @Test
    @DisplayName("Verify binary size is efficient")
    public void testBinarySizeEfficiency() {
        BinaryStream.Writer writer = stream.new Writer();
        String data = "Hello World";
        writer.writeString(data);
        
        byte[] binary = writer.getBuffer();
        // String length (2 bytes) + UTF-8 encoded string
        assertTrue(binary.length < 100, "Binary size should be reasonable");
    }

    @Test
    @DisplayName("Varint encoding reduces size for small numbers")
    public void testVarintSizeOptimization() {
        BinaryStream.Writer smallWriter = stream.new Writer();
        smallWriter.writeInt(127);  // Should use varint (1 byte)
        byte[] smallBinary = smallWriter.getBuffer();
        
        BinaryStream.Writer largeWriter = new BinaryStream().new Writer();
        largeWriter.writeInt(1000000);  // Requires more bytes
        byte[] largeBinary = largeWriter.getBuffer();
        
        assertTrue(smallBinary.length < largeBinary.length, 
            "Small numbers should use fewer bytes");
    }
}
