#!/usr/bin/env python3
"""Builds the four benchmark plots from results/results.csv and results/bonus.csv.

Every axis is drawn in scientific notation (log-log axes with power-of-ten
tick labels), as required by the assignment.
"""

import csv
import math
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.ticker import LogFormatterSciNotation

ROOT = Path(__file__).resolve().parent.parent
RESULTS_CSV = ROOT / "results" / "results.csv"
BONUS_CSV = ROOT / "results" / "bonus.csv"
PLOTS_DIR = ROOT / "results" / "plots"

ALGORITHMS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
COLORS = {
    "Kruskal": "#1f77b4",
    "Prim": "#d62728",
    "Kosaraju": "#2ca02c",
    "Kahn": "#ff7f0e",
    "DagShortestPath": "#9467bd",
    "Tarjan": "#17becf",
}
DENSITIES = {"sparse": "-", "dense": "--"}


def read_rows(path):
    with path.open(encoding="utf-8") as handle:
        return list(csv.DictReader(handle))


def series(rows, algorithm, density):
    points = [
        row
        for row in rows
        if row["algorithm"] == algorithm and row["graph"].endswith("_" + density)
    ]
    points.sort(key=lambda row: int(row["n"]))
    xs = [int(row["n"]) for row in points]
    scans = [int(row["edge_scans"]) for row in points]
    ratios = [int(row["edge_scans"]) / int(row["m"]) for row in points]
    operations = [
        int(row["dsu_ops"]) + int(row["edge_scans"]) + int(row["comparisons"])
        for row in points
    ]
    return xs, scans, ratios, operations


def log_slope(xs, ys):
    lx = [math.log10(x) for x in xs]
    ly = [math.log10(y) for y in ys]
    n = len(xs)
    mean_x = sum(lx) / n
    mean_y = sum(ly) / n
    numerator = sum((a - mean_x) * (b - mean_y) for a, b in zip(lx, ly))
    denominator = sum((a - mean_x) ** 2 for a in lx)
    return numerator / denominator


def style_axes(ax):
    ax.set_xscale("log")
    ax.set_yscale("log")
    formatter = LogFormatterSciNotation()
    ax.xaxis.set_major_formatter(formatter)
    ax.yaxis.set_major_formatter(formatter)
    ax.grid(True, which="both", alpha=0.3)


def save(fig, name):
    PLOTS_DIR.mkdir(parents=True, exist_ok=True)
    path = PLOTS_DIR / name
    fig.tight_layout()
    fig.savefig(path, dpi=200)
    plt.close(fig)
    print("wrote", path.relative_to(ROOT))


def plot_operations_vs_n(rows):
    fig, ax = plt.subplots(figsize=(9, 6))
    slopes = {}
    for algorithm in ALGORITHMS:
        for density, linestyle in DENSITIES.items():
            xs, _, _, operations = series(rows, algorithm, density)
            fit = log_slope(xs, operations)
            slopes[(algorithm, density)] = fit
            ax.plot(xs, operations, linestyle, color=COLORS[algorithm], marker="o",
                    markersize=4, label=f"{algorithm} {density} (slope {fit:.2f})")
    style_axes(ax)
    ax.set_title("Operations vs n (operations = dsu_ops + edge_scans + comparisons)")
    ax.set_xlabel("n (vertices)")
    ax.set_ylabel("counted operations")
    ax.legend(fontsize=8, ncol=2)
    save(fig, "operations_vs_n.png")
    print("slopes of operations curves:")
    for (algorithm, density), fit in sorted(slopes.items()):
        print(f"  {algorithm:<15} {density:<6} {fit:.3f}")
    print("slopes of edge_scans curves:")
    for algorithm in ALGORITHMS:
        for density in DENSITIES:
            xs, scans, _, _ = series(rows, algorithm, density)
            fit = log_slope(xs, scans)
            print(f"  {algorithm:<15} {density:<6} {fit:.3f}")


def plot_scans_per_edge(rows):
    fig, ax = plt.subplots(figsize=(9, 6))
    for algorithm in ALGORITHMS:
        for density, linestyle in DENSITIES.items():
            xs, _, ratios, _ = series(rows, algorithm, density)
            ax.plot(xs, ratios, linestyle, color=COLORS[algorithm], marker="o",
                    markersize=4, label=f"{algorithm} {density}")
    style_axes(ax)
    ax.set_title("Edge scans per edge (edge_scans / m)")
    ax.set_xlabel("n (vertices)")
    ax.set_ylabel("edge_scans / m")
    ax.legend(fontsize=8, ncol=2)
    save(fig, "scans_per_edge.png")


def plot_kruskal_share(rows):
    fig, ax = plt.subplots(figsize=(9, 6))
    for algorithm in ["Kruskal", "Prim"]:
        for density, linestyle in DENSITIES.items():
            xs, _, ratios, _ = series(rows, algorithm, density)
            ax.plot(xs, ratios, linestyle, color=COLORS[algorithm], marker="o",
                    markersize=4, label=f"{algorithm} {density}")
    style_axes(ax)
    ax.set_title("Share of edges examined for MST (edge_scans / m)")
    ax.set_xlabel("n (vertices)")
    ax.set_ylabel("edge_scans / m")
    ax.legend(fontsize=8)
    save(fig, "kruskal_share.png")


def plot_bonus_tarjan(rows, bonus_rows):
    fig, ax = plt.subplots(figsize=(9, 6))
    for density, linestyle in DENSITIES.items():
        xs, scans, _, _ = series(rows, "Kosaraju", density)
        ax.plot(xs, scans, linestyle, color=COLORS["Kosaraju"], marker="o",
                markersize=4, label=f"Kosaraju {density}")
        points = [
            row
            for row in bonus_rows
            if row["algorithm"] == "Tarjan" and row["graph"].endswith("_" + density)
        ]
        points.sort(key=lambda row: int(row["n"]))
        xs = [int(row["n"]) for row in points]
        scans = [int(row["edge_scans"]) for row in points]
        ax.plot(xs, scans, linestyle, color=COLORS["Tarjan"], marker="s",
                markersize=4, label=f"Tarjan {density}")
    style_axes(ax)
    ax.set_title("Bonus: SCC edge scans - Tarjan (m) vs Kosaraju (2m)")
    ax.set_xlabel("n (vertices)")
    ax.set_ylabel("edge_scans")
    ax.legend(fontsize=8)
    save(fig, "bonus_tarjan_vs_kosaraju.png")


def main():
    rows = read_rows(RESULTS_CSV)
    bonus_rows = read_rows(BONUS_CSV) if BONUS_CSV.exists() else []
    plot_operations_vs_n(rows)
    plot_scans_per_edge(rows)
    plot_kruskal_share(rows)
    if bonus_rows:
        plot_bonus_tarjan(rows, bonus_rows)


if __name__ == "__main__":
    main()
