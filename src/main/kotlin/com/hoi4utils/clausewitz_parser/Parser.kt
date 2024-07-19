package main.kotlin.com.hoi4utils.clausewitz_parser

import main.kotlin.com.hoi4utils.clausewitz_parser.Node.valueObject
import java.io.File
import java.io.IOException
import java.nio.file.Files

/*
* Parser File new
*/
class Parser {
    private val tokens: Tokenizer
    private var rootNode: Node? = null

    constructor(input: String?) {
        /* EOF */
        var input = input
        input += Token.EOF_INDICATOR
        tokens = Tokenizer(input)
    }

    constructor(f: File) {
        /* get input from file */
        var input: String?
        input = try {
            Files.readString(f.toPath())
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        /* EOF */input += Token.EOF_INDICATOR
        tokens = Tokenizer(input)
    }

    @Throws(ParserException::class)
    fun parse(): Node {
        val value = parseBlockContent(tokens)

        /*
		 * need to reach up to eof indicator
		 * if last token is '}' this could indicate there was a
		 * missing '{' in the code
		 */if (tokens.peek()!!.type != TokenType.eof) {
            throw ParserException(
                "Input not completely parsed by clausewitz-file parser \n" +
                        "\t\tlast token: " + tokens.peek()!!.value
            )
        }
        rootNode = Node(value)
        return rootNode!!
    }

    @Throws(ParserException::class)
    fun parseBlockContent(tokens: Tokenizer): ArrayList<Node?> {
        val nodes = ArrayList<Node?>()
        while (true) {
            val nextToken = tokens.peek()
            if (nextToken!!.type == TokenType.eof || nextToken.value == "}") {
                break
            }
            try {
                val n = parseNode(tokens)
                // check if n is a comment node
                if (n.nameToken!!.type != TokenType.comment) {
                    nodes.add(n)
                }
            } catch (e: ParserException) {
                throw e
            }
        }
        return nodes
    }

    @Throws(ParserException::class)
    fun parseNode(tokens: Tokenizer): Node {
        val name = tokens.next()

        /* skip comments */if (name!!.type == TokenType.comment) {
            val node = Node()
            node.identifier = name.value
            node.nameToken = name
            return node
        }

        // System.out.println(name.value);
        if (name.type != TokenType.string && name.type != TokenType.symbol && name.type != TokenType.number) {
            throw ParserException(
                "Parser: incorrect token type " + name.type + ", token: " + name + " at index: " + name.start
            )
        }
        var nextToken = tokens.peek()
        if (nextToken!!.type != TokenType.operator || nextToken.value.matches("^[,;}]$".toRegex())) {
            while (nextToken!!.value.matches("^[,;]$".toRegex())) {
                tokens.next()
                nextToken = tokens.peek()
            }

            /* handle escaped characters */
            val nameValue = unescapeCharacters(name, name.value)

            // TODO node value?
            val node = Node()
            node.identifier = nameValue
            node.nameToken = name
            node.operator = null
            node.operatorToken = null
            /* node.value = null; */return node
        }

        /* "= {" */
        val operator: Token?
        if (nextToken.value == "{") {
            operator = Token(
                "=",
                nextToken.start,
                TokenType.operator
            ) // Create a new Token with the '=' value
            nextToken = Token("=", operator.start, TokenType.operator)
            nextToken.start = operator.start
            operator.value = "="
        } else {
            operator = tokens.next()
        }
        var parsedValue: NodeValue? = parseNodeValue(tokens)

        /* Handle value attachment (e.g., when there's a nested block) */if (parsedValue != null && parsedValue.valueObject() is Node) {
            val peekedToken = tokens.peek()
            if (peekedToken!!.value == "{") {
                parsedValue = parseNodeValue(tokens)
            }
        }

        // Skip comments before tailComma
        var tailComma = tokens.peek()
        while (tailComma!!.type == TokenType.comment || tailComma.value.matches("^[,;]".toRegex())) {
            tokens.next()
            tailComma = tokens.peek()
        }

        // todo
        val node: Node = Node(parsedValue)
        node.identifier = name.value
        node.nameToken = name
        node.operator = operator!!.value
        node.operatorToken = operator
        // node.value = parsedValue;
        // node.valueStartToken = (Token) parsedValue[1];
        // node.valueEndToken = (Token) parsedValue[2];
        return node
    }

    private fun unescapeCharacters(name: Token?, nameValue: String?): String? {
        var nameValue = nameValue
        if (name!!.type == TokenType.string) {
            nameValue = nameValue!!.substring(1, nameValue.length - 2)
                .replace(escape_quote_regex.toRegex(), "\"")
                .replace(escape_backslash_regex.toRegex(), "\\")
        }
        return nameValue
    }

    @Throws(ParserException::class)
    fun parseNodeValue(tokens: Tokenizer): NodeValue? {
        val nextToken = tokens.next() ?: return null
        // todo maybe?
        return when (nextToken.type) {
            TokenType.string -> {
                /* substring from 1 to length() - 2: don't replace "" */
                if (nextToken.value.length == 2) {
                    NodeValue(nextToken.value)
                } else NodeValue(
                    nextToken.value.substring(1, nextToken.length() - 2)
                        .replace(
                            escape_quote_regex.toRegex(),
                            "\""
                        )
                        .replace(
                            escape_backslash_regex.toRegex(),
                            "\\"
                        )
                )
            }

            TokenType.number -> {
                /* handles hexadecimal or floating-point/integers */
                NodeValue(
                    if (nextToken.value.startsWith("0x")) nextToken.value.substring(2)
                        .toInt(16) else nextToken.value.toDouble()
                )
            }

            TokenType.symbol -> {
                NodeValue(nextToken.value)
            }

            TokenType.operator -> {
                if (nextToken.value == "{") {
                    val result = parseBlockContent(tokens)
                    val right = tokens.next()
                    if (right!!.value != "}") {
                        throw ParserException("Parser expected a matching \"}\"")
                    }
                    return NodeValue(result)
                }
                break // necessary if addtl. case added, so will keep.
            }

            else -> throw ParserException("Unexpected value: " + nextToken.type)
        }
        throw ParserException("Parser expected a string, number, symbol, or {")
    }

    fun rootNode(): Node? {
        return rootNode
    }

    companion object {
        const val escape_backslash_regex = "\\\\"
        const val escape_quote_regex = "\\\\\""
    }
}
