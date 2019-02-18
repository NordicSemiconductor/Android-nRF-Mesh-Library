package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;


/**
 * State class for handling GenericLevelGet messages.
 */
@SuppressWarnings("unused")
class GenericOnOffGetState extends GenericMessageState {

    private static final String TAG = GenericOnOffGetState.class.getSimpleName();

    /**
     * Constructs GenericLevelGetState
     *
     * @param context         Context of the application
     * @param src             Source address
     * @param dst             Destination address to which the message must be sent to
     * @param genericOnOffGet Wrapper class {@link GenericOnOffGet} containing the opcode and parameters for {@link GenericOnOffGet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    @Deprecated
    GenericOnOffGetState(@NonNull final Context context,
                         @NonNull final byte[] src,
                         @NonNull final byte[] dst,
                         @NonNull final GenericOnOffGet genericOnOffGet,
                         @NonNull final MeshTransport meshTransport,
                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), genericOnOffGet, meshTransport, callbacks);        createAccessMessage();
    }

    /**
     * Constructs GenericLevelGetState
     *
     * @param context         Context of the application
     * @param src             Source address
     * @param dst             Destination address to which the message must be sent to
     * @param genericOnOffGet Wrapper class {@link GenericOnOffGet} containing the opcode and parameters for {@link GenericOnOffGet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    GenericOnOffGetState(@NonNull final Context context,
                         final int src,
                         final int dst,
                         @NonNull final GenericOnOffGet genericOnOffGet,
                         @NonNull final MeshTransport meshTransport,
                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, genericOnOffGet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_ON_OFF_GET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final GenericOnOffGet genericOnOffGet = (GenericOnOffGet) mMeshMessage;
        final byte[] key = genericOnOffGet.getAppKey();
        final int akf = genericOnOffGet.getAkf();
        final int aid = genericOnOffGet.getAid();
        final int aszmic = genericOnOffGet.getAszmic();
        final int opCode = genericOnOffGet.getOpCode();
        final byte[] parameters = genericOnOffGet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending Generic OnOff get");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
