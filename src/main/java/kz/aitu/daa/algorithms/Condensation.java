package kz.aitu.daa.algorithms;

import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;

public final class Condensation {

    private Condensation() {
    }

    public static CondensationResult run(Graph graph, Metrics metrics) {
        SccResult scc = Tarjan.run(graph, new Metrics());
        int k = scc.componentCount();
        int[] componentOf = scc.componentOf();
        if (k == 0) {
            return new CondensationResult(0, componentOf, 0, new int[0]);
        }
        Graph.Builder builder = Graph.builder(k, true);
        for (int i = 0; i < graph.edgeCount(); i++) {
            metrics.addEdgeScans(1);
            int cu = componentOf[graph.edgeU()[i]];
            int cv = componentOf[graph.edgeV()[i]];
            if (cu != cv) {
                builder.addEdge(cu, cv, -graph.edgeW()[i]);
            }
        }
        Graph negatedCondensation = builder.build();
        KahnResult order = Kahn.run(negatedCondensation, new Metrics());
        if (order.hasCycle()) {
            throw new IllegalStateException("condensation is not a DAG - SCC computation is wrong");
        }
        DagPathsResult paths = DagShortestPath.run(negatedCondensation, order.order(), metrics);
        long best = Long.MIN_VALUE;
        int bestVertex = -1;
        for (int v = 0; v < k; v++) {
            long longest = -paths.distance()[v];
            if (longest > best) {
                best = longest;
                bestVertex = v;
            }
        }
        return new CondensationResult(k, componentOf, best, paths.pathTo(bestVertex));
    }
}
