package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;

public class SensorDescriptor {

    private final DeviceProperty property;
    private final short positiveTolerance;
    private final short negativeTolerance;
    private final SensorSamplingFunction sensorSamplingFunction;
    private final byte measurementPeriod;
    private final byte updateInterval;

    /**
     * Constructs the Sensor Descriptor
     *
     * @param property               The Sensor Property  describes the meaning and the format of data reported by a sensor.
     * @param positiveTolerance      12-bit value representing the magnitude of a possible positive error associated with
     *                               the measurements the sensor is reporting.
     * @param negativeTolerance      12-bit value representing the magnitude of a possible negative error associated with
     *                               the measurements the sensor is reporting.
     * @param sensorSamplingFunction Sensor Sampling Function field specifies the averaging operation or type of sampling
     *                               function applied to the measured value.
     * @param measurementPeriod      This Sensor Measurement Period field specifies a UInt8 value n that represents the averaging time span,
     *                               accumulation time, or measurement period in seconds over which the measurement is taken.
     * @param updateInterval         The measurement reported by a sensor is internally refreshed at the frequency indicated in
     *                               the Sensor Update Interval field.
     */
    public SensorDescriptor(@NonNull final DeviceProperty property,
                            final short positiveTolerance,
                            final short negativeTolerance,
                            @NonNull final SensorSamplingFunction sensorSamplingFunction,
                            final byte measurementPeriod,
                            final byte updateInterval) {
        this.property = property;
        this.positiveTolerance = positiveTolerance;
        this.negativeTolerance = negativeTolerance;
        this.sensorSamplingFunction = sensorSamplingFunction;
        this.measurementPeriod = measurementPeriod;
        this.updateInterval = updateInterval;
    }

    /**
     * Returns the sensor property.
     */
    public DeviceProperty getProperty() {
        return property;
    }

    /**
     * Returns the positive error tolerance.
     */
    public short getPositiveTolerance() {
        return positiveTolerance;
    }

    /**
     * Returns the negative tolerance.
     */
    public short getNegativeTolerance() {
        return negativeTolerance;
    }

    /**
     * Returns Sensor Sampling Function.
     */
    public SensorSamplingFunction getSensorSamplingFunction() {
        return sensorSamplingFunction;
    }

    /**
     * Returns the measurement period.
     */
    public byte getMeasurementPeriod() {
        return measurementPeriod;
    }

    /**
     * Returns the update interval.
     */
    public byte getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public String toString() {
        return "SensorDescriptor{" +
                "property=" + Integer.toString(property.getPropertyId(), 16) +
                ", positiveTolerance=" + Integer.toHexString(positiveTolerance) +
                ", negativeTolerance=" + Integer.toHexString(negativeTolerance) +
                ", sensorSamplingFunction=" + sensorSamplingFunction.getSensorSamplingFunction() +
                ", measurementPeriod=" + Integer.toHexString(measurementPeriod) +
                ", updateInterval=" + Integer.toHexString(updateInterval) +
                '}';
    }
}
