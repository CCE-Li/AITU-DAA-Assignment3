package kz.aitu.daa.algorithms;

import java.util.Arrays;

public record DagPathsResult(long sumOfDistances, long[] distance, int[] parent) {

    public int[] pathTo(int v) {
        if (v < 0 || v >= distance.length) {
            throw new IllegalArgumentException("vertex out of range: " + v);
        }
        if (distance[v] == DagShortestPath.UNREACHABLE) {
            return new int[0];
        }
        int length = 0;
        for (int x = v; x != -1; x = parent[x]) {
            length++;
        }
        int[] path = new int[length];
        int i = length - 1;
        for (int x = v; x != -1; x = parent[x]) {
            path[i--] = x;
        }
        return path;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DagPathsResult r
                && sumOfDistances == r.sumOfDistances
                && Arrays.equals(distance, r.distance)
                && Arrays.equals(parent, r.parent);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * Long.hashCode(sumOfDistances) + Arrays.hashCode(distance)) + Arrays.hashCode(parent);
    }

    @Override
    public String toString() {
        return "DagPathsResult{sumOfDistances=" + sumOfDistances + "}";
    }
}
