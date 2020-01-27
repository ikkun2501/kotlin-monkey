package com.example.monkey.ast

import com.example.monkey.token.LetToken

/**
 * Let文
 * let identifier = expression
 */
data class LetStatement(
    val identifier: Identifier,
    val value: Expression
) : Statement {

    val token = LetToken
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return "${tokenLiteral()} ${identifier.tokenLiteral()} = ${value.inspect()};"
    }
}