package kz.aitu.daa.algorithms;

import java.util.Arrays;

public record SccResult(int componentCount, int[] componentOf) {

    @Override
    public boolean equals(Object other) {
        return other instanceof SccResult r
                && componentCount == r.componentCount
                && Arrays.equals(componentOf, r.componentOf);
    }

    @Override
    public int hashCode() {
        return 31 * Integer.hashCode(componentCount) + Arrays.hashCode(componentOf);
    }

    @Override
    public String toString() {
        return "SccResult{componentCount=" + componentCount + "}";
    }
}
