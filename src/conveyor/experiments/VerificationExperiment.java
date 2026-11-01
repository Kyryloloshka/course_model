package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.Constant;
import conveyor.model.Model;
import conveyor.model.ModelConfig;
import conveyor.model.RunResult;
import conveyor.model.Uniform;

import java.util.Locale;

public final class VerificationExperiment {
    static final double H = 100_000;

    private VerificationExperiment() {
    }

    record Case(String id, String what, ModelConfig cfg) {
    }

    public static void run() {
        ModelConfig b = ModelConfig.base().withHorizon(H);
        Case[] cases = {
                new Case("V1", "базовий варіант 1", b),
                new Case("V2", "усі затримки детерміновані", b.withType1(5, new Constant(5))
                        .withType2(20, new Constant(20))),
                new Case("V3", "подвоєне надходження обох типів", b.withType1(10, new Uniform(5, 1))
                        .withType2(40, new Uniform(20, 7))),
                new Case("V4", "надходження типу 2 вдвічі рідше", b.withType2(20, new Uniform(40, 14))),
                new Case("V5", "надходження типу 1 вдвічі рідше", b.withType1(5, new Uniform(10, 2))),
                new Case("V6", "конвеєр удвічі повільніший", b.withKit(10, 20)),
                new Case("V7", "конвеєр удвічі швидший", b.withKit(10, 5)),
                new Case("V8", "комплект 5 + 5 кожні 5 хв", b.withKit(5, 5)),
                new Case("V9", "варіант 2: 20 + 20 кожні 20 хв", b.withKit(20, 20)),
                new Case("V10", "тип 2 без розкиду інтервалу", b.withType2(20, new Constant(20))),
                new Case("V11", "початковий запас 50 + 50", b.withInitial(50, 50)),
                new Case("V12", "місткість накопичувачів 40 шт.", b.withBufferLimit(40)),
        };
        Csv csv = new Csv("verification", "id", "what", "n1", "tau1", "n2", "tau2", "T", "K", "q0",
                "rho1", "rho2", "arr1", "arr1_th", "arr2", "arr2_th", "sections", "sections_th", "filled",
                "p_skip", "p_th", "mean1", "mean1_th", "mean2", "mean2_th", "max1", "max2", "balance1", "balance2",
                "little_err", "blocked1", "blocked2");
        for (Case c : cases) {
            ModelConfig cfg = c.cfg;
            RunResult r = new Model(cfg).run();
            double s1 = cfg.supply(1);
            double s2 = cfg.supply(2);
            double cap = cfg.capacity();
            double sectionsTh = Math.floor(H / cfg.sectionPeriod().mean());

            double pTh = Math.max(0, 1 - Math.min(s1, s2) / cap);

            double kitRate = Math.min(Math.min(s1, s2), cap);
            double m1Th = (s1 - kitRate) * H / 2;
            double m2Th = (s2 - kitRate) * H / 2;
            long bal1 = cfg.initial1() + r.arrived1() - (long) cfg.kitSize() * r.filled() - r.end1();
            long bal2 = cfg.initial2() + r.arrived2() - (long) cfg.kitSize() * r.filled() - r.end2();
            csv.row(c.id, c.what, cfg.batch1(), cfg.interval1().notation(), cfg.batch2(), cfg.interval2().notation(),
                    cfg.sectionPeriod().notation(), cfg.kitSize(), cfg.initial1(), cfg.load(1), cfg.load(2),
                    r.arrived1(), s1 * H, r.arrived2(), s2 * H, r.sections(), sectionsTh, r.filled(), r.pSkip(), pTh,
                    r.mean1(), m1Th, r.mean2(), m2Th, r.max1(), r.max2(), bal1, bal2, r.littleError(), r.blocked1(),
                    r.blocked2());
            System.out.printf(Locale.ROOT, "%-4s %-34s P=%.4f (теор. >= %.4f) L1=%.1f L2=%.1f баланс %d/%d%n",
                    c.id, c.what, r.pSkip(), pTh, r.mean1(), r.mean2(), bal1, bal2);
        }
        csv.save();
    }
}
