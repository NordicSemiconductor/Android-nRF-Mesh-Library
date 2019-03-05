package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling SceneStoreState messages.
 */
@SuppressWarnings("WeakerAccess")
class SceneRecallState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = SceneRecallState.class.getSimpleName();

    /**
     * Constructs {@link SceneRecallState}
     *
     * @param context     Context of the application
     * @param src         Source address
     * @param dst         Destination address to which the message must be sent to
     * @param sceneRecall Wrapper class {@link SceneStore} containing the opcode and parameters for {@link SceneStore} message
     * @param callbacks   {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    @Deprecated
    SceneRecallState(@NonNull final Context context,
                     @NonNull final byte[] src,
                     @NonNull final byte[] dst,
                     @NonNull final SceneRecall sceneRecall,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), sceneRecall, meshTransport, callbacks);
    }

    /**
     * Constructs {@link SceneRecallState}
     *
     * @param context     Context of the application
     * @param src         Source address
     * @param dst         Destination address to which the message must be sent to
     * @param sceneRecall Wrapper class {@link SceneStore} containing the opcode and parameters for {@link SceneStore} message
     * @param callbacks   {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    SceneRecallState(@NonNull final Context context,
                     final int src,
                     final int dst,
                     @NonNull final SceneRecall sceneRecall,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, sceneRecall, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.SCENE_RECALL_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final SceneRecall sceneRecall = (SceneRecall) mMeshMessage;
        final byte[] key = sceneRecall.getAppKey();
        final int akf = sceneRecall.getAkf();
        final int aid = sceneRecall.getAid();
        final int aszmic = sceneRecall.getAszmic();
        final int opCode = sceneRecall.getOpCode();
        final byte[] parameters = sceneRecall.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending Scene Store acknowledged");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
