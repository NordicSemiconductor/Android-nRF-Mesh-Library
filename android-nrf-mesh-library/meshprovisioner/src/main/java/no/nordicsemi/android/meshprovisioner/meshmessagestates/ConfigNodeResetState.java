package no.nordicsemi.android.meshprovisioner.meshmessagestates;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.ConfigModelPublicationSet;
import no.nordicsemi.android.meshprovisioner.messages.ConfigNodeReset;
import no.nordicsemi.android.meshprovisioner.messages.ConfigNodeResetStatus;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelGet;
import no.nordicsemi.android.meshprovisioner.messagetypes.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.Message;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling ConfigNodeReset messages.
 */
public class ConfigNodeResetState extends ConfigMessageState {

    private static final String TAG = ConfigNodeResetState.class.getSimpleName();
    private final ConfigNodeReset configNodeReset;

    /**
     * Constructs {@link ConfigNodeResetState}
     *
     * @param context         Context of the application
     * @param configNodeReset Wrapper class {@link ConfigNodeReset} containing the opcode and parameters for {@link ConfigNodeReset} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException
     */
    public ConfigNodeResetState(@NonNull final Context context,
                                @NonNull final ConfigNodeReset configNodeReset,
                                @NonNull final InternalMeshMsgHandlerCallbacks callbacks)  {
        super(context, configNodeReset.getMeshNode(), callbacks);
        this.configNodeReset = configNodeReset;
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
                final ConfigNodeResetStatus configNodeResetStatus = new ConfigNodeResetStatus(mNode, (AccessMessage) message);
                //mInternalTransportCallbacks.updateMeshNode(mNode);
                mInternalTransportCallbacks.onMeshNodeReset(mNode);
                mMeshStatusCallbacks.onMeshNodeResetStatusReceived(configNodeResetStatus);
                return true;
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
        final int akf = configNodeReset.getAkf();
        final int aid = configNodeReset.getAid();
        final int aszmic = configNodeReset.getAszmic();
        final int opCode = configNodeReset.getOpCode();
        final byte[] parameters = configNodeReset.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, key, akf, aid, aszmic, opCode, parameters);
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

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mNode, message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mNode);
    }
}
