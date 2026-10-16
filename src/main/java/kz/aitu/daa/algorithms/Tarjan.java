package kz.aitu.daa.algorithms;

import java.util.Arrays;
import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.Metrics;

public final class Tarjan {

    private Tarjan() {
    }

    public static SccResult run(Graph graph, Metrics metrics) {
        int n = graph.vertexCount();
        int[] index = new int[n];
        int[] lowlink = new int[n];
        Arrays.fill(index, -1);
        int[] componentOf = new int[n];
        Arrays.fill(componentOf, -1);
        int[] activeStack = new int[n];
        int[] frameVertex = new int[n];
        int[] frameIndex = new int[n];
        int activeSize = 0;
        int nextIndex = 0;
        int components = 0;

        for (int start = 0; start < n; start++) {
            if (index[start] != -1) {
                continue;
            }
            index[start] = nextIndex++;
            lowlink[start] = index[start];
            activeStack[activeSize++] = start;
            int sp = 1;
            frameVertex[0] = start;
            frameIndex[0] = 0;
            metrics.observeDepth(sp);
            while (sp > 0) {
                int u = frameVertex[sp - 1];
                if (frameIndex[sp - 1] < graph.degree(u)) {
                    int v = graph.neighbor(u, frameIndex[sp - 1]++);
                    metrics.addEdgeScans(1);
                    if (index[v] == -1) {
                        index[v] = nextIndex++;
                        lowlink[v] = index[v];
                        activeStack[activeSize++] = v;
                        frameVertex[sp] = v;
                        frameIndex[sp] = 0;
                        sp++;
                        metrics.observeDepth(sp);
                    } else if (componentOf[v] == -1) {
                        lowlink[u] = Math.min(lowlink[u], index[v]);
                    }
                } else {
                    sp--;
                    if (lowlink[u] == index[u]) {
                        while (true) {
                            int w = activeStack[--activeSize];
                            componentOf[w] = components;
                            if (w == u) {
                                break;
                            }
                        }
                        components++;
                    }
                    if (sp > 0) {
                        int parent = frameVertex[sp - 1];
                        lowlink[parent] = Math.min(lowlink[parent], lowlink[u]);
                    }
                }
            }
        }
        return new SccResult(components, componentOf);
    }
}
