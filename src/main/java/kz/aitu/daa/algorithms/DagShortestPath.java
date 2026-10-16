package kz.aitu.daa.algorithms;

import java.util.Arrays;
import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;

public final class DagShortestPath {

    public static final long UNREACHABLE = Long.MAX_VALUE / 4;

    private DagShortestPath() {
    }

    public static DagPathsResult run(Graph graph, int[] topologicalOrder, Metrics metrics) {
        int n = graph.vertexCount();
        int[] indegree = new int[n];
        for (int u = 0; u < n; u++) {
            int degree = graph.degree(u);
            for (int i = 0; i < degree; i++) {
                indegree[graph.neighbor(u, i)]++;
            }
        }
        long[] distance = new long[n];
        Arrays.fill(distance, UNREACHABLE);
        for (int v = 0; v < n; v++) {
            if (indegree[v] == 0) {
                distance[v] = 0;
            }
        }
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        for (int pos = 0; pos < topologicalOrder.length; pos++) {
            int u = topologicalOrder[pos];
            if (distance[u] == UNREACHABLE) {
                continue;
            }
            int degree = graph.degree(u);
            for (int i = 0; i < degree; i++) {
                metrics.addEdgeScans(1);
                metrics.addComparisons(1);
                int v = graph.neighbor(u, i);
                long candidate = distance[u] + graph.weightAt(u, i);
                if (candidate < distance[v]) {
                    distance[v] = candidate;
                    parent[v] = u;
                }
            }
        }
        long sum = 0;
        for (long d : distance) {
            if (d != UNREACHABLE) {
                sum += d;
            }
        }
        return new DagPathsResult(sum, distance, parent);
    }

    public static DagPathsResult run(Graph graph, Metrics metrics) {
        KahnResult order = Kahn.run(graph, new Metrics());
        if (order.hasCycle()) {
            throw new IllegalArgumentException("graph is not a DAG: cycle detected");
        }
        return run(graph, order.order(), metrics);
    }
}
