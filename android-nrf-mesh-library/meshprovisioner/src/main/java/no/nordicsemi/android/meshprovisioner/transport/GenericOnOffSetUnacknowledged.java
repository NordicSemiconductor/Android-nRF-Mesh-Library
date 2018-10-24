package no.nordicsemi.android.meshprovisioner.transport;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a GenericOnOffSetUnacknowledged message.
 */
@SuppressWarnings("unused")
public class GenericOnOffSetUnacknowledged extends GenericMessage {

    private static final String TAG = GenericOnOffSetUnacknowledged.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_ON_OFF_SET_UNACKNOWLEDGED;
    private static final int GENERIC_ON_OFF_SET_TRANSITION_PARAMS_LENGTH = 4;
    private static final int GENERIC_ON_OFF_SET_PARAMS_LENGTH = 2;

    private final Integer mTransitionSteps;
    private final Integer mTransitionResolution;
    private final Integer mDelay;
    private final boolean mState;

    /**
     * Constructs GenericOnOffSet message.
     *
     * @param node        Mesh node this message is to be sent to
     * @param appKey      Application key for this message
     * @param state       Boolean state of the GenericOnOffModel
     * @param aszmic      Size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public GenericOnOffSetUnacknowledged(@NonNull final ProvisionedMeshNode node,
                                         @NonNull final byte[] appKey,
                                         final boolean state,
                                         final int aszmic) throws IllegalArgumentException {
        this(node, appKey, state, null, null, null, aszmic);
    }

    /**
     * Constructs GenericOnOffSet message.
     *
     * @param node                 Mesh node this message is to be sent to
     * @param appKey               Application key for this message
     * @param state                Boolean state of the GenericOnOffModel
     * @param transitionSteps      Transition steps for the level
     * @param transitionResolution Transition resolution for the level
     * @param delay                Delay for this message to be executed 0 - 1275 milliseconds
     * @param aszmic               Size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    @SuppressWarnings("WeakerAccess")
    public GenericOnOffSetUnacknowledged(@NonNull final ProvisionedMeshNode node,
                                         @NonNull final byte[] appKey,
                                         final boolean state,
                                         @Nullable final Integer transitionSteps,
                                         @Nullable final Integer transitionResolution,
                                         @Nullable final Integer delay,
                                         final int aszmic) {
        super(node, appKey, aszmic);
        this.mTransitionSteps = transitionSteps;
        this.mTransitionResolution = transitionResolution;
        this.mDelay = delay;
        this.mState = state;
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
        Log.v(TAG, "State: " + (mState ? "ON" : "OFF"));
        if (mTransitionSteps == null || mTransitionResolution == null || mDelay == null) {
            paramsBuffer = ByteBuffer.allocate(GENERIC_ON_OFF_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.put((byte) (mState ? 0x01 : 0x00));
            paramsBuffer.put((byte) mNode.getSequenceNumber());
        } else {
            Log.v(TAG, "Transition steps: " + mTransitionSteps);
            Log.v(TAG, "Transition step resolution: " + mTransitionResolution);
            paramsBuffer = ByteBuffer.allocate(GENERIC_ON_OFF_SET_TRANSITION_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.put((byte) (mState ? 0x01 : 0x00));
            paramsBuffer.put((byte) mNode.getSequenceNumber());
            paramsBuffer.put((byte) (mTransitionResolution << 6 | mTransitionSteps));
            final int delay = mDelay;
            paramsBuffer.put((byte) delay);
        }
        mParameters = paramsBuffer.array();

    }
}
