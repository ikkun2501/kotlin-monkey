package com.example.monkey.obj

/**
 * 文字列オブジェクト
 */
data class StringObject(
    val value: String
) : Object, Hashable {

    override val type: ObjectType = ObjectType.STRING
    override val inspect: String = value

    override fun hashKey(): HashKey {
        return HashKey(type = type, value = value.hashCode())
    }
}