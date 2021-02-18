package no.nordicsemi.android.mesh.sensorutils;

/**
 * This Sensor Sampling Function field specifies the averaging operation or type of sampling function applied to the measured value.
 */
public enum SensorSamplingFunction {

    /**
     * Sampling function type.
     */
    UNSPECIFIED((byte) 0x00),
    INSTANTANEOUS((byte) 0x01),
    ARITHMETIC_MEAN((byte) 0x02),
    RMS((byte) 0x03),
    MAXIMUM((byte) 0x04),
    MINIMUM((byte) 0x05),
    ACCUMULATED((byte) 0x06),
    COUNT((byte) 0x07),
    UNKNOWN((byte) 0x08);

    private static final String TAG = SensorSamplingFunction.class.getSimpleName();
    private final byte samplingFunction;

    SensorSamplingFunction(final byte samplingFunction) {
        this.samplingFunction = samplingFunction;
    }

    /**
     * Returns the SensorSamplingFunction.
     *
     * @param samplingFunction Sampling function.
     */
    public static SensorSamplingFunction from(final byte samplingFunction) {
        switch (samplingFunction) {
            case 0x00:
                return UNSPECIFIED;
            case 0x01:
                return INSTANTANEOUS;
            case 0x02:
                return ARITHMETIC_MEAN;
            case 0x03:
                return RMS;
            case 0x04:
                return MAXIMUM;
            case 0x05:
                return MINIMUM;
            case 0x06:
                return ACCUMULATED;
            case 0x07:
                return COUNT;
            default:
                return UNKNOWN;
        }
    }

    public static String getSamplingFunction(final SensorSamplingFunction type) {
        switch (type) {
            case UNSPECIFIED:
                return "Unspecified";
            case INSTANTANEOUS:
                return "Instantaneous";
            case ARITHMETIC_MEAN:
                return "Arithmetic Mean";
            case RMS:
                return "RMS";
            case MAXIMUM:
                return "Maximum";
            case MINIMUM:
                return "Minimum";
            case ACCUMULATED:
                return "Accumulated";
            case COUNT:
                return "Count";
            default:
                return "Unknown";
        }
    }

    /**
     * Returns the Sampling Function
     */
    public byte getSensorSamplingFunction() {
        return samplingFunction;
    }
}
