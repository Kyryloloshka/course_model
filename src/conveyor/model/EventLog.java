package conveyor.model;

import java.util.Locale;
import java.util.function.Supplier;

public final class EventLog {
    public static final EventLog OFF = new EventLog(0);

    private final int limit;
    private final StringBuilder sb = new StringBuilder();
    private int lines;
    private Supplier<String> state = () -> "";

    public EventLog(int limit) {
        this.limit = limit;
    }

    void attach(Supplier<String> state) {
        this.state = state;
    }

    void event(double t, String element, String text) {
        if (lines >= limit) {
            return;
        }
        lines++;
        sb.append(String.format(Locale.ROOT, "%9s | %-8s | %-62s | %s%n", time(t), element, text, state.get()));
    }

    static String time(double t) {
        return String.format(Locale.ROOT, "%.3f", t);
    }

    public String text() {
        return sb.toString();
    }
}
