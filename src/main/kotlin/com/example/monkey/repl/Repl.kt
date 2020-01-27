package com.example.monkey.repl

import com.example.monkey.evaluator.defineMacros
import com.example.monkey.evaluator.eval
import com.example.monkey.evaluator.expandMacros
import com.example.monkey.lexer.Lexer
import com.example.monkey.obj.Environment
import com.example.monkey.parser.Parser

fun main() {
    val env = Environment()
    val macroEnv = Environment()
    while (true) {
        print(">> ")
        val line = readLine() ?: return

        // 字句解析
        val lexer = Lexer(line)
        //
        val parser = Parser(lexer)

        val program = parser.parseProgram()

        val evaluated = eval(expandMacros(defineMacros(program, macroEnv), macroEnv), env)
        println(evaluated.inspect)
    }
}
