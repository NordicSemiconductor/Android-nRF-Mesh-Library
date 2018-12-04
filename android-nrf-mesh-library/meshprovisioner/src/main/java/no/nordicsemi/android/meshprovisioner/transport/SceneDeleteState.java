package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling SceneDeleteState messages.
 */
class SceneDeleteState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = SceneDeleteState.class.getSimpleName();

    /**
     * Constructs {@link SceneDeleteState}
     *
     * @param context         Context of the application
     * @param dstAddress      Destination address to which the message must be sent to
     * @param sceneDelete Wrapper class {@link SceneDelete} containing the opcode and parameters for {@link SceneDelete} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    SceneDeleteState(@NonNull final Context context,
                     @NonNull final byte[] dstAddress,
                     @NonNull final SceneDelete sceneDelete,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, sceneDelete, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.SCENE_DELETE_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final SceneDelete sceneDelete = (SceneDelete) mMeshMessage;
        final byte[] key = sceneDelete.getAppKey();
        final int akf = sceneDelete.getAkf();
        final int aid = sceneDelete.getAid();
        final int aszmic = sceneDelete.getAszmic();
        final int opCode = sceneDelete.getOpCode();
        final byte[] parameters = sceneDelete.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, mDstAddress, key, akf, aid, aszmic, opCode, parameters);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending Scene Delete acknowledged");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
        }
    }
}
