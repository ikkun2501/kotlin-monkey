package com.example.monkey.ast

import com.example.monkey.token.Token

/**
 * 文字列
 */
data class StringLiteral(val token: Token, val value: String) : Expression {

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return token.literal
    }
}