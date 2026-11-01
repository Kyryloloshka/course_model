package conveyor.model;

public final class Kitting extends Element {
    private final int kit;
    private final Store q1;
    private final Store q2;
    private final EventLog log;
    private long sections;
    private long filled;
    private long lack1;
    private long lack2;
    private long lackBoth;

    Kitting(String name, int kit, Store q1, Store q2, EventLog log) {
        super(name);
        this.kit = kit;
        this.q1 = q1;
        this.q2 = q2;
        this.log = log;
    }

    boolean blocked() {
        return q1.length() < kit || q2.length() < kit;
    }

    void inAct(double t) {
        sections++;
        if (!blocked()) {
            q1.take(t, kit);
            q2.take(t, kit);
            filled++;
            log.event(t, name, "секція №" + sections + " укомплектована (" + kit + " + " + kit + ")");
        } else {
            boolean no1 = q1.length() < kit;
            boolean no2 = q2.length() < kit;
            if (no1 && no2) {
                lackBoth++;
            } else if (no1) {
                lack1++;
            } else {
                lack2++;
            }
            log.event(t, name, "секція №" + sections + " ПРОПУЩЕНА (нестача: "
                    + (no1 && no2 ? "обох типів" : no1 ? "типу 1" : "типу 2") + ")");
        }
    }

    @Override
    public void outAct(double t) {
    }

    public long sections() {
        return sections;
    }

    public long filled() {
        return filled;
    }

    public long skipped() {
        return sections - filled;
    }

    public long lack1() {
        return lack1;
    }

    public long lack2() {
        return lack2;
    }

    public long lackBoth() {
        return lackBoth;
    }
}
