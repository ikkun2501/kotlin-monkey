package com.example.monkey.token

sealed class Token {
    abstract val tokenType: TokenType
    abstract val literal: String
}

data class StringToken(override val literal: String) : Token() {
    override val tokenType = TokenType.STRING
}

data class IdentToken(override val literal: String) : Token() {
    override val tokenType = TokenType.IDENT
}

data class IntToken(override val literal: String) : Token() {
    override val tokenType = TokenType.INT
}

data class IllegalToken(override val literal: String) : Token() {
    override val tokenType = TokenType.ILLEGAL
}

object FunctionToken : Token() {
    override val tokenType = TokenType.FUNCTION
    override val literal = "fn"
}

object LetToken : Token() {
    override val tokenType = TokenType.LET
    override val literal = "let"
}

object ReturnToken : Token() {
    override val tokenType = TokenType.RETURN
    override val literal = "return"
}

object LParenToken : Token() {
    override val tokenType = TokenType.LPAREN
    override val literal = "("
}

object RParenToken : Token() {
    override val tokenType = TokenType.RPAREN
    override val literal = ")"
}

object LBraceToken : Token() {
    override val tokenType = TokenType.LBRACE
    override val literal = "{"
}

object RBraceToken : Token() {
    override val tokenType = TokenType.RBRACE
    override val literal = "}"
}

object CommaToken : Token() {
    override val tokenType = TokenType.COMMA
    override val literal = "."
}

object ColonToken : Token() {
    override val tokenType = TokenType.COLON
    override val literal = ","
}

object SemicolonToken : Token() {
    override val tokenType = TokenType.SEMICOLON
    override val literal = ";"
}

object AssignToken : Token() {
    override val tokenType = TokenType.ASSIGN
    override val literal = "="
}

object EqualToken : Token() {
    override val tokenType = TokenType.EQ
    override val literal = "=="
}

object NotEqualToken : Token() {
    override val tokenType = TokenType.NOT_EQ
    override val literal = "!="
}

object PlusToken : Token() {
    override val tokenType = TokenType.PLUS
    override val literal = "+"
}

object MinusToken : Token() {
    override val tokenType = TokenType.MINUS
    override val literal = "-"
}

object SlashToken : Token() {
    override val tokenType = TokenType.SLASH
    override val literal = "/"
}

object AsteriskToken : Token() {
    override val tokenType = TokenType.ASTERISK
    override val literal = "*"
}

object EofToken : Token() {
    override val tokenType = TokenType.EOF
    override val literal = Char.MIN_VALUE.toString()
}

object LessThanToken : Token() {
    override val tokenType = TokenType.LT
    override val literal = "<"
}

object GreaterThanToken : Token() {
    override val tokenType = TokenType.GT
    override val literal = ">"
}

object BangToken : Token() {
    override val tokenType = TokenType.BANG
    override val literal = "!"
}

object LBracketToken : Token() {
    override val tokenType = TokenType.LBRACKET
    override val literal = "["
}

object RBracketToken : Token() {
    override val tokenType = TokenType.RBRACKET
    override val literal = "]"
}

object IfToken : Token() {
    override val tokenType = TokenType.IF
    override val literal = "if"
}

object ElseToken : Token() {
    override val tokenType = TokenType.ELSE
    override val literal = "else"
}

object TrueToken : Token() {
    override val tokenType = TokenType.TRUE
    override val literal = "true"
}

object FalseToken : Token() {
    override val tokenType = TokenType.FALSE
    override val literal = "false"
}

object MacroToken : Token() {
    override val tokenType = TokenType.MACRO
    override val literal = "macro"
}