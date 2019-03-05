package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling SceneRegisterGetState messages.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
class SceneRegisterGetState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = SceneRegisterGetState.class.getSimpleName();

    /**
     * Constructs {@link SceneRegisterGetState}
     *
     * @param context          Context of the application
     * @param src              Source address
     * @param dst              Destination address to which the message must be sent to
     * @param sceneRegisterGet Wrapper class {@link SceneRegisterGet} containing the opcode and parameters for {@link SceneRegisterGet} message
     * @param callbacks        {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    @Deprecated
    SceneRegisterGetState(@NonNull final Context context,
                          @NonNull final byte[] src,
                          @NonNull final byte[] dst,
                          @NonNull final SceneRegisterGet sceneRegisterGet,
                          @NonNull final MeshTransport meshTransport,
                          @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), sceneRegisterGet, meshTransport, callbacks);
    }

    /**
     * Constructs {@link SceneRegisterGetState}
     *
     * @param context          Context of the application
     * @param src              Source address
     * @param dst              Destination address to which the message must be sent to
     * @param sceneRegisterGet Wrapper class {@link SceneRegisterGet} containing the opcode and parameters for {@link SceneRegisterGet} message
     * @param callbacks        {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    SceneRegisterGetState(@NonNull final Context context,
                          final int src,
                          final int dst,
                          @NonNull final SceneRegisterGet sceneRegisterGet,
                          @NonNull final MeshTransport meshTransport,
                          @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, sceneRegisterGet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.SCENE_REGISTER_GET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final SceneRegisterGet sceneRegisterGet = (SceneRegisterGet) mMeshMessage;
        final byte[] key = sceneRegisterGet.getAppKey();
        final int akf = sceneRegisterGet.getAkf();
        final int aid = sceneRegisterGet.getAid();
        final int aszmic = sceneRegisterGet.getAszmic();
        final int opCode = sceneRegisterGet.getOpCode();
        final byte[] parameters = sceneRegisterGet.getParameters();
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
