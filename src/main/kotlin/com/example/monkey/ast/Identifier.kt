package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * 識別子
 */
data class Identifier(
    private val token: Token
) : Expression {

    override fun inspect(): String = token.literal

    override fun tokenLiteral(): String {
        return token.literal
    }
}