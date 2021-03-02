package no.nordicsemi.android.mesh.transport;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.sensorutils.DevicePropertyCharacteristic;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_SETTING_SET;

/**
 * SensorSettingSet message.
 */
public class SensorSettingSet extends ApplicationMessage {

    private static final String TAG = SensorSettingSet.class.getSimpleName();
    private static final int OP_CODE = SENSOR_SETTING_SET;

    private final DeviceProperty propertyId;
    private final DeviceProperty sensorSettingPropertyId;
    private final DevicePropertyCharacteristic<?> sensorSetting;

    /**
     * Constructs SensorSettingSet message.
     *
     * @param appKey                  {@link ApplicationKey} Key for this message.
     * @param propertyId              {@link DeviceProperty} Device property.
     * @param sensorSettingPropertyId {@link DeviceProperty} Sensor setting property ID.
     * @param sensorSetting           {@link DevicePropertyCharacteristic<>} Sensor setting value.
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public SensorSettingSet(@NonNull final ApplicationKey appKey,
                            @NonNull final DeviceProperty propertyId,
                            @NonNull final DeviceProperty sensorSettingPropertyId,
                            @NonNull final DevicePropertyCharacteristic<?> sensorSetting) {
        super(appKey);
        this.propertyId = propertyId;
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

    /**
     * Returns the property id.
     */
    public DeviceProperty getPropertyId() {
        return propertyId;
    }

    /**
     * Returns the Sensor Setting Property id.
     */
    public DeviceProperty getSensorSettingPropertyId() {
        return sensorSettingPropertyId;
    }
}
