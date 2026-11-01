package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.ModelConfig;
import conveyor.model.RunResult;
import conveyor.stats.Regression;
import conveyor.stats.Summary;

import java.util.Locale;

public final class GrowthExperiment {
    static final double[] HORIZONS = {240, 480, 960, 1440, 2400, 4800, 9600, 19200, 48000, 96000};
    static final int RUNS = 400;

    private GrowthExperiment() {
    }

    public static void run() {
        Csv csv = new Csv("growth", "variant", "horizon", "response", "mean", "sd", "half");
        Csv fits = new Csv("growth_fit", "variant", "response", "a", "b", "b_se", "r2");
        Csv sqrt = new Csv("growth_sqrt", "variant", "response", "f", "b0", "b1", "b1_se", "r2");
        Response[] resp = {Response.P_SKIP, Response.MEAN1, Response.MEAN2, Response.MAX1, Response.MAX2,
                Response.WAIT1, Response.WAIT2, Response.DIFF, Response.WIP};
        for (int v = 1; v <= 2; v++) {
            ModelConfig cfg = v == 1 ? ModelConfig.base() : ModelConfig.variant2();
            double[][] means = new double[resp.length][HORIZONS.length];
            for (int h = 0; h < HORIZONS.length; h++) {
                RunResult[] runs = Runner.replicate(cfg.withHorizon(HORIZONS[h]), RUNS);
                for (int i = 0; i < resp.length; i++) {
                    Summary s = Summary.of(resp[i].of(runs));
                    means[i][h] = s.mean();
                    csv.row(v, HORIZONS[h], resp[i].name(), s.mean(), s.sd(), s.halfWidth());
                }
            }
            for (int i = 0; i < resp.length; i++) {
                int from = 3;
                int n = HORIZONS.length - from;
                double[][] x = new double[n][1];
                double[] y = new double[n];
                for (int h = from; h < HORIZONS.length; h++) {
                    x[h - from][0] = Math.log(HORIZONS[h]);
                    y[h - from] = Math.log(means[i][h]);
                }
                Regression.Fit f = Regression.fit(x, y);
                fits.row(v, resp[i].name(), Math.exp(f.b()[0]), f.b()[1], f.se()[1], f.r2());
                System.out.printf(Locale.ROOT, "варіант %d %-7s y = %.4f·T^%.3f (R²=%.4f)%n", v, resp[i].name(),
                        Math.exp(f.b()[0]), f.b()[1], f.r2());

                boolean inverse = resp[i] == Response.P_SKIP;
                double[][] xs = new double[n][1];
                double[] ys = new double[n];
                for (int h = from; h < HORIZONS.length; h++) {
                    xs[h - from][0] = inverse ? 1 / Math.sqrt(HORIZONS[h]) : Math.sqrt(HORIZONS[h]);
                    ys[h - from] = means[i][h];
                }
                Regression.Fit g = Regression.fit(xs, ys);
                sqrt.row(v, resp[i].name(), inverse ? "1/sqrtT" : "sqrtT", g.b()[0], g.b()[1], g.se()[1], g.r2());
                System.out.printf(Locale.ROOT, "              y = %.4f + %.4f·%s (R²=%.5f)%n", g.b()[0], g.b()[1],
                        inverse ? "1/√T" : "√T", g.r2());
            }
        }
        csv.save();
        fits.save();
        sqrt.save();
    }
}
