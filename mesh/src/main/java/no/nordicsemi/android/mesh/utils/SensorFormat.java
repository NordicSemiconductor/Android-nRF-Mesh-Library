package no.nordicsemi.android.mesh.utils;

/**
 * The Format field is a 1-bit bit field that identifies the format of the Length and Property ID fields of Sensor Data
 */
public enum SensorFormat {

    FORMAT_A((byte) 0x00),
    FORMAT_B((byte) 0x01);

    private static final String TAG = SensorFormat.class.getSimpleName();
    private final byte formatField;

    SensorFormat(final byte formatField) {
        this.formatField = formatField;
    }

    public static SensorFormat from(final byte format) {
        switch (format) {
            case 0x00:
                return FORMAT_A;
            case 0x01:
                return FORMAT_B;
            default:
                throw new IllegalArgumentException("Unknown Format");
        }
    }

    public static String formatField(final SensorFormat type) {
        switch (type) {
            case FORMAT_A:
                return "Format A";
            case FORMAT_B:
                return "Format B";
            default:
                return "Unknown";
        }
    }

    /**
     * Returns the Format value
     */
    public byte getFormatField() {
        return formatField;
    }
}
