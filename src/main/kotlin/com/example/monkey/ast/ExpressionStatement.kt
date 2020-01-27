package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * 式文
 */
data class ExpressionStatement(
    val token: Token,
    val expression: Expression
) : Statement {

    override fun inspect(): String {
        return expression.inspect()
    }

    override fun tokenLiteral(): String {
        return token.literal
    }
}