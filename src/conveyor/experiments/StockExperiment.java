package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.ModelConfig;
import conveyor.model.RunResult;
import conveyor.stats.Summary;

import java.util.Locale;

public final class StockExperiment {
    static final int[] STOCK = {0, 10, 20, 30, 40, 60, 80};
    static final int RUNS = 1000;

    private StockExperiment() {
    }

    public static void run() {
        Csv csv = new Csv("stock", "variant", "stock", "response", "mean", "half");
        Response[] resp = {Response.P_SKIP, Response.MEAN1, Response.MEAN2, Response.WIP, Response.WAIT1,
                Response.WAIT2};
        for (int v = 1; v <= 2; v++) {
            for (int s : STOCK) {
                ModelConfig cfg = (v == 1 ? ModelConfig.base() : ModelConfig.variant2()).withInitial(s, s);
                RunResult[] runs = Runner.replicate(cfg, RUNS);
                for (Response r : resp) {
                    Summary sm = Summary.of(r.of(runs));
                    csv.row(v, s, r.name(), sm.mean(), sm.halfWidth());
                }
                System.out.printf(Locale.ROOT, "варіант %d запас %d: P=%.4f WIP=%.1f%n", v, s,
                        Summary.of(Response.P_SKIP.of(runs)).mean(), Summary.of(Response.WIP.of(runs)).mean());
            }
        }
        csv.save();
    }
}
