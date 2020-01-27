package com.example.monkey.obj

import com.example.monkey.ast.Node

/**
 * Quoteオブジェクト
 */
data class QuoteObject(
    val node: Node
) : Object {
    override val type: ObjectType = ObjectType.QUOTE
    override val inspect: String = """QUOTE(${node.inspect()})"""
}