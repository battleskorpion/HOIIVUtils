package main.kotlin.com.hoi4utils.clausewitz_parser

/**
 * clausewitz token types
 */
enum class TokenType {
    comment,
    symbol,
    operator,
    string,
    number,
    eof,
    unknown
}
