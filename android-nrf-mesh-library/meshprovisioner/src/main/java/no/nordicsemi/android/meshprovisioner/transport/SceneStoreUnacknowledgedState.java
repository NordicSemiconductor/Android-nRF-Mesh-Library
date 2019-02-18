package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling SceneStoreUnacknowledgedState messages.
 */
@SuppressWarnings("WeakerAccess")
class SceneStoreUnacknowledgedState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = SceneStoreUnacknowledgedState.class.getSimpleName();

    /**
     * Constructs {@link SceneStoreUnacknowledgedState}
     *
     * @param context                  Context of the application
     * @param src                      Source address
     * @param dst                      Destination address to which the message must be sent to
     * @param sceneStoreUnacknowledged Wrapper class {@link SceneStoreUnacknowledged} containing the opcode and parameters for {@link SceneStoreUnacknowledged} message
     * @param callbacks                {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     * @deprecated in favour of {@link #SceneStoreUnacknowledgedState(Context, int, int, SceneStoreUnacknowledged, MeshTransport, InternalMeshMsgHandlerCallbacks)}
     */
    @Deprecated
    SceneStoreUnacknowledgedState(@NonNull final Context context,
                                  @NonNull final byte[] src,
                                  @NonNull final byte[] dst,
                                  @NonNull final SceneStoreUnacknowledged sceneStoreUnacknowledged,
                                  @NonNull final MeshTransport meshTransport,
                                  @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), sceneStoreUnacknowledged, meshTransport, callbacks);
    }

    /**
     * Constructs {@link SceneStoreUnacknowledgedState}
     *
     * @param context                  Context of the application
     * @param src                      Source address
     * @param dst                      Destination address to which the message must be sent to
     * @param sceneStoreUnacknowledged Wrapper class {@link SceneStoreUnacknowledged} containing the opcode and parameters for {@link SceneStoreUnacknowledged} message
     * @param callbacks                {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    SceneStoreUnacknowledgedState(@NonNull final Context context,
                                  final int src,
                                  final int dst,
                                  @NonNull final SceneStoreUnacknowledged sceneStoreUnacknowledged,
                                  @NonNull final MeshTransport meshTransport,
                                  @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, sceneStoreUnacknowledged, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.SCENE_STORE_UNACKNOWLEDGED_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final SceneStoreUnacknowledged sceneStoreUnacknowledged = (SceneStoreUnacknowledged) mMeshMessage;
        final byte[] key = sceneStoreUnacknowledged.getAppKey();
        final int akf = sceneStoreUnacknowledged.getAkf();
        final int aid = sceneStoreUnacknowledged.getAid();
        final int aszmic = sceneStoreUnacknowledged.getAszmic();
        final int opCode = sceneStoreUnacknowledged.getOpCode();
        final byte[] parameters = sceneStoreUnacknowledged.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending Scene Store acknowledged");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null) {
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
            }
        }
    }
}
