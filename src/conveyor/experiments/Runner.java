package conveyor.experiments;

import conveyor.model.Model;
import conveyor.model.ModelConfig;
import conveyor.model.RunResult;

import java.util.stream.IntStream;

public final class Runner {
    private Runner() {
    }

    public static RunResult[] replicate(ModelConfig cfg, int n) {
        return IntStream.rangeClosed(1, n).parallel()
                .mapToObj(s -> new Model(cfg.withSeed(s)).run())
                .toArray(RunResult[]::new);
    }
}
