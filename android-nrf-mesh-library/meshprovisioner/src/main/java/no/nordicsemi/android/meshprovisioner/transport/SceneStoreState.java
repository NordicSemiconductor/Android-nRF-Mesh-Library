package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling SceneStoreState messages.
 */
class SceneStoreState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = SceneStoreState.class.getSimpleName();

    /**
     * Constructs {@link SceneStoreState}
     *
     * @param context         Context of the application
     * @param dstAddress      Destination address to which the message must be sent to
     * @param sceneStore      Wrapper class {@link SceneStore} containing the opcode and parameters for {@link SceneStore} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    SceneStoreState(@NonNull final Context context,
                    @NonNull final byte[] dstAddress,
                    @NonNull final SceneStore sceneStore,
                    @NonNull final MeshTransport meshTransport,
                    @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, sceneStore, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.SCENE_STORE_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final SceneStore sceneStore = (SceneStore) mMeshMessage;
        final byte[] key = sceneStore.getAppKey();
        final int akf = sceneStore.getAkf();
        final int aid = sceneStore.getAid();
        final int aszmic = sceneStore.getAszmic();
        final int opCode = sceneStore.getOpCode();
        final byte[] parameters = sceneStore.getParameters();
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
