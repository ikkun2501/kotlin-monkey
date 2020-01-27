package com.example.monkey.`object`

import com.example.monkey.obj.BooleanObject
import com.example.monkey.obj.IntegerObject
import com.example.monkey.obj.StringObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class ObjectTest {
    @Test
    fun testStringHashKey() {
        val hello1 = StringObject("Hello World!")
        val hello2 = StringObject("Hello World!")

        val diff1 = StringObject("My name is johnny")
        val diff2 = StringObject("My name is johnny")

        assertEquals(hello1.hashKey(), hello2.hashKey())
        assertEquals(diff1.hashKey(), diff2.hashKey())

        assertNotEquals(hello1.hashKey(), diff1.hashKey())
        assertNotEquals(hello2.hashKey(), diff2.hashKey())
    }

    @Test
    fun testIntegerHashKey() {
        val int1 = IntegerObject(value = 1)
        val int2 = IntegerObject(value = 1)
        val int3 = IntegerObject(value = 2)
        assertEquals(int1.hashKey(), int2.hashKey())
        assertNotEquals(int1.hashKey(), int3.hashKey())
    }

    @Test
    fun testBooleanHashKey() {
        val bool1 = BooleanObject.TrueObject
        val bool2 = BooleanObject.TrueObject
        val bool3 = BooleanObject.FalseObject
        val bool4 = BooleanObject.FalseObject

        assertEquals(bool1.hashKey(), bool2.hashKey())
        assertEquals(bool3.hashKey(), bool4.hashKey())
        assertNotEquals(bool1.hashKey(), bool3.hashKey())
    }
}