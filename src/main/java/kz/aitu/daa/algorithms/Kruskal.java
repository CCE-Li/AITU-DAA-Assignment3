package kz.aitu.daa.algorithms;

import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;

public final class Kruskal {

    private Kruskal() {
    }

    public static MstResult run(Graph graph, Metrics metrics) {
        int[] weights = graph.edgeW();
        int[] order = EdgeSorter.sortedByWeight(weights, metrics);
        Dsu dsu = new Dsu(graph.vertexCount(), metrics);
        long total = 0;
        int used = 0;
        for (int i = 0; i < order.length; i++) {
            int e = order[i];
            metrics.addEdgeScans(1);
            if (dsu.union(graph.edgeU()[e], graph.edgeV()[e])) {
                total += weights[e];
                used++;
            }
        }
        return new MstResult(total, dsu.components(), used);
    }
}
