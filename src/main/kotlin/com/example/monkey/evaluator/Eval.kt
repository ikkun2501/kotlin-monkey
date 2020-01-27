package com.example.monkey.evaluator

import com.example.monkey.ast.*
import com.example.monkey.obj.*
import com.example.monkey.token.FalseToken
import com.example.monkey.token.IntToken
import com.example.monkey.token.TrueToken

/**
 * 評価
 */
fun eval(node: Node, env: Environment): Object {
    return when (node) {
        is Program -> evalProgram(node, env)
        is LetStatement -> evalLetStatement(node, env)
        is CallExpression -> evalCallExpression(node, env)
        is FunctionLiteral -> FunctionObject(parameters = node.parameters, body = node.body, env = env)
        is Identifier -> evalIdentifier(node, env)
        is BlockStatement -> evalBlockStatements(node.statements, env)
        is IfExpression -> evalIfExpression(node, env)
        is PrefixExpression -> evalPrefixExpression(node, env)
        is InfixExpression -> evalInfixExpression(node, env)
        is ExpressionStatement -> eval(node.expression, env)
        is IntegerLiteral -> IntegerObject(value = node.value)
        is BooleanExpression -> nativeBoolToBooleanObject(input = node.bool)
        is StringLiteral -> StringObject(value = node.value)
        is HashLiteral -> evalHashLiteral(node, env)
        is ReturnStatement -> evalReturnStatement(node, env)
        is ArrayLiteral -> evalArrayLiteral(node, env)
        is IndexExpression -> evalIndexExpression(node, env)
        else -> throw NotImplementedError("literal:${node.inspect()} , type:${node::class}")
    }
}

fun evalCallExpression(callExpression: CallExpression, env: Environment): Object {
    if (callExpression.function.tokenLiteral() == "quote") {
        return quote(callExpression.arguments[0], env)
    }

    val function = eval(callExpression.function, env)
    if (function is ErrorObject) {
        return function
    }
    val args = evalExpressions(callExpression.arguments, env)
    if (args.size == 1 && args[0] is ErrorObject) {
        return args[0]
    }
    return applyFunction(function, args)
}

fun evalPrefixExpression(prefixExpression: PrefixExpression, env: Environment): Object {
    val right = eval(prefixExpression.right, env)
    if (right is ErrorObject) {
        return right
    }

    return when (val operator = prefixExpression.operator) {
        "!" -> evalBangOperatorExpression(right)
        "-" -> evalMinusPrefixOperatorExpression(right)
        else -> ErrorObject("unknown operator: $operator${right.type}")
    }
}

fun evalLetStatement(letStatement: LetStatement, env: Environment): Object {
    val value = eval(letStatement.value, env)
    if (value is ErrorObject) {
        return value
    }
    env.set(letStatement.identifier, value)
    return value
}

fun evalArrayLiteral(arrayLiteral: ArrayLiteral, env: Environment): Object {
    val elements = evalExpressions(arrayLiteral.elements, env)
    if (elements.size == 1 && elements.first() is ErrorObject) {
        return elements.first()
    }

    return ArrayObject(elements = elements)
}

fun evalInfixExpression(infixExpression: InfixExpression, env: Environment): Object {
    val left = eval(infixExpression.left, env)
    if (left is ErrorObject) {
        return left
    }
    val right = eval(infixExpression.right, env)
    if (right is ErrorObject) {
        return right
    }
    val operator = infixExpression.operator
    return when {
        left is IntegerObject && right is IntegerObject -> evalIntegerInfixExpression(
            left,
            operator,
            right
        )
        left is BooleanObject && right is BooleanObject -> evalBooleanInfixExpression(
            left,
            operator,
            right
        )
        left is StringObject && right is StringObject -> evalStringInfixExpression(
            left,
            operator,
            right
        )
        left.type != right.type -> ErrorObject("type mismatch: ${left.type} $operator ${right.type}")
        else -> ErrorObject("unknown operator: ${left.type} $operator ${right.type}")
    }
}

