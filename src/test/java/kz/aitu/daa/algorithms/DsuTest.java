package kz.aitu.daa.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import kz.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

class DsuTest {

    @Test
    void findAgreesWithReferenceAfterRandomUnions() {
        int n = 300;
        Random rnd = new Random(7);
        Metrics metrics = new Metrics();
        Dsu dsu = new Dsu(n, metrics);
        int[] label = new int[n];
        for (int i = 0; i < n; i++) {
            label[i] = i;
        }
        for (int step = 0; step < 5_000; step++) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            boolean merged = dsu.union(a, b);
            assertEquals(referenceUnion(label, a, b), merged, "union(" + a + ", " + b + ")");
            assertEquals(referenceComponents(label), dsu.components());
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                assertEquals(label[i] == label[j], dsu.find(i) == dsu.find(j),
                        "same-set check for " + i + " and " + j);
            }
        }
        assertTrue(metrics.findCalls() > 0);
        assertEquals(metrics.findCalls() + metrics.successfulUnions(), metrics.dsuOps());
    }

    @Test
    void unionByRankKeepsFindsShallow() {
        int n = 10_000;
        Metrics metrics = new Metrics();
        Dsu dsu = new Dsu(n, metrics);
        for (int i = 1; i < n; i++) {
            dsu.union(0, i);
        }
        assertEquals(1, dsu.components());
        long before = metrics.findCalls();
        for (int i = 0; i < n; i++) {
            dsu.find(i);
        }
        assertTrue(metrics.findCalls() - before >= n);
    }

    private static boolean referenceUnion(int[] label, int a, int b) {
        if (label[a] == label[b]) {
            return false;
        }
        int from = label[b];
        int to = label[a];
        for (int i = 0; i < label.length; i++) {
            if (label[i] == from) {
                label[i] = to;
            }
        }
        return true;
    }

    private static int referenceComponents(int[] label) {
        int count = 0;
        for (int i = 0; i < label.length; i++) {
            if (label[i] == i) {
                count++;
            }
        }
        return count;
    }
}
