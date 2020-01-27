package com.example.monkey.obj

import com.example.monkey.ast.Identifier

/**
 * 組み込みオブジェクト
 */
interface BuiltinObject : Object {
    fun builtinFunction(vararg args: Object): Object
}

class LenBuiltinObject : BuiltinObject {

    override fun builtinFunction(vararg args: Object): Object {

        if (args.size != 1) {
            return ErrorObject("wrong number of arguments. got=${args.size}, want=1")
        }

        return when (val arg = args.first()) {
            is StringObject -> IntegerObject(arg.value.length)
            is ArrayObject -> IntegerObject(arg.elements.size)
            else -> ErrorObject("argument to `len` not supported, got ${arg.type}")
        }
    }

    override val inspect: String = "len builtin function"
    override val type: ObjectType = ObjectType.BUILTIN
}

class FirstBuiltinObject : BuiltinObject {

    override fun builtinFunction(vararg args: Object): Object {
        if (args.size != 1) {
            return ErrorObject("wrong number of arguments. got=${args.size}, want=1")
        }
        return when (val arg = args.first()) {
            is ArrayObject -> arg.elements.firstOrNull() ?: NullObject
            else -> ErrorObject("argument to `len` not supported, got ${arg.type}")
        }
    }

    override val inspect: String = "len builtin function"
    override val type: ObjectType = ObjectType.BUILTIN
}

class LastBuiltinObject : BuiltinObject {

    override fun builtinFunction(vararg args: Object): Object {
        if (args.size != 1) {
            return ErrorObject("wrong number of arguments. got=${args.size}, want=1")
        }
        return when (val arg = args.first()) {
            is ArrayObject -> arg.elements.lastOrNull() ?: NullObject
            else -> ErrorObject("argument to `len` not supported, got ${arg.type}")
        }
    }

    override val inspect: String = "last builtin function"
    override val type: ObjectType = ObjectType.BUILTIN
}

class PushBuiltinObject : BuiltinObject {

    override fun builtinFunction(vararg args: Object): Object {
        if (args.size != 2) {
            return ErrorObject("wrong number of arguments. got=${args.size}, want=2")
        }
        if (args.first() !is ArrayObject) {
            return ErrorObject("argument to `push` must be ARRAY, got ${args.first().type}")
        }
        return when (val arg = args.first()) {
            is ArrayObject -> ArrayObject(elements = arg.elements.plus(args[1]))
            else -> ErrorObject("argument to `len` not supported, got ${arg.type}")
        }
    }

    override val inspect: String = "len builtin function"
    override val type: ObjectType = ObjectType.BUILTIN
}

class RestBuiltinObject : BuiltinObject {

    override fun builtinFunction(vararg args: Object): Object {
        if (args.size != 1) {
            return ErrorObject("wrong number of arguments. got=${args.size}, want=1")
        }
        return when (val arg = args.first()) {
            is ArrayObject -> ArrayObject(elements = arg.elements.drop(1).toTypedArray())
            else -> ErrorObject("argument to `len` not supported, got ${arg.type}")
        }
    }

    override val inspect: String = "rest builtin function"
    override val type: ObjectType = ObjectType.BUILTIN
}

class PutsBuiltinObject : BuiltinObject {

    override fun builtinFunction(vararg args: Object): Object {
        if (args.size != 1) {
            return ErrorObject("wrong number of arguments. got=${args.size}, want=1")
        }
        println(args.first().inspect)

        return NullObject
    }

    override val inspect: String = "puts builtin function"
    override val type: ObjectType = ObjectType.BUILTIN
}

fun builtins(identifier: Identifier): Object? {
    return when (identifier.tokenLiteral()) {
        "len" -> LenBuiltinObject()
        "first" -> FirstBuiltinObject()
        "last" -> LastBuiltinObject()
        "rest" -> RestBuiltinObject()
        "push" -> PushBuiltinObject()
        "puts" -> PutsBuiltinObject()
        else -> null
    }
}
