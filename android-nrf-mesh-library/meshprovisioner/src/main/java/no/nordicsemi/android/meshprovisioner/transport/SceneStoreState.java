package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling SceneStoreState messages.
 */
@SuppressWarnings("WeakerAccess")
class SceneStoreState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = SceneStoreState.class.getSimpleName();

    /**
     * Constructs {@link SceneStoreState}
     *
     * @param context    Context of the application
     * @param src        Source address
     * @param dst        Destination address to which the message must be sent to
     * @param sceneStore Wrapper class {@link SceneStore} containing the opcode and parameters for {@link SceneStore} message
     * @param callbacks  {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    @Deprecated
    SceneStoreState(@NonNull final Context context,
                    @NonNull final byte[] src,
                    @NonNull final byte[] dst,
                    @NonNull final SceneStore sceneStore,
                    @NonNull final MeshTransport meshTransport,
                    @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), sceneStore, meshTransport, callbacks);
    }

    /**
     * Constructs {@link SceneStoreState}
     *
     * @param context    Context of the application
     * @param src        Source address
     * @param dst        Destination address to which the message must be sent to
     * @param sceneStore Wrapper class {@link SceneStore} containing the opcode and parameters for {@link SceneStore} message
     * @param callbacks  {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    SceneStoreState(@NonNull final Context context,
                    final int src,
                    final int dst,
                    @NonNull final SceneStore sceneStore,
                    @NonNull final MeshTransport meshTransport,
                    @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, sceneStore, meshTransport, callbacks);
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
