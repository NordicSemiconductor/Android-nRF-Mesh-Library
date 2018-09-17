package no.nordicsemi.android.meshprovisioner.configuration;


import android.content.Context;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

public class GenericOnOffGet extends GenericMessageState {


    private static final String TAG = GenericOnOffGet.class.getSimpleName();

    private final int mAszmic;
    private final byte[] dstAddress;

    public GenericOnOffGet(final Context context,
                           final ProvisionedMeshNode provisionedMeshNode,
                           final InternalMeshMsgHandlerCallbacks callbacks,
                           final MeshModel model, final boolean aszmic,
                           final byte[] dstAddress, final int appKeyIndex) {
        super(context, provisionedMeshNode, callbacks);
        this.mAszmic = aszmic ? 1 : 0;
        this.dstAddress = dstAddress;
        this.mMeshModel = model;
        this.mAppKeyIndex = appKeyIndex;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_ON_OFF_GET_STATE;
    }

    @Override
    public boolean parseMeshPdu(final byte[] pdu) {
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
        final byte[] key = MeshParserUtils.toByteArray(mMeshModel.getBoundAppkeys().get(mAppKeyIndex));
        int akf = 1;
        int aid = SecureUtils.calculateK4(key);
        message = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, dstAddress, key, akf, aid, mAszmic,
                ApplicationMessageOpCodes.GENERIC_ON_OFF_GET, null);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending Generic OnOff get");
        super.executeSend();

        if (!mPayloads.isEmpty()) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onGenericOnOffGetSent(mProvisionedMeshNode);
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
