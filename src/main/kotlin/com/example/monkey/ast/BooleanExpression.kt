package com.example.monkey.ast

import com.example.monkey.token.FalseToken
import com.example.monkey.token.Token
import com.example.monkey.token.TrueToken

/**
 * 真偽値式
 */
data class BooleanExpression(
    val token: Token
) : Expression {

    val bool: Boolean = when (token) {
        is TrueToken -> true
        is FalseToken -> false
        else -> throw IllegalAccessError("BooleanExpressionに不正なトークンが設定されています。")
    }

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return token.literal
    }
}