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
public class LightLightnessSet extends ApplicationMessage {

    private static final String TAG = LightLightnessSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.LIGHT_LIGHTNESS_SET;
    private static final int LIGHT_LIGHTNESS_SET_TRANSITION_PARAMS_LENGTH = 5;
    private static final int LIGHT_LIGHTNESS_SET_PARAMS_LENGTH = 3;

    private final Integer mTransitionSteps;
    private final Integer mTransitionResolution;
    private final Integer mDelay;
    private final int mLightness;
    private final int tId;

    /**
     * Constructs GenericLevelSet message.
     *
     * @param appKey         {@link ApplicationKey} key for this message
     * @param lightLightness lightLightness of the LightLightnessModel
     * @param tId            transaction id
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public LightLightnessSet(@NonNull final ApplicationKey appKey,
                             final int lightLightness,
                             final int tId) throws IllegalArgumentException {
        this(appKey, null, null, null, lightLightness, tId);
    }

    /**
     * Constructs GenericLevelSet message.
     *
     * @param appKey               application key for this message
     * @param transitionSteps      transition steps for the level
     * @param transitionResolution transition resolution for the level
     * @param delay                delay for this message to be executed 0 - 1275 milliseconds
     * @param lightLightness       lightLightness
     * @param tId                  transaction id
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    @SuppressWarnings("WeakerAccess")
    public LightLightnessSet(@NonNull final ApplicationKey appKey,
                             @Nullable final Integer transitionSteps,
                             @Nullable final Integer transitionResolution,
                             @Nullable final Integer delay,
                             final int lightLightness,
                             final int tId) throws IllegalArgumentException {
        super(appKey);
        this.mTransitionSteps = transitionSteps;
        this.mTransitionResolution = transitionResolution;
        this.mDelay = delay;
        this.tId = tId;
        if (lightLightness < 0 || lightLightness > 0xFFFF)
            throw new IllegalArgumentException("Light lightness value must be between 0x0000 and 0xFFFF");
        this.mLightness = lightLightness;
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
        if (mTransitionSteps == null || mTransitionResolution == null || mDelay == null) {
            paramsBuffer = ByteBuffer.allocate(LIGHT_LIGHTNESS_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) mLightness);
            paramsBuffer.put((byte) tId);
        } else {
            Log.v(TAG, "Transition steps: " + mTransitionSteps);
            Log.v(TAG, "Transition step resolution: " + mTransitionResolution);
            paramsBuffer = ByteBuffer.allocate(LIGHT_LIGHTNESS_SET_TRANSITION_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) (mLightness));
            paramsBuffer.put((byte) tId);
            paramsBuffer.put((byte) (mTransitionResolution << 6 | mTransitionSteps));
            final int delay = mDelay;
            paramsBuffer.put((byte) delay);
        }
        mParameters = paramsBuffer.array();
    }


}
