package kz.aitu.daa.benchmark;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import kz.aitu.daa.algorithms.Condensation;
import kz.aitu.daa.algorithms.CondensationResult;
import kz.aitu.daa.algorithms.DagPathsResult;
import kz.aitu.daa.algorithms.DagShortestPath;
import kz.aitu.daa.algorithms.Kahn;
import kz.aitu.daa.algorithms.KahnResult;
import kz.aitu.daa.algorithms.Kosaraju;
import kz.aitu.daa.algorithms.Kruskal;
import kz.aitu.daa.algorithms.MstResult;
import kz.aitu.daa.algorithms.Prim;
import kz.aitu.daa.algorithms.SccResult;
import kz.aitu.daa.algorithms.Tarjan;
import kz.aitu.daa.generator.GraphGenerator;
import kz.aitu.daa.graph.Graph;
import kz.aitu.daa.metrics.CsvWriter;
import kz.aitu.daa.metrics.Metrics;

public final class Benchmark {

    private static final int[] SIZES = {1_000, 3_162, 10_000, 31_623, 100_000};
    private static final int RUNS = 5;
    private static final Path CSV_PATH = Path.of("results", "results.csv");
    private static final Path BONUS_CSV_PATH = Path.of("results", "bonus.csv");

    private final List<String> failures = new ArrayList<>();
    private final List<Row> rows = new ArrayList<>();
    private final List<BonusRow> bonusRows = new ArrayList<>();

    private record Row(String algorithm, String graph, int n, int m, double timeMs,
                       Metrics metrics, long result, long expected) {
    }

    private record BonusRow(String algorithm, String graph, int n, int m, double timeMs,
                            Metrics metrics, long result) {
    }

    private record Timed<T>(T value, Metrics metrics, double medianMs) {
    }

    private interface Algorithm<T> {
        T run(Graph graph, Metrics metrics);
    }

    public static void main(String[] args) throws IOException {
        new Benchmark().run();
    }

    private void run() throws IOException {
        System.out.printf("%-18s %7s %9s %-16s %10s %10s %13s %13s %9s %13s %13s%n",
                "graph", "n", "m", "algorithm", "time_ms", "dsu_ops", "edge_scans",
                "comparisons", "max_depth", "result", "expected");
        for (int n : SIZES) {
            for (int factor : new int[]{GraphGenerator.SPARSE_FACTOR, GraphGenerator.DENSE_FACTOR}) {
                runConfig(n, n * factor, factor == GraphGenerator.SPARSE_FACTOR ? "sparse" : "dense");
            }
        }
        runBonusLoop();
        writeCsv();
        writeBonusCsv();
        printCounterRatios();
        System.out.println();
        if (failures.isEmpty()) {
            System.out.println("All " + rows.size() + " rows match the reference values and all cross-checks passed.");
        } else {
            System.out.println("BENCHMARK FAILED with " + failures.size() + " problem(s):");
            for (String failure : failures) {
                System.out.println("  - " + failure);
            }
            System.exit(1);
        }
    }

    private void runConfig(int n, int m, String density) throws IOException {
        ReferenceValues.Row reference = ReferenceValues.get(n, m);
        runUndirected(n, m, density, reference);
        runDirected(n, m, density, reference);
        runDag(n, m, density, reference);
        System.gc();
    }

    private void runUndirected(int n, int m, String density, ReferenceValues.Row reference) {
        Graph graph = GraphGenerator.undirected(n, m);
        Timed<MstResult> kruskal = measure(graph, Kruskal::run);
        Timed<MstResult> prim = measure(graph, Prim::run);
        if (kruskal.value().totalWeight() != prim.value().totalWeight()) {
            failures.add("Kruskal and Prim disagree on undirected_" + density + " n=" + n + ": "
                    + kruskal.value().totalWeight() + " vs " + prim.value().totalWeight());
        }
        checkResult("MST weight", "undirected_" + density, n, m, kruskal.value().totalWeight(), reference.mstWeight());
        checkResult("MST weight", "undirected_" + density, n, m, prim.value().totalWeight(), reference.mstWeight());
        if (kruskal.value().treeEdges() != n - 1 || prim.value().treeEdges() != n - 1) {
            failures.add("MST on connected graph must have n-1 edges on undirected_" + density + " n=" + n
                    + ": kruskal=" + kruskal.value().treeEdges() + ", prim=" + prim.value().treeEdges());
        }
        addRow("Kruskal", "undirected_" + density, n, m, kruskal, reference.mstWeight());
        addRow("Prim", "undirected_" + density, n, m, prim, reference.mstWeight());
    }

