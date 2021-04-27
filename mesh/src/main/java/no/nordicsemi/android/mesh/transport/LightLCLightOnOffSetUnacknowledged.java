package no.nordicsemi.android.mesh.transport;


import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;

/**
 * To be used as a wrapper class to create light ctl get message.
 */
@SuppressWarnings("unused")
public class LightLCLightOnOffSetUnacknowledged extends ApplicationMessage {

    private static final String TAG = LightLCLightOnOffSetUnacknowledged.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.LIGHT_LC_LIGHT_ON_OFF_SET_UNACKNOWLEDGED;
    private final Integer mTransitionSteps;
    private final Integer mTransitionResolution;
    private final Integer mDelay;
    private final boolean mState;
    private final int tId;

    /**
     * Constructs LightLCLightOnOffSetUnacknowledged message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @param state  Boolean state of the Light LC Light
     * @param tId    transaction id
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public LightLCLightOnOffSetUnacknowledged(@NonNull final ApplicationKey appKey,
                                              final boolean state,
                                              final int tId) throws IllegalArgumentException {
        this(appKey, state, tId, null, null, null);
    }

    /**
     * Constructs LightLCLightOnOffSetUnacknowledged message.
     *
     * @param appKey               {@link ApplicationKey} key for this message
     * @param state                Boolean state of the Light LC Light
     * @param tId                  transaction id
     * @param transitionSteps      Transition steps for the level
     * @param transitionResolution Transition resolution for the level
     * @param delay                Delay for this message to be executed 0 - 1275 milliseconds
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public LightLCLightOnOffSetUnacknowledged(@NonNull final ApplicationKey appKey,
                                              final boolean state,
                                              final int tId,
                                              @Nullable final Integer transitionSteps,
                                              @Nullable final Integer transitionResolution,
                                              @Nullable final Integer delay) {
        super(appKey);
        this.mTransitionSteps = transitionSteps;
        this.mTransitionResolution = transitionResolution;
        this.mDelay = delay;
        this.mState = state;
        this.tId = tId;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = (byte) mAppKey.getAid();
        final ByteBuffer paramsBuffer;
        Log.v(TAG, "State: " + (mState ? "ON" : "OFF"));
        if (mTransitionSteps == null || mTransitionResolution == null || mDelay == null) {
            paramsBuffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.put((byte) (mState ? 0x01 : 0x00));
            paramsBuffer.put((byte) this.tId);
        } else {
            Log.v(TAG, "Transition steps: " + mTransitionSteps);
            Log.v(TAG, "Transition step resolution: " + mTransitionResolution);
            paramsBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.put((byte) (mState ? 0x01 : 0x00));
            paramsBuffer.put((byte) this.tId);
            paramsBuffer.put((byte) (mTransitionResolution << 6 | mTransitionSteps));
            final int delay = mDelay;
            paramsBuffer.put((byte) delay);
        }
        mParameters = paramsBuffer.array();
    }
}
