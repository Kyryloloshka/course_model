package conveyor.qnet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class Element {
    private final String name;
    private final List<Route> routes = new ArrayList<>();
    protected EventLog log = EventLog.OFF;
    protected long quantity;

    protected Element(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public Element route(Route r) {
        routes.add(r);
        routes.sort(Comparator.comparingInt((Route x) -> x.priority()).reversed());
        return this;
    }

    public List<Route> routes() {
        return routes;
    }

    protected Route openRoute() {
        for (Route r : routes) {
            if (!r.isBlocked()) {
                return r;
            }
        }
        return null;
    }

    void attach(EventLog log) {
        this.log = log;
    }

    public long quantity() {
        return quantity;
    }

    public abstract double tnext();

    public abstract void inAct(Request request, double t);

    public abstract void outAct(double t);

    public boolean isBlocked() {
        return false;
    }

    public void tryUnblock(double t) {
    }

    public void doStatistics(double delta) {
    }
}
