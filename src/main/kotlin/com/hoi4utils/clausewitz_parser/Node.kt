package main.kotlin.com.hoi4utils.clausewitz_parser

import main.java.com.hoi4utils.clauzewitz.BoolType
import org.jetbrains.annotations.NotNull
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream

class Node(
    @JvmField var identifier: String?,
    @JvmField var operator: String?,
    value: NodeValue<*>?,
    @JvmField var nameToken: Token?,
    @JvmField var operatorToken: Token?
) : NodeStreamable<Node?>, NodeValueType {
    @NotNull
    private val value: NodeValue<*> = value ?: NodeValue(null)

    @JvmOverloads
    constructor(value: NodeValue<*>? = null) : this(null, null, value, null, null)
    constructor(value: ArrayList<Node>) : this(NodeValue(value))

    fun name(): String? {
        return identifier
    }

    fun value(): NodeValue<*> {
        return value
    }

    fun valueObject(): Any? {
        return value.valueObject()
    }

    // no clue
    fun stream(): NodeStream<Node> {
        return NodeStream(this)
    }

    override fun getStream(): Stream<Node?>? {
        return stream().stream
    }

    override fun filter(predicate: Predicate<in Node>): NodeStreamable<Node> {
        return NodeStream(this).filter(predicate)
    }

    override fun <R : Node?> map(mapper: Function<in Node, out R>): NodeStreamable<R> {
        return NodeStream(this).map(mapper)
    }

    override fun <R : Node?> flatMap(mapper: Function<in Node, out NodeStreamable<R>>): NodeStreamable<R> {
        return NodeStream(this).flatMap(mapper)
    }

    override fun toList(): List<Node> {
        return NodeStream(this).toList()
    }

    override fun forEach(action: Consumer<in Node>) {
        NodeStream(this).forEach(action)
    }

    override fun findFirst(): Node {
        return NodeStream(this).findFirst()
    }

    override fun findFirst(predicate: Predicate<Node>): Node {
        val result = NodeStream(this).findFirst(predicate)
        return result ?: Node()
    }

    // note: was findFirstName, refactored
    override fun findFirst(str: String): Node {
        val result = NodeStream(this).findFirst(str)
        return result ?: Node()
    }

    override fun filterName(str: String): NodeStreamable<Node?>? {
        return NodeStream(this).filterName(str)
    }

    override fun filter(str: String): NodeStreamable<Node?>? {
        return filterName(str)
    }

    override fun anyMatch(predicate: Predicate<in Node>): Boolean {
        return NodeStream(this).anyMatch(predicate)
    }

    fun getValue(id: String): NodeValue<*> {
        return findFirst(id).value
    }

    val isParent: Boolean
        get() = value().isList()

    fun valueIsNull(): Boolean {
        return value().valueObject() == null
    }

    override fun toString(): String {
        return identifier + operator + value.asString() // todo
    }

    fun nameAsInteger(): Int {
        return identifier!!.toInt()
    }

    fun nameEquals(s: String): Boolean {
        return if (identifier == null) {
            false
        } else identifier == s
    }

    companion object {
        var boolType: BoolType? = null
    }
}