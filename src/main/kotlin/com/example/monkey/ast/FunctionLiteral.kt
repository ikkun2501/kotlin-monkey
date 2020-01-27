package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * 関数
 */
data class FunctionLiteral(
    val token: Token,
    val parameters: List<Identifier>,
    val body: BlockStatement
) : Expression {

    override fun tokenLiteral(): String {
        return this.token.literal
    }

    override fun inspect(): String {
        return buildString {
            append(this@FunctionLiteral.tokenLiteral())
            append("(")
            append(parameters.joinToString(",") { it.toString() })
            append(")")
            append(this@FunctionLiteral.body.inspect())
        }
    }
}