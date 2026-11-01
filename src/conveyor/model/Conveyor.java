package conveyor.model;

import java.util.Random;

public final class Conveyor extends Element {
    private final Distribution period;
    private final Kitting kitting;
    private final Random rnd;

    Conveyor(String name, Distribution period, Kitting kitting, Random rnd) {
        super(name);
        this.period = period;
        this.kitting = kitting;
        this.rnd = rnd;
        this.tnext = period.sample(rnd);
    }

    @Override
    public void outAct(double t) {
        tnext = t + period.sample(rnd);
        kitting.inAct(t);
    }
}
