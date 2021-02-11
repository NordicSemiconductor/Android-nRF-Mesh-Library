package no.nordicsemi.android.mesh.transport;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static no.nordicsemi.android.mesh.Scene.isValidSceneNumber;

/**
 * To be used as a wrapper class when creating a SceneStoreUnacknowledged message.
 */
public class SceneStoreUnacknowledged extends ApplicationMessage {

    private static final String TAG = SceneStoreUnacknowledged.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SCENE_STORE_UNACKNOWLEDGED;
    private static final int SCENE_STORE_PARAMS_LENGTH = 2;

    private int mSceneNumber;

    /**
     * Constructs SceneStoreUnacknowledged message.
     *
     * @param appKey      {@link ApplicationKey} for this message
     * @param sceneNumber Scene number of SceneStoreUnacknowledged message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public SceneStoreUnacknowledged(@NonNull final ApplicationKey appKey,
                                    final int sceneNumber) {
        super(appKey);
        if (isValidSceneNumber(sceneNumber))
            this.mSceneNumber = sceneNumber;
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
        Log.v(TAG, "Scenes Number: " + mSceneNumber);
        paramsBuffer = ByteBuffer.allocate(SCENE_STORE_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        paramsBuffer.putShort((short) mSceneNumber);
        mParameters = paramsBuffer.array();
    }
}
