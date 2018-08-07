package no.nordicsemi.android.meshprovisioner.configuration;


import android.content.Context;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshConfigurationStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

public class GenericOnOffGet extends ConfigMessage {


    private static final String TAG = GenericOnOffGet.class.getSimpleName();

    private final int mAszmic;
    private final byte[] dstAddress;

    public GenericOnOffGet(final Context context, final ProvisionedMeshNode provisionedMeshNode, final MeshModel model, final boolean aszmic,
                           final byte[] dstAddress, final int appKeyIndex) {
        super(context, provisionedMeshNode);
        this.mAszmic = aszmic ? 1 : 0;
        this.dstAddress = dstAddress;
        this.mMeshModel = model;
        this.mAppKeyIndex = appKeyIndex;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_ON_OFF_GET;
    }

    @Override
    protected void parseMessage(final byte[] pdu) {
        //Do nothing here
    }

    public void setTransportCallbacks(final InternalTransportCallbacks callbacks) {
        this.mInternalTransportCallbacks = callbacks;
    }

    public void setConfigurationStatusCallbacks(final MeshConfigurationStatusCallbacks callbacks) {
        this.mConfigStatusCallbacks = callbacks;
    }
    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final byte[] key = MeshParserUtils.toByteArray(mMeshModel.getBoundAppkeys().get(mAppKeyIndex));
        int akf = 1;
        int aid = SecureUtils.calculateK4(key);
        accessMessage = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, dstAddress, key, akf, aid, mAszmic,
                ApplicationMessageOpCodes.GENERIC_ON_OFF_GET, null);
        mPayloads.putAll(accessMessage.getNetworkPdu());
    }

    /**
     * Starts sending the mesh pdu
     */
    public void executeSend() {
        if (!mPayloads.isEmpty()) {
            Log.v(TAG, "Sending Generic OnOff get");
            for (int i = 0; i < mPayloads.size(); i++) {
                if (mInternalTransportCallbacks != null) {
                    mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, mPayloads.get(i));
                }
            }

            if (mConfigStatusCallbacks != null)
                mConfigStatusCallbacks.onAppKeyAddSent(mProvisionedMeshNode);
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, message.getNetworkPdu().get(0));
        mConfigStatusCallbacks.onBlockAcknowledgementSent(mProvisionedMeshNode);
    }
}
