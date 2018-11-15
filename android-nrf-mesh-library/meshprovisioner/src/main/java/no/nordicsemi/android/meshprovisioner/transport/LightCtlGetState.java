package no.nordicsemi.android.meshprovisioner.transport;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling LightCtlGetState messages.
 */
class LightCtlGetState extends GenericMessageState {

    private static final String TAG = LightCtlGetState.class.getSimpleName();

    /**
     * Constructs LightCtlGetState
     *
     * @param context           Context of the application
     * @param dstAddress        Destination address to which the message must be sent to
     * @param lightCtlGet Wrapper class {@link LightCtlGet} containing the opcode and parameters for {@link LightCtlGet} message
     * @param callbacks         {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    LightCtlGetState(@NonNull final Context context,
                     @NonNull final byte[] dstAddress,
                     @NonNull final LightCtlGet lightCtlGet,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, lightCtlGet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.LIGHT_CTL_GET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final LightCtlGet lightCtlGet = (LightCtlGet) mMeshMessage;
        final byte[] key = lightCtlGet.getAppKey();
        final int akf = lightCtlGet.getAkf();
        final int aid = lightCtlGet.getAid();
        final int aszmic = lightCtlGet.getAszmic();
        final int opCode = lightCtlGet.getOpCode();
        final byte[] parameters = lightCtlGet.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, mDstAddress, key, akf, aid, aszmic, opCode, parameters);
        lightCtlGet.setMessage(message);
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending Light Ctl get");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
        }
    }
}
