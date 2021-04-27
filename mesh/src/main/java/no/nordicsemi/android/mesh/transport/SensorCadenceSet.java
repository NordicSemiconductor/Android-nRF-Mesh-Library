package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.transport.SensorMessage.SensorCadence;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * SensorCadenceSet message.
 */
public class SensorCadenceSet extends ApplicationMessage {

    private static final String TAG = SensorCadenceSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SENSOR_CADENCE_SET;

    private final SensorCadence cadence;

    /**
     * Constructs SensorCadenceSet message.
     *
     * @param appKey  {@link ApplicationKey} key for this message.
     * @param cadence {@link SensorCadence} Sensor Cadence.
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public SensorCadenceSet(@NonNull final ApplicationKey appKey,
                            @NonNull final SensorCadence cadence) {
        super(appKey);
        this.cadence = cadence;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        mParameters = cadence.toBytes();
    }
}
