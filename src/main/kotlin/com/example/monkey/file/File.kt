package com.example.monkey.file

import com.example.monkey.evaluator.defineMacros
import com.example.monkey.evaluator.eval
import com.example.monkey.evaluator.expandMacros
import com.example.monkey.lexer.Lexer
import com.example.monkey.obj.Environment
import com.example.monkey.parser.Parser
import java.nio.file.Files
import java.nio.file.Paths

/**
 * args[0] absolute file path
 */
fun main(args: Array<String>) {

    val filePath = args.first()

    val env = Environment()
    val macroEnv = Environment()

    val input = Files.readAllLines(Paths.get(filePath)).joinToString("")

    // 字句解析
    val lexer = Lexer(input)
    //
    val parser = Parser(lexer)

    val program = parser.parseProgram()

    val evaluated = eval(expandMacros(defineMacros(program, macroEnv), macroEnv), env)

    println(evaluated.inspect)
}
