package tja.integration.ae2;

public interface ITIckSetter {

    default int getTickTime() {
        return 1;
    }

    default void setTickTime(String tickTime) {}
}
