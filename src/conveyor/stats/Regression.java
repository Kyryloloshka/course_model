package conveyor.stats;

public final class Regression {
    private Regression() {
    }

    public record Fit(double[] b, double r2, double[] se) {
        public double predict(double[] f) {
            double y = b[0];
            for (int i = 0; i < f.length; i++) {
                y += b[i + 1] * f[i];
            }
            return y;
        }

        public double r() {
            return Math.sqrt(Math.max(0, r2));
        }
    }

    public static Fit fit(double[][] x, double[] y) {
        int n = y.length;
        int m = x[0].length + 1;
        double[][] a = new double[m][m];
        double[] c = new double[m];
        for (int j = 0; j < n; j++) {
            double[] row = row(x[j]);
            for (int i = 0; i < m; i++) {
                c[i] += row[i] * y[j];
                for (int k = 0; k < m; k++) {
                    a[i][k] += row[i] * row[k];
                }
            }
        }
        double[][] inv = invert(a);
        double[] b = new double[m];
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < m; k++) {
                b[i] += inv[i][k] * c[k];
            }
        }
        double mean = 0;
        for (double v : y) {
            mean += v / n;
        }
        double ssRes = 0;
        double ssTot = 0;
        for (int j = 0; j < n; j++) {
            double[] row = row(x[j]);
            double yh = 0;
            for (int i = 0; i < m; i++) {
                yh += b[i] * row[i];
            }
            ssRes += (y[j] - yh) * (y[j] - yh);
            ssTot += (y[j] - mean) * (y[j] - mean);
        }
        double s2 = n > m ? ssRes / (n - m) : 0;
        double[] se = new double[m];
        for (int i = 0; i < m; i++) {
            se[i] = Math.sqrt(s2 * inv[i][i]);
        }
        return new Fit(b, 1 - ssRes / ssTot, se);
    }

    private static double[] row(double[] f) {
        double[] r = new double[f.length + 1];
        r[0] = 1;
        System.arraycopy(f, 0, r, 1, f.length);
        return r;
    }

    private static double[][] invert(double[][] a) {
        int n = a.length;
        double[][] m = new double[n][2 * n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(a[i], 0, m[i], 0, n);
            m[i][n + i] = 1;
        }
        for (int col = 0; col < n; col++) {
            int piv = col;
            for (int r = col + 1; r < n; r++) {
                if (Math.abs(m[r][col]) > Math.abs(m[piv][col])) {
                    piv = r;
                }
            }
            double[] tmp = m[col];
            m[col] = m[piv];
            m[piv] = tmp;
            double d = m[col][col];
            for (int k = 0; k < 2 * n; k++) {
                m[col][k] /= d;
            }
            for (int r = 0; r < n; r++) {
                if (r != col) {
                    double f = m[r][col];
                    for (int k = 0; k < 2 * n; k++) {
                        m[r][k] -= f * m[col][k];
                    }
                }
            }
        }
        double[][] inv = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(m[i], n, inv[i], 0, n);
        }
        return inv;
    }
}
