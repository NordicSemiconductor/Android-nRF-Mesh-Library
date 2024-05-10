package no.nordicsemi.android.mesh.transport;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.logger.MeshLogger;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a GenericLevelDeltaSet message.
 */
@SuppressWarnings("unused")
public class GenericDeltaSet extends ApplicationMessage {

    private static final String TAG = GenericDeltaSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_DELTA_SET;
    private static final int GENERIC_DELTA_SET_TRANSITION_PARAMS_LENGTH = 7;
    private static final int GENERIC_DELTA_SET_PARAMS_LENGTH = 5;

    private final Integer mTransitionSteps;
    private final Integer mTransitionResolution;
    private final Integer mDelay;
    private final int mDelta;
    private final int tId;

    /**
     * Constructs GenericLevelDeltaSet message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @param delta  Level delta value
     * @param tId    Transaction id which must be incremented for every message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public GenericDeltaSet(@NonNull final ApplicationKey appKey,
                           final int delta,
                           final int tId) throws IllegalArgumentException {
        this(appKey, null, null, null, delta, tId);
    }

    /**
     * Constructs GenericLevelDeltaSet message.
     *
     * @param appKey               {@link ApplicationKey} key for this message
     * @param transitionSteps      Transition steps for the level
     * @param transitionResolution Transition resolution for the level
     * @param delay                Delay for this message to be executed 0 - 1275 milliseconds
     * @param delta                Level delta of the GenericLevelModel
     * @param tId                  Transaction id
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public GenericDeltaSet(@NonNull final ApplicationKey appKey,
                           @Nullable final Integer transitionSteps,
                           @Nullable final Integer transitionResolution,
                           @Nullable final Integer delay,
                           final int delta,
                           final int tId) {
        super(appKey);
        this.mTransitionSteps = transitionSteps;
        this.mTransitionResolution = transitionResolution;
        this.mDelay = delay;
        this.tId = tId;
        this.mDelta = delta;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        final ByteBuffer paramsBuffer;
        MeshLogger.verbose(TAG, "Delta: " + mDelta);
        if (mTransitionSteps == null || mTransitionResolution == null || mDelay == null) {
            paramsBuffer = ByteBuffer.allocate(GENERIC_DELTA_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putInt((short) mDelta);
            paramsBuffer.put((byte) tId);
        } else {
            MeshLogger.verbose(TAG, "Transition steps: " + mTransitionSteps);
            MeshLogger.verbose(TAG, "Transition step resolution: " + mTransitionResolution);
            paramsBuffer = ByteBuffer.allocate(GENERIC_DELTA_SET_TRANSITION_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putInt((short) (mDelta));
            paramsBuffer.put((byte) tId);
            paramsBuffer.put((byte) (mTransitionResolution << 6 | mTransitionSteps));
            final int delay = mDelay;
            paramsBuffer.put((byte) delay);
        }
        mParameters = paramsBuffer.array();
    }
}
