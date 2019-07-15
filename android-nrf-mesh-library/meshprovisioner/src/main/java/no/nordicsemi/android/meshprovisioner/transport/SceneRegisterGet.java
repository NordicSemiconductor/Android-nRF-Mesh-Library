package no.nordicsemi.android.meshprovisioner.transport;

import androidx.annotation.NonNull;

import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a SceneRegisterGet message.
 */
@SuppressWarnings("unused")
public class SceneRegisterGet extends GenericMessage {

    private static final String TAG = SceneRegisterGet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SCENE_REGISTER_GET;

    /**
     * Constructs SceneRegisterGet message.
     *
     * @param appKey application key for this message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public SceneRegisterGet(@NonNull final ApplicationKey appKey) {
        super(appKey);
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
    }
}
