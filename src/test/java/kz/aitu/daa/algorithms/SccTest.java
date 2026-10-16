package kz.aitu.daa.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;
import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class SccTest {

    @Test
    void kosarajuMatchesBruteForceOnRandomGraphs() {
        Random rnd = new Random(13);
        for (int iter = 0; iter < 300; iter++) {
            int n = 1 + rnd.nextInt(200);
            int m = rnd.nextInt(4 * n + 1);
            Graph graph = randomDirected(n, m, rnd);
            SccResult result = Kosaraju.run(graph, new Metrics());
            int[] brute = bruteForceComponents(graph);
            assertEquals(distinctCount(brute), result.componentCount(), "iteration " + iter);
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    assertEquals(brute[u] == brute[v], result.componentOf()[u] == result.componentOf()[v],
                            "components of " + u + " and " + v + " in iteration " + iter);
                }
            }
        }
    }

    @Test
    void smallCyclicGraphHasTwoComponents() {
        Graph graph = Graph.read(Path.of("data", "cyclic.txt"));
        SccResult result = Kosaraju.run(graph, new Metrics());
        assertEquals(2, result.componentCount());
        assertEquals(result.componentOf()[0], result.componentOf()[1]);
        assertEquals(result.componentOf()[1], result.componentOf()[2]);
        assertEquals(result.componentOf()[3], result.componentOf()[4]);
        assertNotEquals(result.componentOf()[0], result.componentOf()[3]);
    }

    @Test
    void selfLoopAndIsolatedVertexAreTheirOwnComponents() {
        Graph graph = Graph.builder(4, true)
                .addEdge(0, 0, 1)
                .addEdge(2, 3, 1)
                .addEdge(3, 2, 1)
                .build();
        SccResult result = Kosaraju.run(graph, new Metrics());
        assertEquals(3, result.componentCount());
        assertEquals(result.componentOf()[2], result.componentOf()[3]);
        assertNotEquals(result.componentOf()[0], result.componentOf()[1]);
        assertNotEquals(result.componentOf()[1], result.componentOf()[2]);
    }

    @Test
    void iterativeDfsSurvivesPathOfHundredThousandVertices() {
        int n = 100_000;
        Graph.Builder builder = Graph.builder(n, true);
        for (int v = 0; v + 1 < n; v++) {
            builder.addEdge(v, v + 1, 1);
        }
        Graph graph = builder.build();
        Metrics metrics = new Metrics();
        SccResult result = Kosaraju.run(graph, metrics);
        assertEquals(n, result.componentCount());
        assertEquals(n, metrics.maxDepth());
        assertEquals(2L * (n - 1), metrics.edgeScans());
    }

    private static Graph randomDirected(int n, int m, Random rnd) {
        Graph.Builder builder = Graph.builder(n, true);
        for (int i = 0; i < m; i++) {
            builder.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(100));
        }
        return builder.build();
    }

    private static int[] bruteForceComponents(Graph graph) {
        int n = graph.vertexCount();
        boolean[][] reach = new boolean[n][n];
        boolean[] visited = new boolean[n];
        for (int s = 0; s < n; s++) {
            Arrays.fill(visited, false);
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(s);
            visited[s] = true;
            while (!queue.isEmpty()) {
                int u = queue.poll();
                reach[s][u] = true;
                int degree = graph.degree(u);
                for (int i = 0; i < degree; i++) {
                    int v = graph.neighbor(u, i);
                    if (!visited[v]) {
                        visited[v] = true;
                        queue.add(v);
                    }
                }
            }
        }
        int[] component = new int[n];
        Arrays.fill(component, -1);
        int next = 0;
        for (int u = 0; u < n; u++) {
            if (component[u] != -1) {
                continue;
            }
            component[u] = next;
            for (int v = u + 1; v < n; v++) {
                if (component[v] == -1 && reach[u][v] && reach[v][u]) {
                    component[v] = next;
                }
            }
            next++;
        }
        return component;
    }

    private static long distinctCount(int[] labels) {
        return Arrays.stream(labels).distinct().count();
    }
}
