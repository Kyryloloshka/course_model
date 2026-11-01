package conveyor.experiments;

import conveyor.model.RunResult;

import java.util.function.ToDoubleFunction;

public enum Response {
    P_SKIP("P_проп", "ймовірність пропуску секції", RunResult::pSkip),
    MEAN1("L1_сер", "середня черга виробів типу 1, шт.", RunResult::mean1),
    MEAN2("L2_сер", "середня черга виробів типу 2, шт.", RunResult::mean2),
    MAX1("L1_max", "максимальна черга виробів типу 1, шт.", r -> r.max1()),
    MAX2("L2_max", "максимальна черга виробів типу 2, шт.", r -> r.max2()),
    WAIT1("W1", "середній час очікування виробу типу 1, хв", RunResult::wait1),
    WAIT2("W2", "середній час очікування виробу типу 2, хв", RunResult::wait2),
    DIFF("|L1-L2|", "середня різниця черг |L1 - L2|, шт.", RunResult::meanDiff),
    WIP("L_сум", "середня сумарна кількість виробів у накопичувачах, шт.", RunResult::wip),
    KPH("П", "продуктивність, укомплектованих секцій/год", RunResult::kitsPerHour),
    PPH("П_в", "продуктивність, виробів кожного типу/год", RunResult::partsPerHour),
    BLOCK1("B1", "частка часу блокування надходження виробів типу 1", RunResult::blocked1),
    BLOCK2("B2", "частка часу блокування надходження виробів типу 2", RunResult::blocked2);

    public final String symbol;
    public final String title;
    public final ToDoubleFunction<RunResult> fn;

    Response(String symbol, String title, ToDoubleFunction<RunResult> fn) {
        this.symbol = symbol;
        this.title = title;
        this.fn = fn;
    }

    public double[] of(RunResult[] runs) {
        double[] x = new double[runs.length];
        for (int i = 0; i < runs.length; i++) {
            x[i] = fn.applyAsDouble(runs[i]);
        }
        return x;
    }
}
