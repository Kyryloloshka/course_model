package conveyor.model;

import java.util.Random;

public record Uniform(double mean, double halfWidth) implements Distribution {
    public Uniform {
        if (halfWidth < 0 || mean - halfWidth < 0) {
            throw new IllegalArgumentException("unif(" + mean + ", " + halfWidth + ")");
        }
    }

    @Override
    public double sample(Random rnd) {
        return mean - halfWidth + 2 * halfWidth * rnd.nextDouble();
    }

    @Override
    public String notation() {
        return "unif(" + Format.num(mean) + ", " + Format.num(halfWidth) + ")";
    }
}