    private void runDirected(int n, int m, String density, ReferenceValues.Row reference) {
        Graph graph = GraphGenerator.directed(n, m);
        Timed<SccResult> kosaraju = measure(graph, Kosaraju::run);
        if (kosaraju.metrics().edgeScans() != 2L * m) {
            failures.add("Kosaraju must scan exactly 2m edges, got " + kosaraju.metrics().edgeScans()
                    + " on directed_" + density + " n=" + n);
        }
        checkResult("SCC count", "directed_" + density, n, m, kosaraju.value().componentCount(), reference.sccCount());
        addRow("Kosaraju", "directed_" + density, n, m, kosaraju, reference.sccCount());
    }

    private void runDag(int n, int m, String density, ReferenceValues.Row reference) {
        Graph graph = GraphGenerator.dag(n, m);
        Timed<KahnResult> kahn = measure(graph, Kahn::run);
        if (kahn.value().hasCycle()) {
            failures.add("Kahn found a cycle in the generated DAG on dag_" + density + " n=" + n);
        }
        if (kahn.metrics().edgeScans() != m) {
            failures.add("Kahn must scan exactly m edges, got " + kahn.metrics().edgeScans()
                    + " on dag_" + density + " n=" + n);
        }
        validateTopologicalOrder(graph, kahn.value());
        checkResult("Kahn order size", "dag_" + density, n, m, kahn.value().verticesInOrder(), n);
        addRow("Kahn", "dag_" + density, n, m, kahn, n);

        int[] order = kahn.value().order();
        Timed<DagPathsResult> paths = measure(graph, (g, metrics) -> DagShortestPath.run(g, order, metrics));
        if (paths.metrics().edgeScans() != m) {
            failures.add("DAG shortest paths must scan exactly m edges, got " + paths.metrics().edgeScans()
                    + " on dag_" + density + " n=" + n);
        }
        checkResult("DAG distance sum", "dag_" + density, n, m, paths.value().sumOfDistances(), reference.dagDistanceSum());
        addRow("DagShortestPath", "dag_" + density, n, m, paths, reference.dagDistanceSum());
    }

