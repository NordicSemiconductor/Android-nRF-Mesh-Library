package no.nordicsemi.android.mesh.transport;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.sensorutils.DevicePropertyCharacteristic;

/**
 * LightLCPropertySet
 */
@SuppressWarnings("unused")
public class LightLCPropertySet extends ApplicationMessage {

    private static final String TAG = LightLCPropertySet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.LIGHT_LC_PROPERTY_SET;
    private final DeviceProperty property;
    private final DevicePropertyCharacteristic<?> characteristic;

    /**
     * Constructs LightLCPropertySet message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public LightLCPropertySet(@NonNull final ApplicationKey appKey, @NonNull final DeviceProperty property, @NonNull final DevicePropertyCharacteristic<?> characteristic) throws IllegalArgumentException {
        super(appKey);
        this.property = property;
        this.characteristic = characteristic;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = (byte) mAppKey.getAid();
        mParameters = ByteBuffer.allocate(2 + characteristic.getLength()).order(ByteOrder.LITTLE_ENDIAN).putShort(property.getPropertyId()).put(characteristic.getBytes()).array();
    }
}
