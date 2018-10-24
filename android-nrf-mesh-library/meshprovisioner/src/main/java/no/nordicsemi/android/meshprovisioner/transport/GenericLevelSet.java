package no.nordicsemi.android.meshprovisioner.transport;


import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a GenericLevelSet message.
 */
@SuppressWarnings("unused")
public class GenericLevelSet extends GenericMessage {

    private static final String TAG = GenericLevelSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_LEVEL_SET;
    private static final int GENERIC_LEVEL_SET_TRANSITION_PARAMS_LENGTH = 5;
    private static final int GENERIC_LEVEL_SET_PARAMS_LENGTH = 3;

    private final Integer mTransitionSteps;
    private final Integer mTransitionResolution;
    private final Integer mDelay;
    private final int mLevel;

    /**
     * Constructs GenericLevelSet message.
     *
     * @param node        Mesh node this message is to be sent to
     * @param appKey      application key for this message
     * @param level       level of the GenericLevelModel
     * @param aszmic      size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public GenericLevelSet(@NonNull final ProvisionedMeshNode node,
                           @NonNull final byte[] appKey,
                           final int level,
                           final int aszmic) throws IllegalArgumentException {
        this(node, appKey, null, null, null, level, aszmic);
    }

    /**
     * Constructs GenericLevelSet message.
     *
     * @param node                 Mesh node this message is to be sent to
     * @param appKey               application key for this message
     * @param transitionSteps      transition steps for the level
     * @param transitionResolution transition resolution for the level
     * @param delay                delay for this message to be executed 0 - 1275 milliseconds
     * @param level                level of the GenericLevelModel
     * @param aszmic               size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    @SuppressWarnings("WeakerAccess")
    public GenericLevelSet(@NonNull final ProvisionedMeshNode node,
                           @NonNull final byte[] appKey,
                           @NonNull final Integer transitionSteps,
                           @NonNull final Integer transitionResolution,
                           @NonNull final Integer delay,
                           final int level,
                           final int aszmic) throws IllegalArgumentException {
        super(node, appKey, aszmic);
        this.mTransitionSteps = transitionSteps;
        this.mTransitionResolution = transitionResolution;
        this.mDelay = delay;
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
        mAid = SecureUtils.calculateK4(mAppKey);
        final ByteBuffer paramsBuffer;
        Log.v(TAG, "Level: " + mLevel);
        if (mTransitionSteps == null || mTransitionResolution == null || mDelay == null) {
            paramsBuffer = ByteBuffer.allocate(GENERIC_LEVEL_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) mLevel);
            paramsBuffer.put((byte) mNode.getSequenceNumber());
        } else {
            Log.v(TAG, "Transition steps: " + mTransitionSteps);
            Log.v(TAG, "Transition step resolution: " + mTransitionResolution);
            paramsBuffer = ByteBuffer.allocate(GENERIC_LEVEL_SET_TRANSITION_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) (mLevel));
            paramsBuffer.put((byte) mNode.getSequenceNumber());
            paramsBuffer.put((byte) (mTransitionResolution << 6 | mTransitionSteps));
            final int delay = mDelay;
            paramsBuffer.put((byte) delay);
        }
        mParameters = paramsBuffer.array();
    }


}
