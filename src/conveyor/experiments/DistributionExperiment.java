package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.Model;
import conveyor.model.ModelConfig;
import conveyor.model.RunResult;
import conveyor.stats.ChiSquare;
import conveyor.stats.Special;
import conveyor.stats.Summary;

import java.util.Locale;
import java.util.function.DoubleUnaryOperator;

public final class DistributionExperiment {
    static final int RUNS = 1000;

    private DistributionExperiment() {
    }

    static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    public static void run() {
        Csv tests = new Csv("dist_tests", "variant", "response", "law", "params", "chi2", "df", "crit", "p",
                "accepted");
        Csv bins = new Csv("dist_bins", "variant", "response", "law", "lo", "hi", "observed", "expected");
        for (int v = 1; v <= 2; v++) {
            ModelConfig cfg = v == 1 ? ModelConfig.base() : ModelConfig.variant2();
            RunResult[] runs = Runner.replicate(cfg, RUNS);
            for (Response r : new Response[]{Response.MAX1, Response.MAX2, Response.MEAN1, Response.MEAN2}) {
                double[] x = r.of(runs);
                Summary s = Summary.of(x);
                double m = s.mean();
                double sd = s.sd();
                double ls2 = Math.log(1 + sd * sd / (m * m));
                double lm = Math.log(m) - ls2 / 2;
                double ls = Math.sqrt(ls2);
                double k = m * m / (sd * sd);
                double theta = sd * sd / m;
                Object[][] laws = {
                        {"нормальний", String.format(Locale.ROOT, "a = %.3f, σ = %.3f", m, sd),
                                (DoubleUnaryOperator) z -> Special.normalCdf((z - m) / sd)},
                        {"логнормальний", String.format(Locale.ROOT, "μ = %.4f, σ = %.4f", lm, ls),
                                (DoubleUnaryOperator) z -> z <= 0 ? 0 : Special.normalCdf((Math.log(z) - lm) / ls)},
                        {"гамма", String.format(Locale.ROOT, "k = %.3f, θ = %.3f", k, theta),
                                (DoubleUnaryOperator) z -> Special.gammaP(k, z / theta)},
                };

                double lattice = r == Response.MAX1 ? gcd(cfg.batch1(), cfg.kitSize())
                        : r == Response.MAX2 ? gcd(cfg.batch2(), cfg.kitSize()) : 0;
                for (Object[] law : laws) {
                    ChiSquare.Result res = lattice > 0
                            ? ChiSquare.testLattice((String) law[0], (String) law[1], x, lattice,
                            (DoubleUnaryOperator) law[2], 2)
                            : ChiSquare.test((String) law[0], (String) law[1], x, 20, (DoubleUnaryOperator) law[2], 2);
                    tests.row(v, r.name(), res.law(), res.params(), res.chi2(), res.df(), res.critical(), res.p(),
                            res.accepted() ? "так" : "ні");
                    for (ChiSquare.Bin b : res.bins()) {
                        bins.row(v, r.name(), res.law(), b.lo(), b.hi(), b.observed(), b.expected());
                    }
                    System.out.printf(Locale.ROOT, "варіант %d %-7s %-14s χ²=%.2f (кр. %.2f, df=%d) %s%n", v,
                            r.name(), res.law(), res.chi2(), res.critical(), res.df(), res.accepted() ? "+" : "-");
                }
            }
        }
        tests.save();
        bins.save();

        Csv occ = new Csv("queue_occupancy", "variant", "queue", "length", "share");
        for (int v = 1; v <= 2; v++) {
            ModelConfig cfg = v == 1 ? ModelConfig.base() : ModelConfig.variant2();
            double[][] acc = new double[2][400];
            double total = 0;
            for (int s = 1; s <= 200; s++) {
                Model m = new Model(cfg.withSeed(s));
                m.run();
                double[][] t = {m.queue1().timeAtLength(), m.queue2().timeAtLength()};
                for (int q = 0; q < 2; q++) {
                    for (int l = 0; l < Math.min(t[q].length, 400); l++) {
                        acc[q][l] += t[q][l];
                    }
                }
                total += cfg.horizon();
            }
            for (int q = 0; q < 2; q++) {
                for (int l = 0; l < 400; l++) {
                    if (acc[q][l] > 0) {
                        occ.row(v, q + 1, l, acc[q][l] / total);
                    }
                }
            }
        }
        occ.save();
    }
}
