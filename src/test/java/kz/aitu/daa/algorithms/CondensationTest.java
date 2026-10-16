package kz.aitu.daa.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Random;
import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class CondensationTest {

    @Test
    void condensationMatchesKosarajuPartitionOnRandomGraphs() {
        Random rnd = new Random(41);
        for (int iter = 0; iter < 200; iter++) {
            int n = 1 + rnd.nextInt(150);
            int m = rnd.nextInt(4 * n + 1);
            Graph graph = randomDirected(n, m, rnd);
            SccResult kosaraju = Kosaraju.run(graph, new Metrics());
            CondensationResult result = Condensation.run(graph, new Metrics());
            assertEquals(kosaraju.componentCount(), result.componentCount(), "iteration " + iter);
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    assertEquals(kosaraju.componentOf()[u] == kosaraju.componentOf()[v],
                            result.componentOf()[u] == result.componentOf()[v],
                            "components of " + u + " and " + v + " in iteration " + iter);
                }
            }
            assertTrue(result.criticalPathLength() >= 0);
        }
    }

    @Test
    void criticalPathMatchesBruteForceOnSmallPositiveGraphs() {
        Random rnd = new Random(43);
        for (int iter = 0; iter < 200; iter++) {
            int n = 2 + rnd.nextInt(8);
            Graph graph = randomDirectedPositive(n, rnd);
            CondensationResult result = Condensation.run(graph, new Metrics());
            long expected = bruteForceCriticalPath(graph, result.componentOf(), result.componentCount());
            assertEquals(expected, result.criticalPathLength(), "iteration " + iter);
            assertPathIsConsistent(graph, result);
        }
    }

    @Test
    void smallExampleCriticalPath() {
        Graph graph = Graph.builder(5, true)
                .addEdge(0, 1, 5)
                .addEdge(1, 2, 5)
                .addEdge(2, 0, 5)
                .addEdge(1, 3, 10)
                .addEdge(3, 4, 1)
                .build();
        CondensationResult result = Condensation.run(graph, new Metrics());
        assertEquals(3, result.componentCount());
        assertEquals(11, result.criticalPathLength());
        int[] path = result.criticalPath();
        assertEquals(3, path.length);
        assertEquals(result.componentOf()[0], path[0]);
        assertEquals(result.componentOf()[3], path[1]);
        assertEquals(result.componentOf()[4], path[2]);
    }

    @Test
    void cyclicFileHasNoCriticalPath() {
        Graph graph = Graph.read(Path.of("data", "cyclic.txt"));
        CondensationResult result = Condensation.run(graph, new Metrics());
        assertEquals(2, result.componentCount());
        assertEquals(0, result.criticalPathLength());
        assertEquals(1, result.criticalPath().length);
    }

    @Test
    void emptyGraphHasEmptyCondensation() {
        Graph empty = Graph.builder(0, true).build();
        CondensationResult result = Condensation.run(empty, new Metrics());
        assertEquals(0, result.componentCount());
        assertEquals(0, result.criticalPathLength());
        assertEquals(0, result.criticalPath().length);
    }

    private static void assertPathIsConsistent(Graph graph, CondensationResult result) {
        int[] path = result.criticalPath();
        assertTrue(path.length >= 1);
        long total = 0;
        for (int i = 1; i < path.length; i++) {
            long weight = maxEdgeWeight(graph, result.componentOf(), path[i - 1], path[i]);
            assertTrue(weight != Long.MIN_VALUE, "path uses a missing condensation edge");
            total += weight;
        }
        assertEquals(result.criticalPathLength(), total);
    }

    private static long maxEdgeWeight(Graph graph, int[] componentOf, int from, int to) {
        long best = Long.MIN_VALUE;
        for (int i = 0; i < graph.edgeCount(); i++) {
            if (componentOf[graph.edgeU()[i]] == from && componentOf[graph.edgeV()[i]] == to) {
                best = Math.max(best, graph.edgeW()[i]);
            }
        }
        return best;
    }

    private static long bruteForceCriticalPath(Graph graph, int[] componentOf, int componentCount) {
        long[][] weight = new long[componentCount][componentCount];
        boolean[][] exists = new boolean[componentCount][componentCount];
        int[] indegree = new int[componentCount];
        for (int i = 0; i < graph.edgeCount(); i++) {
            int cu = componentOf[graph.edgeU()[i]];
            int cv = componentOf[graph.edgeV()[i]];
            if (cu == cv) {
                continue;
            }
            if (!exists[cu][cv]) {
                exists[cu][cv] = true;
                indegree[cv]++;
            }
            weight[cu][cv] = Math.max(weight[cu][cv], graph.edgeW()[i]);
        }
        long best = 0;
        for (int s = 0; s < componentCount; s++) {
            if (indegree[s] == 0) {
                best = Math.max(best, longestFrom(weight, exists, s));
            }
        }
        return best;
    }

    private static long longestFrom(long[][] weight, boolean[][] exists, int u) {
        long best = 0;
        for (int v = 0; v < weight.length; v++) {
            if (exists[u][v]) {
                best = Math.max(best, weight[u][v] + longestFrom(weight, exists, v));
            }
        }
        return best;
    }

    private static Graph randomDirected(int n, int m, Random rnd) {
        Graph.Builder builder = Graph.builder(n, true);
        for (int i = 0; i < m; i++) {
            builder.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(100));
        }
        return builder.build();
    }

    private static Graph randomDirectedPositive(int n, Random rnd) {
        Graph.Builder builder = Graph.builder(n, true);
        int m = 1 + rnd.nextInt(3 * n);
        for (int i = 0; i < m; i++) {
            builder.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(30));
        }
        return builder.build();
    }
}
