package com.example.monkey.obj

import com.example.monkey.ast.Identifier

data class Environment(
    private val outer: Environment? = null
) {
    /**
     * 変数のMAP
     */
    private val store: MutableMap<Identifier, Object> = mutableMapOf()

    /**
     * 変数の値を取得
     */
    fun get(ident: Identifier): Object? {

        val obj = store[ident]

        // 外側にデータがあれば取得する
        if (obj == null && outer != null) {
            return outer.get(ident)
        }

        return obj
    }

    /**
     * パラメータの設定
     */
    fun set(ident: Identifier, value: Object): Object {
        store[ident] = value
        return value
    }
}