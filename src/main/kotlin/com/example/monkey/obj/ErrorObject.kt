package com.example.monkey.obj

/**
 * Errorオブジェクト
 */
data class ErrorObject(
    val message: String
) : Object {

    override val type: ObjectType = ObjectType.ERROR

    /**
     * エラーメッセージ
     */
    override val inspect: String = message
}