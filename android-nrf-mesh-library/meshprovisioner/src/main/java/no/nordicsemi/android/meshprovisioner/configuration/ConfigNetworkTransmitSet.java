package no.nordicsemi.android.meshprovisioner.configuration;


import android.content.Context;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshConfigurationStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * The Network Transmit state is a composite state that controls the number and timing of the transmissions of Network PDU originating from a node.
 * The state includes a Network Transmit Count field and a Network Transmit Interval Steps field.
 * There is a single instance of this state for the node.
 */
public class ConfigNetworkTransmitSet extends ConfigMessage {


    private static final String TAG = ConfigNetworkTransmitSet.class.getSimpleName();

    private final int mAszmic;
    private int networkTransmit;

    public ConfigNetworkTransmitSet(final Context context, final ProvisionedMeshNode provisionedMeshNode, final boolean aszmic,
                                    final InternalTransportCallbacks mInternalTransportCallbacks,
                                    final MeshConfigurationStatusCallbacks meshConfigurationStatusCallbacks, Integer networkTransmitCount,Integer networkTransmitIntervalSteps)  {
        super(context, provisionedMeshNode);
        this.mAszmic = aszmic ? 1 : 0;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
        this.mConfigStatusCallbacks = meshConfigurationStatusCallbacks;
        this.networkTransmit=networkTransmitIntervalSteps << 3 | networkTransmitCount;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_NETWORK_TRANSMIT_SET;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final byte[] key = mProvisionedMeshNode.getDeviceKey();
        int akf = 0;
        int aid = 0;
        byte[] parameters=new byte[1];
        parameters[0]= (byte) (byte)networkTransmit;

        final AccessMessage accessMessage = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, key, akf, aid, mAszmic,
                ConfigMessageOpCodes.CONFIG_NETWORK_TRANSMIT_SET, parameters);
        mPayloads.putAll(accessMessage.getNetworkPdu());
    }

    /**
     * Starts sending the mesh pdu
     */
    public void executeSend() {
        if (!mPayloads.isEmpty()) {
            for (int i = 0; i < mPayloads.size(); i++) {
                mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, mPayloads.get(i));
            }
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
