package kz.aitu.daa.algorithms;

public record MstResult(long totalWeight, int components, int treeEdges) {

    @Override
    public String toString() {
        return "MstResult{totalWeight=" + totalWeight + ", components=" + components
                + ", treeEdges=" + treeEdges + "}";
    }
}
