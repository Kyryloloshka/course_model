package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.Constant;
import conveyor.model.EventLog;
import conveyor.model.Model;
import conveyor.model.ModelConfig;
import conveyor.model.RunResult;

public final class TraceExperiment {
    private TraceExperiment() {
    }

    public static void run() {
        trace("base", ModelConfig.base().withHorizon(150));
        trace("variant2", ModelConfig.variant2().withHorizon(150));
        trace("deterministic", ModelConfig.base()
                .withType1(5, new Constant(5)).withType2(20, new Constant(20)).withHorizon(60));
    }

    private static void trace(String name, ModelConfig cfg) {
        EventLog log = new EventLog(10_000);
        Model m = new Model(cfg, log);
        RunResult r = m.run();
        String head = String.format("%9s | %-8s | %-62s | %s%n", "t, хв", "елемент", "зміна стану", "стан після події");
        Csv.text("trace/" + name + ".txt", head + m.protocol()
                + "\nT_mod = " + r.horizon() + " хв; секцій " + r.sections() + ", укомплектовано " + r.filled()
                + ", пропущено " + r.skipped() + "; надійшло виробів: тип 1 - " + r.arrived1() + ", тип 2 - "
                + r.arrived2() + "\n");
        System.out.println("протокол " + name + ": " + r.sections() + " секцій");
    }
}
