package main.kotlin.com.hoi4utils.clausewitz_parser

import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream

interface NodeStreamable<NodeType : Node?> {
    fun filter(predicate: Predicate<in NodeType>?): NodeStreamable<NodeType>
    fun <R : Node?> map(mapper: Function<in NodeType, out R>?): NodeStreamable<R>
    fun <R : Node?> flatMap(mapper: Function<in NodeType, out NodeStreamable<R>>): NodeStreamable<R?>
    val stream: Stream<NodeType>
    fun toList(): List<NodeType>
    fun forEach(action: Consumer<in NodeType>?)
    fun findFirst(): NodeType
    fun findFirst(predicate: Predicate<NodeType>?): NodeType
    fun findFirst(str: String): Node?

    // todo filter name should filter.. a name.
    fun filterName(str: String): NodeStreamable<NodeType>
    fun filter(str: String): NodeStreamable<NodeType>
    operator fun contains(str: String): Boolean {
        return !filter(str).toList().isEmpty()
    }

    fun anyMatch(predicate: Predicate<in NodeType>?): Boolean

    companion object {
        fun <T : Node?> of(stream: Stream<T>): NodeStreamable<T>? {
            return NodeStream(stream)
        }
    }
}
