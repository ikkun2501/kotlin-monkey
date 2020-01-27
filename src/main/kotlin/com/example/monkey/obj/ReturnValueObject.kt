package com.example.monkey.obj

/**
 * 戻り値オブジェクト
 */
data class ReturnValueObject(
    val value: Object
) : Object {
    override val type: ObjectType = ObjectType.RETURN_VALUE
    override val inspect: String = value.inspect
}