package conveyor.model;

import java.util.Random;

public final class Source extends Element {
    private final Distribution interval;
    private final int batch;
    private final Store target;
    private final Random rnd;
    private final EventLog log;
    private long arrivals;
    private boolean blocked;
    private double blockedSince;
    private double blockedTime;
    private long blockings;

    Source(String name, Distribution interval, int batch, Store target, Random rnd, EventLog log) {
        super(name);
        this.interval = interval;
        this.batch = batch;
        this.target = target;
        this.rnd = rnd;
        this.log = log;
        this.tnext = interval.sample(rnd);
    }

    @Override
    public void outAct(double t) {
        if (!target.fits(batch)) {
            blocked = true;
            blockedSince = t;
            blockings++;
            tnext = Double.POSITIVE_INFINITY;
            target.waitFor(this);
            log.event(t, name, "маршрут до " + target.name() + " заблоковано, партія " + batch + " шт. чекає");
            return;
        }
        arrivals++;
        target.put(t, batch);
        tnext = t + interval.sample(rnd);
        log.event(t, name, "надійшла партія " + batch + " шт. -> " + target.name() + ", наступна о "
                + EventLog.time(tnext));
    }

    void unblock(double t) {
        if (blocked && target.fits(batch)) {
            blocked = false;
            blockedTime += t - blockedSince;
            tnext = t;
        }
    }

    double blockedTime(double t) {
        return blockedTime + (blocked ? t - blockedSince : 0);
    }

    public long blockings() {
        return blockings;
    }

    public long arrivals() {
        return arrivals;
    }

    public long parts() {
        return arrivals * batch;
    }
}
