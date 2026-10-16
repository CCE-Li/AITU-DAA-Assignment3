package kz.aitu.daa.algorithms;

import java.util.ArrayDeque;
import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;

public final class Kahn {

    private Kahn() {
    }

    public static KahnResult run(Graph graph, Metrics metrics) {
        int n = graph.vertexCount();
        int[] indegree = new int[n];
        for (int u = 0; u < n; u++) {
            int degree = graph.degree(u);
            for (int i = 0; i < degree; i++) {
                indegree[graph.neighbor(u, i)]++;
            }
        }
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (indegree[v] == 0) {
                queue.add(v);
            }
        }
        int[] order = new int[n];
        int count = 0;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order[count++] = u;
            int degree = graph.degree(u);
            for (int i = 0; i < degree; i++) {
                metrics.addEdgeScans(1);
                int v = graph.neighbor(u, i);
                if (--indegree[v] == 0) {
                    queue.add(v);
                }
            }
        }
        return new KahnResult(count, order, count < n);
    }
}
