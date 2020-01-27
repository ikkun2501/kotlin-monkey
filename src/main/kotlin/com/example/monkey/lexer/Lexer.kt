package com.example.monkey.lexer

import com.example.monkey.token.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.Delegates

/**
 * 字句解析
 */
class Lexer(private val src: String) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    // 読んでいるポジション
    private var readPosition: Int = 0
    // 現在のポジション
    private var position: Int by Delegates.notNull()
    // 読み込んだchar
    private var ch: Char by Delegates.notNull()

    /**
     * 初期化処理
     */
    init {
        readChar()
    }

    /**
     * 文字の読み込み
     */
    private fun readChar() {

        // 読み込む位置がソースの範囲内か
        ch = if (readPosition < src.length) {
            src[readPosition]
        } else {
            // 読み込みが終了したらCharの最小値を返す
            Char.MIN_VALUE
        }

        logger.debug("ch:$ch")

        position = readPosition
        readPosition++
    }

    /**
     * トークンの取得
     */
    fun nextToken(): Token {

        // 空白をスキップする
        skipWhitespace()

        // 文字だったら
        if (isLetter(ch)) {
            // 文字列として読み込む
            return readIdentifier()
        }
        // 文字が数値だったら
        if (isDigit(ch)) {
            // 数字として読み込む
            return readNumber()
        }

        val token = when (ch) {
            ';' -> SemicolonToken
            ':' -> ColonToken
            '(' -> LParenToken
            ')' -> RParenToken
            '{' -> LBraceToken
            '}' -> RBraceToken
            ',' -> CommaToken
            '+' -> PlusToken
            '/' -> SlashToken
            '-' -> MinusToken
            '*' -> AsteriskToken
            '=' ->
                if (peekChar() == '=') {
                    readChar()
                    EqualToken
                } else {
                    AssignToken
                }
            '!' ->
                if (peekChar() == '=') {
                    readChar()
                    NotEqualToken
                } else {
                    BangToken
                }
            '<' -> LessThanToken
            '>' -> GreaterThanToken
            '[' -> LBracketToken
            ']' -> RBracketToken
            '"' -> StringToken(this.readString())
            Char.MIN_VALUE -> EofToken
            else -> IllegalToken(ch.toString())
        }

        // 次の文字を読み込む
        readChar()

        logger.debug("token:$token")

        return token
    }

    /**
     * 数値の読み込み
     */
    private fun readNumber(): Token {

        val startPosition = position

        while (isDigit(ch)) {
            readChar()
        }

        return IntToken(src.substring(startPosition, position))
    }

    /**
     * 数値判定
     */
    private fun isDigit(char: Char): Boolean {
        return char in '0'..'9'
    }

    /**
     * 変数の読み込み
     */
    private fun readIdentifier(): Token {
        val startPosition = position
        while (isLetter(ch)) {
            readChar()
        }
        val literal = src.substring(startPosition, position)
        logger.debug("literal:$literal")
        return lookupIdent(literal)
    }

    /**
     * 文字列の読み込み
     */
    private fun readString(): String {

        val startPosition = this.position + 1

        do {
            this.readChar()
        } while (ch != '"' && ch != Char.MIN_VALUE)

        return this.src.substring(startPosition, this.position)
    }

    /**
     * 文字列判定
     */
    private fun isLetter(ch: Char): Boolean {
        return ch in 'a'..'z' || ch in 'A'..'Z' || ch == '_'
    }

    /**
     * 識別子を返す
     * 予約語または、識別子
     */
    private fun lookupIdent(ident: String): Token {
        return keywords.getOrDefault(ident, IdentToken(ident))
    }

    /**
     * 空白をスキップ
     */
    private fun skipWhitespace() {
        while (whiteSpaces.contains(ch)) {
            readChar()
        }
    }

    /**
     * 次の文字を取得する
     */
    private fun peekChar(): Char {

        return if (readPosition < src.length) {
            src[readPosition]
        } else {
            Char.MIN_VALUE
        }
    }

    companion object {
        /**
         * 予約後とそれに対応するトークン
         */
        val keywords = mapOf(
            "fn" to FunctionToken,
            "let" to LetToken,
            "if" to IfToken,
            "else" to ElseToken,
            "true" to TrueToken,
            "false" to FalseToken,
            "return" to ReturnToken,
            "macro" to MacroToken
        )
        /**
         * 空白として扱うもの
         */
        val whiteSpaces = arrayOf(' ', '\t', '\n', '\r')
    }
}