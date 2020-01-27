package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * マクロ
 * macro(parameters){
 *   statements
 * }
 */
data class MacroLiteral(
    val token: Token,
    val parameters: List<Identifier>,
    val body: BlockStatement
) : Expression {

    override fun tokenLiteral(): String {
        return this.token.literal
    }

    override fun inspect(): String {
        return buildString {
            append(this@MacroLiteral.tokenLiteral())
            append("(")
            append(parameters.joinToString(",") { it.toString() })
            append(")")
            append(this@MacroLiteral.body.inspect())
        }
    }
}