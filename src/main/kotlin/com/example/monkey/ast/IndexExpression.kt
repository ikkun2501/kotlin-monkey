package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 *　添字式
 */
data class IndexExpression(
    val token: Token,
    val left: Expression,
    val index: Expression
) : Expression {

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return buildString {
            append("(")
            append(left.inspect())
            append("[")
            append(index.inspect())
            append("]")
            append(")")
        }
    }
}