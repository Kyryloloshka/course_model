package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.ModelConfig;
import conveyor.model.RunResult;
import conveyor.stats.Summary;

import java.util.Locale;

public final class ReplicationsExperiment {
    static final int PILOT = 100;

    private ReplicationsExperiment() {
    }

    public static void run() {
        Csv csv = new Csv("replications", "variant", "response", "pilot_mean", "pilot_sd", "eps", "required");
        Object[][] eps = {
                {Response.P_SKIP, 0.001}, {Response.MEAN1, 0.5}, {Response.MEAN2, 0.5},
                {Response.MAX1, 1.0}, {Response.MAX2, 1.0}};
        for (int v = 1; v <= 2; v++) {
            ModelConfig cfg = v == 1 ? ModelConfig.base() : ModelConfig.variant2();
            RunResult[] runs = Runner.replicate(cfg, PILOT);
            for (Object[] e : eps) {
                Response r = (Response) e[0];
                double eps0 = (double) e[1];
                Summary s = Summary.of(r.of(runs));
                long req = Summary.requiredRuns(s.sd(), eps0);
                csv.row(v, r.name(), s.mean(), s.sd(), eps0, req);
                System.out.printf(Locale.ROOT, "варіант %d %-7s σ=%.4f ε=%.4f -> p=%d%n", v, r.name(), s.sd(), eps0,
                        req);
            }
        }
        csv.save();
    }
}
