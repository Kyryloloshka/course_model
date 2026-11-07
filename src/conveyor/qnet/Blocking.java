package conveyor.qnet;

@FunctionalInterface
public interface Blocking {
    Blocking NEVER = () -> false;

    boolean blocked();
}
