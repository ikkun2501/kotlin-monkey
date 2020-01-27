package com.example.monkey.evaluator

import com.example.monkey.ast.*
import com.example.monkey.obj.Environment
import com.example.monkey.obj.MacroObject
import com.example.monkey.obj.QuoteObject

fun defineMacros(program: Program, env: Environment): Program {

    program.statements.filter(::isMacroDefinition).forEach {
        addMacro(it, env)
    }

    return program.copy(statements = program.statements.filterNot(::isMacroDefinition).toMutableList())
}

fun isMacroDefinition(node: Statement): Boolean {
    val letStatement = node as? LetStatement ?: return false
    return letStatement.value is MacroLiteral
}

fun addMacro(statement: Statement, env: Environment) {
    val letStatement = statement as LetStatement
    val macroLiteral = letStatement.value as MacroLiteral

    val macro = MacroObject(
        parameters = macroLiteral.parameters,
        env = env,
        body = macroLiteral.body
    )

    env.set(letStatement.identifier, macro)
}

/**
 * マクロ展開
 */
fun expandMacros(program: Node, env: Environment): Node {
    return modify(program) { node ->

        val callExpression = node as? CallExpression ?: return@modify node

        val macro = isMacroCall(callExpression, env) ?: return@modify node

        val args = quoteArgs(callExpression)

        val evalEnv = extendMacroEnv(macro, args)

        val evaluated = eval(macro.body, evalEnv)

        val quote = evaluated as QuoteObject

        quote.node
    }
}

/**
 * Macro呼び出しか判定
 */
fun isMacroCall(exp: CallExpression, env: Environment): MacroObject? {

    val identifier = exp.function as Identifier

    val obj = env.get(identifier) ?: return null

    return obj as? MacroObject
}

/**
 *
 */
fun quoteArgs(exp: CallExpression): List<QuoteObject> {
    return exp.arguments.map(::QuoteObject)
}

fun extendMacroEnv(
    macro: MacroObject,
    args: List<QuoteObject>
): Environment {
    val extended = Environment(macro.env)

    macro.parameters.forEachIndexed { index, identifier ->
        extended.set(identifier, args[index])
    }

    return extended
}