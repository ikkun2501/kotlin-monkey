package com.example.monkey.evaluator

import com.example.monkey.lexer.Lexer
import com.example.monkey.obj.*
import com.example.monkey.parser.Parser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class EvalTest {
    @Test
    fun testEvalIntegerExpression() {
        val tests = listOf(
            "5" to 5,
            "10" to 10,
            "-5" to -5,
            "-10" to -10,
            "5 + 5 + 5 + 5 - 10" to 10,
            "2 * 2 * 2 * 2 * 2" to 32,
            "-50 + 100 + -50" to 0,
            "5 * 2 + 10" to 20,
            "5 + 2 * 10" to 25,
            "20 + 2 * -10" to 0,
            "50 / 2 * 2 + 10" to 60,
            "2 * (5 + 10)" to 30,
            "3 * 3 * 3 + 10" to 37,
            "3 * (3 * 3) + 10" to 37,
            "(5 + 10 * 2 + 15 /3) * 2 + -10" to 50

        )

        tests.forEach { (input, expected) ->
            assertIntegerObject(expected, testEval(input))
        }
    }

    @Test
    fun testEvalBooleanExpression() {

        val tests = listOf(
            "true" to true,
            "false" to false,
            "1 < 2" to true,
            "1 > 2" to false,
            "1 < 1" to false,
            "1 > 1" to false,
            "1 == 1" to true,
            "1 != 1" to false,
            "1 == 2" to false,
            "1 != 2" to true,
            "true == true" to true,
            "false == false" to true,
            "true == false" to false,
            "true != false" to true,
            "false != true" to true,
            "(1 < 2) == true" to true,
            "(1 < 2) == false" to false,
            "(1 > 2) == true" to false,
            "(1 > 2) == false" to true
        )

        tests.forEach { (input, expected) ->
            assertBooleanObject(expected, testEval(input))
        }
    }

    @Test
    fun testBangOperator() {
        val tests = listOf(
            "!true" to false,
            "!false" to true,
            "!5" to false,
            "!!true" to true,
            "!!false" to false,
            "!!5" to true
        )

        tests.forEach { (input, expected) ->
            val evaluated = testEval(input)
            assertBooleanObject(expected, evaluated)
        }
    }

    @Test
    fun testIfElseExpression() {
        val tests = mapOf(
            "if(true) {10}" to 10,
            "if(false) {10}" to null,
            "if(1) { 10 }" to 10,
            "if(1<2) { 10 }" to 10,
            "if(1>2) { 10 }" to null,
            "if(1>2) { 10 } else {20}" to 20,
            "if(1<2) { 10 } else {20}" to 10
        )

        tests.forEach { (input, expected) ->
            when (val evaluated = testEval(input)) {
                is IntegerObject -> assertEquals(expected, evaluated.value)
                is NullObject -> assertNull(expected)
            }
        }
    }

    @Test
    fun testReturnStatements() {
        val tests = mapOf(
            "return 10;" to 10,
            "return 10; 9;" to 10,
            "return 2 * 5; 9;" to 10,
            "9; return 2 * 5; 9;" to 10
        )

        tests.forEach { (input, expected) ->
            val evaluated = testEval(input)
            assertIntegerObject(expected, evaluated)
        }
    }

    @Test
    fun testErrorHandling() {
        val tests = mapOf(
            "5 + true;" to "type mismatch: INTEGER + BOOLEAN",
            "5 + true; 5;" to "type mismatch: INTEGER + BOOLEAN",
            "-true" to "unknown operator: -BOOLEAN",
            "true + false;" to "unknown operator: BOOLEAN + BOOLEAN",
            "5; true + false; 5" to "unknown operator: BOOLEAN + BOOLEAN",
            "if (10 > 1) { true + false; }" to "unknown operator: BOOLEAN + BOOLEAN",
            """if ( 10 > 1) {
                if (10 > 1){    
                    return true + false;
                }
               }
               return 1;
            """.trimMargin() to "unknown operator: BOOLEAN + BOOLEAN",
            "foobar" to "identifier not found: foobar",
            """"Hello" - "World"""" to "unknown operator: STRING - STRING",
            """{"name":"Monkey"}[fn(x){x}];""" to "unusable as hash key: FUNCTION"
        )

        tests.forEach { (input, expected) ->
            val evaluated = testEval(input) as? ErrorObject

            if (evaluated == null) {
                fail("input not ErrorObject, input:$input")
            }

            assertEquals(expected, evaluated.message)
        }
    }

    @Test
    fun testLetStatements() {
        val tests = mapOf(
            "let a= 5; a;" to 5,
            "let a = 5 * 5; a;" to 25,
            "let a = 5; let b =a; b;" to 5,
            "let a = 5; let b= a; let c = a + b + 5; c;" to 15
        )

        tests.forEach { (input, expected) ->
            assertIntegerObject(expected, testEval(input))
        }
    }

    @Test
    fun testFunctionObject() {
        val input = "fn(x) { x + 2; };"
        val function = testEval(input) as FunctionObject

        assertEquals(function.body.statements.size, 1)
        assertEquals(function.parameters[0].inspect(), "x")
        assertEquals(function.body.inspect(), "(x + 2)")
    }

    /**
     * 高階関数のテスト
     */
    @Test
    fun testHigherOrderFunctionObject() {
        val input =
            """let newAdder = fn(a, b) {
                    fn(c) { a + b + c };
                };
                let adder = newAdder(1,2)
                adder(3)
                """.trimMargin()

        val obj = testEval(input) as IntegerObject
        assertEquals(IntegerObject(6), obj)
    }

    @Test
    fun testFunctionApplication() {
        val tests = mapOf(
            "let identify = fn(x){x;}; identify(5);" to 5,
            "let identify = fn(x){return x;}; identify(5);" to 5,
            "let double= fn(x){x*2;}; double(5);" to 10,
            "let add= fn(x,y){x;}; add(5,5);" to 5,
            "let add= fn(x,y){x+y;}; add(5 + 5,add(5,5));" to 20,
            "fn(x){x;}(5)" to 5
        )

        tests.forEach { (input, expected) ->
            assertIntegerObject(expected, testEval(input))
        }
    }

    @Test
    fun testStringLiteral() {

        val input = "\"Hello World!\""

        val evaluated = testEval(input) as StringObject

        assertEquals("Hello World!", evaluated.value)
    }

    @Test
    fun testStringConcatenation() {
        val input = "\"Hello\" + \" \" + \"World!\""

        val evaluated = testEval(input)

        val str = evaluated as StringObject

        assertEquals("Hello World!", str.value)
    }

    @Test
    fun testBuiltinFunctions() {
        val tests = mapOf(
            """len("")""" to 0,
            """len("four")""" to 4,
            """len("hello world")""" to 11,
            """len(1)""" to "argument to `len` not supported, got INTEGER",
            """len("one","two")""" to "wrong number of arguments. got=2, want=1",
            "len([1,2,3,4,5])" to 5,
            "len([])" to 0,
            "let myArray = [1,2,3,4,5];len(myArray)" to 5,
            "first([1,2,3,4,5])" to 1,
            "last([1,2,3,4,5])" to 5,
            "first([])" to null,
            "last([])" to null,
            "let myArray = [1,2,3,4,5];first(myArray)" to 1,
            "let myArray = [1,2,3,4,5];last(myArray)" to 5,
            "let myArray = [];first(myArray)" to null,
            "let myArray = [];last(myArray)" to null,
            "rest([1,2,3])" to arrayOf(2, 3),
            "rest([2,3])" to arrayOf(3),
            "rest([3])" to emptyArray<Int>(),
            "rest([])" to emptyArray<Int>(),
            "rest()" to "wrong number of arguments. got=0, want=1",
            "push([],1)" to arrayOf(1),
            "push([1],2)" to arrayOf(1, 2),
            "push([1,2],3)" to arrayOf(1, 2, 3),
            "push()" to "wrong number of arguments. got=0, want=2",
            "push([])" to "wrong number of arguments. got=1, want=2",
            "push(1,1)" to "argument to `push` must be ARRAY, got INTEGER"

        )

        tests.forEach { (input, expected) ->
            val evaluated = testEval(input)

            when (expected) {
                is Int -> assertIntegerObject(expected, evaluated)
                null -> assertEquals(NullObject, evaluated)
                is Array<*> -> assertArrayEquals(
                    expected,
                    (evaluated as ArrayObject).elements.map { it as IntegerObject }.map { it.value }.toTypedArray()
                )
                is String -> {
                    val errObj = evaluated as ErrorObject
                    assertEquals(expected, errObj.message)
                }
            }
        }
    }

    @Test
    fun testArrayLiterals() {
        val input = "[1, 2 * 2, 3 + 3]"

        val evaluated = testEval(input)
        val result = evaluated as ArrayObject

        assertEquals(3, result.elements.size)

        assertIntegerObject(1, result.elements[0])
        assertIntegerObject(4, result.elements[1])
        assertIntegerObject(6, result.elements[2])
    }

    @Test
    fun testArrayIndexExpression() {
        val tests = listOf(
            "[1,2,3][0]" to 1,
            "[1,2,3][1]" to 2,
            "[1,2,3][2]" to 3,
            "let i =0;[1][i]" to 1,
            "[1,2,3][1+1];" to 3,
            "let myArray = [1,2,3];myArray[2];" to 3,
            "let myArray = [1,2,3];myArray[0]+myArray[1]+myArray[2];" to 6,
            "let myArray = [1,2,3];let i = myArray[0];myArray[i]" to 2,
            "[1,2,3][3]" to null,
            "[1,2,3][-1]" to null
        )

        tests.forEach { (input, expected) ->
            val evaluated = testEval(input)
            if (evaluated is IntegerObject) {
                assertIntegerObject(expected!!, evaluated)
            } else {
                assertEquals(NullObject, evaluated)
            }
        }
    }

    @Test
    fun testHashLiterals() {
        val input = """
            let two = "two";
            {
                "one": 10 - 9,
                two : 1 + 1,
                "thr" + "ee" : 6/2,
                 4:4,
                 true:5,
                 false:6
            }
        """.trimIndent()

        val evaluated = testEval(input)
        val result = evaluated as HashObject

        val expected = mapOf(
            StringObject("one").hashKey() to 1,
            StringObject("two").hashKey() to 2,
            StringObject("three").hashKey() to 3,
            IntegerObject(4).hashKey() to 4,
            BooleanObject.TrueObject.hashKey() to 5,
            BooleanObject.FalseObject.hashKey() to 6
        )

        for ((key, value) in result.map) {
            val integerValue = value as IntegerObject
            assertEquals(expected[key.hashKey()], integerValue.value)
        }
    }

    @Test
    fun testHashIndexExpression() {
        val tests = setOf(
            """{"foo":5}["foo"]""" to 5,
            """{"foo":5}["bar"]""" to null,
            """let key = "foo";{"foo":5}[key]""" to 5,
            """{}["foo"]""" to null,
            """{5:5}[5]""" to 5,
            """{true:5}[true]""" to 5,
            """{false:5}[false]""" to 5
        )

        tests.forEach { (input, expected) ->
            when (val evaluated = testEval(input)) {
                is NullObject -> assertEquals(NullObject, evaluated)
                is IntegerObject -> assertEquals(expected, evaluated.value)
                else -> fail("")
            }
        }
    }

    @Test
    fun testQuote() {
        val tests = setOf(
            "quote(5)" to "5",
            "quote(5 + 8)" to "(5 + 8)",
            "quote(foobar)" to "foobar",
            "quote(foobar + barfoo)" to "(foobar + barfoo)"
        )

        tests.forEach { (input, expected) ->
            val evaluated = testEval(input)
            val quote = evaluated as QuoteObject

            assertNotNull(quote.node)
            assertEquals(expected, quote.node.inspect())
        }
    }

    @Test
    fun testQuoteUnquote() {
        val tests = setOf(
            "quote(unquote(4))" to "4",
            "quote(unquote(4 + 4))" to "8",
            "quote(8 + unquote(4 + 4))" to "(8 + 8)",
            "quote(unquote(4 + 4) + 8)" to "(8 + 8)",
            "quote(unquote(true))" to "true",
            "quote(unquote(true == false))" to "false",
            "quote(unquote(quote(4 + 4)))" to "(4 + 4)",
            """let quotedInfixExpression = quote(4+4);
                |quote(unquote(4 + 4) + unquote(quotedInfixExpression))
            """.trimMargin() to "(8 + (4 + 4))"
        )

        tests.forEach { (input, expected) ->
            val evaluated = testEval(input)
            val quote = evaluated as QuoteObject

            assertNotNull(quote.node)
            assertEquals(expected, quote.node.inspect())
        }
    }

    private fun testEval(input: String): Object {

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        val env = Environment()
        return eval(program, env)
    }

    private fun assertIntegerObject(expected: Int, actual: Object) {
        val integerObject = actual as? IntegerObject
        if (integerObject == null) {
            if (actual is ErrorObject) {
                fail(actual.message)
            }
            fail("$actual.type")
        }
        assertEquals(expected, integerObject.value)
    }

    private fun assertBooleanObject(expected: Boolean, actual: Object) {
        val booleanObject = actual as BooleanObject
        assertEquals(expected, booleanObject.value)
    }
}