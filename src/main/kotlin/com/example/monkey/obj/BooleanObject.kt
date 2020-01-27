package com.example.monkey.obj

/**
 * 真偽値オブジェクト
 */
sealed class BooleanObject(
    val value: Boolean
) : Object, Hashable {

    override val type: ObjectType = ObjectType.BOOLEAN
    override val inspect: String = value.toString()

    /**
     * Trueオブジェクト
     */
    object TrueObject : BooleanObject(true)

    /**
     * Falseオブジェクト
     */
    object FalseObject : BooleanObject(false)

    override fun hashKey(): HashKey {
        return HashKey(type = type, value = value.hashCode())
    }
}
