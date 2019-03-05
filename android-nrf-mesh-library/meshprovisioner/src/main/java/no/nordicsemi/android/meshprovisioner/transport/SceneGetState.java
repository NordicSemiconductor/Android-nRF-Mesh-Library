package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling SceneGetState messages.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
class SceneGetState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = SceneGetState.class.getSimpleName();

    /**
     * Constructs {@link SceneGetState}
     *
     * @param context   Context of the application
     * @param src       Source address
     * @param dst       Destination address to which the message must be sent to
     * @param sceneGet  Wrapper class {@link SceneGet} containing the opcode and parameters for {@link SceneGet} message
     * @param callbacks {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     * @deprecated in favour of {@link #SceneGetState(Context, int, int, SceneGet, MeshTransport, InternalMeshMsgHandlerCallbacks)}
     */
    @Deprecated
    SceneGetState(@NonNull final Context context,
                  @NonNull final byte[] src,
                  @NonNull final byte[] dst,
                  @NonNull final SceneGet sceneGet,
                  @NonNull final MeshTransport meshTransport,
                  @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), sceneGet, meshTransport, callbacks);
    }

    /**
     * Constructs {@link SceneGetState}
     *
     * @param context   Context of the application
     * @param src       Source address
     * @param dst       Destination address to which the message must be sent to
     * @param sceneGet  Wrapper class {@link SceneGet} containing the opcode and parameters for {@link SceneGet} message
     * @param callbacks {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    SceneGetState(@NonNull final Context context,
                  final int src,
                  final int dst,
                  @NonNull final SceneGet sceneGet,
                  @NonNull final MeshTransport meshTransport,
                  @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, sceneGet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.SCENE_GET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final SceneGet sceneGet = (SceneGet) mMeshMessage;
        final byte[] key = sceneGet.getAppKey();
        final int akf = sceneGet.getAkf();
        final int aid = sceneGet.getAid();
        final int aszmic = sceneGet.getAszmic();
        final int opCode = sceneGet.getOpCode();
        final byte[] parameters = sceneGet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending Scene Get acknowledged");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
