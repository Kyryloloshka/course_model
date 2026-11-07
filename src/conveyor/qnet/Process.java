package conveyor.qnet;

import conveyor.model.Distribution;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;

public final class Process extends Element {
    public static final int UNLIMITED = Integer.MAX_VALUE;
    private static final int FREE = 0;
    private static final int BUSY = 1;
    private static final int BLOCKED = 2;

    private final Distribution delay;
    private final Random rnd;
    private final int maxQueue;
    private final int[] state;
    private final double[] tEnd;
    private final Request[] inService;
    private final ArrayDeque<Request> queue = new ArrayDeque<>();
    private long arrivals;
    private long failures;
    private long maxContent;
    private double queueArea;
    private double contentArea;
    private double contentArea2;
    private double busyArea;

    public Process(String name, Distribution delay, Random rnd, int channels, int maxQueue) {
        super(name);
        this.delay = delay;
        this.rnd = rnd;
        this.maxQueue = maxQueue;
        this.state = new int[channels];
        this.tEnd = new double[channels];
        this.inService = new Request[channels];
        Arrays.fill(tEnd, Double.POSITIVE_INFINITY);
    }

    @Override
    public double tnext() {
        double m = Double.POSITIVE_INFINITY;
        for (double v : tEnd) {
            m = Math.min(m, v);
        }
        return m;
    }

    @Override
    public void inAct(Request request, double t) {
        arrivals++;
        int free = freeChannel();
        if (free >= 0) {
            start(free, request, t);
        } else if (queue.size() < maxQueue) {
            queue.addLast(request);
        } else {
            failures++;
            log.event(t, name(), "відмова: немає вільного пристрою і місця в черзі");
        }
        maxContent = Math.max(maxContent, content());
    }

    public void preload(Request request) {
        inAct(request, 0);
        arrivals--;
    }

    @Override
    public void outAct(double t) {
        for (int c = 0; c < state.length; c++) {
            if (tEnd[c] != t) {
                continue;
            }
            Route r = openRoute();
            if (r == null) {
                state[c] = BLOCKED;
                tEnd[c] = Double.POSITIVE_INFINITY;
                log.event(t, name(), "завершення обробки, маршрут заблоковано: пристрій блокований");
                continue;
            }
            Request done = inService[c];
            state[c] = FREE;
            tEnd[c] = Double.POSITIVE_INFINITY;
            inService[c] = null;
            quantity++;
            if (!queue.isEmpty()) {
                start(c, queue.pollFirst(), t);
            }
            r.pass(done, t, log, name());
            log.event(t, name(), "завершення обробки, " + r.describe());
        }
    }

    private void start(int c, Request request, double t) {
        state[c] = BUSY;
        inService[c] = request;
        tEnd[c] = t + delay.sample(rnd);
    }

    private int freeChannel() {
        for (int c = 0; c < state.length; c++) {
            if (state[c] == FREE) {
                return c;
            }
        }
        return -1;
    }

    @Override
    public boolean isBlocked() {
        for (int s : state) {
            if (s == BLOCKED) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void tryUnblock(double t) {
        if (!isBlocked() || openRoute() == null) {
            return;
        }
        for (int c = 0; c < state.length; c++) {
            if (state[c] == BLOCKED) {
                state[c] = BUSY;
                tEnd[c] = t;
                log.event(t, name(), "блокування маршруту знято");
            }
        }
    }

    public long content() {
        long n = queue.size();
        for (int s : state) {
            if (s != FREE) {
                n++;
            }
        }
        return n;
    }

    public int queueSize() {
        return queue.size();
    }

    @Override
    public void doStatistics(double delta) {
        long n = content();
        queueArea += queue.size() * delta;
        contentArea += n * delta;
        contentArea2 += (double) n * n * delta;
        for (int s : state) {
            if (s == BUSY) {
                busyArea += delta;
            }
        }
    }

    public long arrivals() {
        return arrivals;
    }

    public long failures() {
        return failures;
    }

    public long maxContent() {
        return maxContent;
    }

    public double queueArea() {
        return queueArea;
    }

    public double contentArea() {
        return contentArea;
    }

    public double contentArea2() {
        return contentArea2;
    }

    public double busyArea() {
        return busyArea;
    }
}
