package conveyor.model;

import java.util.Locale;

final class Format {
    private Format() {
    }

    static String num(double v) {
        if (v == Math.rint(v)) {
            return String.valueOf((long) v);
        }
        return String.format(Locale.ROOT, "%.2f", v).replaceAll("0+$", "").replace('.', ',');
    }
}
