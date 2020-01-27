package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * ハッシュリテラル
 */
data class HashLiteral(
    val token: Token,
    val map: Map<Expression, Expression>
) : Expression {

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return buildString {
            append("{")
            map.map { "${it.key.inspect()}:${it.value.inspect()}" }.joinToString(", ")
            append("}")
        }
    }
}