package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * State class for handling ConfigNodeReset messages.
 */
class ConfigNodeResetState extends ConfigMessageState {

    private static final String TAG = ConfigNodeResetState.class.getSimpleName();


    /**
     * Constructs {@link ConfigNodeResetState}
     *
     * @param context         Context of the application
     * @param configNodeReset Wrapper class {@link ConfigNodeReset} containing the opcode and parameters for {@link ConfigNodeReset} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException for any illegal arguments provided.
     */
    ConfigNodeResetState(@NonNull final Context context,
                                @NonNull final ConfigNodeReset configNodeReset,
                                @NonNull final MeshTransport meshTransport,
                                @NonNull final InternalMeshMsgHandlerCallbacks callbacks)  {
        super(context, configNodeReset, meshTransport, callbacks);
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

        final ConfigNodeReset configNodeReset = (ConfigNodeReset) mMeshMessage;
        final int akf = configNodeReset.getAkf();
        final int aid = configNodeReset.getAid();
        final int aszmic = configNodeReset.getAszmic();
        final int opCode = configNodeReset.getOpCode();
        final byte[] parameters = configNodeReset.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, key, akf, aid, aszmic, opCode, parameters);
        configNodeReset.setMessage(message);
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
