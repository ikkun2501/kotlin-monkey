package com.example.monkey.parser

import com.example.monkey.token.TokenType

/**
 * 優先度
 */
enum class Precedence {
    LOWEST,
    EQUALS,
    LESS_GREATER,
    SUM,
    PRODUCT,
    PREFIX,
    CALL,
    INDEX,
    ;

    companion object {
        fun from(tokenType: TokenType): Precedence {
            return when (tokenType) {
                TokenType.EQ, TokenType.NOT_EQ -> EQUALS
                TokenType.LT, TokenType.GT -> LESS_GREATER
                TokenType.PLUS, TokenType.MINUS -> SUM
                TokenType.SLASH, TokenType.ASTERISK -> PRODUCT
                TokenType.LPAREN -> CALL
                TokenType.LBRACKET -> INDEX
                else -> LOWEST
            }
        }
    }
}