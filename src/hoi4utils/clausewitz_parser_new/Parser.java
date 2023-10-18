package hoi4utils.clausewitz_parser_new;

import java.util.ArrayList;

/*
 * Parser File new
 */
// todo what is a unitnumber? do i need this
public class Parser {
	public static final String escape_backslash_regex = "\\\\";
	public static final String escape_quote_regex = "\\\\\"";
	private static String input = "test = test2";
	private final Tokenizer tokens;

	public static void main(String[] args) {
		Parser parser;
		try {
			parser = new Parser(input);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

//		System.out.println(parser.tokens.next());
//		System.out.println(parser.tokens.next());
		try {
			System.out.println(((ArrayList<Node>)parser.parse().value.value).get(0).name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Parser (String input) {
		/* EOF */
		if (input.charAt(input.length() - 1) != '$') {
			input += "$";
		}

		tokens = new Tokenizer(input);
	}

	public Node parse() throws Exception {
		ArrayList<Node> value = parseBlockContent(tokens);

		/* need to reach up to eof indicator */
		if (tokens.peek().type != TokenType.eof) {
			throw new Exception("Input not completely parsed by clausewitz-file parser");
		}

		return new Node(value);
	}

	public ArrayList<Node> parseBlockContent(Tokenizer tokens) {
		final ArrayList<Node> nodes = new ArrayList<Node>();

		while (true) {
			final Token nextToken = tokens.peek();
			if (nextToken.type == TokenType.eof || nextToken.value.equals("}")) {
				break;
			}

			nodes.add(parseNode(tokens));
		}

		return nodes;
	}

	public Node parseNode(Tokenizer tokens) {
		Token name = tokens.next();
		if (name.type != TokenType.string && name.type != TokenType.symbol && name.type != TokenType.number) {
			throw new IllegalStateException("Parser: name token had incorrect token type ");
		}

		var nextToken = tokens.peek();
		String regex = "^[,;}]";
		if (nextToken.type != TokenType.operator || nextToken.value.matches(regex)) {
			while (nextToken.value.matches(regex)) {
				tokens.next();
				nextToken = tokens.peek();
			}

			var nameValue = name.value;
			if (name.type == TokenType.string) {
				nameValue = nameValue.substring(1, nameValue.length() - 2).replaceAll(escape_quote_regex, "\"").replaceAll(escape_backslash_regex, "\\");
			}

			// TODO
			Node node = new Node();
			node.name = nameValue;
			node.nameToken = name;
			node.operator = null;
			node.operatorToken = null;
			node.value = null;
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

		if (parsedValue != null && parsedValue.value instanceof Node) {
			Token peekedToken = tokens.peek();
			if (peekedToken.value.equals("{")) {
				valueAttachment = new SymbolNode(parsedValue);
//				valueAttachmentToken = (Token) parsedValue; // todo?
				parsedValue = parseNodeValue(tokens);
			}
		}

		Token tailComma = tokens.peek();
		while (tailComma.value.matches("^[,;]")) {
			tokens.next();
			tailComma = tokens.peek();
		}

		// todo
		Node node = new Node();
		node.name = name.value;
		node.nameToken = name;
		node.operator = operator.value;
		node.operatorToken = operator;
		node.value = parsedValue;
//		node.valueStartToken = (Token) parsedValue[1];
//		node.valueEndToken = (Token) parsedValue[2];
		node.valueAttachment = valueAttachment;
		node.valueAttachmentToken = valueAttachmentToken;

		return node;
	}

	public NodeValue parseNodeValue(Tokenizer tokens) {
		var nextToken = tokens.next();

		// todo eeeh?
		switch (nextToken.type) {
			case string -> {
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
		}

		throw new IllegalStateException("Parser expected a string, number, symbol, or {");
	}

	public class Node {
		public String name;
		public String operator;

		public NodeValue value;
		public SymbolNode valueAttachment;
		public Token valueAttachmentToken;
		public Token nameToken;
		public Token operatorToken;

		public Node (String name, String operator, NodeValue value, SymbolNode valueAttachment,
		             Token valueAttachmentToken, Token nameToken, Token operatorToken) {
			this.name = name;
			this.operator = operator;
			this.value = value;
			this.valueAttachment = valueAttachment;
			this.valueAttachmentToken = valueAttachmentToken;
			this.nameToken = nameToken;
			this.operatorToken = operatorToken;
		}

		public Node(NodeValue value) {
			this(null, null, value, null, null, null, null);
		}

		public Node() {
			this((NodeValue) null);
		}

		public Node(ArrayList<Node> value) {
			this(new NodeValue(value));
		}

		public String name() {
			return name;
		}
	}

	public class SymbolNode {
		public String name;

		public SymbolNode(NodeValue parsedValue) {
			this.name = parsedValue.value.toString();   // todo?
		}
	}

	/**
	 *  stores string, number, ArrayList<Node>, SymbolNode, or null
	 */
	public class NodeValue {
		public Object value;

		public NodeValue(Object value) {
			this.value = value;
		}

		public NodeValue(ArrayList<Node> value) {
			this.value = value;
		}

		// todo check allowables
	}
}
