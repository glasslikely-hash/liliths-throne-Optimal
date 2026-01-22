package com.lilithsthrone.utils;

/**
 * Replacement for JavaFX KeyCode enum.
 * Provides a cross-platform way to handle keyboard input codes.
 */
public enum KeyCode {
	// Special keys
	ESCAPE("Esc"),
	ENTER("Enter"),
	TAB("Tab"),
	BACK_SPACE("Back space"),
	DELETE("Delete"),
	INSERT("Insert"),
	HOME("Home"),
	END("End"),
	PAGE_UP("Page Up"),
	PAGE_DOWN("Page Down"),
	
	// Arrow keys
	UP("Up"),
	DOWN("Down"),
	LEFT("Left"),
	RIGHT("Right"),
	
	// Modifiers
	SHIFT("Shift"),
	CONTROL("Ctrl"),
	ALT("Alt"),
	META("Meta"),
	CAPS("Caps"),
	
	// Function keys
	F1("F1"), F2("F2"), F3("F3"), F4("F4"), F5("F5"),
	F6("F6"), F7("F7"), F8("F8"), F9("F9"), F10("F10"),
	F11("F11"), F12("F12"),
	
	// Numpad
	NUM_LOCK("Num Lock"),
	NUMPAD0("0"), NUMPAD1("1"), NUMPAD2("2"), NUMPAD3("3"),
	NUMPAD4("4"), NUMPAD5("5"), NUMPAD6("6"), NUMPAD7("7"),
	NUMPAD8("8"), NUMPAD9("9"),
	MULTIPLY("*"),
	ADD("+"),
	DIVIDE("/"),
	SUBTRACT("-"),
	DECIMAL("."),
	
	// Symbol keys
	SPACE(" "),
	COMMA(","),
	PERIOD("."),
	SLASH("/"),
	SEMICOLON(";"),
	QUOTE("'"),
	OPEN_BRACKET("["),
	CLOSE_BRACKET("]"),
	BACK_SLASH("\\"),
	MINUS("-"),
	EQUALS("="),
	BACK_QUOTE("`"),
	
	// Alternative names
	BRACELEFT("{"),
	BRACERIGHT("}"),
	COLON(":"),
	DOLLAR("$"),
	AMPERSAND("&"),
	ASTERISK("*"),
	
	// Letter keys (A-Z)
	A("A"), B("B"), C("C"), D("D"), E("E"), F("F"), G("G"), H("H"),
	I("I"), J("J"), K("K"), L("L"), M("M"), N("N"), O("O"), P("P"),
	Q("Q"), R("R"), S("S"), T("T"), U("U"), V("V"), W("W"), X("X"),
	Y("Y"), Z("Z"),
	
	// Number keys (0-9)
	DIGIT0("0"), DIGIT1("1"), DIGIT2("2"), DIGIT3("3"), DIGIT4("4"),
	DIGIT5("5"), DIGIT6("6"), DIGIT7("7"), DIGIT8("8"), DIGIT9("9"),
	
	// Unknown
	UNDEFINED("Unknown");
	
	private final String displayName;
	
	KeyCode(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
}
