package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.ModelConfig;
import conveyor.model.RunResult;
import conveyor.model.Uniform;
import conveyor.stats.Anova;
import conveyor.stats.Summary;

import java.util.Locale;

public final class FactorialExperiment {
    static final double[] SPREAD = {0, 3.5, 7, 10.5};
    static final int RUNS = 300;

    private FactorialExperiment() {
    }

    public static void run() {
        Response[] resp = {Response.P_SKIP, Response.MEAN1, Response.MEAN2, Response.MAX2};
        double[][][][] y = new double[resp.length][2][SPREAD.length][];
        Csv cells = new Csv("factorial_cells", "response", "variant", "spread2", "mean", "half");
        for (int v = 0; v < 2; v++) {
            for (int b = 0; b < SPREAD.length; b++) {
                ModelConfig cfg = (v == 0 ? ModelConfig.base() : ModelConfig.variant2())
                        .withType2(20, new Uniform(20, SPREAD[b]));
                RunResult[] runs = Runner.replicate(cfg, RUNS);
                for (int i = 0; i < resp.length; i++) {
                    y[i][v][b] = resp[i].of(runs);
                    Summary s = Summary.of(y[i][v][b]);
                    cells.row(resp[i].name(), v + 1, SPREAD[b], s.mean(), s.halfWidth());
                }
            }
        }
        cells.save();
        Csv tab = new Csv("factorial_anova", "response", "source", "ss", "df", "ms", "F", "F_crit", "p");
        for (int i = 0; i < resp.length; i++) {
            for (Anova.Row r : Anova.twoWay("Варіант (A)", "Розкид δ2 (B)", y[i])) {
                tab.row(resp[i].name(), r.source(), r.ss(), r.df(), r.ms(), r.f(), r.fCrit(), r.p());
                System.out.printf(Locale.ROOT, "%-7s %-14s F=%.2f Fкр=%.2f p=%.3g%n", resp[i].name(), r.source(),
                        r.f(), r.fCrit(), r.p());
            }
        }
        tab.save();
    }
}
