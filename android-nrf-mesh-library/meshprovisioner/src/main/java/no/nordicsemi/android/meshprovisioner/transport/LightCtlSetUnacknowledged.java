package no.nordicsemi.android.meshprovisioner.transport;


import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a LightCtlSetUnacknowledged message.
 */
@SuppressWarnings("unused")
public class LightCtlSetUnacknowledged extends GenericMessage {

    private static final String TAG = LightCtlSetUnacknowledged.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.LIGHT_CTL_SET_UNACKNOWLEDGED;
    private static final int LIGHT_LIGHTNESS_SET_TRANSITION_PARAMS_LENGTH = 9;
    private static final int LIGHT_LIGHTNESS_SET_PARAMS_LENGTH = 7;

    private final Integer mTransitionSteps;
    private final Integer mTransitionResolution;
    private final Integer mDelay;
    private final int mLightness;
    private final int mTemperature;
    private final int mDeltaUv;
    private final byte tId;

    /**
     * Constructs LightCtlSetUnacknowledged message.
     *
     * @param node             Mesh node this message is to be sent to
     * @param appKey           application key for this message
     * @param lightLightness   lightLightness of the LightCtlModel
     * @param lightTemperature temperature of the LightCtlModel
     * @param lightDeltaUv     delta uv of the LightCtlModel
     * @param aszmic           size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public LightCtlSetUnacknowledged(@NonNull final ProvisionedMeshNode node,
                                     @NonNull final byte[] appKey,
                                     final int lightLightness,
                                     final int lightTemperature,
                                     final int lightDeltaUv,
                                     final byte tId,
                                     final int aszmic) throws IllegalArgumentException {
        this(node, appKey, null, null, null, lightLightness, lightTemperature, lightDeltaUv, tId, aszmic);
    }

    /**
     * Constructs LightCtlSetUnacknowledged message.
     *
     * @param node                 Mesh node this message is to be sent to
     * @param appKey               application key for this message
     * @param transitionSteps      transition steps for the lightLightness
     * @param transitionResolution transition resolution for the lightLightness
     * @param delay                delay for this message to be executed 0 - 1275 milliseconds
     * @param lightLightness       lightLightness of the LightCtlModel
     * @param lightTemperature     temperature of the LightCtlModel
     * @param lightDeltaUv         delta uv of the LightCtlModel
     * @param aszmic               size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    @SuppressWarnings("WeakerAccess")
    public LightCtlSetUnacknowledged(@NonNull final ProvisionedMeshNode node,
                                     @NonNull final byte[] appKey,
                                     @NonNull final Integer transitionSteps,
                                     @NonNull final Integer transitionResolution,
                                     @NonNull final Integer delay,
                                     final int lightLightness,
                                     final int lightTemperature,
                                     final int lightDeltaUv,
                                     final byte tId,
                                     final int aszmic) throws IllegalArgumentException {
        super(node, appKey, aszmic);
        this.mTransitionSteps = transitionSteps;
        this.mTransitionResolution = transitionResolution;
        this.mDelay = delay;
        if (lightLightness < 0 || lightLightness > 0xFFFF)
            throw new IllegalArgumentException("Light lightness value must be between 0 to 0xFFFF");
        if (lightTemperature < 0x0320 || lightTemperature > 0x4E20)
            throw new IllegalArgumentException("Light temperature value must be between 0x0320 to 0x4E20");
        if (lightDeltaUv != 0 && lightDeltaUv < 0x8000 ||lightDeltaUv > 0x7fff)
            throw new IllegalArgumentException("Light delta uv value must be between 0x8000 to 0x7FFF or 0");
        this.mLightness = lightLightness;
        this.mTemperature = lightTemperature;
        this.mDeltaUv = lightDeltaUv;
        this.tId = tId;
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
        Log.v(TAG, "Temperature: " + mTemperature);
        Log.v(TAG, "Delta UV: " + mDeltaUv);
        Log.v(TAG, "TID: " + tId);
        if (mTransitionSteps == null || mTransitionResolution == null || mDelay == null) {
            paramsBuffer = ByteBuffer.allocate(LIGHT_LIGHTNESS_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) mLightness);
            paramsBuffer.putShort((short) mTemperature);
            paramsBuffer.putShort((short) mDeltaUv);
            paramsBuffer.put(tId);
        } else {
            Log.v(TAG, "Transition steps: " + mTransitionSteps);
            Log.v(TAG, "Transition step resolution: " + mTransitionResolution);
            paramsBuffer = ByteBuffer.allocate(LIGHT_LIGHTNESS_SET_TRANSITION_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) mLightness);
            paramsBuffer.putShort((short) mTemperature);
            paramsBuffer.putShort((short) mDeltaUv);
            paramsBuffer.put(tId);
            paramsBuffer.put((byte) (mTransitionResolution << 6 | mTransitionSteps));
            final int delay = mDelay;
            paramsBuffer.put((byte) delay);
        }
        mParameters = paramsBuffer.array();
    }


}
