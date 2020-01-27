package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * ブロック構文
 */
data class BlockStatement(
    val token: Token,
    val statements: List<Statement>
) : Statement {

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return statements.joinToString { it.inspect() }
    }
}