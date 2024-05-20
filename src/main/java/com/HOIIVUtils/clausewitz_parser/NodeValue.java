package com.HOIIVUtils.clausewitz_parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * stores string, number, ArrayList<Node>, SymbolNode, or null
 */
// todo should be subclasseed to impl effect parameter?
public final class NodeValue {
	private final Object value;

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
		if (value == null)
			return null;

		// todo better error handling
		System.err.println("Expected NodeValue value to be a string, value: " + value);
		throw new IllegalStateException("Expected NodeValue value to be a string");
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

	public double rational() {
		if (value instanceof Number) {
			if (value instanceof Double) {
				return (double) value;
			}
			return ((Number) value).doubleValue();
		}

		// todo better error handling
		throw new IllegalStateException("Expected NodeValue to be a Number");
	}

	public ArrayList<Node> list() {
		if (value instanceof ArrayList<?>) {
			return (ArrayList<Node>) value; // ! unchecked cast
		}
		if (value instanceof Node) {
			ArrayList<Node> list = new ArrayList<>();
			list.add((Node) value);
			return list;
		}
		if (value == null)
			return null;

		// todo better error handling
		throw new IllegalStateException("Expected NodeValue to be an ArrayList<Node>");
	}

	public Node node() {
		if (value instanceof Node) {
			return (Node) value;
		}
		if (value == null)
			return null;

		throw new IllegalStateException("Expected NodeValue to be a Node, value: " + value);
	}

	public String asString() {
		if (value instanceof String) {
			return (String) value;
		}
		if (value == null)
			return "[null]";
		if (value instanceof Integer)
			return Integer.toString((int) value);
		if (value instanceof Double)
			return Double.toString((double) value);
		if (value instanceof Number)
			return Long.toString((long) value); // sure why not
		// if (value instanceof List<?>) return "[list]"; // sure why not
		if (value instanceof List<?> l)
			return Arrays.toString(l.toArray()); // sure why not
		if (value instanceof Node)
			return ((Node) value).toString();
		return "[invalid type]";
	}

	public boolean isList() {
		return value instanceof List<?>;
	}

	public boolean isString() {
		return value instanceof String;
	}

	public boolean isNumber() {
		return value instanceof Number;
	}

	// public boolean isBoolean() {
	// return value instanceof Boolean;
	// }

	// todo check allowables
}
