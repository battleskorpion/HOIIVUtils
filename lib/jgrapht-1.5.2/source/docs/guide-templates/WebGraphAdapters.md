---
title: WebGraph Adapters
---

# {{ page.title }}

[WebGraph](https://webgraph.di.unimi.it/) is a framework for storing and
accessing graphs (and in particular web graphs) in a compressed form,
making it possible to load and access very large graphs with a moderate
amount of memory. You can download ready-made graphs in this form from the
[LAW web site](http://law.di.unimi.it/datasets.php), or compress your own
graphs using the instructions provided in the [package overview](https://webgraph.di.unimi.it/docs/).

For example, the memory footprint of a [snapshot of web sites from Indochina in 2004](http://law.di.unimi.it/webdata/indochina-2004/)
with 200 million edges would be a few gigabytes in a trivial representation, it is 260MB in JGraphT's
[sparse representation](https://jgrapht.org/javadoc/org.jgrapht.opt/org/jgrapht/opt/graph/sparse/SparseIntDirectedGraph.html),
but it is just 59MB in WebGraph.

The adapters in the package
[org.jgrapht.webgraph](https://jgrapht.org/javadoc/org.jgrapht.unimi.dsi/org/jgrapht/webgraph/package-summary.html)
make it possible to use graphs in WebGraph format in JGraphT.

The typical use case for these adapters is:

- You need a compact format (vertices will be just contiguous integers starting from zero).
- The type of graph you are storing is compressible.
- You have metadata associated with the vertices, but not with the arcs.

Such metadata can be easily stored in an array indexed by the vertices,
or possibly by a [`fastutil` big array](https://fastutil.di.unimi.it/docs/it/unimi/dsi/fastutil/BigArrays.html)
if you have more than 2<sup>31</sup> vertices (lists and [big lists](https://fastutil.di.unimi.it/docs/it/unimi/dsi/fastutil/BigList.html) are another option).

If you need to associate metadata with the arcs, and manage the graph in a
compact format, a succinct representation from the package
[org.jgrapht.sux4j](https://jgrapht.org/javadoc/org.jgrapht.unimi.dsi/org/jgrapht/sux4j/package-summary.html)
might be more appropriate, as those representation associate with
each edge an integer in a contiguous range starting from zero.
A separate [guide](Sux4JImplementations) is available for 
succinct graph adapters.

WebGraph has two versions: the standard version manages graph with
at most 2<sup>31</sup> vertices, whereas the big version manages graphs with
at most 2<sup>63</sup> vertices. For each version, there is a directed
adapter and an undirected adapter. The Javadoc documentation of
[`ImmutableDirectedGraphAdapter`](https://jgrapht.org/javadoc/org.jgrapht.unimi.dsi/org/jgrapht/webgraph/ImmutableDirectedGraphAdapter.html)
and [`ImmutableUndirectedGraphAdapter`](https://jgrapht.org/javadoc/org.jgrapht.unimi.dsi/org/jgrapht/webgraph/ImmutableUndirectedGraphAdapter.html)
contain examples of how to load graphs in webgraph and use them
to build an adapter. The big adapters work in the same way.

Note that WebGraph has two main representations:
[`BVGraph`](https://webgraph.di.unimi.it/docs/it/unimi/dsi/webgraph/BVGraph.html)
uses compression techniques that work well with web graphs;
[`EFGraph`](https://webgraph.di.unimi.it/docs/it/unimi/dsi/webgraph/EFGraph.html)
uses [succinct techniques](https://en.wikipedia.org/wiki/Succinct_data_structure), 
which might be more useful with less repetitive graphs such as social graphs. In particular, `EFGraph` implements a
[fast adjacency test](https://jgrapht.org/javadoc/org.jgrapht.core/org/jgrapht/Graph.html#containsEdge%28V,V%29).
You should choose the representation that better suits your data and access primitives.
