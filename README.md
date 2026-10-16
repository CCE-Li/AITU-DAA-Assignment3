# DAA Assignment 3 - Graphs: DSU, MST, SCC and Shortest Paths

Java implementation of the City Network & Job Scheduler scenario: one adjacency-list graph class, six hand-written algorithms, operation counters that are reproducible on any machine, a self-checking benchmark that reproduces the assignment's reference values exactly, and JUnit 5 tests that cross-check every algorithm against an independent implementation.

| Algorithm | Class |
|---|---|
| Disjoint Set Union (union by rank + path compression) | `algorithms/Dsu` |
| Kruskal MST (stable counting merge sort + DSU) | `algorithms/Kruskal` |
| Prim MST (lazy binary-heap, forest on disconnected input) | `algorithms/Prim` |
| Kosaraju SCC (iterative DFS, explicit stack) | `algorithms/Kosaraju` |
| Kahn topological order (cycle detection) | `algorithms/Kahn` |
| DAG shortest paths (super-source, parent chain, negative weights) | `algorithms/DagShortestPath` |
| **Bonus:** Tarjan SCC (iterative, single pass) | `algorithms/Tarjan` |
| **Bonus:** condensation graph + critical path | `algorithms/Condensation` |

## Requirements

* JDK 21 or newer
* Maven 3.9 or newer
* Python 3 with `matplotlib` (only for `scripts/plot.py`)

## Build, test, benchmark

```bash
mvn test                # 27 JUnit 5 tests
mvn -q exec:java        # self-checking benchmark -> results/results.csv (+ results/bonus.csv)
python scripts/plot.py  # results/plots/*.png (log-log axes in scientific notation)
```

The benchmark takes several minutes on the large graphs. It:

1. generates the ten seeded graphs (`Random(42)`, `n = 1 000, 3 162, 10 000, 31 623, 100 000`, sparse `m = 4n` / dense `m = 20n`, `n·10` half-decade steps),
2. runs each algorithm, counts operations and takes the median of 5 runs for the clock,
3. **fails loudly** (`exit 1`) if Kruskal and Prim disagree on the MST weight, if a topological order is invalid, if the counters differ between runs, or if **any reference value differs**.

Reference values reproduced exactly by the benchmark:

| n / m | MST weight (Kruskal = Prim) | SCC count (Kosaraju) | Sum of DAG distances |
|---|---|---|---|
| 1 000 / 4 000 | 147 970 | 27 | 396 274 |
| 1 000 / 20 000 | 30 363 | 1 | 252 411 |
| 3 162 / 12 648 | 468 911 | 137 | 1 271 911 |
| 3 162 / 63 240 | 97 589 | 1 | 723 157 |
| 10 000 / 40 000 | 1 486 139 | 373 | 3 941 828 |
| 10 000 / 200 000 | 309 550 | 1 | 2 381 342 |
| 31 623 / 126 492 | 4 715 515 | 1 188 | 12 551 891 |
| 31 623 / 632 460 | 968 517 | 1 | 7 376 612 |
| 100 000 / 400 000 | 15 012 094 | 3 923 | 40 096 539 |
| 100 000 / 2 000 000 | 3 056 681 | 1 | 23 512 087 |

`results/results.csv` holds the 50 required rows (algorithm, graph, n, m, time_ms, dsu_ops, edge_scans, comparisons, max_depth, result); `results/bonus.csv` holds the 20 bonus rows for Tarjan and the condensation.

## Layout

```
data/                        small input graphs (one disconnected, one cyclic)
  small_connected.txt        self-loop + parallel edge + cycle
  small_disconnected.txt     two components, one isolated vertex
  cyclic.txt                 directed, two cycles -> Kahn must detect the cycle
  small_dag.txt              directed DAG with negative weights
scripts/plot.py              builds the four PNG plots from the CSVs
src/main/java/kz/aitu/daa/
  graph/                     Graph (adjacency list, directed/undirected, file input), Edge
  algorithms/                Dsu, Kruskal, Prim, Kosaraju, Kahn, DagShortestPath, Tarjan, Condensation
  metrics/                   Metrics (operation counters), CsvWriter
  generator/                 GraphGenerator (exact seeded generator)
  benchmark/                 Benchmark (main), ReferenceValues
src/test/java/kz/aitu/daa/   27 JUnit 5 tests
results/results.csv          main benchmark output (50 rows)
results/bonus.csv            bonus benchmark output (20 rows)
results/plots/               operations_vs_n.png, scans_per_edge.png, kruskal_share.png,
                             bonus_tarjan_vs_kosaraju.png (scientific-notation axes)
REPORT.md                    complexity table, counters vs formulas, correctness
                             arguments, "which algorithm when", slope check, plots
```

## Input file format

```
n m directed|undirected
u v w
u v w
...
```

Blank lines and `#` comments are ignored. Self-loops, parallel edges, isolated vertices and disconnected graphs are handled everywhere (MST returns a forest, Kahn detects cycles instead of guessing).

## Tests

`mvn test` runs 27 tests: DSU against a reference labelling; Kruskal vs Prim on 100 random connected graphs and vs brute force on tiny ones; Kosaraju vs a brute-force BFS-both-directions partition; topological order validity and cycle detection; DAG shortest paths vs a multi-source Bellman-Ford including negative weights; Tarjan vs Kosaraju on every graph; condensation acyclicity and critical path vs brute force; and a 100 000-vertex path that must not overflow the JVM stack.

## Git workflow

* `main` - only working code, tagged `v1.0`
* `feature/dsu`, `feature/mst`, `feature/scc`, `feature/paths`, `feature/bonus`, `feature/metrics` - one branch per feature, merged into `main` with merge commits and conventional commit messages (`feat(dsu): ...`, `test(mst): ...`, `docs(report): ...`)

## AI-use acknowledgement

A general-purpose AI coding assistant (CommandCode) was used to draft the implementation, the tests, the benchmark and this documentation. Every result was reproduced locally by running `mvn test`, `mvn -q exec:java` and `scripts/plot.py`; all ten reference-value rows and all cross-checks pass, and the analysis in `REPORT.md` is based on the generated counters, not on assumptions.

## Report

See [REPORT.md](REPORT.md) for the complexity table, the counter-vs-formula check, the two correctness arguments, the "which algorithm when" table, the slope check and the plots.
