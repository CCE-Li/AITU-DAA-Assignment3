package kz.aitu.daa.graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class Graph {

    private final int n;
    private final boolean directed;
    private final int[][] adjTo;
    private final int[][] adjW;
    private final int[] edgeU;
    private final int[] edgeV;
    private final int[] edgeW;

    private Graph(int n, boolean directed, int[][] adjTo, int[][] adjW,
                  int[] edgeU, int[] edgeV, int[] edgeW) {
        this.n = n;
        this.directed = directed;
        this.adjTo = adjTo;
        this.adjW = adjW;
        this.edgeU = edgeU;
        this.edgeV = edgeV;
        this.edgeW = edgeW;
    }

    public int vertexCount() {
        return n;
    }

    public int edgeCount() {
        return edgeU.length;
    }

    public boolean isDirected() {
        return directed;
    }

    public int degree(int u) {
        return adjTo[u].length;
    }

    public int neighbor(int u, int i) {
        return adjTo[u][i];
    }

    public int weightAt(int u, int i) {
        return adjW[u][i];
    }

    public int[] edgeU() {
        return edgeU;
    }

    public int[] edgeV() {
        return edgeV;
    }

    public int[] edgeW() {
        return edgeW;
    }

    public Edge edge(int i) {
        return new Edge(edgeU[i], edgeV[i], edgeW[i]);
    }

    public static Builder builder(int n, boolean directed) {
        return new Builder(n, directed);
    }

    public static Graph read(Path path) {
        try (BufferedReader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = nextContentLine(in);
            if (header == null) {
                throw new IllegalArgumentException(path + ": empty file");
            }
            String[] head = header.trim().split("\\s+");
            if (head.length < 3) {
                throw new IllegalArgumentException(path + ": expected 'n m directed|undirected'");
            }
            int n = Integer.parseInt(head[0]);
            int m = Integer.parseInt(head[1]);
            String kind = head[2].toLowerCase();
            if (!kind.equals("directed") && !kind.equals("undirected")) {
                throw new IllegalArgumentException(path + ": unknown graph kind '" + head[2] + "'");
            }
            Builder builder = builder(n, kind.equals("directed"));
            for (int i = 0; i < m; i++) {
                String line = nextContentLine(in);
                if (line == null) {
                    throw new IllegalArgumentException(path + ": expected " + m + " edges, found " + i);
                }
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 3) {
                    throw new IllegalArgumentException(path + ": edge line must be 'u v w', got '" + line + "'");
                }
                builder.addEdge(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
            return builder.build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String nextContentLine(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                return line;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Graph(n=" + n + ", m=" + edgeU.length + ", " + (directed ? "directed" : "undirected") + ")";
    }

    public static final class Builder {

        private final int n;
        private final boolean directed;
        private int count;
        private int[] u = new int[16];
        private int[] v = new int[16];
        private int[] w = new int[16];

        private Builder(int n, boolean directed) {
            if (n < 0) {
                throw new IllegalArgumentException("negative vertex count: " + n);
            }
            this.n = n;
            this.directed = directed;
        }

        public Builder addEdge(int from, int to, int weight) {
            if (from < 0 || from >= n || to < 0 || to >= n) {
                throw new IllegalArgumentException("edge " + from + " -> " + to + " outside 0.." + (n - 1));
            }
            if (count == u.length) {
                int capacity = u.length * 2;
                u = Arrays.copyOf(u, capacity);
                v = Arrays.copyOf(v, capacity);
                w = Arrays.copyOf(w, capacity);
            }
            u[count] = from;
            v[count] = to;
            w[count] = weight;
            count++;
            return this;
        }

        public Graph build() {
            int[] degree = new int[n];
            for (int i = 0; i < count; i++) {
                degree[u[i]]++;
                if (!directed && u[i] != v[i]) {
                    degree[v[i]]++;
                }
            }
            int[][] to = new int[n][];
            int[][] weight = new int[n][];
            for (int i = 0; i < n; i++) {
                to[i] = new int[degree[i]];
                weight[i] = new int[degree[i]];
            }
            int[] cursor = new int[n];
            for (int i = 0; i < count; i++) {
                int a = u[i];
                int b = v[i];
                int wt = w[i];
                to[a][cursor[a]] = b;
                weight[a][cursor[a]] = wt;
                cursor[a]++;
                if (!directed && a != b) {
                    to[b][cursor[b]] = a;
                    weight[b][cursor[b]] = wt;
                    cursor[b]++;
                }
            }
            return new Graph(n, directed, to, weight,
                    Arrays.copyOf(u, count), Arrays.copyOf(v, count), Arrays.copyOf(w, count));
        }
    }
}
