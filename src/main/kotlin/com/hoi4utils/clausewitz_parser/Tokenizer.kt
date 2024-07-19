package main.kotlin.com.hoi4utils.clausewitz_parser

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * The Tokenizer class is used to tokenize a string of text into tokens.
 *
 * A Tokenizer is an iterator that produces a sequence of tokens from a string
 * of text. It uses regular expressions to match the tokens in the text and
 * provides methods for iterating over the tokens.
 *
 * Here's an example of how to use a Tokenizer to tokenize a string of text:
 *
 * <pre>
 * `Tokenizer tokenizer = new Tokenizer("Hello, world!");
 * Token token;
 * while ((token = tokenizer.next()) != null) {
 * System.out.println(token);
 * }
` *
</pre> *
 */
class Tokenizer(@field:Suppress("unused") private val input: String) {
    private val pattern: Pattern
    private val matcher: Matcher
    private var findStartIndex = 0
    private var peekOccurred = false

    init {
        pattern = createPattern()
        matcher = pattern.matcher(input)
    }

    /**
     * Returns the next token from the tokenizer, or null if there are no more
     * tokens.
     *
     * @return the next token from the tokenizer, or null if there are no more
     * tokens
     */
    operator fun next(): Token? {
        return try {
            /*
			 * matcher.find(int) resets the matcher, therefore,
			 * only use this call when necessary
			 */
            if (peekOccurred) {
                if (matcher.find(findStartIndex)) {
                    val value = matcher.group(0)
                    val start = matcher.start()
                    peekOccurred = false
                    return Token(value, start)
                }
            } else if (matcher.find()) {
                val value = matcher.group(0)
                val start = matcher.start()
                /* peek occurred already false */return Token(value, start)
            }
            null
        } catch (e: StackOverflowError) {
            System.err.println("EEEE") // todo
            System.err.println(matcher.group())
            null
        }
    }

    /**
     * `Matcher` has no peek() function therefore this logic is
     * necessary.
     * returns the next token from matcher, the next `find()` call to
     * `matcher` however will be from the same start index.
     *
     * @see Matcher
     *
     * @return Next token from matcher
     */
    fun peek(): Token? {
        if (peekOccurred) {
            if (matcher.find(findStartIndex)) {
                val value = matcher.group()
                val start = matcher.start()
                /* find_start_index remains the same */
                /* peek occurred already true */return Token(value, start)
            }
        } else if (matcher.find()) {
            val value = matcher.group()
            val start = matcher.start()
            findStartIndex = matcher.start()
            peekOccurred = true
            return Token(value, start)
        }
        return null
    }

    /**
     * Creates a pattern that matches the tokens in the text.
     *
     * @return a pattern that matches the tokens in the text
     */
    private fun createPattern(): Pattern {
        val patternBuilder = StringBuilder()
        for (tokenType in TokenType.entries) {
            val tokenRegex: Pattern? = Token.tokenRegex[tokenType]
            if (tokenRegex != null) {
                patternBuilder.append(String.format("|(?<%s>%s)", tokenType, tokenRegex.pattern()))
            }
        }
        return if (patternBuilder.isNotEmpty()) {
            Pattern.compile(patternBuilder.substring(1)) // Skip the leading "|"
        } else {
            throw IllegalStateException("No patterns found for token types.")
        }
    }
}
