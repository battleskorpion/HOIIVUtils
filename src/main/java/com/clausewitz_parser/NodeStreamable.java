package clausewitz_parser;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface NodeStreamable<NodeType extends Node> {
	static <T extends Node> NodeStreamable<T> of(Stream<T> stream) {
		return new NodeStream<>(stream);
	}

	NodeStreamable<NodeType> filter(Predicate<? super NodeType> predicate);
	<R extends Node> NodeStreamable<R> map(Function<? super NodeType, ? extends R> mapper);

	<R extends Node> NodeStreamable<R> flatMap(Function<? super NodeType, ? extends NodeStreamable<R>> mapper);

	Stream<NodeType> getStream();

	List<NodeType> toList();

	void forEach(Consumer<? super NodeType> action);

	NodeType findFirst();

	NodeType findFirst(Predicate<NodeType> predicate);

	Node findFirst(String str);

	// todo filter name should filter.. a name.
	NodeStreamable<NodeType> filterName(String str);

	NodeStreamable<NodeType> filter(String str);

	default boolean contains(String str) {
		return !filter(str).toList().isEmpty();
	}
}
