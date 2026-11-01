package conveyor.stats;

public final class Special {
    private static final double[] LANCZOS = {
            0.99999999999980993, 676.5203681218851, -1259.1392167224028, 771.32342877765313,
            -176.61502916214059, 12.507343278686905, -0.13857109526572012, 9.9843695780195716e-6,
            1.5056327351493116e-7};
    private static final double EPS = 1e-14;
    private static final double TINY = 1e-300;

    private Special() {
    }

    public static double lnGamma(double x) {
        if (x < 0.5) {
            return Math.log(Math.PI / Math.abs(Math.sin(Math.PI * x))) - lnGamma(1 - x);
        }
        x -= 1;
        double a = LANCZOS[0];
        double t = x + 7.5;
        for (int i = 1; i < LANCZOS.length; i++) {
            a += LANCZOS[i] / (x + i);
        }
        return 0.5 * Math.log(2 * Math.PI) + (x + 0.5) * Math.log(t) - t + Math.log(a);
    }

    public static double gammaP(double s, double x) {
        if (x <= 0) {
            return 0;
        }
        if (x < s + 1) {
            double term = 1 / s;
            double sum = term;
            for (int n = 1; n < 100_000; n++) {
                term *= x / (s + n);
                sum += term;
                if (Math.abs(term) < Math.abs(sum) * EPS) {
                    break;
                }
            }
            return sum * Math.exp(-x + s * Math.log(x) - lnGamma(s));
        }
        double b = x + 1 - s;
        double c = 1 / TINY;
        double d = 1 / b;
        double h = d;
        for (int i = 1; i < 100_000; i++) {
            double an = -i * (i - s);
            b += 2;
            d = an * d + b;
            if (Math.abs(d) < TINY) {
                d = TINY;
            }
            c = b + an / c;
            if (Math.abs(c) < TINY) {
                c = TINY;
            }
            d = 1 / d;
            double delta = d * c;
            h *= delta;
            if (Math.abs(delta - 1) < EPS) {
                break;
            }
        }
        return 1 - Math.exp(-x + s * Math.log(x) - lnGamma(s)) * h;
    }

    public static double betaI(double a, double b, double x) {
        if (x <= 0) {
            return 0;
        }
        if (x >= 1) {
            return 1;
        }
        double front = Math.exp(lnGamma(a + b) - lnGamma(a) - lnGamma(b) + a * Math.log(x) + b * Math.log(1 - x));
        if (x < (a + 1) / (a + b + 2)) {
            return front * betaCf(a, b, x) / a;
        }
        return 1 - front * betaCf(b, a, 1 - x) / b;
    }

    private static double betaCf(double a, double b, double x) {
        double qab = a + b;
        double qap = a + 1;
        double qam = a - 1;
        double c = 1;
        double d = 1 - qab * x / qap;
        if (Math.abs(d) < TINY) {
            d = TINY;
        }
        d = 1 / d;
        double h = d;
        for (int m = 1; m < 100_000; m++) {
            int m2 = 2 * m;
            double aa = m * (b - m) * x / ((qam + m2) * (a + m2));
            d = 1 + aa * d;
            d = Math.abs(d) < TINY ? TINY : d;
            c = 1 + aa / c;
            c = Math.abs(c) < TINY ? TINY : c;
            d = 1 / d;
            h *= d * c;
            aa = -(a + m) * (qab + m) * x / ((a + m2) * (qap + m2));
            d = 1 + aa * d;
            d = Math.abs(d) < TINY ? TINY : d;
            c = 1 + aa / c;
            c = Math.abs(c) < TINY ? TINY : c;
            d = 1 / d;
            double del = d * c;
            h *= del;
            if (Math.abs(del - 1) < EPS) {
                break;
            }
        }
        return h;
    }

    public static double normalCdf(double z) {
        double v = gammaP(0.5, z * z / 2);
        return z >= 0 ? 0.5 + v / 2 : 0.5 - v / 2;
    }

    public static double studentCdf(double t, double df) {
        double x = df / (df + t * t);
        double tail = 0.5 * betaI(df / 2, 0.5, x);
        return t >= 0 ? 1 - tail : tail;
    }

    public static double studentQuantile(double p, double df) {
        return bisect(x -> studentCdf(x, df), p, -1e3, 1e3);
    }

    public static double fisherCdf(double f, double d1, double d2) {
        if (f <= 0) {
            return 0;
        }
        return betaI(d1 / 2, d2 / 2, d1 * f / (d1 * f + d2));
    }

    public static double fisherQuantile(double p, double d1, double d2) {
        return bisect(x -> fisherCdf(x, d1, d2), p, 0, 1e4);
    }

    public static double chiSquareCdf(double x, double df) {
        return gammaP(df / 2, x / 2);
    }

    public static double chiSquareQuantile(double p, double df) {
        return bisect(x -> chiSquareCdf(x, df), p, 0, 1e4);
    }

    private static double bisect(java.util.function.DoubleUnaryOperator cdf, double p, double lo, double hi) {
        for (int i = 0; i < 200; i++) {
            double mid = (lo + hi) / 2;
            if (cdf.applyAsDouble(mid) < p) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        return (lo + hi) / 2;
    }
}
