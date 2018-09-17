package no.nordicsemi.android.meshprovisioner.configuration;


import android.content.Context;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public class ConfigNodeReset extends ConfigMessageState {


    private static final String TAG = ConfigNodeReset.class.getSimpleName();

    private final int mAszmic;

    public ConfigNodeReset(final Context context, final ProvisionedMeshNode provisionedMeshNode, final boolean aszmic,
                           final InternalMeshMsgHandlerCallbacks callbacks)  {
        super(context, provisionedMeshNode, callbacks);
        this.mAszmic = aszmic ? 1 : 0;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_NODE_RESET_STATE;
    }

    @Override
    protected boolean parseMeshPdu(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof AccessMessage) {
                final byte[] accessPayload = ((AccessMessage) message).getAccessPdu();
                Log.v(TAG, "Unexpected access message received: " + MeshParserUtils.bytesToHex(accessPayload, false));
            } else {
                parseControlMessage((ControlMessage) message, mPayloads.size());
                return true;
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
        final byte[] key = mProvisionedMeshNode.getDeviceKey();
        int akf = 0;
        int aid = 0;
        message = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, key, akf, aid, mAszmic,
                ConfigMessageOpCodes.CONFIG_NODE_RESET, null);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending config node reset");
        super.executeSend();

        if (!mPayloads.isEmpty()) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onMeshNodeResetSent(mProvisionedMeshNode);
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mProvisionedMeshNode);
    }
}
