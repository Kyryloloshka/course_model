package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.ModelConfig;
import conveyor.model.RunResult;
import conveyor.stats.Summary;

import java.util.Locale;

public final class BufferExperiment {
    static final int[] LIMITS = {30, 40, 50, 60, 80, 100, 150, ModelConfig.UNLIMITED};
    static final double[] HORIZONS = {1440, 48000};
    static final int RUNS = 400;

    private BufferExperiment() {
    }

    public static void run() {
        Csv csv = new Csv("buffer", "variant", "limit", "horizon", "response", "mean", "half");
        Response[] resp = {Response.P_SKIP, Response.MEAN1, Response.MEAN2, Response.MAX1, Response.MAX2,
                Response.WIP, Response.KPH, Response.PPH, Response.WAIT1, Response.WAIT2, Response.BLOCK1, Response.BLOCK2};
        for (int v = 1; v <= 2; v++) {
            for (int limit : LIMITS) {
                if (v == 2 && limit < 40) {
                    continue;
                }
                for (double h : HORIZONS) {
                    ModelConfig cfg = (v == 1 ? ModelConfig.base() : ModelConfig.variant2()).withBufferLimit(limit)
                            .withHorizon(h);
                    RunResult[] runs = Runner.replicate(cfg, RUNS);
                    for (Response r : resp) {
                        Summary s = Summary.of(r.of(runs));
                        csv.row(v, limit == ModelConfig.UNLIMITED ? -1 : limit, h, r.name(), s.mean(), s.halfWidth());
                    }
                    System.out.printf(Locale.ROOT, "варіант %d L_max=%s T_mod=%.0f: P=%.4f WIP=%.1f П=%.3f B2=%.3f%n",
                            v, limit == ModelConfig.UNLIMITED ? "∞" : limit, h,
                            Summary.of(Response.P_SKIP.of(runs)).mean(), Summary.of(Response.WIP.of(runs)).mean(),
                            Summary.of(Response.KPH.of(runs)).mean(), Summary.of(Response.BLOCK2.of(runs)).mean());
                }
            }
        }
        csv.save();
    }
}
