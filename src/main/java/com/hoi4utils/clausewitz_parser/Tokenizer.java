//package com.hoi4utils.clausewitz_parser;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * The Tokenizer class is used to tokenize a string of text into tokens.
// *
// * <p>
// * A Tokenizer is an iterator that produces a sequence of tokens from a string
// * of text. It uses regular expressions to match the tokens in the text and
// * provides methods for iterating over the tokens.
// *
// * <p>
// * Here's an example of how to use a Tokenizer to tokenize a string of text:
// *
// * <pre>
// * {@code
// * Tokenizer tokenizer = new Tokenizer("Hello, world!");
// * Token token;
// * while ((token = tokenizer.next()) != null) {
// * 	System.out.println(token);
// * }
// * }
// * </pre>
// */
//public class Tokenizer {
//	@SuppressWarnings("unused")
//	private final String input;
//	private final Pattern pattern;
//	private final Matcher matcher;
//	private int findStartIndex = 0;
//	private boolean peekOccurred = false;
//
//	public Tokenizer(String input) {
//		this.input = input;
//		this.pattern = createPattern();
//		this.matcher = this.pattern.matcher(input);
//	}
//
//	/**
//	 * Returns the next token from the tokenizer, or null if there are no more
//	 * tokens.
//	 *
//	 * @return the next token from the tokenizer, or null if there are no more
//	 *         tokens
//	 */
//	public Token next() {
//		try {
//			/*
//			 * matcher.find(int) resets the matcher, therefore,
//			 * only use this call when necessary
//			 */
//			if (peekOccurred) {
//				if (matcher.find(findStartIndex)) {
//					String value = matcher.group(0);
//					int start = matcher.start();
//					peekOccurred = false;
//					return new Token(value, start);
//				}
//			} else if (matcher.find()) {
//				String value = matcher.group(0);
//				int start = matcher.start();
//				/* peek occurred already false */
//				return new Token(value, start);
//			}
//			return null;
//		} catch (StackOverflowError e) {
//			System.err.println("EEEE"); // todo
//			System.err.println(matcher.group());
//			return null;
//		}
//	}
//
//	/**
//	 * <code> Matcher </code> has no peek() function therefore this logic is
//	 * necessary.
//	 * returns the next token from matcher, the next <code> find() </code> call to
//	 * <code> matcher </code> however will be from the same start index.
//	 *
//	 * @see Matcher
//	 * @return Next token from matcher
//	 */
//	public Token peek() {
//		if (peekOccurred) {
//			if (matcher.find(findStartIndex)) {
//				String value = matcher.group();
//				int start = matcher.start();
//				/* find_start_index remains the same */
//				/* peek occurred already true */
//				return new Token(value, start);
//			}
//		} else if (matcher.find()) {
//			String value = matcher.group();
//			int start = matcher.start();
//			findStartIndex = matcher.start();
//			peekOccurred = true;
//			return new Token(value, start);
//		}
//
//		return null;
//	}
//
//	/**
//	 * Creates a pattern that matches the tokens in the text.
//	 *
//	 * @return a pattern that matches the tokens in the text
//	 */
//	private Pattern createPattern() {
//		StringBuilder patternBuilder = new StringBuilder();
//
//		for (TokenType tokenType : TokenType.values()) {
//			Pattern tokenRegex = Token.tokenRegex.get(tokenType);
//			if (tokenRegex != null) {
//				patternBuilder.append(String.format("|(?<%s>%s)", tokenType, tokenRegex.pattern()));
//			}
//		}
//
//		if (!patternBuilder.isEmpty()) {
//			return Pattern.compile(patternBuilder.substring(1)); // Skip the leading "|"
//		} else {
//			throw new IllegalStateException("No patterns found for token types.");
//		}
//	}
//}
