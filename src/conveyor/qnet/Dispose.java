package conveyor.qnet;

import java.util.function.DoubleConsumer;

public final class Dispose extends Element {
    private DoubleConsumer onArrival = t -> { };

    public Dispose(String name) {
        super(name);
    }

    public Dispose onArrival(DoubleConsumer action) {
        this.onArrival = action;
        return this;
    }

    @Override
    public double tnext() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public void inAct(Request request, double t) {
        quantity++;
        onArrival.accept(t);
    }

    @Override
    public void outAct(double t) {
    }
}
