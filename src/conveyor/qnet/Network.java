package conveyor.qnet;

import java.util.ArrayList;
import java.util.List;

public final class Network {
    private final List<Element> elements = new ArrayList<>();
    private EventLog log = EventLog.OFF;
    private double tcurr;

    public <E extends Element> E add(E element) {
        elements.add(element);
        element.attach(log);
        return element;
    }

    public void log(EventLog log) {
        this.log = log;
        for (Element e : elements) {
            e.attach(log);
        }
    }

    public double time() {
        return tcurr;
    }

    public interface Probe {
        void sample(double t);
    }

    public void simulate(double horizon, Probe probe, double step, Statistics stats) {
        double nextProbe = step;
        while (true) {
            double tn = Double.POSITIVE_INFINITY;
            for (Element e : elements) {
                tn = Math.min(tn, e.tnext());
            }
            while (probe != null && nextProbe <= Math.min(tn, horizon)) {
                advance(nextProbe, stats);
                probe.sample(nextProbe);
                nextProbe += step;
            }
            if (tn > horizon) {
                advance(horizon, stats);
                return;
            }
            advance(tn, stats);
            boolean again = true;
            while (again) {
                again = false;
                for (Element e : elements) {
                    if (e.tnext() == tcurr) {
                        e.outAct(tcurr);
                        unblock();
                        again = true;
                    }
                }
            }
        }
    }

    private void unblock() {
        for (Element e : elements) {
            if (e.isBlocked()) {
                e.tryUnblock(tcurr);
            }
        }
    }

    private void advance(double t, Statistics stats) {
        double delta = t - tcurr;
        if (delta > 0) {
            for (Element e : elements) {
                e.doStatistics(delta);
            }
            if (stats != null) {
                stats.collect(delta);
            }
        }
        tcurr = t;
    }

    @FunctionalInterface
    public interface Statistics {
        void collect(double delta);
    }
}
