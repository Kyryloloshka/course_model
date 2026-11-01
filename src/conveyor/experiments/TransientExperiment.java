package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.Model;
import conveyor.model.ModelConfig;

public final class TransientExperiment {
    static final double H = 48_000;
    static final double STEP = 240;
    static final int RUNS = 8;

    private TransientExperiment() {
    }

    public static void run() {
        Csv csv = new Csv("transient", "config", "run", "t", "mean1", "mean2", "p_skip", "q1", "q2");
        Object[][] configs = {
                {"K10_T10", ModelConfig.base()},
                {"K20_T20", ModelConfig.variant2()},
                {"K10_T9", ModelConfig.base().withKit(10, 9)},
                {"K10_L60", ModelConfig.base().withBufferLimit(60)},
        };
        for (Object[] c : configs) {
            String name = (String) c[0];
            ModelConfig cfg = ((ModelConfig) c[1]).withHorizon(H);
            for (int run = 1; run <= RUNS; run++) {
                final int rr = run;
                Model m = new Model(cfg.withSeed(run));
                m.run((t, model) -> {
                    double p = model.kitting().sections() == 0 ? 0
                            : (double) model.kitting().skipped() / model.kitting().sections();
                    csv.row(name, rr, t, model.queue1().area() / t, model.queue2().area() / t, p,
                            model.queue1().length(), model.queue2().length());
                }, STEP);
            }
            System.out.println("перехідний період: " + name);
        }
        csv.save();
    }
}
