//package com.hoi4utils.clausewitz_parser;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.regex.Pattern;
//
///**
// * Adapted partially from <a href="https://github.com/herbix/hoi4modutilities/blob/master/src/hoiformat/hoiparser.ts">hoiparser.ts</a>
// * from repo <a href="https://github.com/herbix/hoi4modutilities">herbix/hoi4modutilities</a>
// *
// */
//public class Token {
//	public String value;
//	public TokenType type;
//	public int start;
//
//	public static final Map<TokenType, Pattern> tokenRegex = new HashMap<>();
//	public static final String EOF_INDICATOR = "$";
//
//
//	static {
//		tokenRegex.put(TokenType.comment, Pattern.compile("#.*")); // Nullifies Comments  // prev: "#.*(?:[\r\n]|$)"
//		tokenRegex.put(TokenType.symbol, Pattern.compile("(?:\\d+\\.)?[a-zA-Z_@\\[\\]][\\w:.@\\[\\]\\-?^/\\u00A0-\\u024F]*")); // Symbol
//		tokenRegex.put(TokenType.operator, Pattern.compile("[={}<>;,]|>=|<=|!=")); // Seperates Operators
//		tokenRegex.put(TokenType.string, Pattern.compile("\"(\\\\.|[^\"])*\"")); // Seperates Double Quotes
//		tokenRegex.put(TokenType.number, Pattern.compile("-?\\d*\\.\\d+|-?\\d+|0x\\d+")); // Seperates Numbers
//		tokenRegex.put(TokenType.eof, Pattern.compile(EOF_INDICATOR));
//	}
//
//
//	public Token (String value, int start, TokenType type) {
//		this.value = value;
//		this.start = start;
//		this.type = type;
//	}
//
//	public Token(String value, int start) {
//		this.value = value;
//		this.start = start;
//		this.type = determineTokenType(value);
//	}
//
//	/**
//	 * Determine token type based on token regex map
// 	 */
//	private TokenType determineTokenType(String value) {
//		for (Map.Entry<TokenType, Pattern> entry : tokenRegex.entrySet()) {
//			if (entry.getValue().matcher(value).matches()) {
//				return entry.getKey();
//			}
//		}
//		return TokenType.unknown;
//	}
//
//	public int length() {
//		return this.value.length();
//	}
//
//	public String toString() {
//		return this.value;
//	}
//}
