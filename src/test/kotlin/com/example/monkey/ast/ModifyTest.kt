package com.example.monkey.ast

import com.example.monkey.token.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ModifyTest {

    @Test
    fun testModify() {
        val one = fun(): Expression {
            return IntegerLiteral(IntToken("1"), 1)
        }

        val two = fun(): Expression {
            return IntegerLiteral(IntToken("2"), 2)
        }

        val turnOneIntoTwo = fun(node: Node): Node {
            val integer = node as? IntegerLiteral ?: return node
            if (integer.value != 1) {
                return node
            }

            return IntegerLiteral(IntToken("2"), 2)
        }

        val tests = setOf(
            one() to two(),
            Program(
                statements = arrayListOf(
                    ExpressionStatement(
                        FunctionToken,
                        expression = one()
                    )
                )
            ) to
                    Program(
                        statements = arrayListOf(
                            ExpressionStatement(
                                FunctionToken,
                                expression = two()
                            )
                        )
                    ),

            InfixExpression(token = FunctionToken, left = one(), operator = "+", right = two()) to
                    InfixExpression(token = FunctionToken, left = two(), operator = "+", right = two()),

            InfixExpression(token = FunctionToken, left = two(), operator = "+", right = one()) to
                    InfixExpression(token = FunctionToken, left = two(), operator = "+", right = two()),

            PrefixExpression(operator = "-", right = one(), token = FunctionToken) to
                    PrefixExpression(operator = "-", right = two(), token = FunctionToken),

            IndexExpression(left = one(), index = one(), token = IdentToken("a")) to
                    IndexExpression(left = two(), index = two(), token = IdentToken("a")),

            IfExpression(
                token = IfToken,
                condition = one(),
                consequence = BlockStatement(
                    token = LBraceToken,
                    statements = listOf(ExpressionStatement(token = FunctionToken, expression = one()))
                ),
                alternative = BlockStatement(
                    token = LBraceToken,
                    statements = listOf(ExpressionStatement(token = FunctionToken, expression = one()))
                )
            ) to IfExpression(
                token = IfToken,
                condition = two(),
                consequence = BlockStatement(
                    token = LBraceToken,
                    statements = listOf(ExpressionStatement(token = FunctionToken, expression = two()))
                ),
                alternative = BlockStatement(
                    token = LBraceToken,
                    statements = listOf(ExpressionStatement(token = FunctionToken, expression = two()))
                )
            ),
            ReturnStatement(returnValue = one()) to
                    ReturnStatement(returnValue = two()),
            LetStatement(
                identifier = Identifier(IdentToken("ident")),
                value = one()
            ) to LetStatement(
                identifier = Identifier(IdentToken("ident")),
                value = two()
            ),
            FunctionLiteral(
                parameters = emptyList(),
                token = FunctionToken,
                body = BlockStatement(
                    token = LBraceToken,
                    statements = listOf(ExpressionStatement(token = FunctionToken, expression = one()))
                )
            ) to FunctionLiteral(
                parameters = emptyList(),
                token = FunctionToken,
                body = BlockStatement(
                    token = LBraceToken,
                    statements = listOf(ExpressionStatement(token = FunctionToken, expression = two()))
                )
            ),
            ArrayLiteral(token = IdentToken("array"), elements = listOf(one(), one()))
                    to ArrayLiteral(token = IdentToken("array"), elements = listOf(two(), two()))
        )

        tests.forEach { (input, expected) ->
            val modified = modify(input, turnOneIntoTwo)
            assertEquals(expected, modified)
        }

        val hashLiteral = HashLiteral(
            token = IdentToken("hash"),
            map = mapOf(one() to one(), one() to one())
        )

        val actualHashLiteral = modify(hashLiteral, turnOneIntoTwo) as HashLiteral
        assertEquals(actualHashLiteral.map, mapOf(two() to two(), two() to two()))
    }
}