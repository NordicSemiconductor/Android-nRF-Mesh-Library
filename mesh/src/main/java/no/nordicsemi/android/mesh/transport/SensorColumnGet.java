package no.nordicsemi.android.mesh.transport;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * SensorColumnGet message.
 */
public class SensorColumnGet extends ApplicationMessage {

    private static final String TAG = SensorColumnGet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SENSOR_COLUMN_GET;

    private final DeviceProperty property;
    private final byte[] rawValueX;

    /**
     * Constructs SensorColumnGet message.
     *
     * @param appKey    {@link ApplicationKey} key for this message.
     * @param property  {@link DeviceProperty} device property.
     * @param rawValueX Raw value x
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public SensorColumnGet(@NonNull final ApplicationKey appKey, @NonNull final DeviceProperty property, @NonNull final byte[] rawValueX) {
        super(appKey);
        this.property = property;
        this.rawValueX = rawValueX;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        mParameters = ByteBuffer.allocate(2 + rawValueX.length).order(LITTLE_ENDIAN)
                .putShort(property.getPropertyId())
                .put(rawValueX)
                .array();
    }
}
