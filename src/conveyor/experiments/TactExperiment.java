package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.ModelConfig;
import conveyor.model.RunResult;
import conveyor.stats.Summary;

import java.util.Locale;

public final class TactExperiment {
    static final double[] REL = {0.8, 0.85, 0.9, 0.925, 0.95, 0.975, 1.0, 1.025, 1.05, 1.1};
    static final double[] HORIZONS = {1440, 9600};
    static final int RUNS = 400;

    private TactExperiment() {
    }

    public static void run() {
        Csv csv = new Csv("tact", "kit", "T", "rho", "horizon", "response", "mean", "half");
        Response[] resp = {Response.P_SKIP, Response.MEAN1, Response.MEAN2, Response.MAX1, Response.MAX2,
                Response.WIP, Response.KPH, Response.PPH, Response.WAIT1, Response.WAIT2};
        for (int kit : new int[]{10, 20}) {
            for (double rel : REL) {
                double T = kit * rel;
                for (double h : HORIZONS) {
                    ModelConfig cfg = ModelConfig.base().withKit(kit, T).withHorizon(h);
                    RunResult[] runs = Runner.replicate(cfg, RUNS);
                    for (Response r : resp) {
                        Summary s = Summary.of(r.of(runs));
                        csv.row(kit, T, cfg.load(1), h, r.name(), s.mean(), s.halfWidth());
                    }
                    if (h == 1440) {
                        System.out.printf(Locale.ROOT, "K=%d T=%.2f P=%.4f WIP=%.1f%n", kit, T,
                                Summary.of(Response.P_SKIP.of(runs)).mean(), Summary.of(Response.WIP.of(runs)).mean());
                    }
                }
            }
        }
        csv.save();
    }
}
