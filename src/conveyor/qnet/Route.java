package conveyor.qnet;

public final class Route {
    private final Element target;
    private final int priority;
    private final int group;
    private final int ungroup;
    private final Blocking blocking;
    private int pending;
    private long passed;

    private Route(Element target, int priority, int group, int ungroup, Blocking blocking) {
        this.target = target;
        this.priority = priority;
        this.group = group;
        this.ungroup = ungroup;
        this.blocking = blocking;
    }

    public static Route to(Element target) {
        return new Route(target, 1, 1, 1, Blocking.NEVER);
    }

    public Route priority(int p) {
        return new Route(target, p, group, ungroup, blocking);
    }

    public Route group(int k) {
        return new Route(target, priority, k, ungroup, blocking);
    }

    public Route ungroup(int k) {
        return new Route(target, priority, group, k, blocking);
    }

    public Route blockedWhen(Blocking b) {
        return new Route(target, priority, group, ungroup, b);
    }

    public boolean isBlocked() {
        return blocking.blocked();
    }

    void pass(Request request, double t, EventLog log, String from) {
        passed++;
        if (group > 1) {
            pending++;
            if (pending < group) {
                return;
            }
            pending = 0;
            target.inAct(new Request(request.id(), request.createdAt()), t);
            return;
        }
        for (int i = 0; i < ungroup; i++) {
            target.inAct(new Request(request.id() * ungroup + i, t), t);
        }
    }

    String describe() {
        if (group > 1) {
            return pending == 0 ? "дуга " + group + ": " + group + " запитів згруповано в 1 → " + target.name()
                    : "дуга " + group + ": накопичено " + pending + " з " + group;
        }
        if (ungroup > 1) {
            return "дуга 1/" + ungroup + ": " + ungroup + " запитів → " + target.name();
        }
        return "→ " + target.name();
    }

    public Element target() {
        return target;
    }

    public int priority() {
        return priority;
    }

    public long passed() {
        return passed;
    }

    public int pending() {
        return pending;
    }
}