    private void runBonusLoop() {
        System.out.println();
        System.out.println("Bonus: Tarjan's SCC and condensation / critical path");
        System.out.printf("%-18s %7s %9s %-13s %9s %13s %13s %13s %s%n",
                "graph", "n", "m", "algorithm", "time_ms", "edge_scans", "comparisons", "result", "check");
        for (int n : SIZES) {
            for (int factor : new int[]{GraphGenerator.SPARSE_FACTOR, GraphGenerator.DENSE_FACTOR}) {
                int m = n * factor;
                String density = factor == GraphGenerator.SPARSE_FACTOR ? "sparse" : "dense";
                Graph graph = GraphGenerator.directed(n, m);

                SccResult kosaraju = Kosaraju.run(graph, new Metrics());
                Timed<SccResult> tarjan = measure(graph, Tarjan::run);
                if (tarjan.value().componentCount() != kosaraju.componentCount()) {
                    failures.add("Tarjan and Kosaraju disagree on directed_" + density + " n=" + n + ": "
                            + tarjan.value().componentCount() + " vs " + kosaraju.componentCount());
                }
                if (tarjan.metrics().edgeScans() != m) {
                    failures.add("Tarjan must scan exactly m edges, got " + tarjan.metrics().edgeScans()
                            + " on directed_" + density + " n=" + n);
                }
                bonusRows.add(new BonusRow("Tarjan", "directed_" + density, n, m, tarjan.medianMs(),
                        tarjan.metrics(), tarjan.value().componentCount()));
                System.out.printf("%-18s %7d %9d %-13s %9.2f %13d %13d %13d kosaraju %d scc / %d scans%n",
                        "directed_" + density, n, m, "Tarjan", tarjan.medianMs(),
                        tarjan.metrics().edgeScans(), tarjan.metrics().comparisons(),
                        tarjan.value().componentCount(), kosaraju.componentCount(), 2L * m);

                Timed<CondensationResult> condensation = measure(graph, Condensation::run);
                CondensationResult result = condensation.value();
                if (result.componentCount() != kosaraju.componentCount()) {
                    failures.add("Condensation component count differs from Kosaraju on directed_" + density
                            + " n=" + n);
                }
                if (result.criticalPathLength() < 0 || result.criticalPath().length == 0) {
                    failures.add("Condensation critical path is invalid on directed_" + density + " n=" + n);
                }
                validateCriticalPath(graph, result);
                bonusRows.add(new BonusRow("Condensation", "directed_" + density, n, m, condensation.medianMs(),
                        condensation.metrics(), result.criticalPathLength()));
                System.out.printf("%-18s %7d %9d %-13s %9.2f %13d %13d %13d path of %d components%n",
                        "directed_" + density, n, m, "Condensation", condensation.medianMs(),
                        condensation.metrics().edgeScans(), condensation.metrics().comparisons(),
                        result.criticalPathLength(), result.criticalPath().length);
                System.gc();
            }
        }
    }

    private void validateCriticalPath(Graph graph, CondensationResult result) {
        int[] componentOf = result.componentOf();
        int[] path = result.criticalPath();
        int sourceComponent = path[0];
        int sinkComponent = path[path.length - 1];
        for (int i = 0; i < graph.edgeCount(); i++) {
            int cu = componentOf[graph.edgeU()[i]];
            int cv = componentOf[graph.edgeV()[i]];
            if (cu == cv) {
                continue;
            }
            if (cv == sourceComponent) {
                failures.add("critical path does not start at a source component");
                return;
            }
            if (cu == sinkComponent) {
                failures.add("critical path does not end at a sink component");
                return;
            }
        }
    }

    private void writeBonusCsv() throws IOException {
        CsvWriter writer = new CsvWriter();
        for (BonusRow row : bonusRows) {
            writer.row(row.algorithm(), row.graph(), row.n(), row.m(), row.timeMs(),
                    row.metrics().dsuOps(), row.metrics().edgeScans(), row.metrics().comparisons(),
                    row.metrics().maxDepth(), row.result());
        }
        writer.writeTo(BONUS_CSV_PATH);
        System.out.println();
        System.out.println("Wrote " + bonusRows.size() + " bonus rows to " + BONUS_CSV_PATH.toAbsolutePath());
    }

    private void validateTopologicalOrder(Graph graph, KahnResult result) {
        int n = graph.vertexCount();
        int[] pos = new int[n];
        for (int i = 0; i < result.verticesInOrder(); i++) {
            pos[result.order()[i]] = i;
        }
        for (int u = 0; u < n; u++) {
            if (pos[u] < 0) {
                failures.add("topological order misses vertex " + u);
                return;
            }
            int degree = graph.degree(u);
            for (int i = 0; i < degree; i++) {
                int v = graph.neighbor(u, i);
                if (pos[u] >= pos[v]) {
                    failures.add("topological order violated on edge " + u + " -> " + v);
                    return;
                }
            }
        }
    }

    private <T> Timed<T> measure(Graph graph, Algorithm<T> algorithm) {
        T value = null;
        Metrics metrics = null;
        double[] times = new double[RUNS];
        for (int run = 0; run < RUNS; run++) {
            Metrics current = new Metrics();
            long start = System.nanoTime();
            T result = algorithm.run(graph, current);
            long end = System.nanoTime();
            times[run] = (end - start) / 1_000_000.0;
            if (run == 0) {
                value = result;
                metrics = current;
            } else if (current.dsuOps() != metrics.dsuOps()
                    || current.edgeScans() != metrics.edgeScans()
                    || current.comparisons() != metrics.comparisons()
                    || current.maxDepth() != metrics.maxDepth()) {
                throw new IllegalStateException("counters are not deterministic across runs: "
                        + current + " vs " + metrics);
            }
        }
        Arrays.sort(times);
        return new Timed<>(value, metrics, times[RUNS / 2]);
    }

