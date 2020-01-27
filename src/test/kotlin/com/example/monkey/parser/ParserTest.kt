package com.example.monkey.parser

import com.example.monkey.ast.*
import com.example.monkey.lexer.Lexer
import com.example.monkey.token.FunctionToken
import com.example.monkey.token.IdentToken
import com.example.monkey.token.IntToken
import com.example.monkey.token.LBraceToken
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ParserTest {

    @Test
    fun testLetStatement() {
        val input = """
            let x = 5;
            let y = 10;
            let foobar = 838383;
        """.trimIndent()

        val parser = Parser(Lexer(input))

        val program = parser.parseProgram()
        checkParserErrors(parser)

        assertEquals(3, program.statements.size)

        val expectedIdentifiers = arrayOf("x", "y", "foobar")

        for ((index, expectedIdentifier) in expectedIdentifiers.withIndex()) {
            val statement = program.statements[index]
            assertEquals("let", statement.tokenLiteral())
            when (statement) {
                is LetStatement -> {
                    assertEquals(expectedIdentifier, statement.identifier.inspect())
                }
                else -> throw IllegalArgumentException()
            }
        }
    }

    @Test
    fun testLetStatementHasError() {
        val input = """
            let = aiueo;
        """.trimIndent()

        val parser = Parser(Lexer(input))

        assertThrows(BadGrammarException::class.java) {
            parser.parseProgram()
        }
    }

    @Test
    fun testReturnStatements() {
        val input = """
            return 5;
            return 10;
            return 993322;
        """.trimIndent()

        val parser = Parser(Lexer(input))
        val program = parser.parseProgram()
        checkParserErrors(parser)

        assertEquals(3, program.statements.size)

        for (statement in program.statements) {
            assertEquals("return", statement.tokenLiteral())
        }
    }

    private fun checkParserErrors(parser: Parser) {
        val errors = parser.errors

        if (errors.isEmpty()) {
            return
        }

        println("parser has ${errors.size} errors")

        for (error in errors) {
            println("parser error: $error")
        }
    }

    @Test
    fun testIdentifierExpression() {
        val input = "foobar;"

        val parser = Parser(Lexer(input))
        val program = parser.parseProgram()
        checkParserErrors(parser)

        assertEquals(1, program.statements.size)

        val stmt = program.statements.first() as ExpressionStatement
        val ident = stmt.expression as Identifier

        assertEquals("foobar", ident.inspect())
        assertEquals("foobar", ident.tokenLiteral())
    }

    @Test
    fun testIntegerLiteralExpression() {
        val input = "5;"

        val parser = Parser(Lexer(input))
        val program = parser.parseProgram()
        checkParserErrors(parser)

        assertEquals(1, program.statements.size)
        val stmt = program.statements.first() as ExpressionStatement
        val literal = stmt.expression as IntegerLiteral

        assertEquals(5, literal.value)
        assertEquals("5", literal.tokenLiteral())
    }

    @Test
    fun testParsingPrefixExpressions() {
        val expectedArray = arrayOf(
            Triple("!5;", "!", "5"),
            Triple("-15;", "-", "15")
        )

        for ((input, operator, right) in expectedArray) {

            val parser = Parser(Lexer(input))
            val program = parser.parseProgram()
            checkParserErrors(parser)

            assertEquals(1, program.statements.size)
            val stmt = program.statements.first() as ExpressionStatement

            val exp = stmt.expression as PrefixExpression

            assertEquals(operator, exp.operator)
            assertEquals(right, exp.right.inspect())
        }
    }

    @Test
    fun testParsingInfixExpression() {

        data class InfixTest(val input: String, val leftValue: Any, val operator: String, val rightValue: Any)

        val expectedArray = arrayOf(
            InfixTest("5 + 5;", 5, "+", 5),
            InfixTest("5 - 5;", 5, "-", 5),
            InfixTest("5 / 5;", 5, "/", 5),
            InfixTest("5 * 5;", 5, "*", 5),
            InfixTest("5 > 5;", 5, ">", 5),
            InfixTest("5 < 5;", 5, "<", 5),
            InfixTest("5 == 5;", 5, "==", 5),
            InfixTest("5 != 5;", 5, "!=", 5),
            InfixTest("true == true;", true, "==", true),
            InfixTest("true != false;", true, "!=", false),
            InfixTest("false != false;", false, "!=", false)
        )

        for ((input, left, operator, right) in expectedArray) {
            assertInfixExpression(input, left, operator, right)
        }
    }

    private fun assertInfixExpression(input: String, left: Any, operator: Any, right: Any) {
        val parser = Parser(Lexer(input))
        val program = parser.parseProgram()
        checkParserErrors(parser)

        program.statements.forEach { println(it.inspect()) }

        assertEquals(1, program.statements.size)

        val stmt = program.statements.first() as ExpressionStatement
        val exp = stmt.expression as InfixExpression

        assertEquals(left.toString(), exp.left.inspect())
        assertEquals(operator, exp.operator)
        assertEquals(right.toString(), exp.right.inspect())
    }

    @Test
    fun testOperatorPrecedenceParsing() {
        val tests = setOf(
            "-a * b" to "((-a) * b)",
            "!-a" to "(!(-a))",
            "a + b + c" to "((a + b) + c)",
            "a + b - c" to "((a + b) - c)",
            "a * b * c" to "((a * b) * c)",
            "a * b / c" to "((a * b) / c)",
            "a + b / c" to "(a + (b / c))",
            "a + b * c + d / e - f" to "(((a + (b * c)) + (d / e)) - f)",
            "3 + 4; -5 * 5" to "(3 + 4)((-5) * 5)",
            "5 > 4 == 3 < 4" to "((5 > 4) == (3 < 4))",
            "5 < 4 != 3 > 4" to "((5 < 4) != (3 > 4))",
            "3 + 4 * 5 == 3 * 1 + 4 * 5" to "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))",
            "true" to "true",
            "false" to "false",
            "3 > 5 == false" to "((3 > 5) == false)",
            "3 > 5 == true" to "((3 > 5) == true)",
            "-(5 + 5)" to "(-(5 + 5))",
            "!(true == true)" to "(!(true == true))",
            "a + add(b * c) + d" to "((a + add((b * c))) + d)",
            "add(a, b, 1, 2 * 3, 4 + 5, add(6, 7 * 8))" to "add(a, b, 1, (2 * 3), (4 + 5), add(6, (7 * 8)))",
            "add(a + b + c * d / f + g)" to "add((((a + b) + ((c * d) / f)) + g))",
            "a * [1, 2, 3, 4][b * c] * d" to "((a * ([1, 2, 3, 4][(b * c)])) * d)",
            "add(a * b[2], b[1], 2 * [1, 2][1])" to "add((a * (b[2])), (b[1]), (2 * ([1, 2][1])))"
        )
//        val tests = setOf(
//            "[1]" to "[1]"
//        )

        for ((input, expected) in tests) {
            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()
            checkParserErrors(parser)

            assertEquals(expected, program.inspect())
        }
    }

    @Test
    fun testBooleanExpression() {
        val tests = setOf(
            "true" to true,
            "false" to false
        )
        for ((input, expected) in tests) {
            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()
            checkParserErrors(parser)

            val expression = program.statements.first() as ExpressionStatement
            val actual = expression.expression as BooleanExpression
            assertEquals(expected, actual.bool)
        }
    }

    @Test
    fun testIfExpression() {
        val input = "if (x < y) { x }"

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParserErrors(parser)

        program.statements.forEach {
            println(it.inspect())
        }

        assertEquals(1, program.statements.size)

        val expressionStatement = program.statements.first() as ExpressionStatement

        val exp = expressionStatement.expression as IfExpression
        val infixExpression = exp.condition as InfixExpression

        assertEquals("x", infixExpression.left.inspect())
        assertEquals("<", infixExpression.operator)
        assertEquals("y", infixExpression.right.inspect())
        assertEquals(exp.consequence.statements.size, 1)

        val consequence = exp.consequence.statements.first()

        assertEquals(consequence.inspect(), "x")

        assertNull(exp.alternative)
    }

    /**
     * 高階関数のテスト
     */
    @Test
    fun testHigherOrderFunctionLiteralParsing() {
        val input =
            """let newAdder = fn(a, b) {
                    fn(c) { a + b + c };
                };""".trimMargin()
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()

        println(program.inspect())
        val letStatement = program.statements.first() as LetStatement
        val identifier = letStatement.identifier
        assertEquals("newAdder", identifier.tokenLiteral())
        val newAdderFunction = letStatement.value as FunctionLiteral
        assertEquals(
            listOf(Identifier(IdentToken("a")), Identifier(IdentToken("b"))),
            newAdderFunction.parameters
        )

        assertEquals(
            ExpressionStatement(
                FunctionToken, FunctionLiteral(
                    FunctionToken, listOf(Identifier(IdentToken("c"))), BlockStatement(
                        token = LBraceToken,
                        statements = listOf(Parser(Lexer("a + b + c")).parseProgram().statements.first())
                    )
                )
            ),
            newAdderFunction.body.statements.first()
        )
    }

    @Test
    fun testFunctionLiteralParsing() {
        val input = "fn(x, y) { x + y;}"
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParserErrors(parser)

        assertEquals(program.statements.size, 1)

        val statement = program.statements.first() as ExpressionStatement

        val function = statement.expression as FunctionLiteral

        assertEquals(function.parameters.size, 2)
        assertEquals(function.parameters[0].inspect(), "x")
        assertEquals(function.parameters[1].inspect(), "y")

        assertEquals(function.body.statements.size, 1)

        val bodyStatement = function.body.statements.first() as ExpressionStatement

        assertInfixExpression(bodyStatement.expression.inspect(), "x", "+", "y")
    }

    @Test
    fun testCallExpressionParsing() {
        val input = "add(1, 2 * 3, 4+5);"

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParserErrors(parser)

        program.statements.forEach { println(it.inspect()) }
        assertEquals(program.statements.size, 1)

        val statement = program.statements.first() as ExpressionStatement
        val expression = statement.expression as CallExpression

        assertEquals("add", expression.function.tokenLiteral())
        assertEquals(3, expression.arguments.size)
        assertEquals("1", expression.arguments[0].inspect())
        assertInfixExpression(expression.arguments[1].inspect(), "2", "*", "3")
        assertInfixExpression(expression.arguments[2].inspect(), "4", "+", "5")
    }

    @Test
    fun testStringLiteralExpression() {

        val input = "\"hello world\";"

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()

        val stmt = program.statements[0] as ExpressionStatement
        val literal = stmt.expression as StringLiteral

        assertEquals("hello world", literal.value)
    }

    @Test
    fun testParsingArrayLiterals() {
        val input = "[1, 2 * 2, 3 + 3]"

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()

        val statement = program.statements.first() as ExpressionStatement

        val array = statement.expression as ArrayLiteral
        assertEquals(3, array.elements.size)

        assertEquals(IntegerLiteral(IntToken("1"), 1), array.elements[0])
        assertInfixExpression(array.elements[1].inspect(), "2", "*", "2")
        assertInfixExpression(array.elements[2].inspect(), "3", "+", "3")
    }

    @Test
    fun testAbstractTree() {
        val input = "2 + 2 * 5 / 4"

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()

        val statement = program.statements.first() as ExpressionStatement

        assertEquals("(2 + ((2 * 5) / 4))", statement.inspect())
    }

    @Test
    fun testParsingIndexExpressions() {
        val input = "myArray[1 + 1]"

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = (parser.parseProgram())

        val statement = program.statements.first() as ExpressionStatement
        val indexExpression = statement.expression as IndexExpression
        assertEquals("myArray", indexExpression.left.inspect())
        assertInfixExpression(indexExpression.index.inspect(), "1", "+", "1")
    }

    @Test
    fun testParsingHashLiteralStringKeys() {
        val input = """{"one":1,"two":2,"three":3}"""
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()

        val stmt = program.statements.first() as ExpressionStatement
        val hash = stmt.expression as HashLiteral
        assertEquals(3, hash.map.size)

        val expectedMap = mapOf("one" to 1, "two" to 2, "three" to 3)

        hash.map.forEach { (key, value) ->
            val string = key as StringLiteral
            val integer = value as IntegerLiteral
            assertEquals(expectedMap[string.value], integer.value)
        }
    }

    @Test
    fun testParsingEmptyHashLiteral() {
        val input = "{}"
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()

        val stmt = program.statements.first() as ExpressionStatement
        val hash = stmt.expression as HashLiteral
        assertEquals(0, hash.map.size)
        assertTrue(hash.map.isEmpty())
    }

    @ParameterizedTest
    @MethodSource("provideTestFunctionParameterParsing")
    fun testFunctionParameterParsing(input: String, expectedParameters: List<String>) {
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParserErrors(parser)
        val statement = program.statements.first() as ExpressionStatement

        val function = statement.expression as FunctionLiteral

        val actualParameters = function.parameters.map { it.inspect() }

        assertIterableEquals(expectedParameters, actualParameters)
    }

    @Test
    fun testParsingHashLiteralWithExpression() {
        val input = """{"one":0+1,"two":10-8,"three":15/3}"""
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()

        val stmt = program.statements.first() as ExpressionStatement
        val hash = stmt.expression as HashLiteral
        assertEquals(3, hash.map.size)

        hash.map.forEach { (key, value) ->
            val keyStringLiteral = key as StringLiteral
            when (keyStringLiteral.value) {
                "one" -> {
                    assertInfixExpression(value.inspect(), "0", "+", "1")
                }
                "two" -> {
                    assertInfixExpression(value.inspect(), "10", "-", "8")
                }
                "three" -> {
                    assertInfixExpression(value.inspect(), "15", "/", "3")
                }
            }
        }
    }

    @Test
    fun testMacroLiteralParsing() {
        val input = "macro(x,y){x + y;}"

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        assertEquals(1, program.statements.size)

        val stmt = program.statements.first() as ExpressionStatement
        val macro = stmt.expression as MacroLiteral

        assertEquals(2, macro.parameters.size)

        assertEquals("x", macro.parameters[0].tokenLiteral())
        assertEquals("y", macro.parameters[1].tokenLiteral())

        assertEquals(1, macro.body.statements.size)

        val body = macro.body.statements.first() as ExpressionStatement

        assertInfixExpression(body.inspect(), "x", "+", "y")
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun provideTestFunctionParameterParsing() =
            listOf(
                Arguments.of("fn(){}", listOf<String>()),
                Arguments.of("fn(x){}", listOf("x")),
                Arguments.of("fn(x,y,z){}", listOf("x", "y", "z"))
            )
    }
}
