// File: src/main/kotlin/com/hoi4utils/clausewitz_parser/NodeValue.kt
package main.kotlin.com.hoi4utils.clausewitz_parser

import main.java.com.hoi4utils.clauzewitz.BoolType

class NodeValue<T>(private val value: T?) {

    fun valueObject(): T? {
        return value
    }

    fun asString(): String {
        return value.toString()
    }

    fun isList(): Boolean {
        return value is List<*>
    }

    fun isString(): Boolean {
        return value is String
    }

    fun isNumber(): Boolean {
        return value is Number
    }
}
