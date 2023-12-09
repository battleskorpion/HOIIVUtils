package com.HOIIVUtils.clausewitz_parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

/*
 * Parser File new
 */
// todo what is a unitnumber? do i need this
public class Parser {
	public static final String escape_backslash_regex = "\\\\";
	public static final String escape_quote_regex = "\\\\\"";
	private final Tokenizer tokens;
	private Node rootNode;

	public Parser (String input) {
		/* EOF */
		input += Token.EOF_INDICATOR;
		tokens = new Tokenizer(input);
	}

	public Parser(File focusFile) {
		/* get input from file */
		String input;
		try {
			input = Files.readString(focusFile.toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		/* EOF */
		input += Token.EOF_INDICATOR;
		tokens = new Tokenizer(input);
	}

	public Node parse() throws ParserException {
		ArrayList<Node> value = parseBlockContent(tokens);

		/*
		need to reach up to eof indicator
		if last token is '}' this could indicate there was a
		missing '{' in the code
		*/
		if (tokens.peek().type != TokenType.eof) {
			throw new ParserException("Input not completely parsed by clausewitz-file parser \n" +
					"\t\tlast token: " +tokens.peek().value);
		}

		rootNode = new Node(value);
		return rootNode;
	}

	public ArrayList<Node> parseBlockContent(Tokenizer tokens) throws ParserException {
		final ArrayList<Node> nodes = new ArrayList<>();

		while (true) {
			final Token nextToken = tokens.peek();
			if (nextToken.type == TokenType.eof || nextToken.value.equals("}")) {
				break;
			}

			try {
				nodes.add(parseNode(tokens));
			} catch (ParserException e) {
				throw e;
			}
		}

		return nodes;
	}

	public Node parseNode(Tokenizer tokens) throws ParserException {
		Token name = tokens.next();

		/* skip comments */
		if (name.type == TokenType.comment) {
			Node node = new Node();
			node.name = name.value;
			node.nameToken = name;
			return node;
		}
//		System.out.println(name.value);

		if (name.type != TokenType.string && name.type != TokenType.symbol && name.type != TokenType.number) {
			throw new ParserException("Parser: incorrect token type " + name.type + ", token: " + name + " at index: " + name.start);
		}

		var nextToken = tokens.peek();
		if (nextToken.type != TokenType.operator || nextToken.value.matches("^[,;}]$")) {
			while (nextToken.value.matches("^[,;]$")) {
				tokens.next();
				nextToken = tokens.peek();
			}

			/* handle escaped characters */
			var nameValue = unescapeCharacters(name, name.value);

			// TODO
			Node node = new Node();
			node.name = nameValue;
			node.nameToken = name;
			node.operator = null;
			node.operatorToken = null;
			/* node.value = null; */
			node.valueAttachment = null;
			node.valueAttachmentToken = null;

			return node;
		}

		/* "= {" */
		Token operator;
		if (nextToken.value.equals("{")) {
			operator = new Token("=", nextToken.start, TokenType.operator); // Create a new Token with the '=' value
			nextToken = new Token("=", operator.start, TokenType.operator);
			nextToken.start = operator.start;
			operator.value = "=";
		} else {
			operator = tokens.next();
		}

		SymbolNode valueAttachment = null;      // potential 
		Token valueAttachmentToken = null;
		NodeValue parsedValue = parseNodeValue(tokens);

		/* Handle value attachment (e.g., when there's a nested block) */
		if (parsedValue != null && parsedValue.valueObject() instanceof Node) {
			Token peekedToken = tokens.peek();
			if (peekedToken.value.equals("{")) {
				valueAttachment = new SymbolNode(parsedValue);
//				valueAttachmentToken = (Token) parsedValue; // todo?
				parsedValue = parseNodeValue(tokens);
			}
		}

		// Skip comments before tailComma
		Token tailComma = tokens.peek();
		while (tailComma.type == TokenType.comment || tailComma.value.matches("^[,;]")) {
			tokens.next();
			tailComma = tokens.peek();
		}

		// todo
		Node node = new Node(parsedValue);
		node.name = name.value;
		node.nameToken = name;
		node.operator = operator.value;
		node.operatorToken = operator;
//		node.value = parsedValue;
//		node.valueStartToken = (Token) parsedValue[1];
//		node.valueEndToken = (Token) parsedValue[2];
		node.valueAttachment = valueAttachment;
		node.valueAttachmentToken = valueAttachmentToken;

		return node;
	}

	private String unescapeCharacters(Token name, String nameValue) {
		if (name.type == TokenType.string) {
			nameValue = nameValue.substring(1, nameValue.length() - 2)
					.replaceAll(escape_quote_regex, "\"")
					.replaceAll(escape_backslash_regex, "\\");
		}
		return nameValue;
	}

	public NodeValue parseNodeValue(Tokenizer tokens) throws ParserException {
		var nextToken = tokens.next();
		// todo maybe?
		if (nextToken == null) {
			return null;
		}

		// todo eeeh?
		switch (nextToken.type) {
			case string -> {
				/* substring from 1 to length() - 2: don't replace "" */
				if (nextToken.value.length() == 2) {
					return new NodeValue(nextToken.value);
				}
				return new NodeValue(
						nextToken.value.substring(1, nextToken.length() - 2)
								.replaceAll(escape_quote_regex, "\"")
								.replaceAll(escape_backslash_regex, "\\")
				);
			}
			case number -> {
				/* handles hexadecimal or floating-point/integers */
				return new NodeValue(
						nextToken.value.startsWith("0x")
								? Integer.parseInt(nextToken.value.substring(2), 16)
								: Double.parseDouble(nextToken.value)
						);
			}
			case symbol -> {
				return new NodeValue(nextToken.value);
			}
			case operator -> {
				if (nextToken.value.equals("{")) {
					var result = parseBlockContent(tokens);
					Token right = tokens.next();
					if (!right.value.equals("}")) {
						throw new IllegalStateException("Parser expected a matching \"}\"");
					}

					return new NodeValue(result);
				}
				break; 			// necessary if addtl. case added, so will keep.
			}
			default -> throw new IllegalStateException("Unexpected value: " + nextToken.type);
		}

		throw new IllegalStateException("Parser expected a string, number, symbol, or {");
	}

	public Node rootNode() {
		return rootNode;
	}
}
