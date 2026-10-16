package kz.aitu.daa.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Random;
import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class TopoTest {

    @Test
    void validOrderOnRandomDags() {
        Random rnd = new Random(29);
        for (int iter = 0; iter < 200; iter++) {
            int n = 2 + rnd.nextInt(200);
            Graph graph = randomDag(n, rnd);
            KahnResult result = Kahn.run(graph, new Metrics());
            assertFalse(result.hasCycle(), "iteration " + iter);
            assertEquals(n, result.verticesInOrder());
            int[] pos = new int[n];
            for (int i = 0; i < result.verticesInOrder(); i++) {
                pos[result.order()[i]] = i;
            }
            for (int u = 0; u < n; u++) {
                int degree = graph.degree(u);
                for (int i = 0; i < degree; i++) {
                    assertTrue(pos[u] < pos[graph.neighbor(u, i)],
                            "edge " + u + " -> " + graph.neighbor(u, i) + " in iteration " + iter);
                }
            }
        }
    }

    @Test
    void cycleDetectedOnCyclicGraph() {
        Graph graph = Graph.read(Path.of("data", "cyclic.txt"));
        KahnResult result = Kahn.run(graph, new Metrics());
        assertTrue(result.hasCycle());
        assertTrue(result.verticesInOrder() < graph.vertexCount());
    }

    @Test
    void cycleDetectedOnSelfLoop() {
        Graph graph = Graph.builder(2, true)
                .addEdge(0, 0, 5)
                .addEdge(0, 1, 1)
                .build();
        KahnResult result = Kahn.run(graph, new Metrics());
        assertTrue(result.hasCycle());
    }

    @Test
    void acyclicGraphWithoutEdgesIsFullyOrdered() {
        Graph graph = Graph.builder(5, true).build();
        KahnResult result = Kahn.run(graph, new Metrics());
        assertFalse(result.hasCycle());
        assertEquals(5, result.verticesInOrder());
        assertEquals(0, kahnMetric(graph).edgeScans());
    }

    private static Metrics kahnMetric(Graph graph) {
        Metrics metrics = new Metrics();
        Kahn.run(graph, metrics);
        return metrics;
    }

    private static Graph randomDag(int n, Random rnd) {
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
            builder.addEdge(a, b, rnd.nextInt(101));
        }
        return builder.build();
    }
}
