package conveyor;

import conveyor.experiments.BufferExperiment;
import conveyor.experiments.DistributionExperiment;
import conveyor.experiments.FactorialExperiment;
import conveyor.experiments.GrowthExperiment;
import conveyor.experiments.ReplicationsExperiment;
import conveyor.experiments.StockExperiment;
import conveyor.experiments.TactExperiment;
import conveyor.experiments.TraceExperiment;
import conveyor.experiments.TransientExperiment;
import conveyor.experiments.VariantsExperiment;
import conveyor.experiments.VerificationExperiment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Map<String, Runnable> all = new LinkedHashMap<>();
        all.put("trace", TraceExperiment::run);
        all.put("verify", VerificationExperiment::run);
        all.put("transient", TransientExperiment::run);
        all.put("growth", GrowthExperiment::run);
        all.put("reps", ReplicationsExperiment::run);
        all.put("variants", VariantsExperiment::run);
        all.put("factorial", FactorialExperiment::run);
        all.put("tact", TactExperiment::run);
        all.put("stock", StockExperiment::run);
        all.put("buffer", BufferExperiment::run);
        all.put("dist", DistributionExperiment::run);
        List<String> names = args.length == 0 ? List.copyOf(all.keySet()) : List.of(args);
        for (String n : names) {
            Runnable r = all.get(n);
            if (r == null) {
                System.err.println("невідомий експеримент: " + n + "; доступні: " + all.keySet());
                System.exit(1);
            }
            long t0 = System.nanoTime();
            System.out.println("=== " + n);
            r.run();
            System.out.printf("--- %s: %.1f с%n", n, (System.nanoTime() - t0) / 1e9);
        }
    }
}
