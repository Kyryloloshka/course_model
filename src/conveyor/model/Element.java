package conveyor.model;

public abstract class Element {
    protected final String name;
    protected double tnext = Double.POSITIVE_INFINITY;

    protected Element(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public double tnext() {
        return tnext;
    }

    public abstract void outAct(double t);
}
