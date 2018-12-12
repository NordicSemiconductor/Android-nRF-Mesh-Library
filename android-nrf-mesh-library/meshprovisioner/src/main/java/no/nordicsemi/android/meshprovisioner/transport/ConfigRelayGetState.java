package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State that handles the ConfigRelayGet message
 */
@SuppressWarnings("unused")
public final class ConfigRelayGetState extends ConfigMessageState {

    private static final String TAG = ConfigRelayGetState.class.getSimpleName();

    /**
     * Constructs the state for {@link ConfigRelayGet} message
     *
     * @param context        context
     * @param configRelayGet {@link ConfigRelayGet} message
     * @param meshTransport  {@link MeshTransport}
     * @param callbacks      {@link InternalMeshMsgHandlerCallbacks} Internal mesh handler callbacks
     */
    ConfigRelayGetState(@NonNull final Context context,
                        @NonNull final ConfigRelayGet configRelayGet,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configRelayGet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_NETWORK_TRANSMIT_GET_STATE;
    }

    private void createAccessMessage() {
        final byte[] key = mNode.getDeviceKey();
        final ConfigRelayGet configRelayGet = (ConfigRelayGet) mMeshMessage;
        final int akf = configRelayGet.getAkf();
        final int aid = configRelayGet.getAid();
        final int aszmic = configRelayGet.getAszmic();
        final int opCode = configRelayGet.getOpCode();
        final byte[] parameters = configRelayGet.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, key, akf, aid, aszmic, opCode, parameters);
        configRelayGet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config relay get");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
        }
    }
}
