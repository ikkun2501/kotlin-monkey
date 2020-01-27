package com.example.monkey.obj

/**
 * オブジェクト
 */
interface Object {
    /**
     * オブジェクト種別
     */
    val type: ObjectType

    /**
     * 検査
     */
    val inspect: String
}