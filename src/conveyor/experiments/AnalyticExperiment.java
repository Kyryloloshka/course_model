package conveyor.experiments;

import conveyor.io.Csv;
import conveyor.model.Exponential;
import conveyor.qnet.Create;
import conveyor.qnet.Dispose;
import conveyor.qnet.Network;
import conveyor.qnet.Process;
import conveyor.qnet.Route;
import conveyor.stats.Summary;

import java.util.Locale;
import java.util.Random;
import java.util.stream.IntStream;

public final class AnalyticExperiment {
    static final double ARRIVAL = 2.0;
    static final double[] SERVICE = {1.4, 3.0, 1.6};
    static final int[] CHANNELS = {1, 2, 1};
    static final int[] MAX_QUEUE = {Process.UNLIMITED, Process.UNLIMITED, 3};
    static final double HORIZON = 100_000;
    static final int RUNS = 20;

    private AnalyticExperiment() {
    }

    public static void run() {
        double[][][] sim = new double[3][3][RUNS];
        IntStream.range(0, RUNS).parallel().forEach(s -> {
            Network net = new Network();
            Create in = net.add(new Create("Надходження", new Exponential(ARRIVAL), new Random(1000L + s)));
            Process[] p = new Process[3];
            for (int i = 0; i < 3; i++) {
                p[i] = net.add(new Process("СМО" + (i + 1), new Exponential(SERVICE[i]),
                        new Random(2000L + 10L * s + i), CHANNELS[i], MAX_QUEUE[i]));
            }
            Dispose out = net.add(new Dispose("Вихід"));
            in.route(Route.to(p[0]));
            p[0].route(Route.to(p[1]));
            p[1].route(Route.to(p[2]));
            p[2].route(Route.to(out));
            net.simulate(HORIZON, null, Double.POSITIVE_INFINITY, null);
            for (int i = 0; i < 3; i++) {
                sim[i][0][s] = p[i].queueArea() / HORIZON;
                sim[i][1][s] = p[i].busyArea() / HORIZON;
                sim[i][2][s] = (double) p[i].failures() / p[i].arrivals();
            }
        });
        Csv csv = new Csv("analytic", "smo", "response", "theory", "mean", "half");
        String[] names = {"QUEUE", "BUSY", "FAIL"};
        for (int i = 0; i < 3; i++) {
            double[] th = theory(1 / ARRIVAL, SERVICE[i], CHANNELS[i], MAX_QUEUE[i]);
            for (int r = 0; r < 3; r++) {
                Summary s = Summary.of(sim[i][r]);
                csv.row(i + 1, names[r], th[r], s.mean(), s.halfWidth());
                System.out.printf(Locale.ROOT, "СМО%d %-5s теорія %.4f імітація %.4f ± %.4f%n", i + 1, names[r],
                        th[r], s.mean(), s.halfWidth());
            }
        }
        csv.save();
    }

    static double[] theory(double lambda, double service, int n, int maxQueue) {
        double a = lambda * service;
        int cap = maxQueue == Process.UNLIMITED ? -1 : n + maxQueue;
        double rho = a / n;
        double norm = 0;
        double queue = 0;
        double busy = 0;
        double pk = 1;
        int limit = cap < 0 ? 100_000 : cap;
        double last = 0;
        for (int k = 0; k <= limit; k++) {
            if (k > 0) {
                pk *= k <= n ? a / k : rho;
            }
            norm += pk;
            queue += Math.max(0, k - n) * pk;
            busy += Math.min(k, n) * pk;
            last = pk;
            if (cap < 0 && k > n && pk < 1e-15) {
                break;
            }
        }
        return new double[]{queue / norm, busy / norm, cap < 0 ? 0 : last / norm};
    }
}
