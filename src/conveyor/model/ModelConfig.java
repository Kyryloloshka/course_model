package conveyor.model;

public record ModelConfig(int batch1, Distribution interval1, int batch2, Distribution interval2,
                          Distribution sectionPeriod, int kitSize, int bufferLimit, int initial1, int initial2,
                          double horizon, long seed) {
    public static final int UNLIMITED = Integer.MAX_VALUE;

    public static ModelConfig base() {
        return new ModelConfig(5, new Uniform(5, 1), 20, new Uniform(20, 7), new Constant(10), 10, UNLIMITED, 0, 0,
                1440, 1);
    }

    public static ModelConfig variant2() {
        return base().withKit(20, 20);
    }

    public ModelConfig withKit(int kit, double period) {
        return new ModelConfig(batch1, interval1, batch2, interval2, new Constant(period), kit, bufferLimit, initial1,
                initial2, horizon, seed);
    }

    public ModelConfig withType1(int batch, Distribution interval) {
        return new ModelConfig(batch, interval, batch2, interval2, sectionPeriod, kitSize, bufferLimit, initial1,
                initial2, horizon, seed);
    }

    public ModelConfig withType2(int batch, Distribution interval) {
        return new ModelConfig(batch1, interval1, batch, interval, sectionPeriod, kitSize, bufferLimit, initial1,
                initial2, horizon, seed);
    }

    public ModelConfig withBufferLimit(int limit) {
        return new ModelConfig(batch1, interval1, batch2, interval2, sectionPeriod, kitSize, limit, initial1,
                initial2, horizon, seed);
    }

    public ModelConfig withInitial(int q1, int q2) {
        return new ModelConfig(batch1, interval1, batch2, interval2, sectionPeriod, kitSize, bufferLimit, q1, q2,
                horizon, seed);
    }

    public ModelConfig withHorizon(double h) {
        return new ModelConfig(batch1, interval1, batch2, interval2, sectionPeriod, kitSize, bufferLimit, initial1,
                initial2, h, seed);
    }

    public ModelConfig withSeed(long s) {
        return new ModelConfig(batch1, interval1, batch2, interval2, sectionPeriod, kitSize, bufferLimit, initial1,
                initial2, horizon, s);
    }

    public double supply(int type) {
        return type == 1 ? batch1 / interval1.mean() : batch2 / interval2.mean();
    }

    public double capacity() {
        return kitSize / sectionPeriod.mean();
    }

    public double load(int type) {
        return supply(type) / capacity();
    }
}
