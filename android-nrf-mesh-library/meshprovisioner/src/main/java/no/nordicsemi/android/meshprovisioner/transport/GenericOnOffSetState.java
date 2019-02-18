package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling GenericOnOffSetState messages.
 */
@SuppressWarnings("WeakerAccess")
class GenericOnOffSetState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = GenericOnOffSetState.class.getSimpleName();

    /**
     * Constructs {@link GenericOnOffSetState}
     *
     * @param context         Context of the application
     * @param src             Source address
     * @param dst             Destination address to which the message must be sent to
     * @param genericOnOffSet Wrapper class {@link GenericOnOffSet} containing the opcode and parameters for {@link GenericOnOffSet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     * @deprecated in favour of {@link #GenericOnOffSetState(Context, int, int, GenericOnOffSet, MeshTransport, InternalMeshMsgHandlerCallbacks)}
     */
    @Deprecated
    GenericOnOffSetState(@NonNull final Context context,
                         @NonNull final byte[] src,
                         @NonNull final byte[] dst,
                         @NonNull final GenericOnOffSet genericOnOffSet,
                         @NonNull final MeshTransport meshTransport,
                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), genericOnOffSet, meshTransport, callbacks);
    }

    /**
     * Constructs {@link GenericOnOffSetState}
     *
     * @param context         Context of the application
     * @param src             Source address
     * @param dst             Destination address to which the message must be sent to
     * @param genericOnOffSet Wrapper class {@link GenericOnOffSet} containing the opcode and parameters for {@link GenericOnOffSet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    GenericOnOffSetState(@NonNull final Context context,
                         final int src,
                         final int dst,
                         @NonNull final GenericOnOffSet genericOnOffSet,
                         @NonNull final MeshTransport meshTransport,
                         @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, genericOnOffSet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_ON_OFF_SET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final GenericOnOffSet genericOnOffSet = (GenericOnOffSet) mMeshMessage;
        final byte[] key = genericOnOffSet.getAppKey();
        final int akf = genericOnOffSet.getAkf();
        final int aid = genericOnOffSet.getAid();
        final int aszmic = genericOnOffSet.getAszmic();
        final int opCode = genericOnOffSet.getOpCode();
        final byte[] parameters = genericOnOffSet.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending Generic OnOff set acknowledged");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
        }
    }
}
