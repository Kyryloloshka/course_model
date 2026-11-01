package conveyor.stats;

public record Summary(int n, double mean, double sd, double min, double max, double halfWidth) {
    public static final double BETA = 0.95;

    public static Summary of(double[] x) {
        int n = x.length;
        double s = 0;
        double mn = Double.POSITIVE_INFINITY;
        double mx = Double.NEGATIVE_INFINITY;
        for (double v : x) {
            s += v;
            mn = Math.min(mn, v);
            mx = Math.max(mx, v);
        }
        double mean = s / n;
        double ss = 0;
        for (double v : x) {
            ss += (v - mean) * (v - mean);
        }
        double sd = n > 1 ? Math.sqrt(ss / (n - 1)) : 0;
        double t = n > 1 ? Special.studentQuantile(1 - (1 - BETA) / 2, n - 1) : 0;
        return new Summary(n, mean, sd, mn, mx, t * sd / Math.sqrt(n));
    }

    public static long requiredRuns(double sd, double eps) {
        return (long) Math.ceil(sd * sd / (eps * eps * (1 - BETA)));
    }
}
