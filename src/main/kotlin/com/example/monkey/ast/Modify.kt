package com.example.monkey.ast

/**
 *
 */
fun modify(node: Node, modifier: (Node) -> Node): Node {
    return when (node) {
        is Program -> {
            Program(node.statements.map { modify(it, modifier) as Statement }.toMutableList())
        }
        is ExpressionStatement -> ExpressionStatement(
            token = node.token,
            expression = modify(node.expression, modifier) as Expression
        )
        is BlockStatement -> BlockStatement(
            token = node.token,
            statements = node.statements.map { modify(it, modifier) as Statement }
        )
        is InfixExpression -> InfixExpression(
            token = node.token,
            left = modify(node.left, modifier) as Expression,
            operator = node.operator,
            right = modify(node.right, modifier) as Expression
        )
        is PrefixExpression -> PrefixExpression(
            operator = node.operator,
            right = modify(node.right, modifier) as Expression,
            token = node.token
        )
        is IndexExpression -> IndexExpression(
            token = node.token,
            left = modify(node.left, modifier) as Expression,
            index = modify(node.index, modifier) as Expression
        )
        is IfExpression -> IfExpression(
            token = node.token,
            condition = modify(node.condition, modifier) as Expression,
            alternative = node.alternative?.let { modify(node.alternative, modifier) as BlockStatement },
            consequence = modify(node.consequence, modifier) as BlockStatement
        )
        is ReturnStatement -> ReturnStatement(
            returnValue = node.returnValue?.let { modify(it, modifier) as Expression }
        )
        is LetStatement -> LetStatement(
            identifier = node.identifier,
            value = modify(node.value, modifier) as Expression
        )
        is FunctionLiteral -> node.copy(
            body = node.body.copy(statements = node.body.statements.map { modify(it, modifier) as Statement })
        )
        is ArrayLiteral -> node.copy(
            elements = node.elements.map { modify(it, modifier) as Expression }
        )
        is HashLiteral -> node.copy(
            map = node.map
                .mapKeys { (key, _) -> modify(key, modifier) as Expression }
                .mapValues { (_, value) -> modify(value, modifier) as Expression }
        )
        else -> modifier(node)
    }
}
