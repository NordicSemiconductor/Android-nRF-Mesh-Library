package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling GenericOnOffSetState messages.
 */
@SuppressWarnings("WeakerAccess")
class GenericOnOffSetUnacknowledgedState extends GenericMessageState {

    private static final String TAG = GenericOnOffSetUnacknowledgedState.class.getSimpleName();

    /**
     * Constructs {@link GenericOnOffSetState}
     *
     * @param context                       Context of the application
     * @param src                           Source address
     * @param dst                           Destination address to which the message must be sent to
     * @param genericOnOffSetUnacknowledged Wrapper class {@link GenericOnOffSetUnacknowledged} containing the opcode and parameters for {@link GenericOnOffSetUnacknowledged} message
     * @param callbacks                     {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    @Deprecated
    GenericOnOffSetUnacknowledgedState(@NonNull final Context context,
                                       @NonNull final byte[] src,
                                       @NonNull final byte[] dst,
                                       @NonNull final GenericOnOffSetUnacknowledged genericOnOffSetUnacknowledged,
                                       @NonNull final MeshTransport meshTransport,
                                       @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        this(context, MeshParserUtils.bytesToInt(src), MeshParserUtils.bytesToInt(dst), genericOnOffSetUnacknowledged, meshTransport, callbacks);
    }

    /**
     * Constructs {@link GenericOnOffSetState}
     *
     * @param context                       Context of the application
     * @param src                           Source address
     * @param dst                           Destination address to which the message must be sent to
     * @param genericOnOffSetUnacknowledged Wrapper class {@link GenericOnOffSetUnacknowledged} containing the opcode and parameters for {@link GenericOnOffSetUnacknowledged} message
     * @param callbacks                     {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    GenericOnOffSetUnacknowledgedState(@NonNull final Context context,
                                       final int src,
                                       final int dst,
                                       @NonNull final GenericOnOffSetUnacknowledged genericOnOffSetUnacknowledged,
                                       @NonNull final MeshTransport meshTransport,
                                       @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, src, dst, genericOnOffSetUnacknowledged, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_ON_OFF_SET_UNACKNOWLEDGED_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final GenericOnOffSetUnacknowledged genericOnOffSetUnacknowledged = (GenericOnOffSetUnacknowledged) mMeshMessage;
        final byte[] key = genericOnOffSetUnacknowledged.getAppKey();
        final int akf = genericOnOffSetUnacknowledged.getAkf();
        final int aid = genericOnOffSetUnacknowledged.getAid();
        final int aszmic = genericOnOffSetUnacknowledged.getAszmic();
        final int opCode = genericOnOffSetUnacknowledged.getOpCode();
        final byte[] parameters = genericOnOffSetUnacknowledged.getParameters();
        message = mMeshTransport.createMeshMessage(mSrc, mDst, key, akf, aid, aszmic, opCode, parameters);
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending Generic OnOff set unacknowledged: ");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null) {
                mMeshStatusCallbacks.onMeshMessageSent(mDst, mMeshMessage);
            }
        }
    }
}
