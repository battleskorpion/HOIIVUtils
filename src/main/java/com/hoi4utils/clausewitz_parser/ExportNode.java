package com.hoi4utils.clausewitz_parser;//package hoi4utils.clausewitz_parser_new;
//
//import java.util.Map;
//import java.util.HashMap;
//
//// todo good name?
//public class ExportNode {
//	public static <T> Map<String, Object> convertNodeToJson(Node node, SchemaDef<T> schemaDef) {
//		SchemaDef schema = schemaDef;
//		Map<String, Object> result;
//		node = applyConstantsToNode(node, new HashMap<>());
//
//		if (schema instanceof String) {
//			switch ((String) schema) {
//				case "string":
//					result = convertString(node);
//					break;
//				case "number":
//					result = convertNumber(node);
//					break;
//				case "numberlike":
//					result = convertNumberLike(node);
//					break;
//				case "stringignorecase":
//					result = convertStringIgnoreCase(node);
//					break;
//				case "boolean":
//					result = convertBoolean(node);
//					break;
//				case "enum":
//					result = convertEnum(node);
//					break;
//				case "raw":
//					result = Map.of("_raw", node);
//					break;
//				default:
//					throw new IllegalArgumentException("Unknown schema " + schema);
//			}
//		} else if (schema instanceof Map) {
//			String type = ((Map) schema).get("_type").toString();
//			if (type.equals("map")) {
//				result = convertMap(node, ((Map) schema).get("_innerType"));
//			} else if (type.equals("array")) {
//				throw new IllegalArgumentException("Array can't be here.");
//			} else if (type.equals("detailvalue")) {
//				result = convertDetailValue(node, ((Map) schema).get("_innerType"));
//			} else {
//				result = convertObject(node, (Map) schema);
//			}
//		} else {
//			throw new IllegalArgumentException("Bad schema " + schema);
//		}
//
//		if (result != null && result instanceof Map) {
//			((Map<String, Object>) result).put("_token", node.nameToken != null ? node.nameToken : null);
//		}
//
//		return result;
//	}
//
//	private static Node applyConstantsToNode(Node node, Map<String, NodeValue> constants) {
//		// Implement applyConstantsToNode logic here
//		return node;
//	}
//
//	private static Map<String, Object> convertString(Node node) {
//		// Implement convertString logic here
//		return null;
//	}
//
//	private static Map<String, Object> convertNumber(Node node) {
//		// Implement convertNumber logic here
//		return null;
//	}
//
//	private static Map<String, Object> convertNumberLike(Node node) {
//		// Implement convertNumberLike logic here
//		return null;
//	}
//
//	private static Map<String, Object> convertStringIgnoreCase(Node node) {
//		// Implement convertStringIgnoreCase logic here
//		return null;
//	}
//
//	private static Map<String, Object> convertBoolean(Node node) {
//		// Implement convertBoolean logic here
//		return null;
//	}
//
//	private static Map<String, Object> convertEnum(Node node) {
//		// Implement convertEnum logic here
//		return null;
//	}
//
//	private static Map<String, Object> convertMap(Node node, Object innerType) {
//		// Implement convertMap logic here
//		return null;
//	}
//
//	private static Map<String, Object> convertDetailValue(Node node, Object innerType) {
//		// Implement convertDetailValue logic here
//		return null;
//	}
//
//	private static Map<String, Object> convertObject(Node node, Map<String, Object> schema) {
//		// Implement convertObject logic here
//		return null;
//	}
//}
