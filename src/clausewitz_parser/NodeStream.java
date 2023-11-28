package clausewitz_parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// todo T extends node? or no.
public class NodeStream<NodeType extends Node> implements NodeStreamable<NodeType> {
	public Stream<NodeType> stream;

	public NodeStream(Stream<NodeType> stream) {
		this.stream = stream;
	}

	public NodeStream(NodeType nodeToStream) {
		this(Stream.of(nodeToStream));

		// todo necessary etc.?
		if (nodeToStream.valueObject() instanceof ArrayList<?>) {
			stream = stream.flatMap(node -> ((ArrayList<NodeType>) node.valueObject()).stream());
		} else if (nodeToStream.valueObject() instanceof Node) {
//			stream = concat(stream, ((NodeType) nodeToStream.value()).stream());
			stream = Stream.concat(stream, Stream.of((NodeType) nodeToStream.valueObject()));
		}
	}

	private Stream<NodeType> concat(Stream<NodeType> stream1, Stream<NodeType> stream2) {
		return Stream.concat(stream1, stream2);
	}

	private Stream<NodeType> concat(NodeStream<NodeType> stream1, Stream<NodeType> stream2) {
		return Stream.concat(stream1.getStream(), stream2);
	}

	@Override
	public NodeStreamable<NodeType> filter(Predicate<? super NodeType> predicate) {
		stream = stream.filter(predicate);
		return this;
	}

	@Override
	public <R extends Node> NodeStreamable<R> map(Function<? super NodeType, ? extends R> mapper) {
		return new NodeStream<>(stream.map(mapper));
	}

	@Override
	public <R extends Node> NodeStreamable<R> flatMap(Function<? super NodeType, ? extends NodeStreamable<R>> mapper) {
		return new NodeStream<>(stream.flatMap(item -> mapper.apply(item).getStream()));
	}

	public Stream<NodeType> getStream() {
		return stream;
	}

	@Override
	public List<NodeType> toList() {
		return stream.collect(Collectors.toList());
	}

	@Override
	public void forEach(Consumer<? super NodeType> action) {
		stream.forEach(action);
	}

	@Override
	public NodeType findFirst() {
		return stream.findFirst().orElse(null);
	}

	@Override
	public NodeType findFirst(Predicate<NodeType> predicate) {
		return stream
				.filter(predicate) // Apply the predicate
				.findFirst()
				.orElse(null);
	}

	@Override
	public NodeType findFirst(String str) {
		return findFirst(node -> node.name.equals(str));
	}

	@Override
	public NodeStreamable<NodeType> filterName(String str) {
		return filter(node -> node.name.equals(str));
	}

	@Override
	public NodeStreamable<NodeType> filter(String str) {
		return filterName(str);
	}
}
