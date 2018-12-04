package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling SceneStoreState messages.
 */
class SceneRecallUnacknowledgedState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = SceneRecallUnacknowledgedState.class.getSimpleName();

    /**
     * Constructs {@link SceneRecallUnacknowledgedState}
     *
     * @param context         Context of the application
     * @param dstAddress      Destination address to which the message must be sent to
     * @param sceneRecallUnacknowledged Wrapper class {@link SceneStore} containing the opcode and parameters for {@link SceneStore} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    SceneRecallUnacknowledgedState(@NonNull final Context context,
                     @NonNull final byte[] dstAddress,
                     @NonNull final SceneRecallUnacknowledged sceneRecallUnacknowledged,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, sceneRecallUnacknowledged, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.SCENE_RECALL_UNACKNOWLEDGED_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final SceneRecallUnacknowledged sceneRecallUnacknowledged = (SceneRecallUnacknowledged) mMeshMessage;
        final byte[] key = sceneRecallUnacknowledged.getAppKey();
        final int akf = sceneRecallUnacknowledged.getAkf();
        final int aid = sceneRecallUnacknowledged.getAid();
        final int aszmic = sceneRecallUnacknowledged.getAszmic();
        final int opCode = sceneRecallUnacknowledged.getOpCode();
        final byte[] parameters = sceneRecallUnacknowledged.getParameters();
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
