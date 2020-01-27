package com.example.monkey.ast

import com.example.monkey.token.ReturnToken

/**
 * Return文
 */
data class ReturnStatement(
    val returnValue: Expression? = null
) : Statement {

    val token = ReturnToken

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun inspect(): String {
        return "${tokenLiteral()} ${returnValue?.inspect()}"
    }
}