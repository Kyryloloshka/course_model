package conveyor.model;

import java.util.Random;

public interface Distribution {
    double sample(Random rnd);

    double mean();

    String notation();
}
