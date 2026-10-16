package kz.aitu.daa.algorithms;

import java.util.Arrays;

public record KahnResult(int verticesInOrder, int[] order, boolean hasCycle) {

    @Override
    public boolean equals(Object other) {
        return other instanceof KahnResult r
                && verticesInOrder == r.verticesInOrder
                && hasCycle == r.hasCycle
                && Arrays.equals(order, r.order);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * Integer.hashCode(verticesInOrder) + Boolean.hashCode(hasCycle)) + Arrays.hashCode(order);
    }

    @Override
    public String toString() {
        return "KahnResult{verticesInOrder=" + verticesInOrder + ", hasCycle=" + hasCycle + "}";
    }
}
