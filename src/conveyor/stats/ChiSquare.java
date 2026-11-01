package conveyor.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public final class ChiSquare {
    private ChiSquare() {
    }

    public record Bin(double lo, double hi, int observed, double expected) {
    }

    public record Result(String law, String params, double chi2, int df, double critical, double p, List<Bin> bins) {
        public boolean accepted() {
            return chi2 < critical;
        }
    }

    public static Result test(String law, String params, double[] x, int k, DoubleUnaryOperator cdf, int fitted) {
        double[] s = x.clone();
        Arrays.sort(s);
        double lo = s[0];
        double hi = s[s.length - 1];
        double w = (hi - lo) / k;
        int[] obs = new int[k];
        for (double v : s) {
            obs[Math.min(k - 1, (int) ((v - lo) / w))]++;
        }
        int n = s.length;
        List<Bin> raw = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            double a = lo + i * w;
            double b = i == k - 1 ? hi : a + w;
            double fa = i == 0 ? 0 : cdf.applyAsDouble(a);
            double fb = i == k - 1 ? 1 : cdf.applyAsDouble(b);
            raw.add(new Bin(a, b, obs[i], n * (fb - fa)));
        }
        List<Bin> bins = new ArrayList<>();
        Bin acc = null;
        for (Bin b : raw) {
            acc = acc == null ? b : new Bin(acc.lo, b.hi, acc.observed + b.observed, acc.expected + b.expected);
            if (acc.expected >= 5) {
                bins.add(acc);
                acc = null;
            }
        }
        if (acc != null) {
            Bin last = bins.remove(bins.size() - 1);
            bins.add(new Bin(last.lo, acc.hi, last.observed + acc.observed, last.expected + acc.expected));
        }
        return finish(law, params, bins, fitted);
    }

    public static Result testLattice(String law, String params, double[] x, double h, DoubleUnaryOperator cdf,
                                     int fitted) {
        java.util.TreeMap<Double, Integer> counts = new java.util.TreeMap<>();
        for (double v : x) {
            counts.merge(Math.rint(v / h) * h, 1, Integer::sum);
        }
        double first = counts.firstKey();
        double last = counts.lastKey();
        int n = x.length;
        List<Bin> raw = new ArrayList<>();
        for (double v = first; v <= last + 1e-9; v += h) {
            double fa = v == first ? 0 : cdf.applyAsDouble(v - h / 2);
            double fb = v + h > last + 1e-9 ? 1 : cdf.applyAsDouble(v + h / 2);
            raw.add(new Bin(v - h / 2, v + h / 2, counts.getOrDefault(v, 0), n * (fb - fa)));
        }
        List<Bin> bins = new ArrayList<>();
        Bin acc = null;
        for (Bin b : raw) {
            acc = acc == null ? b : new Bin(acc.lo, b.hi, acc.observed + b.observed, acc.expected + b.expected);
            if (acc.expected >= 5) {
                bins.add(acc);
                acc = null;
            }
        }
        if (acc != null) {
            Bin lastBin = bins.remove(bins.size() - 1);
            bins.add(new Bin(lastBin.lo, acc.hi, lastBin.observed + acc.observed, lastBin.expected + acc.expected));
        }
        return finish(law, params, bins, fitted);
    }

    private static Result finish(String law, String params, List<Bin> bins, int fitted) {
        double chi2 = 0;
        for (Bin b : bins) {
            chi2 += Math.pow(b.observed - b.expected, 2) / b.expected;
        }
        int df = bins.size() - 1 - fitted;
        return new Result(law, params, chi2, df, Special.chiSquareQuantile(0.95, df),
                1 - Special.chiSquareCdf(chi2, df), bins);
    }
}
