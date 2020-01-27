package com.example.monkey.evaluator

import com.example.monkey.ast.Identifier
import com.example.monkey.ast.Program
import com.example.monkey.lexer.Lexer
import com.example.monkey.obj.Environment
import com.example.monkey.obj.MacroObject
import com.example.monkey.parser.Parser
import com.example.monkey.token.IdentToken
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MacroExpansionTest {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Test
    fun testNormal() {
        val input = """
            let newAdder = fn(a, b) {
                fn(c) { a + b + c };
            };
            let adder = newAdder(1, 2);
            adder(8); 
        """.trimIndent()

        val env = Environment()
        val obj = eval(expandMacros(defineMacros(testParseProgram(input), env), env), env)
        println(obj)
    }

    @Test
    fun testDefineMacros() {
        val input = """
           let number = 1;
           let function = fn(x,y){x + y};
           let mymacro = macro(x,y){x+y};
       """.trimIndent()

        val env = Environment()
        val program = defineMacros(testParseProgram(input), env)

        assertEquals(2, program.statements.size)
        assertNull(env.get(Identifier(IdentToken("number"))))
        assertNull(env.get(Identifier(IdentToken("function"))))

        val obj = env.get(Identifier(IdentToken("mymacro")))
        assertNotNull(obj)

        val macro = obj as MacroObject

        assertEquals(2, macro.parameters.size)
        assertEquals("x", macro.parameters[0].tokenLiteral())
        assertEquals("y", macro.parameters[1].tokenLiteral())

        assertEquals("(x + y)", macro.body.inspect())
    }

    @Test
    fun testExpandMacros() {
        val tests = setOf(
            """
                let infixExpression = macro(){quote(1 + 2);};
                infixExpression();
            """.trimIndent()
                    to "(1 + 2)",
            """
                let reverse = macro(a,b){quote(unquote(b) - unquote(a));};
                reverse(2 + 2,10-5);
            """.trimIndent()
                    to "(10 - 5) - (2 + 2)"

        )

        for ((input, expectedInput) in tests) {
            val expected = testParseProgram(expectedInput)

            val env = Environment()
            val program = expandMacros(defineMacros(testParseProgram(input), env), env)

            assertEquals(expected.inspect(), program.inspect())
        }
    }

    @Test
    fun testExpandUnlessMacros() {
        val tests = setOf(
            """
                let unless = macro(condition,consequence,alternative){
                    quote(if(!(unquote(condition))){
                        unquote(consequence);
                    } else {
                        unquote(alternative);
                    });
                };
                
                unless(10 > 5, puts("not greater"),puts("greater"));
            """.trimIndent()
                    to """if (!(10 > 5)) { puts("not greater") } else { puts("greater") } """

        )

        for ((input, expectedInput) in tests) {
            val expected = testParseProgram(expectedInput)

            val env = Environment()
            val program = expandMacros(defineMacros(testParseProgram(input), env), env)

            logger.debug(expected.inspect())
            logger.debug(program.inspect())
            assertEquals(expected.inspect(), program.inspect())
        }
    }

    private fun testParseProgram(input: String): Program {
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        return parser.parseProgram()
    }
}