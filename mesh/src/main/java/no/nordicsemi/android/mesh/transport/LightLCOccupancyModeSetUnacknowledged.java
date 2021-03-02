package no.nordicsemi.android.mesh.transport;


import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;

/**
 * LightLCOccupancyModeSetUnacknowledged
 */
@SuppressWarnings("unused")
public class LightLCOccupancyModeSetUnacknowledged extends ApplicationMessage {

    private static final String TAG = LightLCOccupancyModeSetUnacknowledged.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.LIGHT_LC_OCCUPANCY_MODE_SET_UNACKNOWLEDGED;
    private final boolean mState;

    /**
     * Constructs LightLCOccupancyModeSetUnacknowledged message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @param state  Boolean state of the Light occupancy Mode
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public LightLCOccupancyModeSetUnacknowledged(@NonNull final ApplicationKey appKey,
                                                 final boolean state) throws IllegalArgumentException {
        super(appKey);
        this.mState = state;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = (byte) mAppKey.getAid();
        mParameters = new byte[]{(byte) (mState ? 0x01 : 0x00)};
    }
}
