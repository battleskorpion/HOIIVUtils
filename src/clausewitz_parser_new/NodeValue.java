package clausewitz_parser_new;

import java.util.ArrayList;

/**
 *  stores string, number, ArrayList<Node>, SymbolNode, or null
 */
public final class NodeValue {
	private final Object value;

//	public NodeValue(Object value) {
//		this.value = value;
//	}

	public NodeValue(ArrayList<Node> value) {
		this.value = value;
	}

	public NodeValue(String value) {
		this.value = value;
	}

	// todo? hm
	public NodeValue(Number value) {
		this.value = value;
	}

	public NodeValue(SymbolNode value) {
		this.value = value;
	}

	public NodeValue() {
		this.value = null;
	}

	public Object valueObject() {
		return value;
	}

	public String string() {
		if (value instanceof String) {
			return (String) value;
		}
		if (value == null) return null;

		// todo better error handling
		throw new IllegalStateException("Expected NodeValue to be a string");
	}

	public int integer() {
		if (value instanceof Number) {
			if (value instanceof Integer) {
				return (int) value;
			}
			return ((Number) value).intValue();
		}

		// todo better error handling
		throw new IllegalStateException("Expected NodeValue to be a Number");
	}

	public ArrayList<Node> list() {
		if (value instanceof ArrayList<?>) {
			return (ArrayList<Node>) value;
		}
		if (value == null) return null;

		// todo better error handling
		throw new IllegalStateException("Expected NodeValue to be an ArrayList<Node>");
	}

	// todo check allowables
}
