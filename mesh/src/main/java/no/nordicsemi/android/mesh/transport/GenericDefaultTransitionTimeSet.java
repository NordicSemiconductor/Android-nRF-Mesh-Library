package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.data.GenericTransitionTime;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.ArrayUtils;
import no.nordicsemi.android.mesh.utils.BitWriter;
import no.nordicsemi.android.mesh.utils.SecureUtils;

public class GenericDefaultTransitionTimeSet extends ApplicationMessage {

    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_DEFAULT_TRANSITION_TIME_SET;
    private static final int GENERIC_DEFAULT_TRANSITION_TIME_SET_PARAMS_LENGTH = GenericTransitionTime.TRANSITION_TIME_BITS_LENGTH;

    private final GenericTransitionTime genericTransitionTime;

    /**
     * Generic Default Transition Time Set is an acknowledged message used to set the Generic Default Transition Time state of an element (see Section 3.1.3).
     * The response to the Generic Default Transition Time Set message is a Generic Default Transition Time Status message.
     *
     * @param appKey         the appkey
     * @param genericTransitionTime The value of the Generic Default Transition Time state.
     */
    public GenericDefaultTransitionTimeSet(@NonNull ApplicationKey appKey, GenericTransitionTime genericTransitionTime) {
        super(appKey);
        this.genericTransitionTime = genericTransitionTime;
        assembleMessageParameters();
    }


    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        BitWriter bitWriter = new BitWriter(GENERIC_DEFAULT_TRANSITION_TIME_SET_PARAMS_LENGTH);
        bitWriter.write(genericTransitionTime.getValue(), GENERIC_DEFAULT_TRANSITION_TIME_SET_PARAMS_LENGTH);
        mParameters = ArrayUtils.reverseArray(bitWriter.toByteArray());
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }
}
