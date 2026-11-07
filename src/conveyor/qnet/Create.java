package conveyor.qnet;

import conveyor.model.Distribution;

import java.util.Random;

public final class Create extends Element {
    private final Distribution delay;
    private final Random rnd;
    private double tnext;
    private boolean blocked;
    private double blockedSince;
    private double blockedTime;
    private long nextId = 1;

    public Create(String name, Distribution delay, Random rnd) {
        super(name);
        this.delay = delay;
        this.rnd = rnd;
        this.tnext = delay.sample(rnd);
    }

    @Override
    public double tnext() {
        return tnext;
    }

    @Override
    public void inAct(Request request, double t) {
        throw new UnsupportedOperationException(name() + " не приймає запитів");
    }

    @Override
    public void outAct(double t) {
        Route r = openRoute();
        if (r == null) {
            blocked = true;
            blockedSince = t;
            tnext = Double.POSITIVE_INFINITY;
            log.event(t, name(), "маршрут заблоковано, надходження затримане");
            return;
        }
        quantity++;
        tnext = t + delay.sample(rnd);
        r.pass(new Request(nextId++, t), t, log, name());
        log.event(t, name(), "надходження, " + r.describe() + "; наступне о " + EventLog.time(tnext));
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }

    @Override
    public void tryUnblock(double t) {
        if (blocked && openRoute() != null) {
            blocked = false;
            blockedTime += t - blockedSince;
            tnext = t;
            log.event(t, name(), "блокування маршруту знято");
        }
    }

    public double blockedTime(double t) {
        return blockedTime + (blocked ? t - blockedSince : 0);
    }
}
