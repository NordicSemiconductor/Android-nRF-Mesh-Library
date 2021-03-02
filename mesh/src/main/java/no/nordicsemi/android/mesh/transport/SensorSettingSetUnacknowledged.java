package no.nordicsemi.android.mesh.transport;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.sensorutils.DevicePropertyCharacteristic;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * SensorSettingSetUnacknowledged message.
 */
public class SensorSettingSetUnacknowledged extends ApplicationMessage {

    private static final String TAG = SensorSettingSetUnacknowledged.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SENSOR_SETTING_SET_UNACKNOWLEDGED;

    private final DeviceProperty propertyId;
    private final DeviceProperty sensorSettingPropertyId;
    private final DevicePropertyCharacteristic<?> sensorSetting;
    /**
     * Constructs SensorSettingSetUnacknowledged message.
     *
     * @param appKey   {@link ApplicationKey} key for this message.
     * @param property {@link DeviceProperty} device property.
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public SensorSettingSetUnacknowledged(@NonNull final ApplicationKey appKey,
                                          @NonNull final DeviceProperty property,
                                          @NonNull final DeviceProperty sensorSettingPropertyId,
                                          @NonNull final DevicePropertyCharacteristic<?> sensorSetting) {
        super(appKey);
        this.propertyId = property;
        this.sensorSettingPropertyId = sensorSettingPropertyId;
        this.sensorSetting = sensorSetting;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        mParameters = ByteBuffer.allocate(4 + sensorSetting.getLength()).order(LITTLE_ENDIAN)
                .putShort(propertyId.getPropertyId())
                .putShort(sensorSettingPropertyId.getPropertyId())
                .put(sensorSetting.getBytes())
                .array();
    }
}
