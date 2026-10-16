package kz.aitu.daa.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Random;
import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class TarjanTest {

    @Test
    void matchesKosarajuOnRandomGraphs() {
        Random rnd = new Random(37);
        for (int iter = 0; iter < 300; iter++) {
            int n = 1 + rnd.nextInt(200);
            int m = rnd.nextInt(4 * n + 1);
            Graph graph = randomDirected(n, m, rnd);
            SccResult kosaraju = Kosaraju.run(graph, new Metrics());
            SccResult tarjan = Tarjan.run(graph, new Metrics());
            assertEquals(kosaraju.componentCount(), tarjan.componentCount(), "iteration " + iter);
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    assertEquals(kosaraju.componentOf()[u] == kosaraju.componentOf()[v],
                            tarjan.componentOf()[u] == tarjan.componentOf()[v],
                            "components of " + u + " and " + v + " in iteration " + iter);
                }
            }
        }
    }

    @Test
    void matchesKosarajuOnDataFiles() {
        for (String file : new String[]{"small_connected.txt", "small_disconnected.txt", "cyclic.txt", "small_dag.txt"}) {
            Graph graph = Graph.read(Path.of("data", file));
            SccResult kosaraju = Kosaraju.run(graph, new Metrics());
            SccResult tarjan = Tarjan.run(graph, new Metrics());
            assertEquals(kosaraju.componentCount(), tarjan.componentCount(), file);
            assertEquals(kosaraju.componentOf().length, tarjan.componentOf().length, file);
        }
    }

    @Test
    void emptyAndIsolatedGraphs() {
        Graph empty = Graph.builder(0, true).build();
        assertEquals(0, Tarjan.run(empty, new Metrics()).componentCount());

        Graph isolated = Graph.builder(3, true).build();
        assertEquals(3, Tarjan.run(isolated, new Metrics()).componentCount());
    }

    @Test
    void scansEveryEdgeOnceAndSurvivesHundredThousandPath() {
        int n = 100_000;
        Graph.Builder builder = Graph.builder(n, true);
        for (int v = 0; v + 1 < n; v++) {
            builder.addEdge(v, v + 1, 1);
        }
        Graph graph = builder.build();
        Metrics metrics = new Metrics();
        SccResult tarjan = Tarjan.run(graph, metrics);
        assertEquals(n, tarjan.componentCount());
        assertEquals(n - 1, metrics.edgeScans());
        assertEquals(n, metrics.maxDepth());
    }

    private static Graph randomDirected(int n, int m, Random rnd) {
        Graph.Builder builder = Graph.builder(n, true);
        for (int i = 0; i < m; i++) {
            builder.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(100));
        }
        return builder.build();
    }
}
