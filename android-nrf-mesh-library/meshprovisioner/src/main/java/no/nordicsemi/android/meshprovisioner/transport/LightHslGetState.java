package no.nordicsemi.android.meshprovisioner.transport;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling LightHslGetState messages.
 */
class LightHslGetState extends GenericMessageState {

    private static final String TAG = LightHslGetState.class.getSimpleName();

    /**
     * Constructs LightHslGetState
     *
     * @param context           Context of the application
     * @param dstAddress        Destination address to which the message must be sent to
     * @param lightHslGet       Wrapper class {@link LightHslGet} containing the opcode and parameters for {@link LightHslGet} message
     * @param callbacks         {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    LightHslGetState(@NonNull final Context context,
                     @NonNull final byte[] dstAddress,
                     @NonNull final LightHslGet lightHslGet,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, lightHslGet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.LIGHT_HSL_GET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final LightHslGet lightHslGet = (LightHslGet) mMeshMessage;
        final byte[] key = lightHslGet.getAppKey();
        final int akf = lightHslGet.getAkf();
        final int aid = lightHslGet.getAid();
        final int aszmic = lightHslGet.getAszmic();
        final int opCode = lightHslGet.getOpCode();
        final byte[] parameters = lightHslGet.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, mDstAddress, key, akf, aid, aszmic, opCode, parameters);
        lightHslGet.setMessage(message);
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending Light Hsl get");
        super.executeSend();
        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
        }
    }
}
