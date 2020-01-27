package com.example.monkey.ast

/**
 * プログラム
 */
data class Program(
    val statements: MutableList<Statement>
) : Node {

    override fun inspect(): String {
        return statements.joinToString(separator = "") { it.inspect() }
    }

    override fun tokenLiteral(): String {
        if (statements.isEmpty()) {
            return ""
        }
        return statements.first().tokenLiteral()
    }
}