package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.logger.MeshLogger;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a GenericPowerRangeSet message.
 */
public class GenericPowerRangeSet extends ApplicationMessage {

    private static final String TAG = GenericPowerRangeSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_POWER_RANGE_SET;
    private static final int GENERIC_POWER_RANGE_SET_PARAMS_LENGTH = 4;

    private final int mRangeMin;
    private final int mRangeMax;

    /**
     * Constructs GenericPowerRangeSet message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @param rangeMin Minimum range value
     * @param rangeMax Maximum range value
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public GenericPowerRangeSet(@NonNull final ApplicationKey appKey,
                                final int rangeMin,
                                final int rangeMax) throws IllegalArgumentException {
        super(appKey);
        if (rangeMin < 0 || rangeMin > 65535)
            throw new IllegalArgumentException("Generic power range min value must be between 0 to 65535");
        if (rangeMax < 0 || rangeMax > 65535)
            throw new IllegalArgumentException("Generic power range max value must be between 0 to 65535");
        if (rangeMin > rangeMax)
            throw new IllegalArgumentException("Range min value cannot be greater than range max value");
        this.mRangeMin = rangeMin;
        this.mRangeMax = rangeMax;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        final ByteBuffer paramsBuffer = ByteBuffer.allocate(GENERIC_POWER_RANGE_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        paramsBuffer.putShort((short) mRangeMin);
        paramsBuffer.putShort((short) mRangeMax);
        mParameters = paramsBuffer.array();
        MeshLogger.verbose(TAG, "Power range min: " + mRangeMin + ", max: " + mRangeMax);
    }
}