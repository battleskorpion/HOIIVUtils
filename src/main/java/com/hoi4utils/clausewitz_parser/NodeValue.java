//package com.hoi4utils.clausewitz_parser;
//
//import com.hoi4utils.clausewitz.BoolType;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * stores string, number, ArrayList<Node>, SymbolNode, or null
// */
//// todo should be subclasseed to impl effect parameter?
//public final class NodeValue {
//	private final Object value;
//
//	public NodeValue(ArrayList<Node> value) {
//		this.value = value;
//	}
//
//	public NodeValue(String value) {
//		this.value = value;
//	}
//
//	// todo? hm
//	public NodeValue(Number value) {
//		this.value = value;
//	}
//
//	public NodeValue() {
//		this.value = null;
//	}
//
//	public Object valueObject() {
//		return value;
//	}
//
//	public String string() {
//		if (value instanceof String) {
//			return (String) value;
//		}
//		if (value == null)
//			return null;
//
//		// todo better error handling
//		System.err.println("Expected NodeValue value to be a string, value: " + value);
//		throw new IllegalStateException("Expected NodeValue value to be a string, value: " + value);
//	}
//
//	public int integer() {
//		if (value instanceof Number) {
//			if (value instanceof Integer) {
//				return (int) value;
//			}
//			return ((Number) value).intValue();
//		}
//
//		// todo better error handling
//		throw new IllegalStateException("Expected NodeValue to be a Number, value: " + value);
//	}
//
//	public int integerOrElse(int i) {
//		if (value == null) {
//			return i;
//		}
//
//		if (value instanceof Number) {
//			if (value instanceof Integer) {
//				return (int) value;
//			}
//			return ((Number) value).intValue();
//		}
//
//		// todo better error handling
//		throw new IllegalStateException("Expected NodeValue to be a Number or null, value: " + value);
//	}
//
//	public Integer intClass() {
//		if (value == null) {
//			return null;
//		}
//
//		if (value instanceof Number) {
//			if (value instanceof Integer) {
//				return (Integer) value;
//			}
//			return ((Number) value).intValue();
//		}
//
//		// todo better error handling
//		throw new IllegalStateException("Expected NodeValue to be a Number or null, value: " + value);
//	}
//
//	public double rational() {
//		if (value instanceof Number) {
//			if (value instanceof Double) {
//				return (double) value;
//			}
//			return ((Number) value).doubleValue();
//		}
//
//		// todo better error handling
//		throw new IllegalStateException("Expected NodeValue to be a Number, value: " + value);
//	}
//
//	public Double doubleClass() {
//		if (value == null) {
//			return null;
//		}
//
//		if (value instanceof Number) {
//			if (value instanceof Double) {
//				return (Double) value;
//			}
//			return ((Number) value).doubleValue();
//		}
//
//		// todo better error handling
//		throw new IllegalStateException("Expected NodeValue to be a Number, value: " + value);
//	}
//
//	public boolean bool(BoolType boolType) {
//		if (value instanceof String) {
//			return value.equals(boolType.trueResponse());
//		}
//		if (value == null)
//			return false;
//
//		// todo better error handling
//		throw new IllegalStateException("Expected NodeValue to be interpretable as a Boolean, value: " + value);
//	}
//
//	public ArrayList<Node> list() {
//		if (value instanceof ArrayList<?>) {
//			return (ArrayList<Node>) value; // ! unchecked cast
//		}
//		if (value instanceof Node) {
//			ArrayList<Node> list = new ArrayList<>();
//			list.add((Node) value);
//			return list;
//		}
//		if (value == null)
//			return null;
//
//		// todo better error handling
//		throw new IllegalStateException("Expected NodeValue to be an ArrayList<Node>, value: " + value);
//	}
//
//	public Node node() {
//		if (value instanceof Node) {
//			return (Node) value;
//		}
//		if (value == null)
//			return null;
//
//		throw new IllegalStateException("Expected NodeValue to be a Node, value: " + value);
//	}
//
//	@NotNull
//	public String asString() {
//		return switch (value) {
//			case String s -> s;
//			case Integer i -> Integer.toString(i);
//			case Double d -> Double.toString(d);
//			case Number n -> Long.toString(n.longValue());
//			case List<?> l -> Arrays.toString(l.toArray());
//			case Node n -> n.toString();
//			case null -> "[null]";
//			default -> "[invalid type]";
//		};
//	}
//
//	public boolean isList() {
//		return value instanceof List<?>;
//	}
//
//	public boolean isString() {
//		return value instanceof String;
//	}
//
//	public boolean isNumber() {
//		return value instanceof Number;
//	}
//
//
//	// public boolean isBoolean() {
//	// return value instanceof Boolean;
//	// }
//
//	// todo check allowables
//}
