package kz.aitu.daa.metrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class CsvWriter {

    private final StringBuilder content = new StringBuilder();

    public CsvWriter() {
        content.append("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result\n");
    }

    public void row(String algorithm, String graph, int n, int m, double timeMs,
                    long dsuOps, long edgeScans, long comparisons, long maxDepth, long result) {
        content.append(algorithm).append(',')
                .append(graph).append(',')
                .append(n).append(',')
                .append(m).append(',')
                .append(String.format(Locale.ROOT, "%.3f", timeMs)).append(',')
                .append(dsuOps).append(',')
                .append(edgeScans).append(',')
                .append(comparisons).append(',')
                .append(maxDepth).append(',')
                .append(result).append('\n');
    }

    public void writeTo(Path path) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, content.toString(), StandardCharsets.UTF_8);
    }
}