fun evalReturnStatement(returnStatement: ReturnStatement, env: Environment): Object {
    val returnValue = if (returnStatement.returnValue != null) {
        eval(returnStatement.returnValue, env)
    } else {
        NullObject
    }
    if (returnValue is ErrorObject) {
        return returnValue
    }
    return ReturnValueObject(returnValue)
}

fun evalIndexExpression(indexExpression: IndexExpression, env: Environment): Object {
    val left = eval(indexExpression.left, env)
    if (left is ErrorObject) {
        return left
    }

    val index = eval(indexExpression.index, env)
    if (index is ErrorObject) {
        return index
    }

    return when {
        left is ArrayObject && index is IntegerObject -> {
            evalArrayIndexExpression(left, index) ?: NullObject
        }
        left is HashObject -> {
            evalHashIndexExpression(left, index) ?: NullObject
        }
        else -> ErrorObject("index operator not supported: ${left.type}")
    }
}

fun quote(node: Node, env: Environment): Object {

    return QuoteObject(node = evalUnquoteCalls(node, env))
}

fun evalUnquoteCalls(quoted: Node, env: Environment): Node {
    return modify(quoted) { node: Node ->
        if (!isUnquoteCall(node)) {
            return@modify node
        }

        val call = node as? CallExpression ?: return@modify node

        if (call.arguments.size != 1) {
            return@modify node
        }

        val unquoted = eval(call.arguments.first(), env)
        return@modify convertObjectToASTNode(unquoted)
    }
}

fun convertObjectToASTNode(obj: Object): Node {
    return when (obj) {
        is IntegerObject -> IntegerLiteral(token = IntToken(obj.value.toString()), value = obj.value)
        is BooleanObject -> {
            val token = if (obj.value) {
                TrueToken
            } else {
                FalseToken
            }
            BooleanExpression(token = token)
        }
        is QuoteObject -> {
            obj.node
        }
        else -> throw NotImplementedError("Not Implement type:${obj.type}")
    }
}

fun isUnquoteCall(node: Node): Boolean {
    val callExpression = node as? CallExpression ?: return false

    return callExpression.function.tokenLiteral() == "unquote"
}

fun evalHashLiteral(node: HashLiteral, env: Environment): Object {
    val map = mutableMapOf<Hashable, Object>()

    node.map.forEach { (keyExpression, valueExpression) ->
        val key = eval(keyExpression, env)
        if (key is ErrorObject) {
            return key
        }

        if (key !is Hashable) {
            return ErrorObject("unusable as hash key: ${key.type}")
        }

        val value = eval(valueExpression, env)
        if (value is ErrorObject) {
            return value
        }

        map[key] = value
    }

    return HashObject(map = map.toMap())
}

fun evalHashIndexExpression(left: HashObject, index: Object): Object? {

    if (index !is Hashable) {
        return ErrorObject("unusable as hash key: ${index.type}")
    }

    return left.map[index]
}

fun evalArrayIndexExpression(left: ArrayObject, index: IntegerObject): Object? {
    return left.elements.getOrNull(index.value)
}

fun applyFunction(function: Object, args: Array<Object>): Object {
    return when (function) {
        is FunctionObject -> {
            val extendedEnv = extendFunctionEnv(function, args)
            val evaluated = eval(function.body, extendedEnv)
            return unwrapReturnValue(evaluated)
        }
        is BuiltinObject -> {
            return function.builtinFunction(*args)
        }
        else -> ErrorObject("not a function: ${function.type}")
    }
}

fun unwrapReturnValue(`object`: Object): Object {
    return if (`object` is ReturnValueObject) {
        return `object`.value
    } else {
        `object`
    }
}

fun extendFunctionEnv(function: FunctionObject, args: Array<Object>): Environment {
    return Environment(function.env).apply {
        function.parameters.forEachIndexed { index, param -> this.set(param, args[index]) }
    }
}

fun evalExpressions(arguments: List<Expression>, env: Environment): Array<Object> {
    var result: Array<Object> = emptyArray()
    for (argument in arguments) {
        val evaluated = eval(argument, env)
        if (evaluated is ErrorObject) {
            return arrayOf(evaluated)
        }
        result += (evaluated)
    }
    return result
}

