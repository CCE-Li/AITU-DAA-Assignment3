package kz.aitu.daa.algorithms;

import java.util.Arrays;

public record CondensationResult(int componentCount, int[] componentOf,
                                 long criticalPathLength, int[] criticalPath) {

    @Override
    public boolean equals(Object other) {
        return other instanceof CondensationResult r
                && componentCount == r.componentCount
                && criticalPathLength == r.criticalPathLength
                && Arrays.equals(componentOf, r.componentOf)
                && Arrays.equals(criticalPath, r.criticalPath);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * (31 * Integer.hashCode(componentCount) + Long.hashCode(criticalPathLength))
                + Arrays.hashCode(componentOf)) + Arrays.hashCode(criticalPath);
    }

    @Override
    public String toString() {
        return "CondensationResult{componentCount=" + componentCount
                + ", criticalPathLength=" + criticalPathLength + "}";
    }
}
