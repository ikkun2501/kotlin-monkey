package com.example.monkey.parser

import com.example.monkey.ast.*
import com.example.monkey.lexer.Lexer
import com.example.monkey.token.Token
import com.example.monkey.token.TokenType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Parser(private val lexer: Lexer) {

    /**
     * エラー
     */
    val errors: ArrayList<String> = arrayListOf()
    /**
     * 現在のトークン
     */
    private var curToken: Token
    /**
     * 次のトークン
     */
    private var peekToken: Token

    /**
     * 初期化処理
     */
    init {
        curToken = lexer.nextToken()
        peekToken = lexer.nextToken()
    }

    /**
     * 次のトークンを読み込む
     */
    private fun nextToken() {
        curToken = peekToken
        peekToken = lexer.nextToken()
    }

    /**
     * プログラムをパースする
     */
    fun parseProgram(): Program {

        // TokenがEOFになるまで読み込む
        val statements = mutableListOf<Statement>()
        while (!curTokenIs(TokenType.EOF)) {
            // 文を読み込む
            statements += parseStatement()
            nextToken()
        }

        logger.debug("構文解析結果:")
        statements.forEach {
            logger.debug(it.inspect())
        }

        return Program(statements)
    }

    /**
     * 文の解析
     */
    private fun parseStatement(): Statement {
        return when (curToken.tokenType) {
            TokenType.LET -> parseLetStatement()
            TokenType.RETURN -> parseReturnStatement()
            else -> parseExpressionStatement()
        }
    }

    /**
     * Return文の解析
     */
    private fun parseReturnStatement(): Statement {

        nextToken()

        val returnStatement = ReturnStatement(parseExpression(Precedence.LOWEST))

        while (!curTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }
        return returnStatement
    }

    /**
     * Let文の解析
     * 以下の形式になっているはず
     * let identifier = expression;
     */
    private fun parseLetStatement(): LetStatement {

        expectPeek(TokenType.IDENT)

        val identifier = curToken

        expectPeek(TokenType.ASSIGN)

        nextToken()

        val letStatement = LetStatement(
            identifier = Identifier(token = identifier),
            value = parseExpression(Precedence.LOWEST)
        )

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }

        return letStatement
    }

    /**
     * カレントトークの型を判定
     */
    private fun curTokenIs(tokenType: TokenType): Boolean {
        return curToken.tokenType == tokenType
    }

    /**
     * Peekトークンの型を判定
     */
    private fun peekTokenIs(tokenType: TokenType): Boolean {
        return peekToken.tokenType == tokenType
    }

    /**
     * 次のトークンの型が予期するものか判定しつつ、次のトークンを取得する
     */
    private fun expectPeek(tokenType: TokenType) {
        if (!peekTokenIs(tokenType)) {
            throw BadGrammarException(peekToken.tokenType, tokenType)
        }
        nextToken()
    }

    /**
     * 式を解析
     */
    private fun parseExpressionStatement(): ExpressionStatement {

        val stmt = ExpressionStatement(curToken, parseExpression(Precedence.LOWEST))

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }

        return stmt
    }

    @Throws(NotImplementedError::class)
    private fun parseExpression(precedence: Precedence): Expression {

        // 左辺のparser
        var leftExpression = when (curToken.tokenType) {
            TokenType.IDENT -> Identifier(token = curToken)
            TokenType.INT -> IntegerLiteral(token = curToken, value = curToken.literal.toInt())
            TokenType.BANG, TokenType.MINUS -> parsePrefixExpression()
            TokenType.TRUE, TokenType.FALSE -> parseBoolean()
            TokenType.LPAREN -> parseGroupedExpression()
            TokenType.IF -> parseIfExpression()
            TokenType.FUNCTION -> parseFunctionLiteral()
            TokenType.STRING -> StringLiteral(token = curToken, value = curToken.literal)
            TokenType.LBRACKET -> parseArrayLiteral()
            TokenType.LBRACE -> parseHashLiteral()
            TokenType.MACRO -> parseMacroLiteral()
            else -> {
                throw NotImplementedError("invalid token :${curToken.literal} ${curToken::class}")
            }
        }
        logger.debug("leftExpression:${leftExpression.inspect()}")

        // 右辺があり、次の優先順位が高ければparse
        while (!peekTokenIs(TokenType.SEMICOLON) && precedence < peekPrecedence()) {
            leftExpression = when (peekToken.tokenType) {
                TokenType.PLUS, TokenType.MINUS, TokenType.SLASH, TokenType.ASTERISK, TokenType.EQ, TokenType.NOT_EQ, TokenType.LT, TokenType.GT
                -> {
                    nextToken()
                    val infixExpression = parseInfixExpression(leftExpression)
                    logger.debug("infixExpression:${infixExpression.inspect()}")
                    infixExpression
                }
                TokenType.LBRACKET -> {
                    nextToken()
                    parseIndexExpression(leftExpression)
                }
                TokenType.LPAREN -> {
                    nextToken()
                    parseCallExpression(leftExpression)
                }
                else -> leftExpression
            }
        }

        return leftExpression
    }

    private fun parseMacroLiteral(): Expression {
        val token = curToken
        expectPeek(TokenType.LPAREN)

        val parameters = parseFunctionParameters()

        expectPeek(TokenType.LBRACE)

        val body = parseBlockStatement()

        return MacroLiteral(
            token = token,
            parameters = parameters,
            body = body
        )
    }

    private fun parseHashLiteral(): Expression {
        val token = curToken
        val map = mutableMapOf<Expression, Expression>()

        while (!peekTokenIs(TokenType.RBRACE)) {
            nextToken()
            val key = parseExpression(Precedence.LOWEST)

            expectPeek(TokenType.COLON)

            nextToken()
            val value = parseExpression(Precedence.LOWEST)

            map[key] = value

            if (!(peekTokenIs(TokenType.RBRACE) || peekTokenIs(TokenType.COMMA))) {
                throw BadGrammarException(peekToken.tokenType, TokenType.RBRACE, TokenType.COMMA)
            }

            if (peekTokenIs(TokenType.COMMA)) {
                nextToken()
            }
        }

        expectPeek(TokenType.RBRACE)

        return HashLiteral(token = token, map = map)
    }

    private fun parseIndexExpression(leftExpression: Expression): Expression {

        val token = curToken

        nextToken()

        val indexExpression = parseExpression(Precedence.LOWEST)

        expectPeek(TokenType.RBRACKET)

        return IndexExpression(token = token, left = leftExpression, index = indexExpression)
    }

    private fun parseArrayLiteral(): Expression {
        val token = curToken

        val elements = parseExpressionList()

        return ArrayLiteral(token = token, elements = elements)
    }

    /**
     * 式のリストを解析
     * [expression,expression]
     */
    private fun parseExpressionList(): List<Expression> {

        if (peekTokenIs(TokenType.RBRACKET)) {
            nextToken()
            return emptyList()
        }

        val list = mutableListOf<Expression>()
        nextToken()
        list.add(parseExpression(Precedence.LOWEST))

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken()
            nextToken()
            list.add(parseExpression(Precedence.LOWEST))
        }

        expectPeek(TokenType.RBRACKET)

        return list
    }

    /**
     * 真偽値の構文解析
     */
    private fun parseBoolean(): Expression {
        return BooleanExpression(curToken)
    }

    /**
     * 前置式の構文解析
     */
    private fun parsePrefixExpression(): Expression {
        val token = curToken
        val operator = curToken.literal
        nextToken()
        val right = parseExpression(Precedence.PREFIX)
        return PrefixExpression(token = token, operator = operator, right = right)
    }

    /**
     * 中置式の構文解析
     */
    private fun parseInfixExpression(left: Expression): Expression {
        val precedence = curPrecedence()
        val token = curToken
        nextToken()
        return InfixExpression(
            token = token,
            operator = token.literal,
            left = left,
            right = parseExpression(precedence)
        )
    }

    /**
     *  (..)の構文解析
     */
    private fun parseGroupedExpression(): Expression {

        nextToken()

        val exp = parseExpression(Precedence.LOWEST)

        expectPeek(TokenType.RPAREN)

        return exp
    }

    /**
     * ブロック構文の構文解析
     */
    private fun parseBlockStatement(): BlockStatement {

        val token = curToken

        nextToken()

        val statements = mutableListOf<Statement>()
        while (!curTokenIs(TokenType.RBRACE) && !curTokenIs(TokenType.EOF)) {
            statements += parseStatement()
            nextToken()
        }

        return BlockStatement(token = token, statements = statements)
    }

    /**
     * If式の構文解析
     */
    private fun parseIfExpression(): Expression {

        val token = curToken

        expectPeek(TokenType.LPAREN)

        nextToken()

        val condition = parseExpression(Precedence.LOWEST)

        expectPeek(TokenType.RPAREN)

        expectPeek(TokenType.LBRACE)

        val consequence = parseBlockStatement()

        val alternative = if (peekTokenIs(TokenType.ELSE)) {

            nextToken()

            expectPeek(TokenType.LBRACE)

            parseBlockStatement()
        } else {
            null
        }

        return IfExpression(
            token = token,
            condition = condition,
            consequence = consequence,
            alternative = alternative
        )
    }

    /**
     * 関数リテラルの構文解析
     */
    private fun parseFunctionLiteral(): Expression {

        val token = curToken

        expectPeek(TokenType.LPAREN)

        val parameters: List<Identifier> = parseFunctionParameters()

        expectPeek(TokenType.LBRACE)

        val body = parseBlockStatement()

        curTokenIs(TokenType.RBRACE)

        return FunctionLiteral(
            token = token,
            parameters = parameters,
            body = body
        )
    }

    /**
     * 関数のパラメータを構文解析
     */
    private fun parseFunctionParameters(): List<Identifier> {

        if (peekTokenIs(TokenType.RPAREN)) {
            nextToken()
            return emptyList()
        }

        nextToken()

        val identifiers = mutableListOf<Identifier>()
        // 先頭の引数
        identifiers.add(Identifier(token = curToken))

        while (peekTokenIs(TokenType.COMMA)) {
            // カンマをスキップ
            nextToken()
            // 次の引数
            nextToken()

            identifiers.add(Identifier(token = curToken))
        }

        expectPeek(TokenType.RPAREN)

        return identifiers
    }

    /**
     * 関数呼び出しを構文解析
     */
    private fun parseCallExpression(function: Expression): Expression {
        val token = curToken
        val arguments = parseCallArguments()
        return CallExpression(token = token, function = function, arguments = arguments)
    }

    /**
     * 関数呼び出しの引数を構文解析
     */
    private fun parseCallArguments(): List<Expression> {
        val args = mutableListOf<Expression>()

        if (peekTokenIs(TokenType.RPAREN)) {
            nextToken()
            return emptyList()
        }

        nextToken()
        args.add(requireNotNull(parseExpression(Precedence.LOWEST)))

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken()
            nextToken()
            args.add(requireNotNull(parseExpression(Precedence.LOWEST)))
        }

        expectPeek(TokenType.RPAREN)

        return args
    }

    private fun peekPrecedence(): Precedence = Precedence.from(peekToken.tokenType)

    private fun curPrecedence(): Precedence = Precedence.from(curToken.tokenType)

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}