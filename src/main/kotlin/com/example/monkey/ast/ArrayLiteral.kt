package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * 配列
 */
data class ArrayLiteral(
    val token: Token,
    val elements: List<Expression>
) : Expression {

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return buildString {
            append("[")
            append(elements.joinToString(", ") { it.inspect() })
            append("]")
        }
    }
}