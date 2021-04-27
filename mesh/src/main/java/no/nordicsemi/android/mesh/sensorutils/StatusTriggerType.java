package no.nordicsemi.android.mesh.sensorutils;

/**
 * Status Trigger Type
 */
public enum StatusTriggerType {

    SENSOR_PROPERTY_ID_FORMAT_TYPE(0x00),
    UNIT_LESS(0x01);

    private static final String TAG = StatusTriggerType.class.getSimpleName();
    private final int statusTriggerType;

    StatusTriggerType(final int statusTriggerType) {
        this.statusTriggerType = statusTriggerType;
    }

    public static String getStatusTriggerType(final StatusTriggerType type) {
        switch (type) {
            case SENSOR_PROPERTY_ID_FORMAT_TYPE:
                return "Format Type from Property ID Characteristic";
            case UNIT_LESS:
                return "Unitless";
            default:
                return "Unknown";
        }
    }

    public static StatusTriggerType from(final int statusTriggerType) {
        switch (statusTriggerType) {
            case 0x00:
                return SENSOR_PROPERTY_ID_FORMAT_TYPE;
            case 0x01:
                return UNIT_LESS;
            default:
                return null;
        }
    }

    /**
     * Returns the status trigger type.
     */
    public int getStatusTriggerType() {
        return statusTriggerType;
    }
}
