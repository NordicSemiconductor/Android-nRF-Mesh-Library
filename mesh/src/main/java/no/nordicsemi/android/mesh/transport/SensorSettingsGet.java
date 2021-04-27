package no.nordicsemi.android.mesh.transport;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * SensorSettingsGet message.
 */
public class SensorSettingsGet extends ApplicationMessage {

    private static final String TAG = SensorSettingsGet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SENSOR_SETTINGS_GET;

    private final DeviceProperty propertyId;

    /**
     * Constructs SensorSettingsGet message.
     *
     * @param appKey     {@link ApplicationKey} Key for this message.
     * @param propertyId {@link DeviceProperty} Sensor property ID.
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public SensorSettingsGet(@NonNull final ApplicationKey appKey, final DeviceProperty propertyId) {
        super(appKey);
        this.propertyId = propertyId;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        mParameters = ByteBuffer.allocate(2).order(LITTLE_ENDIAN).putShort(propertyId.getPropertyId()).array();
    }

    public DeviceProperty getPropertyId() {
        return propertyId;
    }
}
