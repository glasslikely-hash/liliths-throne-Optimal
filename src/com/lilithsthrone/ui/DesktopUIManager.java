package com.lilithsthrone.ui;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lilithsthrone.main.Main;

/**
 * Desktop implementation of UIManager.
 * Wraps the JavaFX WebView/WebEngine for platform-independent access.
 * 
 * This implementation maintains 100% compatibility with the existing
 * WebView-based UI while enabling other platforms to provide different
 * implementations.
 */
public class DesktopUIManager implements UIManager {

	private static final String PLATFORM_NAME = "Desktop";

	public DesktopUIManager() {
	}

	@Override
	public Object executeScript(String script) {
		try {
			return Main.mainController.getWebEngine().executeScript(script);
		} catch (Exception e) {
			System.err.println("Error executing script: " + script);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void setContent(String html) {
		try {
			Main.mainController.getWebEngine().load("data:text/html," + html);
		} catch (Exception e) {
			System.err.println("Error setting content");
			e.printStackTrace();
		}
	}

	@Override
	public void appendContent(String html) {
		try {
			// For WebView, we use JavaScript to append content
			String script = "var div = document.createElement('div'); div.innerHTML = `" + 
							html.replace("`", "\\`") + "`; document.body.appendChild(div);";
			executeScript(script);
		} catch (Exception e) {
			System.err.println("Error appending content");
			e.printStackTrace();
		}
	}

	@Override
	public Document getDocument() {
		try {
			return Main.mainController.getWebEngine().getDocument();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String getFormValue(String elementId) {
		try {
			Document doc = getDocument();
			if (doc != null) {
				Element element = doc.getElementById(elementId);
				if (element != null) {
					String value = element.getAttribute("value");
					return value != null ? value : "";
				}
			}
		} catch (Exception e) {
			System.err.println("Error getting form value for: " + elementId);
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public void setFormValue(String elementId, String value) {
		try {
			Document doc = getDocument();
			if (doc != null) {
				Element element = doc.getElementById(elementId);
				if (element != null) {
					element.setAttribute("value", value);
				}
			}
		} catch (Exception e) {
			System.err.println("Error setting form value for: " + elementId);
			e.printStackTrace();
		}
	}

	@Override
	public String getElementText(String elementId) {
		try {
			Document doc = getDocument();
			if (doc != null) {
				Element element = doc.getElementById(elementId);
				if (element != null) {
					String text = element.getTextContent();
					return text != null ? text : "";
				}
			}
		} catch (Exception e) {
			System.err.println("Error getting element text for: " + elementId);
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public void setElementHTML(String elementId, String html) {
		try {
			Document doc = getDocument();
			if (doc != null) {
				Element element = doc.getElementById(elementId);
				if (element != null) {
					element.setTextContent(html);
				}
			}
		} catch (Exception e) {
			System.err.println("Error setting element HTML for: " + elementId);
			e.printStackTrace();
		}
	}

	@Override
	public void setElementText(String elementId, String text) {
		try {
			Document doc = getDocument();
			if (doc != null) {
				Element element = doc.getElementById(elementId);
				if (element != null) {
					element.setTextContent(text);
				}
			}
		} catch (Exception e) {
			System.err.println("Error setting element text for: " + elementId);
			e.printStackTrace();
		}
	}

	@Override
	public boolean elementExists(String elementId) {
		try {
			Document doc = getDocument();
			if (doc != null) {
				return doc.getElementById(elementId) != null;
			}
		} catch (Exception e) {
			// Fall through
		}
		return false;
	}

	@Override
	public int getScrollPosition(String elementId) {
		try {
			Object result = executeScript("document.getElementById('" + elementId + "').scrollTop");
			if (result instanceof Number) {
				return ((Number) result).intValue();
			}
		} catch (Exception e) {
			System.err.println("Error getting scroll position for: " + elementId);
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void setScrollPosition(String elementId, int position) {
		try {
			executeScript("document.getElementById('" + elementId + "').scrollTop = " + position);
		} catch (Exception e) {
			System.err.println("Error setting scroll position for: " + elementId);
			e.printStackTrace();
		}
	}

	@Override
	public void updateElementClass(String elementId, String className, boolean add) {
		try {
			String operation = add ? "add" : "remove";
			executeScript("document.getElementById('" + elementId + "').classList." + operation + "('" + className + "')");
		} catch (Exception e) {
			System.err.println("Error updating class for: " + elementId);
			e.printStackTrace();
		}
	}

	@Override
	public boolean supportsSynchronousDOMOperations() {
		return true; // WebView operations are synchronous
	}

	@Override
	public String getPlatformName() {
		return PLATFORM_NAME;
	}

}
