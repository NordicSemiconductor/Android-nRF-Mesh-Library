package no.nordicsemi.android.meshprovisioner.configuration;

import android.content.Context;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.Message;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

public class VendorModelMessage extends VendorModelMessageState {

    private static final String TAG = VendorModelMessage.class.getSimpleName();
    private static final int VENDOR_MODEL_OPCODE_LENGTH = 4;

    private final int mAszmic;
    private final byte[] dstAddress;
    private final int opCode;
    private final byte[] parameters;

    public VendorModelMessage(final Context context, final ProvisionedMeshNode provisionedMeshNode,
                              final InternalMeshMsgHandlerCallbacks callbacks,
                              final MeshModel model, final boolean aszmic,
                              final byte[] dstAddress, final int appKeyIndex,
                              final int opCode, final byte[] parameters) {
        super(context, provisionedMeshNode, callbacks);
        this.mMeshModel = model;
        this.mAszmic = aszmic ? 1 : 0;
        this.dstAddress = dstAddress;
        this.mAppKeyIndex = appKeyIndex;
        this.opCode = opCode;
        this.parameters = parameters;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return null;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final byte[] key = MeshParserUtils.toByteArray(mMeshModel.getBoundAppkeys().get(mAppKeyIndex));
        int akf = 1;
        int aid = SecureUtils.calculateK4(key);
        message = mMeshTransport.createVendorMeshMessage(mProvisionedMeshNode, (VendorModel) mMeshModel, mSrc, dstAddress, key, akf, aid, mAszmic, opCode, parameters);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending acknowledged vendor model message");
        super.executeSend();
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

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mProvisionedMeshNode);
    }
}
