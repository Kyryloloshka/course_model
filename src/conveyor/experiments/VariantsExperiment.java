package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.ModelConfig;
import conveyor.model.RunResult;
import conveyor.stats.Anova;
import conveyor.stats.Summary;

import java.util.Locale;

public final class VariantsExperiment {
    public static final int RUNS = 15000;

    static final Response[] MAIN = {Response.P_SKIP, Response.MEAN1, Response.MEAN2, Response.MAX1,
            Response.MAX2, Response.WAIT1, Response.WAIT2, Response.WIP, Response.KPH, Response.PPH};

    private VariantsExperiment() {
    }

    public static void run() {
        RunResult[] v1 = Runner.replicate(ModelConfig.base(), RUNS);
        RunResult[] v2 = Runner.replicate(ModelConfig.variant2(), RUNS);
        Csv est = new Csv("estimates", "variant", "response", "mean", "sd", "half", "min", "max");
        Csv cmp = new Csv("variants_anova", "response", "mean1", "mean2", "diff", "diff_half", "ss_f", "df_f", "ms_f",
                "ss_r", "df_r", "ms_r", "F", "F_crit", "p");
        for (Response r : MAIN) {
            double[] a = r.of(v1);
            double[] b = r.of(v2);
            Summary sa = Summary.of(a);
            Summary sb = Summary.of(b);
            est.row(1, r.name(), sa.mean(), sa.sd(), sa.halfWidth(), sa.min(), sa.max());
            est.row(2, r.name(), sb.mean(), sb.sd(), sb.halfWidth(), sb.min(), sb.max());
            double[] d = new double[RUNS];
            for (int i = 0; i < RUNS; i++) {
                d[i] = b[i] - a[i];
            }
            Summary sd = Summary.of(d);
            Anova.Row[] t = Anova.oneWay("Варіант", new double[][]{a, b});
            cmp.row(r.name(), sa.mean(), sb.mean(), sd.mean(), sd.halfWidth(), t[0].ss(), t[0].df(), t[0].ms(),
                    t[1].ss(), t[1].df(), t[1].ms(), t[0].f(), t[0].fCrit(), t[0].p());
            System.out.printf(Locale.ROOT, "%-7s в1=%.4f±%.4f в2=%.4f±%.4f різниця %.4f±%.4f F=%.2f p=%.4g%n",
                    r.name(), sa.mean(), sa.halfWidth(), sb.mean(), sb.halfWidth(), sd.mean(), sd.halfWidth(),
                    t[0].f(), t[0].p());
        }
        est.save();
        cmp.save();

        Csv raw = new Csv("runs_day", "variant", "seed", "p_skip", "mean1", "mean2", "max1", "max2", "wait1",
                "wait2", "kph", "lack1", "lack2", "lack_both", "sections");
        for (int v = 1; v <= 2; v++) {
            RunResult[] rr = v == 1 ? v1 : v2;
            for (int i = 0; i < RUNS; i++) {
                RunResult x = rr[i];
                raw.row(v, i + 1, x.pSkip(), x.mean1(), x.mean2(), x.max1(), x.max2(), x.wait1(), x.wait2(),
                        x.kitsPerHour(), x.lack1(), x.lack2(), x.lackBoth(), x.sections());
            }
        }
        raw.save();
    }
}
