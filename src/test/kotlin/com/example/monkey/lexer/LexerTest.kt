package com.example.monkey.lexer

import com.example.monkey.token.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LexerTest {

    @Test
    fun testNextToken() {
        val input = "=();+-*/"

        val expectedList =
            arrayOf(
                AssignToken,
                LParenToken,
                RParenToken,
                SemicolonToken,
                PlusToken,
                MinusToken,
                AsteriskToken,
                SlashToken
            )

        val lexer = Lexer(src = input)

        for (expected in expectedList) {
            Assertions.assertEquals(expected, lexer.nextToken())
        }
    }

    @Test
    fun testNextToken2() {
        val input = """
            let five = 5;
            let ten = 10;

            let add = fn(x,y){
                x + y;
            };
            let result = add(five, ten);
            "foobar"
            "foo bar"
            [1, 2];
        """.trimIndent()

        val expectedList =
            arrayOf(
                LetToken,
                IdentToken("five"),
                AssignToken,
                IntToken("5"),
                SemicolonToken,
                LetToken,
                IdentToken("ten"),
                AssignToken,
                IntToken("10"),
                SemicolonToken,
                LetToken,
                IdentToken("add"),
                AssignToken,
                FunctionToken,
                LParenToken,
                IdentToken("x"),
                CommaToken,
                IdentToken("y"),
                RParenToken,
                LBraceToken,
                IdentToken("x"),
                PlusToken,
                IdentToken("y"),
                SemicolonToken,
                RBraceToken,
                SemicolonToken,
                LetToken,
                IdentToken("result"),
                AssignToken,
                IdentToken("add"),
                LParenToken,
                IdentToken("five"),
                CommaToken,
                IdentToken("ten"),
                RParenToken,
                SemicolonToken,
                StringToken("foobar"),
                StringToken("foo bar"),
                LBracketToken,
                IntToken("1"),
                CommaToken,
                IntToken("2"),
                RBracketToken,
                SemicolonToken,
                EofToken
            )

        val lexer = Lexer(src = input)

        for (expected in expectedList) {
            Assertions.assertEquals(expected, lexer.nextToken())
        }
    }

    @Test
    fun testNextToken3() {
        val input = """
            !-/*5;
            5 < 10 > 5;
        """.trimIndent()

        val expectedList =
            arrayOf(
                // 1行目
                BangToken,
                MinusToken,
                SlashToken,
                AsteriskToken,
                IntToken("5"),
                SemicolonToken,

                // 2行目
                IntToken("5"),
                LessThanToken,
                IntToken("10"),
                GreaterThanToken,
                IntToken("5"),
                SemicolonToken,
                EofToken
            )

        val lexer = Lexer(src = input)

        for (expected in expectedList) {
            Assertions.assertEquals(expected, lexer.nextToken())
        }
    }

    @Test
    fun testNextToken4() {
        val input = """
            if (5 < 10){
                return true;
            } else {
                return false;
            }
        """.trimIndent()

        val expectedList =
            arrayOf(
                IfToken,
                LParenToken,
                IntToken("5"),
                LessThanToken,
                IntToken("10"),
                RParenToken,
                LBraceToken,
                ReturnToken,
                TrueToken,
                SemicolonToken,
                RBraceToken,
                ElseToken,
                LBraceToken,
                ReturnToken,
                FalseToken,
                SemicolonToken,
                RBraceToken,
                EofToken
            )

        val lexer = Lexer(src = input)

        for (expected in expectedList) {
            Assertions.assertEquals(expected, lexer.nextToken())
        }
    }

    @Test
    fun testNextToken5() {
        val input = """
            10 == 10;
            10 != 9;
            !=
        """.trimIndent()

        val expectedList =
            arrayOf(
                IntToken("10"),
                EqualToken,
                IntToken("10"),
                SemicolonToken,
                IntToken("10"),
                NotEqualToken,
                IntToken("9"),
                SemicolonToken,
                NotEqualToken,
                EofToken
            )

        val lexer = Lexer(src = input)

        for (expected in expectedList) {
            assertEquals(expected, lexer.nextToken())
        }
    }

    @Test
    fun testNextToken6() {
        val input = """
            {"foo":"bar"}
        """.trimIndent()

        val expectedList =
            arrayOf(
                LBraceToken,
                StringToken("foo"),
                ColonToken,
                StringToken("bar"),
                RBraceToken,
                EofToken
            )

        val lexer = Lexer(src = input)

        for (expected in expectedList) {
            assertEquals(expected, lexer.nextToken())
        }
    }

    @Test
    fun testNextToken7() {
        val input = "macro(x,y){x + y;};"
        val expectedList =
            arrayOf(
                MacroToken,
                LParenToken,
                IdentToken("x"),
                CommaToken,
                IdentToken("y"),
                RParenToken,
                LBraceToken,
                IdentToken("x"),
                PlusToken,
                IdentToken("y"),
                SemicolonToken,
                RBraceToken,
                SemicolonToken,
                EofToken
            )
        val lexer = Lexer(src = input)

        for (expected in expectedList) {
            assertEquals(expected, lexer.nextToken())
        }
    }
}