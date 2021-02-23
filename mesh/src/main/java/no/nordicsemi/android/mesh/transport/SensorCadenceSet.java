package no.nordicsemi.android.mesh.transport;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * SensorCadenceSet message.
 */
public class SensorCadenceSet extends ApplicationMessage {

    private static final String TAG = SensorCadenceSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SENSOR_CADENCE_SET;

    private final DeviceProperty property;
    private final byte statusMinInterval;

    /**
     * Constructs SensorCadenceSet message.
     *
     * @param appKey   {@link ApplicationKey} key for this message.
     * @param property {@link DeviceProperty} device property.
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public SensorCadenceSet(@NonNull final ApplicationKey appKey,
                            @NonNull final DeviceProperty property,
                            final byte periodDivisor,
                            final byte statusMinInterval) {
        super(appKey);
        this.property = property;
        this.statusMinInterval = statusMinInterval;
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
