package kz.aitu.daa.algorithms;

import java.util.Arrays;
import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;

public final class Kosaraju {

    private Kosaraju() {
    }

    public static SccResult run(Graph graph, Metrics metrics) {
        int n = graph.vertexCount();
        boolean[] visited = new boolean[n];
        int[] finishOrder = new int[n];
        int[] stackVertex = new int[n];
        int[] stackIndex = new int[n];
        int orderPos = 0;

        for (int s = 0; s < n; s++) {
            if (visited[s]) {
                continue;
            }
            visited[s] = true;
            int sp = 1;
            stackVertex[0] = s;
            stackIndex[0] = 0;
            metrics.observeDepth(sp);
            while (sp > 0) {
                int u = stackVertex[sp - 1];
                int i = stackIndex[sp - 1];
                if (i < graph.degree(u)) {
                    stackIndex[sp - 1] = i + 1;
                    int v = graph.neighbor(u, i);
                    metrics.addEdgeScans(1);
                    if (!visited[v]) {
                        visited[v] = true;
                        stackVertex[sp] = v;
                        stackIndex[sp] = 0;
                        sp++;
                        metrics.observeDepth(sp);
                    }
                } else {
                    finishOrder[orderPos++] = u;
                    sp--;
                }
            }
        }

        Graph reversed = graph.isDirected() ? reverse(graph) : graph;
        int[] componentOf = new int[n];
        Arrays.fill(componentOf, -1);
        int components = 0;
        for (int i = n - 1; i >= 0; i--) {
            int s = finishOrder[i];
            if (componentOf[s] != -1) {
                continue;
            }
            componentOf[s] = components;
            int sp = 1;
            stackVertex[0] = s;
            stackIndex[0] = 0;
            metrics.observeDepth(sp);
            while (sp > 0) {
                int u = stackVertex[sp - 1];
                int j = stackIndex[sp - 1];
                if (j < reversed.degree(u)) {
                    stackIndex[sp - 1] = j + 1;
                    int v = reversed.neighbor(u, j);
                    metrics.addEdgeScans(1);
                    if (componentOf[v] == -1) {
                        componentOf[v] = components;
                        stackVertex[sp] = v;
                        stackIndex[sp] = 0;
                        sp++;
                        metrics.observeDepth(sp);
                    }
                } else {
                    sp--;
                }
            }
            components++;
        }
        return new SccResult(components, componentOf);
    }

    private static Graph reverse(Graph graph) {
        Graph.Builder builder = Graph.builder(graph.vertexCount(), true);
        for (int i = 0; i < graph.edgeCount(); i++) {
            builder.addEdge(graph.edgeV()[i], graph.edgeU()[i], graph.edgeW()[i]);
        }
        return builder.build();
    }
}
