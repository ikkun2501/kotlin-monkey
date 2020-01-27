package com.example.monkey.ast

import com.example.monkey.token.IdentToken
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ProgramTest {
    @Test
    fun testString() {
        val program = Program(
            statements = arrayListOf(
                LetStatement(
                    identifier = Identifier(
                        IdentToken("myVar")
                    ),
                    value = Identifier(
                        token = IdentToken("anotherVar")
                    )
                )
            )
        )

        Assertions.assertEquals("let myVar = anotherVar;", program.inspect())
    }
}