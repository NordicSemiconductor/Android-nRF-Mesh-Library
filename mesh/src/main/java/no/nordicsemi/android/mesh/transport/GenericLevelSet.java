package no.nordicsemi.android.mesh.transport;


import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a GenericLevelSet message.
 */
@SuppressWarnings("unused")
public class GenericLevelSet extends ApplicationMessage {

    private static final String TAG = GenericLevelSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_LEVEL_SET;
    private static final int GENERIC_LEVEL_SET_TRANSITION_PARAMS_LENGTH = 5;
    private static final int GENERIC_LEVEL_SET_PARAMS_LENGTH = 3;

    private final Integer mTransitionSteps;
    private final Integer mTransitionResolution;
    private final Integer mDelay;
    private final int mLevel;
    private final int tId;

    /**
     * Constructs GenericLevelSet message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @param level  Level value
     * @param tId    Transaction id which must be incremented for every message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public GenericLevelSet(@NonNull final ApplicationKey appKey,
                           final int level,
                           final int tId) throws IllegalArgumentException {
        this(appKey, null, null, null, level, tId);
    }

    /**
     * Constructs GenericLevelSet message.
     *
     * @param appKey               {@link ApplicationKey} key for this message
     * @param transitionSteps      Transition steps for the level
     * @param transitionResolution Transition resolution for the level
     * @param delay                Delay for this message to be executed 0 - 1275 milliseconds
     * @param level                Level of the GenericLevelModel
     * @param tId                  Transaction id
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public GenericLevelSet(@NonNull final ApplicationKey appKey,
                           @Nullable final Integer transitionSteps,
                           @Nullable final Integer transitionResolution,
                           @Nullable final Integer delay,
                           final int level,
                           final int tId) throws IllegalArgumentException {
        super(appKey);
        this.mTransitionSteps = transitionSteps;
        this.mTransitionResolution = transitionResolution;
        this.mDelay = delay;
        this.tId = tId;
        if (level < Short.MIN_VALUE || level > Short.MAX_VALUE)
            throw new IllegalArgumentException("Generic level value must be between -32768 to 32767");
        this.mLevel = level;
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
        Log.v(TAG, "Level: " + mLevel);
        if (mTransitionSteps == null || mTransitionResolution == null || mDelay == null) {
            paramsBuffer = ByteBuffer.allocate(GENERIC_LEVEL_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) mLevel);
            paramsBuffer.put((byte) tId);
        } else {
            Log.v(TAG, "Transition steps: " + mTransitionSteps);
            Log.v(TAG, "Transition step resolution: " + mTransitionResolution);
            paramsBuffer = ByteBuffer.allocate(GENERIC_LEVEL_SET_TRANSITION_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) (mLevel));
            paramsBuffer.put((byte) tId);
            paramsBuffer.put((byte) (mTransitionResolution << 6 | mTransitionSteps));
            final int delay = mDelay;
            paramsBuffer.put((byte) delay);
        }
        mParameters = paramsBuffer.array();
    }
}
