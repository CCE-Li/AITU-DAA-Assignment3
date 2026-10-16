package kz.aitu.daa.generator;

import java.util.Random;
import kz.aitu.daa.graph.Graph;

public final class GraphGenerator {

    public static final long SEED = 42;
    public static final int SPARSE_FACTOR = 4;
    public static final int DENSE_FACTOR = 20;

    private GraphGenerator() {
    }

    public static Graph undirected(int n, int m) {
        Random rnd = new Random(SEED);
        int[] perm = shuffled(rnd, n);
        Graph.Builder builder = Graph.builder(n, false);
        int e = 0;
        for (int i = 1; i < n; i++) {
            builder.addEdge(perm[rnd.nextInt(i)], perm[i], 1 + rnd.nextInt(1000));
            e++;
        }
        while (e < m) {
            int[] ab = randomPair(rnd, n);
            builder.addEdge(ab[0], ab[1], 1 + rnd.nextInt(1000));
            e++;
        }
        return builder.build();
    }

    public static Graph directed(int n, int m) {
        Random rnd = new Random(SEED);
        Graph.Builder builder = Graph.builder(n, true);
        for (int i = 0; i < m; i++) {
            int[] ab = randomPair(rnd, n);
            builder.addEdge(ab[0], ab[1], 1 + rnd.nextInt(1000));
        }
        return builder.build();
    }

    public static Graph dag(int n, int m) {
        Random rnd = new Random(SEED);
        int[] rank = shuffled(rnd, n);
        Graph.Builder builder = Graph.builder(n, true);
        for (int i = 0; i < m; i++) {
            int[] ab = randomPair(rnd, n);
            int weight = 1 + rnd.nextInt(1000);
            int a = ab[0];
            int b = ab[1];
            if (rank[a] > rank[b]) {
                int swap = a;
                a = b;
                b = swap;
            }
            builder.addEdge(a, b, weight);
        }
        return builder.build();
    }

    private static int[] randomPair(Random rnd, int n) {
        int a = rnd.nextInt(n);
        int b = rnd.nextInt(n);
        while (b == a) {
            b = rnd.nextInt(n);
        }
        return new int[]{a, b};
    }

    private static int[] shuffled(Random rnd, int n) {
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) {
            perm[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int swap = perm[i];
            perm[i] = perm[j];
            perm[j] = swap;
        }
        return perm;
    }
}
