package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State that handles the ConfigRelayGet message
 */
@SuppressWarnings("unused")
public final class ConfigRelaySetState extends ConfigMessageState {

    private static final String TAG = ConfigRelaySetState.class.getSimpleName();

    /**
     * Constructs the state for {@link ConfigRelayGet} message
     *
     * @param context        context
     * @param configRelaySet {@link ConfigRelaySet} message
     * @param meshTransport  {@link MeshTransport}
     * @param callbacks      {@link InternalMeshMsgHandlerCallbacks} Internal mesh handler callbacks
     */
    ConfigRelaySetState(@NonNull final Context context,
                        @NonNull final ConfigRelaySet configRelaySet,
                        @NonNull final MeshTransport meshTransport,
                        @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, configRelaySet, meshTransport, callbacks);
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_RELAY_SET_STATE;
    }

    private void createAccessMessage() {
        final byte[] key = mNode.getDeviceKey();
        final ConfigRelaySet configRelaySet = (ConfigRelaySet) mMeshMessage;
        final int akf = configRelaySet.getAkf();
        final int aid = configRelaySet.getAid();
        final int aszmic = configRelaySet.getAszmic();
        final int opCode = configRelaySet.getOpCode();
        final byte[] parameters = configRelaySet.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, key, akf, aid, aszmic, opCode, parameters);
        configRelaySet.setMessage(message);
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config relay set");
        super.executeSend();

        if (message.getNetworkPdu().size() > 0) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshMessageSent(mMeshMessage);
        }
    }
}
