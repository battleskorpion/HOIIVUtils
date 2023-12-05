---
title: Sux4J-Based Implementations
---

# {{ page.title }}

[Sux4J](https://sux4j.di.unimi.it/) is a library containing
implementations of [succinct data structures](https://en.wikipedia.org/wiki/Succinct_data_structure)
in Java. Such structures can be used to store graphs in a very compact form. For example,
the memory footprint of the [English Wikipedia graph in 2013](http://law.di.unimi.it/webdata/enwiki-2013/)
would be a few gigabytes in a trivial object-based representation, it is 1.6GB in JGraphT's
[sparse representation](https://jgrapht.org/javadoc/org.jgrapht.opt/org/jgrapht/opt/graph/sparse/SparseIntDirectedGraph.html),
but it is just 500MB in a succinct representation. The denser the graph, the more
marked these differences will be.

The implementations in the package
[org.jgrapht.sux4j](https://jgrapht.org/javadoc/org.jgrapht.unimi.dsi/org/jgrapht/sux4j/package-summary.html)
make it possible to use succinct representation of graphs in JGraphT.
The implementations are serializable, and you can download ready-made
graphs in this form from the [LAW web site](http://law.di.unimi.it/datasets.php).

The typical use case for these adapters is:

- You need a compact format.
- You have less than 2<sup>31</sup> vertices and less than 2<sup>31</sup> edges.
- You have metadata associated with the vertices and with the edges.
- Optionally, you need fast [adjacency tests](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#containsEdge%28V,V%29).

Such metadata can be easily stored in an array or list indexed by the vertices or
the edges. If you have metadata on the vertices only, or if your number
of vertices or edges does not satisfy the limitations above, a [WebGraph
adapter](https://jgrapht.org/javadoc/org.jgrapht.unimi.dsi/org/jgrapht/webgraph/package-summary.html)
might be more appropriate.  A separate [guide](WebGraphAdapters) is available for 
WebGraph adapters.

The two main implementations are [`SuccinctDirectedGraph`](https://jgrapht.org/javadoc/org.jgrapht.unimi.dsi/org/jgrapht/sux4j/SuccinctDirectedGraph.html)
and [`SuccinctUndirectedGraph`](https://jgrapht.org/javadoc/org.jgrapht.unimi.dsi/org/jgrapht/sux4j/SuccinctUndirectedGraph.html).
They both use pairs to represent edges, but you can easily [map edges](https://jgrapht.org/javadoc/org.jgrapht.unimi.dsi/org/jgrapht/sux4j/SuccinctDirectedGraph.html#getEdgeFromIndex%28long%29) 
in a contiguous segment of integers starting from zero; the mapping is reasonably fast.

Note that one of the benefits of the succinct representation used by these classes is that
[adjacency tests](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#containsEdge%28V,V%29) are very fast.

If you need, however, an implementation whose vertex and edge type is
`Integer` (for example, for usage with [Python
bindings](https://pypi.org/project/jgrapht/)), there are classes
[`SuccinctIntDirectedGraph`](https://jgrapht.org/javadoc/org.jgrapht.unimi.dsi/org/jgrapht/sux4j/SuccinctIntDirectedGraph.html)
and
[`SuccinctIntUndirectedGraph`](https://jgrapht.org/javadoc/org.jgrapht.unimi.dsi/org/jgrapht/sux4j/SuccinctIntUndirectedGraph.html).
These classes, however, are fairly slow due to the necessity of
continuously remapping edges from pairs to indices. We suggest that you use them
only in the directed case and for outgoing arcs. However, in some cases
they might provide the only representation of this type that is small
enough to be loaded in main memory.
