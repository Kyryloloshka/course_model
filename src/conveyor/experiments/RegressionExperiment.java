package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.ModelConfig;
import conveyor.model.RunResult;
import conveyor.model.Uniform;
import conveyor.stats.Special;

import java.util.Locale;

public final class RegressionExperiment {
    static final int MEASURES = 4;
    static final int PER_MEASURE = 100;
    static final int RUNS = MEASURES * PER_MEASURE;
    static final String[] FACTOR = {"δ2", "T", "L(0)"};
    static final double[] LOW = {3.5, 9.5, 0};
    static final double[] HIGH = {10.5, 10.0, 30};
    static final String[] TERM = {"b0", "b1", "b2", "b3", "b12", "b13", "b23", "b123"};

    private RegressionExperiment() {
    }

    public static void run() {
        int n = 8;
        int[][] x = new int[n][4];
        for (int i = 0; i < n; i++) {
            x[i][0] = 1;
            x[i][1] = (i & 1) == 0 ? -1 : 1;
            x[i][2] = (i & 2) == 0 ? -1 : 1;
            x[i][3] = (i & 4) == 0 ? -1 : 1;
        }
        Response[] resp = {Response.P_SKIP, Response.WIP};
        double[][] mean = new double[resp.length][n];
        double[][] var = new double[resp.length][n];
        double[][][] measure = new double[resp.length][n][MEASURES];
        Csv design = new Csv("pfe_design", "row", "x1", "x2", "x3", "delta2", "T", "L0", "response", "y1", "y2", "y3",
                "y4", "mean", "var");
        for (int i = 0; i < n; i++) {
            double d2 = level(0, x[i][1]);
            double t = level(1, x[i][2]);
            int l0 = (int) level(2, x[i][3]);
            ModelConfig cfg = ModelConfig.base().withType2(20, new Uniform(20, d2)).withKit(10, t).withInitial(l0, l0);
            RunResult[] runs = Runner.replicate(cfg, RUNS);
            for (int r = 0; r < resp.length; r++) {
                double[] y = resp[r].of(runs);
                double[] ym = measure[r][i];
                for (int k = 0; k < RUNS; k++) {
                    ym[k / PER_MEASURE] += y[k] / PER_MEASURE;
                }
                double m = 0;
                for (double v : ym) {
                    m += v / MEASURES;
                }
                double ss = 0;
                for (double v : ym) {
                    ss += (v - m) * (v - m);
                }
                mean[r][i] = m;
                var[r][i] = ss / (MEASURES - 1);
                design.row(i + 1, x[i][1], x[i][2], x[i][3], d2, t, l0, resp[r].name(), ym[0], ym[1], ym[2], ym[3],
                        m, var[r][i]);
            }
        }
        design.save();

        Csv coef = new Csv("pfe_coef", "response", "term", "b", "t", "t_crit", "significant");
        Csv stats = new Csv("pfe_stats", "response", "G", "G_crit", "S2y", "Sb", "d", "S2ad", "F", "F_crit", "adequate");
        for (int r = 0; r < resp.length; r++) {
            double sumVar = 0;
            double maxVar = 0;
            for (double v : var[r]) {
                sumVar += v;
                maxVar = Math.max(maxVar, v);
            }
            int f = MEASURES - 1;
            double fq = Special.fisherQuantile(1 - 0.05 / n, f, (n - 1) * f);
            double gCrit = fq / (fq + n - 1);
            double g = maxVar / sumVar;
            double s2y = sumVar / n;
            double sb = Math.sqrt(s2y / (n * MEASURES));
            int dfE = n * (MEASURES - 1);
            double tCrit = Special.studentQuantile(0.975, dfE);
            double[] b = new double[TERM.length];
            boolean[] sig = new boolean[TERM.length];
            int d = 0;
            for (int j = 0; j < TERM.length; j++) {
                for (int i = 0; i < n; i++) {
                    b[j] += column(x[i], j) * mean[r][i] / n;
                }
                double tj = Math.abs(b[j]) / sb;
                sig[j] = tj > tCrit;
                if (sig[j]) {
                    d++;
                }
                coef.row(resp[r].name(), TERM[j], b[j], tj, tCrit, sig[j] ? "так" : "ні");
            }
            double ssAd = 0;
            for (int i = 0; i < n; i++) {
                double yh = 0;
                for (int j = 0; j < TERM.length; j++) {
                    if (sig[j]) {
                        yh += b[j] * column(x[i], j);
                    }
                }
                ssAd += (yh - mean[r][i]) * (yh - mean[r][i]);
            }
            double s2ad = n > d ? MEASURES * ssAd / (n - d) : Double.NaN;
            double fv = n > d ? s2ad / s2y : Double.NaN;
            double fCrit = n > d ? Special.fisherQuantile(0.95, n - d, dfE) : Double.NaN;
            String adequate = n > d ? (fv < fCrit ? "так" : "ні") : "-";
            stats.row(resp[r].name(), g, gCrit, s2y, sb, d, s2ad, fv, fCrit, adequate);
            System.out.printf(Locale.ROOT, "%-7s G=%.4f (кр. %.4f) d=%d F=%.3f (кр. %.3f)%n", resp[r].name(), g, gCrit, d,
                    fv, fCrit);
        }
        coef.save();
        stats.save();
    }

    static double level(int factor, int code) {
        return code < 0 ? LOW[factor] : HIGH[factor];
    }

    static int column(int[] row, int term) {
        return switch (term) {
            case 0 -> 1;
            case 1 -> row[1];
            case 2 -> row[2];
            case 3 -> row[3];
            case 4 -> row[1] * row[2];
            case 5 -> row[1] * row[3];
            case 6 -> row[2] * row[3];
            default -> row[1] * row[2] * row[3];
        };
    }
}
