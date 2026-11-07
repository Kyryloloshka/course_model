package conveyor.model;

import java.util.Random;

public record Exponential(double mean) implements Distribution {
    @Override
    public double sample(Random rnd) {
        return -mean * Math.log(1 - rnd.nextDouble());
    }

    @Override
    public String notation() {
        return "exp(" + Format.num(mean) + ")";
    }
}
