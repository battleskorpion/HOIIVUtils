//package com.hoi4utils.clausewitz_parser;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Consumer;
//import java.util.function.Function;
//import java.util.function.Predicate;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//// todo T extends node? or no.
///**
// * The NodeStream class represents a stream of nodes.
// *
// * <p>
// * A NodeStream is an iterable collection of nodes that can be traversed
// * sequentially or in parallel. It provides methods for filtering, mapping,
// * and reducing the nodes in the stream.
// *
// * <p>
// * NodeStreams are typically created from a source of data, such as a file or
// * a network connection, and can be used to process and transform the data in
// * a streaming fashion.
// *
// * <p>
// * Here's an example of how to create a NodeStream from a list of integers
// * and filter out the even numbers:
// *
// * <pre>
// * {@code
// * List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);
// * NodeStream<Integer> stream = new NodeStream<>(numbers);
// * NodeStream<Integer> evenNumbers = stream.filter(n -> n % 2 == 0);
// * }
// * </pre>
// *
// * @param <T> the type of nodes in the stream
// * @see Node
// */
//public class NodeStream<T extends Node> implements NodeStreamable<T> {
//	public Stream<T> stream;
//
//	public NodeStream(Stream<T> stream) {
//		this.stream = stream;
//	}
//
//	/**
//	 * Creates a new NodeStream from a single node.
//	 *
//	 * If the node's value is an ArrayList, it will be flattened into the
//	 * stream. If the node's value is a Node, it will be added to the stream.
//	 *
//	 * @param nodeToStream the node to stream
//	 */
//	public NodeStream(T nodeToStream) {
//		this(Stream.of(nodeToStream));
//
//		// todo necessary etc.?
//		if (nodeToStream.valueObject() instanceof ArrayList<?>) {
//			stream = stream.flatMap(node -> ((ArrayList<T>) node.valueObject()).stream()); // ! Unchecked cast
//		} else if (nodeToStream.valueObject() instanceof Node) {
//			// stream = concat(stream, ((NodeType) nodeToStream.value()).stream());
//			stream = Stream.concat(stream, Stream.of((T) nodeToStream.valueObject())); // ! Unchecked cast
//		}
//	}
//
//	private Stream<T> concat(Stream<T> stream1, Stream<T> stream2) {
//		return Stream.concat(stream1, stream2);
//	}
//
//	private Stream<T> concat(NodeStream<T> stream1, Stream<T> stream2) {
//		return Stream.concat(stream1.getStream(), stream2);
//	}
//
//	@Override
//	public NodeStreamable<T> filter(Predicate<? super T> predicate) {
//		stream = stream.filter(predicate);
//		return this;
//	}
//
//	@Override
//	public <R extends Node> NodeStreamable<R> map(Function<? super T, ? extends R> mapper) {
//		return new NodeStream<>(stream.map(mapper));
//	}
//
//	@Override
//	public <R extends Node> NodeStreamable<R> flatMap(Function<? super T, ? extends NodeStreamable<R>> mapper) {
//		return new NodeStream<>(stream.flatMap(item -> mapper.apply(item).getStream()));
//	}
//
//	public Stream<T> getStream() {
//		return stream;
//	}
//
//	@Override
//	public List<T> toList() {
//		return stream.collect(Collectors.toList());
//	}
//
//	@Override
//	public void forEach(Consumer<? super T> action) {
//		stream.forEach(action);
//	}
//
//	@Override
//	public T findFirst() {
//		return stream.findFirst().orElse(null);
//	}
//
//	@Override
//	public T findFirst(Predicate<T> predicate) {
//		return stream
//				.filter(predicate) // Apply the predicate
//				.findFirst()
//				.orElse(null);
//	}
//
//	@Override
//	public T findFirst(String str) {
//		return findFirst(node -> node.identifier != null && node.identifier.equals(str));
//	}
//
//	@Override
//	public NodeStreamable<T> filterName(String str) {
//		return filter(node -> node.identifier != null && node.identifier.equals(str));
//	}
//
//	@Override
//	public NodeStreamable<T> filter(String str) {
//		return filterName(str);
//	}
//
//	@Override
//	public boolean anyMatch(Predicate<? super T> predicate) {
//		return stream.anyMatch(predicate);
//	}
//}
