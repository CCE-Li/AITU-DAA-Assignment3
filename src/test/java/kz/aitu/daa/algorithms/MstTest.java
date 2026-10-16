package kz.aitu.daa.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Random;
import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class MstTest {

    @Test
    void kruskalAndPrimAgreeOn100RandomConnectedGraphs() {
        Random rnd = new Random(11);
        for (int iter = 0; iter < 100; iter++) {
            int n = 2 + rnd.nextInt(60);
            Graph graph = randomConnectedUndirected(n, rnd, 3 * n);
            MstResult kruskal = Kruskal.run(graph, new Metrics());
            MstResult prim = Prim.run(graph, new Metrics());
            assertEquals(kruskal.totalWeight(), prim.totalWeight(), "iteration " + iter);
            assertEquals(n - 1, kruskal.treeEdges());
            assertEquals(n - 1, prim.treeEdges());
            assertEquals(1, kruskal.components());
            assertEquals(1, prim.components());
        }
    }

    @Test
    void matchesBruteForceOnSmallGraphs() {
        Random rnd = new Random(23);
        for (int iter = 0; iter < 40; iter++) {
            int n = 3 + rnd.nextInt(4);
            Graph graph = randomConnectedUndirected(n, rnd, n);
            long expected = bruteForceMst(graph);
            assertEquals(expected, Kruskal.run(graph, new Metrics()).totalWeight(), "iteration " + iter);
            assertEquals(expected, Prim.run(graph, new Metrics()).totalWeight(), "iteration " + iter);
        }
    }

    @Test
    void disconnectedGraphReturnsForest() {
        Graph graph = Graph.read(Path.of("data", "small_disconnected.txt"));
        MstResult kruskal = Kruskal.run(graph, new Metrics());
        MstResult prim = Prim.run(graph, new Metrics());
        assertEquals(kruskal.totalWeight(), prim.totalWeight());
        assertEquals(13, kruskal.totalWeight());
        assertEquals(4, kruskal.components());
        assertEquals(4, prim.components());
        assertEquals(4, kruskal.treeEdges());
    }

    @Test
    void selfLoopsAndParallelEdgesAreHandled() {
        Graph graph = Graph.read(Path.of("data", "small_connected.txt"));
        MstResult kruskal = Kruskal.run(graph, new Metrics());
        MstResult prim = Prim.run(graph, new Metrics());
        assertEquals(kruskal.totalWeight(), prim.totalWeight());
        assertEquals(12, kruskal.totalWeight());
        assertEquals(5, kruskal.treeEdges());
        assertEquals(1, kruskal.components());
    }

    private static Graph randomConnectedUndirected(int n, Random rnd, int maxExtraEdges) {
        Graph.Builder builder = Graph.builder(n, false);
        for (int v = 1; v < n; v++) {
            builder.addEdge(rnd.nextInt(v), v, rnd.nextInt(51));
        }
        int extra = rnd.nextInt(maxExtraEdges + 1);
        for (int i = 0; i < extra; i++) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            if (a != b) {
                builder.addEdge(a, b, rnd.nextInt(51));
            }
        }
        return builder.build();
    }

    private static long bruteForceMst(Graph graph) {
        int n = graph.vertexCount();
        int edgeCount = graph.edgeCount();
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << edgeCount); mask++) {
            if (Integer.bitCount(mask) != n - 1) {
                continue;
            }
            int[] parent = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
            }
            long weight = 0;
            boolean ok = true;
            for (int i = 0; i < edgeCount && ok; i++) {
                if ((mask & (1 << i)) == 0) {
                    continue;
                }
                int ra = find(parent, graph.edgeU()[i]);
                int rb = find(parent, graph.edgeV()[i]);
                if (ra == rb) {
                    ok = false;
                } else {
                    parent[ra] = rb;
                    weight += graph.edgeW()[i];
                }
            }
            if (ok && weight < best) {
                best = weight;
            }
        }
        return best;
    }

    private static int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }
}
