package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling {@link ConfigProxySet} messages.
 */
class ConfigProxySetState extends ConfigMessageState {

    private static final String TAG = ConfigProxySetState.class.getSimpleName();


    /**
     * Constructs {@link ConfigProxySetState}
     *
     * @param context         Context of the application
     * @param configProxySet Wrapper class {@link ConfigProxySet} containing the opcode and parameters for {@link ConfigProxySet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    ConfigProxySetState(@NonNull final Context context,
                        @NonNull final ConfigProxySet configProxySet,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks)  {
        super(context, configProxySet, meshTransport, callbacks);
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
