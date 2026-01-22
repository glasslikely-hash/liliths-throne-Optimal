package com.lilithsthrone.utils.colours;

/**
 * Simple RGB color representation to replace JavaFX Color.
 * Stores colors as ARGB integer values.
 */
public class ColorRGB {
	
	private int argb; // Alpha (8 bits) | Red (8 bits) | Green (8 bits) | Blue (8 bits)
	
	/**
	 * Create a color from ARGB integer value.
	 */
	public ColorRGB(int argb) {
		this.argb = argb;
	}
	
	/**
	 * Create a color from RGB components (0-255).
	 * Alpha defaults to fully opaque (255).
	 */
	public ColorRGB(int red, int green, int blue) {
		this(255, red, green, blue);
	}
	
	/**
	 * Create a color from ARGB components (0-255).
	 */
	public ColorRGB(int alpha, int red, int green, int blue) {
		this.argb = ((alpha & 0xFF) << 24) |
		            ((red & 0xFF) << 16) |
		            ((green & 0xFF) << 8) |
		            (blue & 0xFF);
	}
	
	/**
	 * Parse a color from a web hex string like "#FF0000" or "0xFF0000".
	 */
	public static ColorRGB web(String colorString) {
		String hex = colorString.startsWith("#") ? colorString.substring(1) : colorString;
		if (hex.startsWith("0x") || hex.startsWith("0X")) {
			hex = hex.substring(2);
		}
		
		// Pad with zeros if necessary
		if (hex.length() == 6) {
			hex = "FF" + hex; // Add full opacity
		} else if (hex.length() != 8) {
			throw new IllegalArgumentException("Invalid color format: " + colorString);
		}
		
		int argb = (int) Long.parseLong(hex, 16);
		return new ColorRGB(argb);
	}
	
	/**
	 * Get red component (0.0 - 1.0).
	 */
	public double getRed() {
		return ((argb >> 16) & 0xFF) / 255.0;
	}
	
	/**
	 * Get green component (0.0 - 1.0).
	 */
	public double getGreen() {
		return ((argb >> 8) & 0xFF) / 255.0;
	}
	
	/**
	 * Get blue component (0.0 - 1.0).
	 */
	public double getBlue() {
		return (argb & 0xFF) / 255.0;
	}
	
	/**
	 * Get alpha component (0.0 - 1.0).
	 */
	public double getOpacity() {
		return ((argb >> 24) & 0xFF) / 255.0;
	}
	
	/**
	 * Convert to hex string format used by JavaFX Color.toString().
	 * Format: 0xAARRGGBB
	 */
	@Override
	public String toString() {
		return String.format("0x%08x", argb);
	}
	
	/**
	 * Get raw ARGB integer value.
	 */
	public int getARGB() {
		return argb;
	}
}
