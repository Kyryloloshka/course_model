package conveyor.model;

import java.util.List;
import java.util.Random;

public final class Model {
    private final ModelConfig cfg;
    private final Store q1;
    private final Store q2;
    private final Source create1;
    private final Source create2;
    private final Kitting kitting;
    private final Conveyor conveyor;

    private final List<Element> events;
    private final EventLog log;
    private double tcurr;
    private double diffArea;

    public Model(ModelConfig cfg) {
        this(cfg, EventLog.OFF);
    }

    public Model(ModelConfig cfg, EventLog log) {
        this.cfg = cfg;
        this.log = log;
        q1 = new Store("Q1", cfg.bufferLimit(), cfg.initial1());
        q2 = new Store("Q2", cfg.bufferLimit(), cfg.initial2());

        create1 = new Source("CREATE1", cfg.interval1(), cfg.batch1(), q1, stream(cfg.seed(), 1), log);
        create2 = new Source("CREATE2", cfg.interval2(), cfg.batch2(), q2, stream(cfg.seed(), 2), log);
        kitting = new Kitting("D", cfg.kitSize(), q1, q2, log);
        conveyor = new Conveyor("CREATE3", cfg.sectionPeriod(), kitting, stream(cfg.seed(), 3));
        events = List.of(create1, create2, conveyor);
        log.attach(() -> "L1=" + q1.length() + " L2=" + q2.length() + " секцій=" + kitting.sections()
                + " пропущено=" + kitting.skipped());
    }

    static Random stream(long seed, int id) {
        long z = seed * 0x9E3779B97F4A7C15L + id * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 30)) * 0x94D049BB133111EBL;
        return new Random(z ^ (z >>> 31));
    }

    public RunResult run() {
        return run(null, Double.POSITIVE_INFINITY);
    }

    public RunResult run(Probe probe, double step) {
        double horizon = cfg.horizon();
        double nextProbe = step;
        while (true) {
            double tn = Double.POSITIVE_INFINITY;
            for (Element e : events) {
                tn = Math.min(tn, e.tnext());
            }
            while (probe != null && nextProbe <= Math.min(tn, horizon)) {
                advanceTo(nextProbe);
                probe.sample(nextProbe, this);
                nextProbe += step;
            }
            if (tn > horizon) {
                advanceTo(horizon);
                break;
            }
            advanceTo(tn);
            for (Element e : events) {
                if (e.tnext() == tcurr) {
                    e.outAct(tcurr);
                }
            }
        }
        return result();
    }

    private void advanceTo(double t) {
        diffArea += Math.abs(q1.length() - q2.length()) * (t - tcurr);
        q1.advance(t);
        q2.advance(t);
        tcurr = t;
    }

    private RunResult result() {
        double h = tcurr;
        double m1 = q1.area() / h;
        double m2 = q2.area() / h;
        double sd1 = Math.sqrt(Math.max(0, q1.area2() / h - m1 * m1));
        double sd2 = Math.sqrt(Math.max(0, q2.area2() / h - m2 * m2));

        double err1 = Math.abs(q1.area() - q1.waitSum() - q1.residualWait(h));
        double err2 = Math.abs(q2.area() - q2.waitSum() - q2.residualWait(h));
        double w1 = q1.taken() == 0 ? 0 : q1.waitSum() / q1.taken();
        double w2 = q2.taken() == 0 ? 0 : q2.waitSum() / q2.taken();
        return new RunResult(h, cfg.kitSize(), kitting.sections(), kitting.filled(), kitting.lack1(), kitting.lack2(),
                kitting.lackBoth(), create1.parts(), create2.parts(), m1, sd1, q1.max(), m2, sd2, q2.max(),
                q1.length(), q2.length(), w1, w2, Math.max(err1, err2), create1.blockedTime(h) / h,
                create2.blockedTime(h) / h, diffArea / h);
    }

    public double time() {
        return tcurr;
    }

    public Store queue1() {
        return q1;
    }

    public Store queue2() {
        return q2;
    }

    public Kitting kitting() {
        return kitting;
    }

    public ModelConfig config() {
        return cfg;
    }

    public String protocol() {
        return log.text();
    }
}
