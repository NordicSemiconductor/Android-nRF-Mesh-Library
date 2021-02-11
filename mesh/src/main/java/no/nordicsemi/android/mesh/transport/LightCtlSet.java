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
 * To be used as a wrapper class when creating a LightCtlSet message.
 */
@SuppressWarnings("unused")
public class LightCtlSet extends ApplicationMessage {

    private static final String TAG = LightCtlSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.LIGHT_CTL_SET;
    private static final int LIGHT_CTL_SET_TRANSITION_PARAMS_LENGTH = 9;
    private static final int LIGHT_CTL_SET_PARAMS_LENGTH = 7;

    private final Integer mTransitionSteps;
    private final Integer mTransitionResolution;
    private final Integer mDelay;
    private final int mLightness;
    private final int mTemperature;
    private final int mDeltaUv;
    private final int tId;

    /**
     * Constructs LightCtlSet message.
     *
     * @param appKey           {@link ApplicationKey} key for this message
     * @param lightLightness   LightLightness of the LightCtlModel
     * @param lightTemperature Temperature of the LightCtlModel
     * @param lightDeltaUv     Delta uv of the LightCtlModel
     * @param tId              Transaction id
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public LightCtlSet(@NonNull final ApplicationKey appKey,
                       final int lightLightness,
                       final int lightTemperature,
                       final int lightDeltaUv,
                       final int tId) throws IllegalArgumentException {
        this(appKey, null, null, null, lightLightness, lightTemperature, lightDeltaUv, tId);
    }

    /**
     * Constructs LightCtlSet message.
     *
     * @param appKey               application key for this message
     * @param transitionSteps      transition steps for the lightLightness
     * @param transitionResolution transition resolution for the lightLightness
     * @param delay                delay for this message to be executed 0 - 1275 milliseconds
     * @param lightLightness       lightLightness of the LightCtlModel
     * @param lightTemperature     temperature of the LightCtlModel
     * @param lightDeltaUv         delta uv of the LightCtlModel
     * @param tId                  transaction id
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    @SuppressWarnings("WeakerAccess")
    public LightCtlSet(@NonNull final ApplicationKey appKey,
                       @Nullable final Integer transitionSteps,
                       @Nullable final Integer transitionResolution,
                       @Nullable final Integer delay,
                       final int lightLightness,
                       final int lightTemperature,
                       final int lightDeltaUv,
                       final int tId) throws IllegalArgumentException {
        super(appKey);
        this.mTransitionSteps = transitionSteps;
        this.mTransitionResolution = transitionResolution;
        this.mDelay = delay;
        if (lightLightness < 0 || lightLightness > 0xFFFF)
            throw new IllegalArgumentException("Light lightness value must be between 0 to 0xFFFF");
        if (lightTemperature < 0x0320 || lightTemperature > 0x4E20)
            throw new IllegalArgumentException("Light temperature value must be between 0x0320 to 0x4E20");
        if (lightDeltaUv < -32768 || lightDeltaUv > 32767)
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
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        final ByteBuffer paramsBuffer;
        Log.v(TAG, "Lightness: " + mLightness);
        Log.v(TAG, "Temperature: " + mTemperature);
        Log.v(TAG, "Delta UV: " + mDeltaUv);
        Log.v(TAG, "TID: " + (byte) tId);
        if (mTransitionSteps == null || mTransitionResolution == null || mDelay == null) {
            paramsBuffer = ByteBuffer.allocate(LIGHT_CTL_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) mLightness);
            paramsBuffer.putShort((short) mTemperature);
            paramsBuffer.putShort((short) mDeltaUv);
            paramsBuffer.put((byte) tId);
        } else {
            Log.v(TAG, "Transition steps: " + mTransitionSteps);
            Log.v(TAG, "Transition step resolution: " + mTransitionResolution);
            paramsBuffer = ByteBuffer.allocate(LIGHT_CTL_SET_TRANSITION_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) mLightness);
            paramsBuffer.putShort((short) mTemperature);
            paramsBuffer.putShort((short) mDeltaUv);
            paramsBuffer.put((byte) tId);
            paramsBuffer.put((byte) (mTransitionResolution << 6 | mTransitionSteps));
            final int delay = mDelay;
            paramsBuffer.put((byte) delay);
        }
        mParameters = paramsBuffer.array();
    }
}
