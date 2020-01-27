package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * IF式
 */
data class IfExpression(
    val token: Token,
    val condition: Expression,
    val consequence: BlockStatement,
    val alternative: BlockStatement? = null
) : Expression {

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return buildString {
            append("if${condition.inspect()} ${consequence.inspect()}")
            if (alternative != null) {
                append("else ${alternative.inspect()}")
            }
        }
    }
}