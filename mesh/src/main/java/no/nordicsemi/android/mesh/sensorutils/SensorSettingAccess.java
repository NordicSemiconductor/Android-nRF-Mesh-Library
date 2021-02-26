package no.nordicsemi.android.mesh.sensorutils;

/**
 * Sensor Setting Access
 */
public enum SensorSettingAccess {

    READ_ONLY(0x01),
    READ_WRITE(0x03);

    private static final String TAG = SensorSettingAccess.class.getSimpleName();
    private final int sensorSetting;

    SensorSettingAccess(final int sensorSetting) {
        this.sensorSetting = sensorSetting;
    }

    public static String getDescription(final SensorSettingAccess type) {
        switch (type) {
            case READ_ONLY:
                return "The device property can be read.";
            case READ_WRITE:
                return "The device property can be read and written.";
            default:
                throw new IllegalArgumentException("Prohibited");
        }
    }

    public static SensorSettingAccess from(final int sensorSetting) {
        switch (sensorSetting) {
            case 0x01:
                return READ_ONLY;
            case 0x03:
                return READ_WRITE;
            default:
                return null;
        }
    }

    /**
     * Returns the sensor setting.
     */
    public int getSensorSetting() {
        return sensorSetting;
    }
}
