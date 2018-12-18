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
        return MessageState.CONFIG_PROXY_SET_STATE;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final byte[] key = mNode.getDeviceKey();

        final ConfigProxySet configProxySet = (ConfigProxySet) mMeshMessage;
        final int akf = configProxySet.getAkf();
        final int aid = configProxySet.getAid();
        final int aszmic = configProxySet.getAszmic();
        final int opCode = configProxySet.getOpCode();
        final byte[] parameters = configProxySet.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, key, akf, aid, aszmic, opCode, parameters);
        configProxySet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config proxy set");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
        }
    }
}
