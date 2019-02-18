package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling SceneDeleteUnacknowledgedState messages.
 */
@SuppressWarnings("WeakerAccess")
class SceneDeleteUnacknowledgedState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = SceneDeleteUnacknowledgedState.class.getSimpleName();

    /**
     * Constructs {@link SceneDeleteUnacknowledgedState}
     *
     * @param context                   Context of the application
     * @param src                       Source address
     * @param dst                       Destination address to which the message must be sent to
     * @param sceneDeleteUnacknowledged Wrapper class {@link SceneDeleteUnacknowledged} containing the opcode and parameters for {@link SceneDeleteUnacknowledged} message
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    @Deprecated
    SceneDeleteUnacknowledgedState(@NonNull final Context context,
                                   @NonNull final byte[] src,
                                   @NonNull final byte[] dst,
                                   @NonNull final SceneDeleteUnacknowledged sceneDeleteUnacknowledged,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), sceneDeleteUnacknowledged, meshTransport, callbacks);
    }

    /**
     * Constructs {@link SceneDeleteUnacknowledgedState}
     *
     * @param context                   Context of the application
     * @param src                       Source address
     * @param dst                       Destination address to which the message must be sent to
     * @param sceneDeleteUnacknowledged Wrapper class {@link SceneDeleteUnacknowledged} containing the opcode and parameters for {@link SceneDeleteUnacknowledged} message
     * @param callbacks                 {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    SceneDeleteUnacknowledgedState(@NonNull final Context context,
                                   final int src,
                                   final int dst,
                                   @NonNull final SceneDeleteUnacknowledged sceneDeleteUnacknowledged,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, sceneDeleteUnacknowledged, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.SCENE_DELETE_UNACKNOWLEDGED_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final SceneDeleteUnacknowledged sceneDeleteUnacknowledged = (SceneDeleteUnacknowledged) mMeshMessage;
        final byte[] key = sceneDeleteUnacknowledged.getAppKey();
        final int akf = sceneDeleteUnacknowledged.getAkf();
        final int aid = sceneDeleteUnacknowledged.getAid();
        final int aszmic = sceneDeleteUnacknowledged.getAszmic();
        final int opCode = sceneDeleteUnacknowledged.getOpCode();
        final byte[] parameters = sceneDeleteUnacknowledged.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending Scene Delete acknowledged");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null) {
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
            }
        }
    }
}
