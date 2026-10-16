package kz.aitu.daa.algorithms;

import java.util.PriorityQueue;
import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;

public final class Prim {

    private Prim() {
    }

    public static MstResult run(Graph graph, Metrics metrics) {
        int n = graph.vertexCount();
        boolean[] inTree = new boolean[n];
        PriorityQueue<int[]> queue = new PriorityQueue<>((a, b) -> {
            metrics.addComparisons(1);
            return Integer.compare(a[0], b[0]);
        });
        long total = 0;
        int used = 0;
        int components = 0;
        for (int start = 0; start < n; start++) {
            if (inTree[start]) {
                continue;
            }
            components++;
            inTree[start] = true;
            pushEdges(graph, start, inTree, queue, metrics);
            while (!queue.isEmpty()) {
                int[] edge = queue.poll();
                int to = edge[1];
                if (inTree[to]) {
                    continue;
                }
                inTree[to] = true;
                total += edge[0];
                used++;
                pushEdges(graph, to, inTree, queue, metrics);
            }
        }
        return new MstResult(total, components, used);
    }

    private static void pushEdges(Graph graph, int u, boolean[] inTree,
                                  PriorityQueue<int[]> queue, Metrics metrics) {
        int degree = graph.degree(u);
        for (int i = 0; i < degree; i++) {
            metrics.addEdgeScans(1);
            int v = graph.neighbor(u, i);
            if (!inTree[v]) {
                queue.add(new int[]{graph.weightAt(u, i), v});
            }
        }
    }
}
