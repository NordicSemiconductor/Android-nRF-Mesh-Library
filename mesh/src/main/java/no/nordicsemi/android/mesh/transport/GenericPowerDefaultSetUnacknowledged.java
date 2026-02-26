package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.logger.MeshLogger;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a GenericPowerDefaultSetUnacknowledged message.
 */
public class GenericPowerDefaultSetUnacknowledged extends ApplicationMessage {

    private static final String TAG = GenericPowerDefaultSetUnacknowledged.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_POWER_DEFAULT_SET_UNACKNOWLEDGED;
    private static final int GENERIC_POWER_DEFAULT_SET_PARAMS_LENGTH = 2;

    private final int mPowerDefault;

    /**
     * Constructs GenericPowerDefaultSetUnacknowledged message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @param powerDefault Default power value
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public GenericPowerDefaultSetUnacknowledged(@NonNull final ApplicationKey appKey,
                                                final int powerDefault) throws IllegalArgumentException {
        super(appKey);
        if (powerDefault < 0 || powerDefault > 65535)
            throw new IllegalArgumentException("Generic power default value must be between 0 to 65535");
        this.mPowerDefault = powerDefault;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        final ByteBuffer paramsBuffer = ByteBuffer.allocate(GENERIC_POWER_DEFAULT_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        paramsBuffer.putShort((short) mPowerDefault);
        mParameters = paramsBuffer.array();
        MeshLogger.verbose(TAG, "Power default: " + mPowerDefault);
    }
}