package kz.aitu.daa.algorithms;

import kz.aitu.daa.metrics.Metrics;

public final class Dsu {

    private final int[] parent;
    private final int[] rank;
    private final Metrics metrics;
    private int components;

    public Dsu(int n, Metrics metrics) {
        if (n < 0) {
            throw new IllegalArgumentException("negative size: " + n);
        }
        this.metrics = metrics;
        this.parent = new int[n];
        this.rank = new int[n];
        this.components = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
    }

    public int find(int x) {
        metrics.addFind();
        int root = x;
        while (parent[root] != root) {
            root = parent[root];
        }
        while (parent[x] != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    public boolean union(int x, int y) {
        int rx = find(x);
        int ry = find(y);
        if (rx == ry) {
            return false;
        }
        if (rank[rx] < rank[ry]) {
            int swap = rx;
            rx = ry;
            ry = swap;
        }
        parent[ry] = rx;
        if (rank[rx] == rank[ry]) {
            rank[rx]++;
        }
        components--;
        metrics.addSuccessfulUnion();
        return true;
    }

    public int components() {
        return components;
    }
}
