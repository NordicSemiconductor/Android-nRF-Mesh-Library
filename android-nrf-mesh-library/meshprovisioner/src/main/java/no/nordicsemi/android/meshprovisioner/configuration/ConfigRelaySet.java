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
 * The Relay state indicates support for the Relay feature.
 * If the Relay feature is supported, then this also indicates and controls whether the Relay feature is enabled or disabled.
 */
public class ConfigRelaySet extends ConfigMessage {


    private static final String TAG = ConfigRelaySet.class.getSimpleName();

    private final int mAszmic;
    private int relay,relayTransmit;

    /**
     *
     * @param context
     * @param provisionedMeshNode
     * @param aszmic
     * @param mInternalTransportCallbacks
     * @param meshConfigurationStatusCallbacks
     * @param relay 0x00 The node support Relay feature that is disabled
     *              0x01 The node supports Relay feature that is enabled
     *              0x02 Relay feature is not supported
     *              0x03–0xFF Prohibited
     * @param relayRetransmitCount 0~7
     * @param relayRetransmitIntervalSteps 0~31
     */
    public ConfigRelaySet(final Context context, final ProvisionedMeshNode provisionedMeshNode, final boolean aszmic,
                          final InternalTransportCallbacks mInternalTransportCallbacks,
                          final MeshConfigurationStatusCallbacks meshConfigurationStatusCallbacks, int relay, int relayRetransmitCount,int relayRetransmitIntervalSteps)  {
        super(context, provisionedMeshNode);
        this.mAszmic = aszmic ? 1 : 0;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
        this.mConfigStatusCallbacks = meshConfigurationStatusCallbacks;
        this.relay=relay;
        this.relayTransmit=relayRetransmitIntervalSteps << 3 | relayRetransmitCount;;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_RELAY_SET;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final byte[] key = mProvisionedMeshNode.getDeviceKey();
        int akf = 0;
        int aid = 0;
        byte[] parameters=new byte[2];
        parameters[0]= (byte) relay;
        parameters[1]= (byte) (byte) (relayTransmit);

        final AccessMessage accessMessage = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, key, akf, aid, mAszmic,
                ConfigMessageOpCodes.CONFIG_RELAY_SET, parameters);
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
