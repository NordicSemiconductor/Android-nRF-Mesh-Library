package no.nordicsemi.android.mesh.sensorutils;

public class StatusTriggerDelta<T> {
    private final T up;
    private final T down;

    /**
     * Status Trigger Delta
     *
     * @param down The Status Trigger Delta Down field shall control the negative change of a measured
     *             quantity that triggers publication of a Sensor Status message.
     * @param up   The Status Trigger Delta Up field shall control the positive change of a measured
     *             quantity that triggers publication of a Sensor Status message.
     */
    public StatusTriggerDelta(final T down, final T up) {
        this.up = up;
        this.down = down;
    }

    public T getUp() {
        return up;
    }

    public T getDown() {
        return down;
    }
}
