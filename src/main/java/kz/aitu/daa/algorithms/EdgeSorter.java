package kz.aitu.daa.algorithms;

import kz.aitu.daa.metrics.Metrics;

public final class EdgeSorter {

    private EdgeSorter() {
    }

    public static int[] sortedByWeight(int[] weights, Metrics metrics) {
        int m = weights.length;
        int[] order = new int[m];
        for (int i = 0; i < m; i++) {
            order[i] = i;
        }
        int[] buffer = new int[m];
        for (int width = 1; width < m; width *= 2) {
            for (int lo = 0; lo < m; lo += 2 * width) {
                int mid = Math.min(lo + width, m);
                int hi = Math.min(lo + 2 * width, m);
                if (mid < hi) {
                    merge(order, buffer, lo, mid, hi, weights, metrics);
                }
            }
        }
        return order;
    }

    private static void merge(int[] order, int[] buffer, int lo, int mid, int hi,
                              int[] weights, Metrics metrics) {
        int i = lo;
        int j = mid;
        int k = lo;
        while (i < mid && j < hi) {
            metrics.addComparisons(1);
            if (weights[order[i]] <= weights[order[j]]) {
                buffer[k++] = order[i++];
            } else {
                buffer[k++] = order[j++];
            }
        }
        while (i < mid) {
            buffer[k++] = order[i++];
        }
        while (j < hi) {
            buffer[k++] = order[j++];
        }
        System.arraycopy(buffer, lo, order, lo, hi - lo);
        metrics.addEdgeScans(hi - lo);
    }
}
