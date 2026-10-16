package kz.aitu.daa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class DagPathsTest {

    @Test
    void matchesMultiSourceBellmanFordIncludingNegativeWeights() {
        Random rnd = new Random(17);
        for (int iter = 0; iter < 300; iter++) {
            Graph dag = randomDagWithNegativeWeights(2 + rnd.nextInt(80), rnd);
            DagPathsResult actual = DagShortestPath.run(dag, new Metrics());
            long[] expected = multiSourceBellmanFord(dag);
            assertArrayEquals(expected, actual.distance(), "iteration " + iter);
            long sum = 0;
            for (long d : expected) {
                sum += d;
            }
            assertEquals(sum, actual.sumOfDistances());
        }
    }

    @Test
    void parentChainIsAValidPath() {
        Random rnd = new Random(19);
        for (int iter = 0; iter < 100; iter++) {
            Graph dag = randomDagWithNegativeWeights(2 + rnd.nextInt(60), rnd);
            int[] order = Kahn.run(dag, new Metrics()).order();
            DagPathsResult result = DagShortestPath.run(dag, order, new Metrics());
            for (int v = 0; v < dag.vertexCount(); v++) {
                if (result.distance()[v] == DagShortestPath.UNREACHABLE) {
                    continue;
                }
                int[] path = result.pathTo(v);
                assertTrue(path.length >= 1);
                assertEquals(v, path[path.length - 1]);
                assertEquals(-1, result.parent()[path[0]], "path must start at a source");
                long length = 0;
                for (int i = 1; i < path.length; i++) {
                    length += minEdgeWeight(dag, path[i - 1], path[i]);
                }
                assertEquals(result.distance()[v], length, "path length to " + v);
            }
        }
    }

    @Test
    void smallDagFileWithNegativeWeights() {
        Graph dag = Graph.read(Path.of("data", "small_dag.txt"));
        DagPathsResult result = DagShortestPath.run(dag, new Metrics());
        assertArrayEquals(new long[]{0, -1, 2, 4, 12, 5}, result.distance());
        assertEquals(22, result.sumOfDistances());
        assertArrayEquals(new int[]{0, 2, 3, 5}, result.pathTo(5));
    }

    @Test
    void edgesAreScannedExactlyOnce() {
        Random rnd = new Random(31);
        Graph dag = randomDagWithNegativeWeights(200, rnd);
        Metrics metrics = new Metrics();
        int[] order = Kahn.run(dag, new Metrics()).order();
        DagShortestPath.run(dag, order, metrics);
        assertEquals(dag.edgeCount(), metrics.edgeScans());
        assertEquals(dag.edgeCount(), metrics.comparisons());
    }

    private static long minEdgeWeight(Graph graph, int from, int to) {
        long best = Long.MAX_VALUE;
        int degree = graph.degree(from);
        for (int i = 0; i < degree; i++) {
            if (graph.neighbor(from, i) == to) {
                best = Math.min(best, graph.weightAt(from, i));
            }
        }
        if (best == Long.MAX_VALUE) {
            throw new AssertionError("no edge " + from + " -> " + to + " although the path claims one");
        }
        return best;
    }

    private static Graph randomDagWithNegativeWeights(int n, Random rnd) {
        Graph.Builder builder = Graph.builder(n, true);
        int m = rnd.nextInt(4 * n + 1);
        for (int i = 0; i < m; i++) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            if (a == b) {
                continue;
            }
            if (a > b) {
                int swap = a;
                a = b;
                b = swap;
            }
            builder.addEdge(a, b, -20 + rnd.nextInt(51));
        }
        return builder.build();
    }

    private static long[] multiSourceBellmanFord(Graph graph) {
        int n = graph.vertexCount();
        long[] distance = new long[n];
        Arrays.fill(distance, DagShortestPath.UNREACHABLE);
        int[] indegree = new int[n];
        for (int u = 0; u < n; u++) {
            int degree = graph.degree(u);
            for (int i = 0; i < degree; i++) {
                indegree[graph.neighbor(u, i)]++;
            }
        }
        for (int v = 0; v < n; v++) {
            if (indegree[v] == 0) {
                distance[v] = 0;
            }
        }
        for (int round = 0; round < n - 1; round++) {
            boolean changed = false;
            for (int u = 0; u < n; u++) {
                if (distance[u] == DagShortestPath.UNREACHABLE) {
                    continue;
                }
                int degree = graph.degree(u);
                for (int i = 0; i < degree; i++) {
                    int v = graph.neighbor(u, i);
                    long candidate = distance[u] + graph.weightAt(u, i);
                    if (candidate < distance[v]) {
                        distance[v] = candidate;
                        changed = true;
                    }
                }
            }
            if (!changed) {
                break;
            }
        }
        return distance;
    }
}
