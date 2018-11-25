package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling SceneStoreState messages.
 */
class SceneRecallState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = SceneRecallState.class.getSimpleName();

    /**
     * Constructs {@link SceneRecallState}
     *
     * @param context         Context of the application
     * @param dstAddress      Destination address to which the message must be sent to
     * @param sceneRecall Wrapper class {@link SceneStore} containing the opcode and parameters for {@link SceneStore} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    SceneRecallState(@NonNull final Context context,
                     @NonNull final byte[] dstAddress,
                     @NonNull final SceneRecall sceneRecall,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, sceneRecall, meshTransport, callbacks);
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
        message = mMeshTransport.createMeshMessage(mNode, mSrc, mDstAddress, key, akf, aid, aszmic, opCode, parameters);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending Scene Store acknowledged");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
        }
    }
}
