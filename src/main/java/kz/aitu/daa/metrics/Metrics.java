package kz.aitu.daa.metrics;

public final class Metrics {

    private long findCalls;
    private long successfulUnions;
    private long edgeScans;
    private long comparisons;
    private long maxDepth;

    public void addFind() {
        findCalls++;
    }

    public void addSuccessfulUnion() {
        successfulUnions++;
    }

    public void addEdgeScans(long amount) {
        edgeScans += amount;
    }

    public void addComparisons(long amount) {
        comparisons += amount;
    }

    public void observeDepth(long depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    public long findCalls() {
        return findCalls;
    }

    public long successfulUnions() {
        return successfulUnions;
    }

    public long dsuOps() {
        return findCalls + successfulUnions;
    }

    public long edgeScans() {
        return edgeScans;
    }

    public long comparisons() {
        return comparisons;
    }

    public long maxDepth() {
        return maxDepth;
    }

    @Override
    public String toString() {
        return "Metrics{dsuOps=" + dsuOps() + ", edgeScans=" + edgeScans
                + ", comparisons=" + comparisons + ", maxDepth=" + maxDepth + "}";
    }
}
