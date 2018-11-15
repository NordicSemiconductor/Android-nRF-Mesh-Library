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
public class LightLightnessSet extends GenericMessage {

    private static final String TAG = LightLightnessSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.LIGHT_LIGHTNESS_SET;
    private static final int LIGHT_LIGHTNESS_SET_TRANSITION_PARAMS_LENGTH = 5;
    private static final int LIGHT_LIGHTNESS_SET_PARAMS_LENGTH = 3;

    private final Integer mTransitionSteps;
    private final Integer mTransitionResolution;
    private final Integer mDelay;
    private final int mLightness;

    /**
     * Constructs GenericLevelSet message.
     *
     * @param node   Mesh node this message is to be sent to
     * @param appKey application key for this message
     * @param lightLightness  lightness of the LightLightnessModel
     * @param aszmic size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public LightLightnessSet(@NonNull final ProvisionedMeshNode node,
                             @NonNull final byte[] appKey,
                             final int lightLightness,
                             final int aszmic) throws IllegalArgumentException {
        this(node, appKey, null, null, null, lightLightness, aszmic);
    }

    /**
     * Constructs GenericLevelSet message.
     *
     * @param node                 Mesh node this message is to be sent to
     * @param appKey               application key for this message
     * @param transitionSteps      transition steps for the lightLightness
     * @param transitionResolution transition resolution for the lightLightness
     * @param delay                delay for this message to be executed 0 - 1275 milliseconds
     * @param lightLightness                lightLightness of the GenericLevelModel
     * @param aszmic               size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    @SuppressWarnings("WeakerAccess")
    public LightLightnessSet(@NonNull final ProvisionedMeshNode node,
                             @NonNull final byte[] appKey,
                             @NonNull final Integer transitionSteps,
                             @NonNull final Integer transitionResolution,
                             @NonNull final Integer delay,
                             final int lightLightness,
                             final int aszmic) throws IllegalArgumentException {
        super(node, appKey, aszmic);
        this.mTransitionSteps = transitionSteps;
        this.mTransitionResolution = transitionResolution;
        this.mDelay = delay;
        if (lightLightness < 0 || lightLightness > 65535)
            throw new IllegalArgumentException("Light lightLightness value must be between 0 to 65535");
        this.mLightness = lightLightness;
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
        Log.v(TAG, "Lightness: " + mLightness);
        if (mTransitionSteps == null || mTransitionResolution == null || mDelay == null) {
            paramsBuffer = ByteBuffer.allocate(LIGHT_LIGHTNESS_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) mLightness);
            paramsBuffer.put((byte) mNode.getSequenceNumber());
        } else {
            Log.v(TAG, "Transition steps: " + mTransitionSteps);
            Log.v(TAG, "Transition step resolution: " + mTransitionResolution);
            paramsBuffer = ByteBuffer.allocate(LIGHT_LIGHTNESS_SET_TRANSITION_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) (mLightness));
            paramsBuffer.put((byte) mNode.getSequenceNumber());
            paramsBuffer.put((byte) (mTransitionResolution << 6 | mTransitionSteps));
            final int delay = mDelay;
            paramsBuffer.put((byte) delay);
        }
        mParameters = paramsBuffer.array();
    }


}
