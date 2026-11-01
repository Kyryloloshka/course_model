package conveyor.model;

public record RunResult(double horizon, int kit, long sections, long filled, long lack1, long lack2, long lackBoth,
                        long arrived1, long arrived2, double mean1, double sd1, long max1, double mean2,
                        double sd2, long max2, long end1, long end2, double wait1, double wait2,
                        double littleError, double blocked1, double blocked2, double meanDiff) {
    public long skipped() {
        return sections - filled;
    }

    public double pSkip() {
        return sections == 0 ? 0 : (double) skipped() / sections;
    }

    public double partsPerHour() {
        return kitsPerHour() * kit;
    }

    public double kitsPerHour() {
        return filled / horizon * 60;
    }

    public double wip() {
        return mean1 + mean2;
    }
}
