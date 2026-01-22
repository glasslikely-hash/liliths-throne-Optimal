package com.lilithsthrone.persistence.binary;

import java.io.IOException;

/**
 * Base interface for all objects that can be serialized to binary format.
 * Implementations must provide deterministic serialization for snapshot consistency.
 * 
 * Contract:
 * - writeBinary() must be deterministic (same input = same bytes)
 * - readBinary() must reconstruct object identically
 * - Order of writes/reads must match exactly
 * - Null handling must be explicit
 * 
 * @since 0.4.11.3
 * @version 0.4.11.3
 * @author Refactoring Agent
 */
public interface BinarySerializable {
	
	/**
	 * Serialize this object to the given stream.
	 * Must write data in deterministic order.
	 * 
	 * @param stream The output stream
	 * @throws IOException if write fails
	 */
	void writeBinary(BinaryStream.Writer stream) throws IOException;
	
	/**
	 * Deserialize this object from the given stream.
	 * Must read data in same order as writeBinary().
	 * 
	 * @param stream The input stream
	 * @throws IOException if read fails
	 */
	void readBinary(BinaryStream.Reader stream) throws IOException;
	
	/**
	 * Get the schema version this object was serialized with.
	 * Used for migration and compatibility checking.
	 * 
	 * @return schema version (default 1)
	 */
	default int getSchemaVersion() {
		return 1;
	}
	
	/**
	 * Get unique identifier for this object type.
	 * Used by SchemaRegistry for type mapping.
	 * 
	 * @return unique type ID
	 */
	String getTypeId();
}
