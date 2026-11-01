package conveyor.model;

import java.util.Random;

public record Constant(double value) implements Distribution {
    @Override
    public double sample(Random rnd) {
        return value;
    }

    @Override
    public double mean() {
        return value;
    }

    @Override
    public String notation() {
        return Format.num(value);
    }
}
