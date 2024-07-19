package main.kotlin.com.hoi4utils.clausewitz_parser

import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

/**
 * Adapted partially from [hoiparser.ts](https://github.com/herbix/hoi4modutilities/blob/master/src/hoiformat/hoiparser.ts)
 * from repo [herbix/hoi4modutilities](https://github.com/herbix/hoi4modutilities)
 *
 */
class Token {
    var value: String
    var type: TokenType
    var start: Int

    constructor(value: String, start: Int, type: TokenType) {
        this.value = value
        this.start = start
        this.type = type
    }

    constructor(value: String, start: Int) {
        this.value = value
        this.start = start
        type = determineTokenType(value)
    }

    /**
     * Determine token type based on token regex map
     */
    private fun determineTokenType(value: String): TokenType {
        for ((key, value1) in tokenRegex) {
            if (value1.matcher(value).matches()) {
                return key
            }
        }
        return TokenType.unknown
    }

    fun length(): Int {
        return value.length
    }

    override fun toString(): String {
        return value
    }

    companion object {
        val tokenRegex: MutableMap<TokenType, Pattern> = EnumMap(TokenType::class.java)
        const val EOF_INDICATOR = "$"

        init {
            tokenRegex[TokenType.comment] =
                Pattern.compile("#.*") // prev: "#.*(?:[\r\n]|$)"
            tokenRegex[TokenType.symbol] =
                Pattern.compile("(?:\\d+\\.)?[a-zA-Z_@\\[\\]][\\w:.@\\[\\]\\-?^/\\u00A0-\\u024F]*")
            tokenRegex[TokenType.operator] =
                Pattern.compile("[={}<>;,]|>=|<=|!=")
            tokenRegex[TokenType.string] =
                Pattern.compile("\"(\\\\.|[^\"])*\"")
            tokenRegex[TokenType.number] =
                Pattern.compile("-?\\d*\\.\\d+|-?\\d+|0x\\d+")
            tokenRegex[TokenType.eof] =
                Pattern.compile(EOF_INDICATOR)
        }
    }
}
