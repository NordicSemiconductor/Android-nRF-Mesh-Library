package no.nordicsemi.android.meshprovisioner.transport;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a GenericLevelSetUnacknowledged message.
 */
@SuppressWarnings("unused")
public class GenericLevelSetUnacknowledged extends GenericMessage {

    private static final String TAG = GenericLevelSetUnacknowledged.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_LEVEL_SET_UNACKNOWLEDGED;
    private static final int GENERIC_LEVEL_SET_TRANSITION_PARAMS_LENGTH = 5;
    private static final int GENERIC_LEVEL_SET_PARAMS_LENGTH = 3;

    private final Integer mTransitionSteps;
    private final Integer mTransitionResolution;
    private final Integer mDelay;
    private final int mLevel;

    /**
     * Constructs GenericLevelSetUnacknowledged message
     *
     * @param node                 Mesh node this message is to be sent to
     * @param appKey               application key for this message
     * @param level                level to be set on the GenericLevelModel
     * @param aszmic               size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public GenericLevelSetUnacknowledged(@NonNull final ProvisionedMeshNode node,
                                         @NonNull final byte[] appKey,
                                         final int level,
                                         final int aszmic) throws IllegalArgumentException {
        this(node, appKey, level, null, null, null, aszmic);
    }

    /**
     * Constructs GenericLevelSetUnacknowledged message
     *
     * @param node                 Mesh node this message is to be sent to
     * @param appKey               application key for this message
     * @param level                level to be set on the GenericLevelModel
     * @param transitionSteps      transition steps for the level
     * @param transitionResolution transition resolution for the level
     * @param delay                delay for this message to be executed 0 - 1275 milliseconds
     * @param aszmic               size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    @SuppressWarnings("WeakerAccess")
    public GenericLevelSetUnacknowledged(@NonNull final ProvisionedMeshNode node,
                                         @NonNull final byte[] appKey,
                                         final int level,
                                         @Nullable final Integer transitionSteps,
                                         @Nullable final Integer transitionResolution,
                                         @Nullable final Integer delay,
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
