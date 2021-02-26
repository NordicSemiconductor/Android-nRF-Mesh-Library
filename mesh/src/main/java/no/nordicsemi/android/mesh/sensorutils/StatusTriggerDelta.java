package no.nordicsemi.android.mesh.sensorutils;

public abstract class StatusTriggerDelta<T> {
    private final T up;
    private final T down;

    /**
     * Status Trigger Delta
     *
     * @param down Status Trigger Delta Down field shall control the negative change of a measured
     *             quantity that triggers publication of a Sensor Status message.
     * @param up   Status Trigger Delta Up field shall control the positive change of a measured
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

    /**
     * Returns the total length of the status trigger delta.
     */
    public abstract int getLength();

    public static class Characteristic<T> extends StatusTriggerDelta<DevicePropertyCharacteristic<T>> {

        /**
         * Status Trigger Delta as characteristic
         *
         * @param down Status Trigger Delta Down field shall control the negative change of a measured
         *             quantity that triggers publication of a Sensor Status message.
         * @param up   Status Trigger Delta Up field shall control the positive change of a measured
         */
        public Characteristic(final DevicePropertyCharacteristic<T> down, final DevicePropertyCharacteristic<T> up) {
            super(down, up);
        }

        @Override
        public int getLength() {
            return getDown().getLength() + getUp().getLength();
        }
    }

    public static class Percent extends StatusTriggerDelta<Float> {

        /**
         * Status Trigger Delta as percent
         *
         * @param down Status Trigger Delta Down field shall control the negative change of a measured
         *             quantity that triggers publication of a Sensor Status message.
         * @param up   Status Trigger Delta Up field shall control the positive change of a measured
         */
        public Percent(final float down, final float up) {
            super(down, up);
        }

        @Override
        public int getLength() {
            return 2 * 2;
        }
    }
}
