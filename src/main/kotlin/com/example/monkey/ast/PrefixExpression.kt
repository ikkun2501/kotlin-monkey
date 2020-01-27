package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * 前置式
 */
data class PrefixExpression(
    val token: Token,
    val operator: String,
    val right: Expression
) : Expression {

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return "($operator${right.inspect()})"
    }
}