package no.nordicsemi.android.meshprovisioner.transport;

import android.support.annotation.NonNull;

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
     * @param node                 Mesh node this message is to be sent to
     * @param appKey               application key for this message
     * @param aszmic               size of message integrity check
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    @SuppressWarnings("WeakerAccess")
    public SceneRegisterGet(@NonNull final ProvisionedMeshNode node,
                            @NonNull final byte[] appKey,
                            final int aszmic) {
        super(node, appKey, aszmic);
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey);
    }
}
