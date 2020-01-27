package com.example.monkey.token

/**
 * トークンクラス
 */
enum class TokenType {

    ILLEGAL,
    EOF,

    COMMA,
    SEMICOLON,
    COLON,

    ASSIGN,
    PLUS,
    MINUS,
    SLASH,
    ASTERISK,
    LT,
    GT,
    BANG,
    EQ,
    NOT_EQ,

    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,

    LBRACKET,
    RBRACKET,

    FUNCTION,
    LET,
    IF,
    ELSE,
    TRUE,
    FALSE,
    RETURN,

    MACRO,

    IDENT,
    INT,
    STRING,
    ;

    override fun toString(): String {
        return this.javaClass.simpleName
    }
}
