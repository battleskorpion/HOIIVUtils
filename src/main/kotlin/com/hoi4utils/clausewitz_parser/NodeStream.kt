package main.kotlin.com.hoi4utils.clausewitz_parser

import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream

// todo T extends node? or no.
/**
 * The NodeStream class represents a stream of nodes.
 *
 *
 *
 * A NodeStream is an iterable collection of nodes that can be traversed
 * sequentially or in parallel. It provides methods for filtering, mapping,
 * and reducing the nodes in the stream.
 *
 *
 *
 * NodeStreams are typically created from a source of data, such as a file or
 * a network connection, and can be used to process and transform the data in
 * a streaming fashion.
 *
 *
 *
 * Here's an example of how to create a NodeStream from a list of integers
 * and filter out the even numbers:
 *
 * <pre>
 * `List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);
 * NodeStream<Integer> stream = new NodeStream<>(numbers);
 * NodeStream<Integer> evenNumbers = stream.filter(n -> n % 2 == 0);
` *
</pre> *
 *
 * @param <T> the type of nodes in the stream
 * @see Node
</T> */
class NodeStream<T : Node?>(stream: Stream<T>) : NodeStreamable<T?> {
    override var stream: Stream<T?>

    init {
        this.stream = stream
    }

    /**
     * Creates a new NodeStream from a single node.
     *
     * If the node's value is an ArrayList, it will be flattened into the
     * stream. If the node's value is a Node, it will be added to the stream.
     *
     * @param nodeToStream the node to stream
     */
    constructor(nodeToStream: T) : this(Stream.of<T>(nodeToStream)) {

        // todo necessary etc.?
        if (nodeToStream!!.valueObject() is ArrayList<*>) {
            stream =
                stream.flatMap { node: T? -> (node!!.valueObject() as ArrayList<T?>?)!!.stream() } // ! Unchecked cast
        } else if (nodeToStream.valueObject() is Node) {
            // stream = concat(stream, ((NodeType) nodeToStream.value()).stream());
            stream = Stream.concat(
                stream, Stream.of(
                    nodeToStream.valueObject() as T?
                )
            ) // ! Unchecked cast
        }
    }

    private fun concat(stream1: Stream<T>, stream2: Stream<T>): Stream<T> {
        return Stream.concat(stream1, stream2)
    }

    private fun concat(stream1: NodeStream<T>, stream2: Stream<T>): Stream<T> {
        return Stream.concat(stream1.stream, stream2)
    }

    override fun filter(predicate: Predicate<in T?>?): NodeStreamable<T?> {
        stream = stream.filter(predicate)
        return this
    }

    override fun <R : Node?> map(mapper: Function<in T?, out R>?): NodeStreamable<R> {
        return NodeStream(stream.map(mapper))
    }

    override fun <R : Node?> flatMap(mapper: Function<in T?, out NodeStreamable<R>>): NodeStreamable<R?> {
        return NodeStream(stream.flatMap { item: T? -> mapper.apply(item).getStream() })
    }

    override fun toList(): List<T?> {
        return stream.collect(Collectors.toList())
    }

    override fun forEach(action: Consumer<in T?>?) {
        stream.forEach(action)
    }

    override fun findFirst(): T? {
        return stream.findFirst().orElse(null)
    }

    override fun findFirst(predicate: Predicate<T?>?): T? {
        return stream
            .filter(predicate) // Apply the predicate
            .findFirst()
            .orElse(null)
    }

    override fun findFirst(str: String): T? {
        return findFirst { node: T? -> node!!.identifier != null && node.identifier == str }
    }

    override fun filterName(str: String): NodeStreamable<T?> {
        return filter { node: T? -> node!!.identifier != null && node.identifier == str }
    }

    override fun filter(str: String): NodeStreamable<T?> {
        return filterName(str)
    }

    override fun anyMatch(predicate: Predicate<in T?>?): Boolean {
        return stream.anyMatch(predicate)
    }
}
