package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a SceneGet message.
 */
public class SceneGet extends ApplicationMessage {

    private static final String TAG = SceneGet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SCENE_GET;

    /**
     * Constructs SceneGet message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public SceneGet(@NonNull final ApplicationKey appKey) {
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
