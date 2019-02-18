package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling SceneStoreState messages.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class SceneRecallUnacknowledgedState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = SceneRecallUnacknowledgedState.class.getSimpleName();

    /**
     * Constructs {@link SceneRecallUnacknowledgedState}
     *
     * @param context                   Context of the application
     * @param src                       Source address
     * @param dst                       Destination address to which the message must be sent to
     * @param sceneRecallUnacknowledged Wrapper class {@link SceneStore} containing the opcode and parameters for {@link SceneStore} message
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     * @deprecated in favour of {@link #SceneRecallUnacknowledgedState(Context, int, int, SceneRecallUnacknowledged, MeshTransport, InternalMeshMsgHandlerCallbacks)}
     */
    @Deprecated
    SceneRecallUnacknowledgedState(@NonNull final Context context,
                                   @NonNull final byte[] src,
                                   @NonNull final byte[] dst,
                                   @NonNull final SceneRecallUnacknowledged sceneRecallUnacknowledged,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), sceneRecallUnacknowledged, meshTransport, callbacks);
    }

    /**
     * Constructs {@link SceneRecallUnacknowledgedState}
     *
     * @param context                   Context of the application
     * @param src                       Source address
     * @param dst                       Destination address to which the message must be sent to
     * @param sceneRecallUnacknowledged Wrapper class {@link SceneStore} containing the opcode and parameters for {@link SceneStore} message
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    SceneRecallUnacknowledgedState(@NonNull final Context context,
                                   final int src,
                                   final int dst,
                                   @NonNull final SceneRecallUnacknowledged sceneRecallUnacknowledged,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, sceneRecallUnacknowledged, meshTransport, callbacks);
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
