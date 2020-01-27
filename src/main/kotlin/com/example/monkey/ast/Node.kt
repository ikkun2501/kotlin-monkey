package com.example.monkey.ast

/**
 *　ASTのインターフェイス
 */
interface Node {
    /**
     * ノードが関連付けられているトークンリテラルを返す
     * デバッグとテストのために使用する
     */
    fun tokenLiteral(): String

    /**
     *  検査
     */
    fun inspect(): String
}