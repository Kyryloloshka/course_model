package conveyor.model;

import java.util.ArrayDeque;
import java.util.Arrays;

public final class Store {
    private final String name;
    private final int limit;
    private Source waiting;
    private final ArrayDeque<double[]> lots = new ArrayDeque<>();
    private long length;
    private long max;
    private double area;
    private double area2;
    private double lastT;
    private long taken;
    private double waitSum;
    private double[] timeAt = new double[64];

    Store(String name, int limit, int initial) {
        this.name = name;
        this.limit = limit;
        if (initial > 0) {
            put(0, initial);
        }
    }

    public String name() {
        return name;
    }

    public long length() {
        return length;
    }

    void advance(double t) {
        double dt = t - lastT;
        if (dt > 0) {
            area += length * dt;
            area2 += (double) length * length * dt;
            if (length >= timeAt.length) {
                timeAt = Arrays.copyOf(timeAt, (int) Math.max(length + 1, timeAt.length * 2L));
            }
            timeAt[(int) length] += dt;
            lastT = t;
        }
    }

    boolean fits(int n) {
        return length + n <= limit;
    }

    void waitFor(Source source) {
        waiting = source;
    }

    void put(double t, int n) {
        lots.addLast(new double[]{t, n});
        length += n;
        max = Math.max(max, length);
    }

    void take(double t, int k) {
        if (k > length) {
            throw new IllegalStateException(name + ": не вистачає виробів");
        }
        length -= k;
        taken += k;
        int left = k;
        while (left > 0) {
            double[] lot = lots.peekFirst();
            int n = (int) Math.min(left, lot[1]);
            waitSum += (t - lot[0]) * n;
            lot[1] -= n;
            left -= n;
            if (lot[1] == 0) {
                lots.pollFirst();
            }
        }
        if (waiting != null) {
            Source s = waiting;
            waiting = null;
            s.unblock(t);
            if (s.tnext() > t) {
                waiting = s;
            }
        }
    }

    double residualWait(double t) {
        double s = 0;
        for (double[] lot : lots) {
            s += (t - lot[0]) * lot[1];
        }
        return s;
    }

    public long max() {
        return max;
    }

    public double area() {
        return area;
    }

    public double area2() {
        return area2;
    }

    public long taken() {
        return taken;
    }

    public double waitSum() {
        return waitSum;
    }

    public double[] timeAtLength() {
        return timeAt;
    }
}
