package conveyor.stats;

public final class Anova {
    private Anova() {
    }

    public record Row(String source, double ss, int df, double ms, double f, double fCrit, double p) {
        public boolean significant() {
            return p < 0.05;
        }
    }

    public static Row[] oneWay(String factor, double[][] y) {
        int q = y.length;
        int p = y[0].length;
        double grand = 0;
        double[] means = new double[q];
        for (int j = 0; j < q; j++) {
            for (double v : y[j]) {
                means[j] += v;
            }
            means[j] /= p;
            grand += means[j];
        }
        grand /= q;
        double ssF = 0;
        double ssR = 0;
        for (int j = 0; j < q; j++) {
            ssF += p * (means[j] - grand) * (means[j] - grand);
            for (double v : y[j]) {
                ssR += (v - means[j]) * (v - means[j]);
            }
        }
        int dfF = q - 1;
        int dfR = q * (p - 1);
        double dF = ssF / dfF;
        double dR = ssR / dfR;
        double f = dF / dR;
        return new Row[]{
                new Row(factor, ssF, dfF, dF, f, Special.fisherQuantile(0.95, dfF, dfR),
                        1 - Special.fisherCdf(f, dfF, dfR)),
                new Row("Залишок", ssR, dfR, dR, Double.NaN, Double.NaN, Double.NaN)};
    }

    public static Row[] twoWay(String fa, String fb, double[][][] y) {
        int na = y.length;
        int nb = y[0].length;
        int p = y[0][0].length;
        double[][] cell = new double[na][nb];
        double[] ma = new double[na];
        double[] mb = new double[nb];
        double grand = 0;
        for (int a = 0; a < na; a++) {
            for (int b = 0; b < nb; b++) {
                for (double v : y[a][b]) {
                    cell[a][b] += v;
                }
                cell[a][b] /= p;
                ma[a] += cell[a][b] / nb;
                mb[b] += cell[a][b] / na;
                grand += cell[a][b] / (na * nb);
            }
        }
        double ssA = 0;
        double ssB = 0;
        double ssAB = 0;
        double ssE = 0;
        for (int a = 0; a < na; a++) {
            ssA += nb * p * Math.pow(ma[a] - grand, 2);
        }
        for (int b = 0; b < nb; b++) {
            ssB += na * p * Math.pow(mb[b] - grand, 2);
        }
        for (int a = 0; a < na; a++) {
            for (int b = 0; b < nb; b++) {
                ssAB += p * Math.pow(cell[a][b] - ma[a] - mb[b] + grand, 2);
                for (double v : y[a][b]) {
                    ssE += (v - cell[a][b]) * (v - cell[a][b]);
                }
            }
        }
        int dfA = na - 1;
        int dfB = nb - 1;
        int dfAB = dfA * dfB;
        int dfE = na * nb * (p - 1);
        double msE = ssE / dfE;
        return new Row[]{row(fa, ssA, dfA, msE, dfE), row(fb, ssB, dfB, msE, dfE),
                row("Взаємодія", ssAB, dfAB, msE, dfE),
                new Row("Залишок", ssE, dfE, msE, Double.NaN, Double.NaN, Double.NaN)};
    }

    private static Row row(String name, double ss, int df, double msE, int dfE) {
        double ms = ss / df;
        double f = ms / msE;
        return new Row(name, ss, df, ms, f, Special.fisherQuantile(0.95, df, dfE), 1 - Special.fisherCdf(f, df, dfE));
    }
}
