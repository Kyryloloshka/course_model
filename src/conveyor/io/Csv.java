package conveyor.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Csv {
    public static final Path OUT = Path.of("out");
    private final Path path;
    private final List<String> lines = new ArrayList<>();

    public Csv(String name, String... header) {
        this.path = OUT.resolve("csv").resolve(name + ".csv");
        lines.add(String.join(";", header));
    }

    public synchronized Csv row(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(';');
            }
            Object v = values[i];
            if (v instanceof Double d) {
                sb.append(String.format(Locale.ROOT, "%.6g", d));
            } else {
                sb.append(v);
            }
        }
        lines.add(sb.toString());
        return this;
    }

    public void save() {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, lines);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void text(String relative, String content) {
        try {
            Path p = OUT.resolve(relative);
            Files.createDirectories(p.getParent());
            Files.writeString(p, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
