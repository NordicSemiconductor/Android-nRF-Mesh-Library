package no.nordicsemi.android.mesh.transport;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * SensorGet message.
 */
public class SensorGet extends ApplicationMessage {

    private static final String TAG = SensorGet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SENSOR_GET;

    final DeviceProperty property;

    /**
     * Constructs SensorGet message.
     *
     * @param appKey   {@link ApplicationKey} key for this message.
     * @param property {@link DeviceProperty} device property.
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public SensorGet(@NonNull final ApplicationKey appKey, @Nullable final DeviceProperty property) {
        super(appKey);
        this.property = property;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        if (property != null)
            mParameters = ByteBuffer.allocate(2).order(LITTLE_ENDIAN).putShort(property.getPropertyId()).array();
    }
}
