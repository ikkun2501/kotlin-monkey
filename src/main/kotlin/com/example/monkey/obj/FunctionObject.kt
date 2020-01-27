package com.example.monkey.obj

import com.example.monkey.ast.BlockStatement
import com.example.monkey.ast.Identifier

/**
 * 関数オブジェクト
 */
data class FunctionObject(
    val parameters: List<Identifier>,
    val body: BlockStatement,
    val env: Environment
) : Object {

    override val type: ObjectType = ObjectType.FUNCTION

    override val inspect: String
        get() {
            return StringBuilder().apply {
                append("fn(${parameters.joinToString(" ,"){it.inspect()}}){\r\n${body.inspect()}\r\n}")
            }.toString()
        }
}