package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * 数値リテラル
 */
data class IntegerLiteral(
    val token: Token,
    val value: Int
) : Expression {

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return token.literal
    }
}