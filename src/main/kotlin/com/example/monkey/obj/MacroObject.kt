package com.example.monkey.obj

import com.example.monkey.ast.BlockStatement
import com.example.monkey.ast.Identifier

/**
 * マクロオブジェクト
 */
data class MacroObject(
    val parameters: List<Identifier>,
    val body: BlockStatement,
    val env: Environment
) : Object {

    override val type: ObjectType = ObjectType.MACRO

    override val inspect: String
        get() {
            return StringBuilder().apply {
                append("macro(${parameters.joinToString(" ,"){it.inspect()}}{\r\n${body.inspect()}}\r\n)")
            }.toString()
        }
}