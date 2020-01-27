package com.example.monkey.parser

import com.example.monkey.token.TokenType

class BadGrammarException(actual: TokenType, vararg expects: TokenType) :
    Exception("expect:${expects.joinToString(" or ") { it.name }} , actual:${actual.name}")
