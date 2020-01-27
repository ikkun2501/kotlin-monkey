package com.example.monkey.obj

/**
 * 配列オブジェクト
 */
class ArrayObject(val elements: Array<Object>) : Object {

    override val type: ObjectType = ObjectType.ARRAY

    override val inspect: String
        get() {
            return buildString {
                append("[")
                append(elements.joinToString(", ") { it.inspect })
                append("]")
            }
        }
}