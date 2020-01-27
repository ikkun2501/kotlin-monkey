package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * 中置式
 */
data class InfixExpression(
    val token: Token,
    val left: Expression,
    val operator: String,
    val right: Expression
) : Expression {

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return "(${left.inspect()} $operator ${right.inspect()})"
    }
}