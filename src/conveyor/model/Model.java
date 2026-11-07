package conveyor.model;

import conveyor.qnet.Create;
import conveyor.qnet.Dispose;
import conveyor.qnet.EventLog;
import conveyor.qnet.Network;
import conveyor.qnet.Process;
import conveyor.qnet.Request;
import conveyor.qnet.Route;

import java.util.Random;

public final class Model {
    private final ModelConfig cfg;
    private final Network net = new Network();
    private final Create arrive1;
    private final Create arrive2;
    private final Create sections;
    private final Process store1;
    private final Process store2;
    private final Process kitting;
    private final Dispose out1;
    private final Dispose out2;
    private final Dispose filled;
    private final Dispose skipped;
    private Route leave1;
    private Route leave2;
    private long lack1;
    private long lack2;
    private long lackBoth;
    private double diffArea;

    public Model(ModelConfig cfg) {
        this(cfg, EventLog.OFF);
    }

    public Model(ModelConfig cfg, EventLog log) {
        this.cfg = cfg;
        int k = cfg.kitSize();
        Constant zero = new Constant(0);
        arrive1 = net.add(new Create("Надходження 1", cfg.interval1(), stream(cfg.seed(), 1)));
        arrive2 = net.add(new Create("Надходження 2", cfg.interval2(), stream(cfg.seed(), 2)));
        sections = net.add(new Create("Секції", cfg.sectionPeriod(), stream(cfg.seed(), 3)));
        store1 = net.add(new Process("СМО1", zero, null, 1, Process.UNLIMITED));
        store2 = net.add(new Process("СМО2", zero, null, 1, Process.UNLIMITED));
        kitting = net.add(new Process("СМО3", zero, null, 1, 0));
        out1 = net.add(new Dispose("N1"));
        out2 = net.add(new Dispose("N2"));
        filled = net.add(new Dispose("Nукомпл"));
        skipped = net.add(new Dispose("Nпроп").onArrival(this::classifySkip));

        arrive1.route(Route.to(store1).ungroup(cfg.batch1())
                .blockedWhen(() -> store1.content() + cfg.batch1() > cfg.bufferLimit()));
        arrive2.route(Route.to(store2).ungroup(cfg.batch2())
                .blockedWhen(() -> store2.content() + cfg.batch2() > cfg.bufferLimit()));
        leave1 = Route.to(out1).group(k).blockedWhen(() -> this.leave1.passed() >= (long) k * kitting.arrivals());
        leave2 = Route.to(out2).group(k).blockedWhen(() -> this.leave2.passed() >= (long) k * kitting.arrivals());
        store1.route(leave1);
        store2.route(leave2);
        sections.route(Route.to(kitting).priority(2)
                .blockedWhen(() -> store1.content() < k || store2.content() < k));
        sections.route(Route.to(skipped).priority(1));
        kitting.route(Route.to(filled));

        for (int i = 0; i < cfg.initial1(); i++) {
            store1.preload(new Request(-1 - i, 0));
        }
        for (int i = 0; i < cfg.initial2(); i++) {
            store2.preload(new Request(-1 - i, 0));
        }
        log.attach(() -> "L1=" + store1.content() + " L2=" + store2.content() + " Nсекц="
                + (kitting.arrivals() + skipped.quantity()) + " Nпроп=" + skipped.quantity());
        net.log(log);
    }

    static Random stream(long seed, int id) {
        long z = seed * 0x9E3779B97F4A7C15L + id * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 30)) * 0x94D049BB133111EBL;
        return new Random(z ^ (z >>> 31));
    }

    private void classifySkip(double t) {
        boolean no1 = store1.content() < cfg.kitSize();
        boolean no2 = store2.content() < cfg.kitSize();
        if (no1 && no2) {
            lackBoth++;
        } else if (no1) {
            lack1++;
        } else {
            lack2++;
        }
    }

    public RunResult run() {
        return run(null, Double.POSITIVE_INFINITY);
    }

    public RunResult run(Probe probe, double step) {
        net.simulate(cfg.horizon(), probe == null ? null : t -> probe.sample(t, this), step,
                delta -> diffArea += Math.abs(store1.content() - store2.content()) * delta);
        return result();
    }

    private RunResult result() {
        double h = net.time();
        double m1 = store1.contentArea() / h;
        double m2 = store2.contentArea() / h;
        double sd1 = Math.sqrt(Math.max(0, store1.contentArea2() / h - m1 * m1));
        double sd2 = Math.sqrt(Math.max(0, store2.contentArea2() / h - m2 * m2));
        double w1 = leave1.passed() == 0 ? 0 : store1.contentArea() / leave1.passed();
        double w2 = leave2.passed() == 0 ? 0 : store2.contentArea() / leave2.passed();
        return new RunResult(h, cfg.kitSize(), sections(), filled.quantity(), lack1, lack2, lackBoth,
                arrive1.quantity() * cfg.batch1(), arrive2.quantity() * cfg.batch2(), m1, sd1, store1.maxContent(),
                m2, sd2, store2.maxContent(), store1.content(), store2.content(), w1, w2,
                arrive1.blockedTime(h) / h, arrive2.blockedTime(h) / h, diffArea / h);
    }

    public long sections() {
        return kitting.arrivals() + skipped.quantity();
    }

    public long skippedSections() {
        return skipped.quantity();
    }

    public double area1() {
        return store1.contentArea();
    }

    public double area2() {
        return store2.contentArea();
    }

    public long content1() {
        return store1.content();
    }

    public long content2() {
        return store2.content();
    }

    public long kits1() {
        return out1.quantity();
    }

    public long kits2() {
        return out2.quantity();
    }

    public ModelConfig config() {
        return cfg;
    }
}
