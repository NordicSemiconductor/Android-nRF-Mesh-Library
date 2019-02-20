package no.nordicsemi.android.meshprovisioner.transport;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling GenericLevelGet messages.
 */
@SuppressWarnings("WeakerAccess")
class GenericLevelGetState extends GenericMessageState {

    private static final String TAG = GenericLevelGetState.class.getSimpleName();

    /**
     * Constructs GenericLevelGetState
     *
     * @param context         Context of the application
     * @param src             Source address
     * @param dst             Destination address to which the message must be sent to
     * @param genericLevelGet Wrapper class {@link GenericLevelGet} containing the opcode and parameters for {@link GenericLevelGet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    @Deprecated
    GenericLevelGetState(@NonNull final Context context,
                         @NonNull final byte[] src,
                         @NonNull final byte[] dst,
                         @NonNull final GenericLevelGet genericLevelGet,
                         @NonNull final MeshTransport meshTransport,
                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), genericLevelGet, meshTransport, callbacks);
    }

    /**
     * Constructs GenericLevelGetState
     *
     * @param context         Context of the application
     * @param src             Source address
     * @param dst             Destination address to which the message must be sent to
     * @param genericLevelGet Wrapper class {@link GenericLevelGet} containing the opcode and parameters for {@link GenericLevelGet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    GenericLevelGetState(@NonNull final Context context,
                         final int src,
                         final int dst,
                         @NonNull final GenericLevelGet genericLevelGet,
                         @NonNull final MeshTransport meshTransport,
                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, genericLevelGet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_LEVEL_GET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final GenericLevelGet genericLevelGet = (GenericLevelGet) mMeshMessage;
        final byte[] key = genericLevelGet.getAppKey();
        final int akf = genericLevelGet.getAkf();
        final int aid = genericLevelGet.getAid();
        final int aszmic = genericLevelGet.getAszmic();
        final int opCode = genericLevelGet.getOpCode();
        final byte[] parameters = genericLevelGet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
        genericLevelGet.setMessage(message);
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending Generic Level get");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