    private void checkResult(String what, String graphName, int n, int m, long actual, long expected) {
        if (actual != expected) {
            failures.add(what + " mismatch on " + graphName + " n=" + n + " m=" + m
                    + ": got " + actual + ", expected " + expected);
        }
    }

    private void addRow(String algorithm, String graph, int n, int m, Timed<?> timed, long expected) {
        long result = extractResult(timed.value());
        Row row = new Row(algorithm, graph, n, m, timed.medianMs(), timed.metrics(), result, expected);
        rows.add(row);
        System.out.printf("%-18s %7d %9d %-16s %10.2f %10d %13d %13d %9d %13d %13d %s%n",
                graph, n, m, algorithm, row.timeMs(),
                row.metrics().dsuOps(), row.metrics().edgeScans(), row.metrics().comparisons(),
                row.metrics().maxDepth(), result, expected,
                result == expected ? "ok" : "MISMATCH");
    }

    private static long extractResult(Object value) {
        if (value instanceof MstResult mst) {
            return mst.totalWeight();
        }
        if (value instanceof SccResult scc) {
            return scc.componentCount();
        }
        if (value instanceof KahnResult kahn) {
            return kahn.verticesInOrder();
        }
        if (value instanceof DagPathsResult paths) {
            return paths.sumOfDistances();
        }
        throw new IllegalArgumentException("unknown result type: " + value);
    }

    private void writeCsv() throws IOException {
        CsvWriter writer = new CsvWriter();
        for (Row row : rows) {
            writer.row(row.algorithm(), row.graph(), row.n(), row.m(), row.timeMs(),
                    row.metrics().dsuOps(), row.metrics().edgeScans(), row.metrics().comparisons(),
                    row.metrics().maxDepth(), row.result());
        }
        writer.writeTo(CSV_PATH);
        System.out.println();
        System.out.println("Wrote " + rows.size() + " rows to " + CSV_PATH.toAbsolutePath());
    }

    private void printCounterRatios() {
        System.out.println();
        System.out.println("Counter vs formula ratios (first size...last size per density):");
        for (String algorithm : new String[]{"Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"}) {
            for (String density : new String[]{"sparse", "dense"}) {
                List<Row> matching = new ArrayList<>();
                for (Row row : rows) {
                    if (row.algorithm().equals(algorithm) && row.graph().endsWith(density)) {
                        matching.add(row);
                    }
                }
                if (matching.isEmpty()) {
                    continue;
                }
                Row first = matching.get(0);
                Row last = matching.get(matching.size() - 1);
                System.out.printf("  %-15s %-6s %-28s %.4f ... %.4f%n",
                        algorithm, density, ratioName(algorithm), ratio(algorithm, first), ratio(algorithm, last));
            }
        }
    }

    private static String ratioName(String algorithm) {
        return switch (algorithm) {
            case "Kruskal" -> "dsu_ops / m";
            case "Prim" -> "edge_scans / (n + m)";
            case "Kosaraju" -> "edge_scans / (n + m)";
            case "Kahn" -> "edge_scans / (n + m)";
            default -> "comparisons / m";
        };
    }

    private static double ratio(String algorithm, Row row) {
        double denom = switch (algorithm) {
            case "Kruskal" -> row.m();
            case "Prim", "Kosaraju", "Kahn" -> (double) (row.n() + row.m());
            default -> row.m();
        };
        double numer = switch (algorithm) {
            case "Kruskal" -> row.metrics().dsuOps();
            case "DagShortestPath" -> row.metrics().comparisons();
            default -> row.metrics().edgeScans();
        };
        return numer / denom;
    }
}
