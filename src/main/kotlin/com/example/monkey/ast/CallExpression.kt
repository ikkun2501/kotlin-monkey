package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * 呼び出し式
 */
data class CallExpression(
    val token: Token,
    val function: Expression, // IdentifierまたはFunctionLiteral
    val arguments: List<Expression>
) : Expression {

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return buildString {
            append(function.inspect())
            append("(")
            append(arguments.joinToString(", ") { it.inspect() })
            append(")")
        }
    }
}