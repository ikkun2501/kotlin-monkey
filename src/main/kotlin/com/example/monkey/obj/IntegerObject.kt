package com.example.monkey.obj

/**
 * 数値オブジェクト
 */
data class IntegerObject(
    val value: Int
) : Object, Hashable {
    override val type: ObjectType = ObjectType.INTEGER

    override val inspect: String = value.toString()

    override fun hashKey(): HashKey {
        return HashKey(type = type, value = value.hashCode())
    }
}