fun evalIdentifier(identifier: Identifier, env: Environment): Object {
    val ident = env.get(identifier)
    if (ident != null) {
        return ident
    }

    val builtin = builtins(identifier)
    if (builtin != null) {
        return builtin
    }

    return ErrorObject("identifier not found: ${identifier.inspect()}")
}

fun evalBlockStatements(statements: List<Statement>, env: Environment): Object {
    var result: Object? = null

    for (statement in statements) {
        result = eval(statement, env)
        if (result is ReturnValueObject || result is ErrorObject) {
            return result
        }
    }
    return requireNotNull(result)
}

fun evalProgram(program: Program, env: Environment): Object {

    var result: Object? = null
    for (statement in program.statements) {
        result = eval(statement, env)

        when (result) {
            is ReturnValueObject -> return result.value
            is ErrorObject -> return result
        }
    }
    return result ?: NullObject
}

fun evalIfExpression(node: IfExpression, env: Environment): Object {
    val condition = eval(node.condition, env)
    if (condition is ErrorObject) {
        return condition
    }
    return when {
        isTruthy(condition) -> eval(node.consequence, env)
        node.alternative != null -> eval(node.alternative, env)
        else -> return NullObject
    }
}

fun isTruthy(condition: Object): Boolean {
    return when (condition) {
        is NullObject -> false
        is BooleanObject.TrueObject -> true
        is BooleanObject.FalseObject -> false
        else -> true
    }
}

// private fun evalInfixExpression(left: Object, operator: String, right: Object): Object {
//    return when {
//        left is IntegerObject && right is IntegerObject -> evalIntegerInfixExpression(
//            left,
//            operator,
//            right
//        )
//        left is BooleanObject && right is BooleanObject -> evalBooleanInfixExpression(
//            left,
//            operator,
//            right
//        )
//        left is StringObject && right is StringObject -> evalStringInfixExpression(
//            left,
//            operator,
//            right
//        )
//        left.type != right.type -> ErrorObject("type mismatch: ${left.type} $operator ${right.type}")
//        else -> ErrorObject("unknown operator: ${left.type} $operator ${right.type}")
//    }
// }

fun evalStringInfixExpression(left: StringObject, operator: String, right: StringObject): Object {
    return when (operator) {
        "+" -> StringObject(left.value + right.value)
        else -> ErrorObject("unknown operator: ${left.type} $operator ${right.type}")
    }
}

fun evalBooleanInfixExpression(left: BooleanObject, operator: String, right: BooleanObject): Object {
    return when (operator) {
        "==" -> nativeBoolToBooleanObject(left == right)
        "!=" -> nativeBoolToBooleanObject(left != right)
        else -> ErrorObject("unknown operator: ${ObjectType.BOOLEAN} $operator ${ObjectType.BOOLEAN}")
    }
}

fun evalIntegerInfixExpression(left: IntegerObject, operator: String, right: IntegerObject): Object {
    return when (operator) {
        "+" -> IntegerObject(left.value + right.value)
        "-" -> IntegerObject(left.value - right.value)
        "*" -> IntegerObject(left.value * right.value)
        "/" -> IntegerObject(left.value / right.value)
        "<" -> nativeBoolToBooleanObject(left.value < right.value)
        ">" -> nativeBoolToBooleanObject(left.value > right.value)
        "==" -> nativeBoolToBooleanObject(left.value == right.value)
        "!=" -> nativeBoolToBooleanObject(left.value != right.value)
        else -> throw NotImplementedError("")
    }
}

private fun evalMinusPrefixOperatorExpression(right: Object): Object {
    return when (right) {
        is IntegerObject -> IntegerObject(right.value * -1)
        else -> ErrorObject("unknown operator: -${right.type}")
    }
}

private fun evalBangOperatorExpression(right: Object): Object {
    return when (right) {
        is BooleanObject.TrueObject -> BooleanObject.FalseObject
        is BooleanObject.FalseObject -> BooleanObject.TrueObject
        is NullObject -> BooleanObject.TrueObject
        else -> BooleanObject.FalseObject
    }
}

private fun nativeBoolToBooleanObject(input: Boolean): Object {
    return when (input) {
        true -> BooleanObject.TrueObject
        false -> BooleanObject.FalseObject
    }
}
