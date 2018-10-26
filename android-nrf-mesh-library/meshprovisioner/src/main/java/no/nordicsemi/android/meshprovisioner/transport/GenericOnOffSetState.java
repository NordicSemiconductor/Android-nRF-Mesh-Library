package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling GenericOnOffSetState messages.
 */
class GenericOnOffSetState extends GenericMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = GenericOnOffSetState.class.getSimpleName();

    /**
     * Constructs {@link GenericOnOffSetState}
     *
     * @param context         Context of the application
     * @param dstAddress      Destination address to which the message must be sent to
     * @param genericOnOffSet Wrapper class {@link GenericOnOffSet} containing the opcode and parameters for {@link GenericOnOffSet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    GenericOnOffSetState(@NonNull final Context context,
                                @NonNull final byte[] dstAddress,
                                @NonNull final GenericOnOffSet genericOnOffSet,
                                @NonNull final MeshTransport meshTransport,
                                @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, genericOnOffSet, meshTransport, callbacks);
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
        message = mMeshTransport.createMeshMessage(mNode, mSrc, mDstAddress, key, akf, aid, aszmic, opCode, parameters);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending Generic OnOff set acknowledged");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
        }
    }
}
