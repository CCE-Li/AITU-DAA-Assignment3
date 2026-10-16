package kz.aitu.daa.benchmark;

public final class ReferenceValues {

    public record Row(int n, int m, long mstWeight, long sccCount, long dagDistanceSum) {
    }

    public static final Row[] ROWS = {
            new Row(1_000, 4_000, 147_970L, 27L, 396_274L),
            new Row(1_000, 20_000, 30_363L, 1L, 252_411L),
            new Row(3_162, 12_648, 468_911L, 137L, 1_271_911L),
            new Row(3_162, 63_240, 97_589L, 1L, 723_157L),
            new Row(10_000, 40_000, 1_486_139L, 373L, 3_941_828L),
            new Row(10_000, 200_000, 309_550L, 1L, 2_381_342L),
            new Row(31_623, 126_492, 4_715_515L, 1_188L, 12_551_891L),
            new Row(31_623, 632_460, 968_517L, 1L, 7_376_612L),
            new Row(100_000, 400_000, 15_012_094L, 3_923L, 40_096_539L),
            new Row(100_000, 2_000_000, 3_056_681L, 1L, 23_512_087L),
    };

    private ReferenceValues() {
    }

    public static Row get(int n, int m) {
        for (Row row : ROWS) {
            if (row.n() == n && row.m() == m) {
                return row;
            }
        }
        throw new IllegalArgumentException("no reference row for n=" + n + ", m=" + m);
    }
}
