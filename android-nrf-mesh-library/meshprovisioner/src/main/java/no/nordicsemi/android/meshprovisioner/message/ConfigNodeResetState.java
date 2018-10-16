package no.nordicsemi.android.meshprovisioner.message;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.message.type.AccessMessage;
import no.nordicsemi.android.meshprovisioner.message.type.ControlMessage;
import no.nordicsemi.android.meshprovisioner.message.type.Message;

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

    @Override
    public boolean parseMeshPdu(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof AccessMessage) {
            } else {
                parseControlMessage((ControlMessage) message, mPayloads.size());
            }
        } else {
            Log.v(TAG, "Message reassembly may not be complete yet");
        }
        return false;
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
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config node reset");
        super.executeSend();

        if (!mPayloads.isEmpty()) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshNodeResetSent(mNode);
        }
    }
}
