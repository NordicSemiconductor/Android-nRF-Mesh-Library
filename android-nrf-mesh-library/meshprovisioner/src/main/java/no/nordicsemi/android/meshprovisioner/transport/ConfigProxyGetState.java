package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling {@link ConfigProxyGet} messages.
 */
class ConfigProxyGetState extends ConfigMessageState {

    private static final String TAG = ConfigProxyGetState.class.getSimpleName();


    /**
     * Constructs {@link ConfigProxyGetState}
     *
     * @param context         Context of the application
     * @param configProxyGet Wrapper class {@link ConfigProxyGet} containing the opcode and parameters for {@link ConfigProxyGet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    ConfigProxyGetState(@NonNull final Context context,
                        @NonNull final ConfigProxyGet configProxyGet,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks)  {
        super(context, configProxyGet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_NODE_RESET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final byte[] key = mNode.getDeviceKey();

        final ConfigProxyGet configProxyGet = (ConfigProxyGet) mMeshMessage;
        final int akf = configProxyGet.getAkf();
        final int aid = configProxyGet.getAid();
        final int aszmic = configProxyGet.getAszmic();
        final int opCode = configProxyGet.getOpCode();
        final byte[] parameters = configProxyGet.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, key, akf, aid, aszmic, opCode, parameters);
        configProxyGet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config node reset");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
        }
    }
}
