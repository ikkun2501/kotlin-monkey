package com.example.monkey.obj

data class HashObject(val map: Map<Hashable, Object>) : Object {

    override val type: ObjectType = ObjectType.HASH

    override val inspect: String
        get() {
            return buildString {
                append("{")
                append(map.map { (key, value) ->
                    "${key.inspect}: ${value.inspect}"
                }.joinToString(", "))
                append("}")
            }
        }
}