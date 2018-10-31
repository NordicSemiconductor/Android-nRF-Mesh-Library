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
 * The Default TTL state determines the TTL value used when sending messages.
 * The Default TTL is applied by the access layer unless the application specifies a TTL
 */
public class ConfigTTLSet extends ConfigMessage {


    private static final String TAG = ConfigTTLSet.class.getSimpleName();

    private final int mAszmic;
    private int ttl;//0x00,0x02-0x7F

    /**
     *
     * @param context
     * @param provisionedMeshNode
     * @param aszmic
     * @param mInternalTransportCallbacks
     * @param meshConfigurationStatusCallbacks
     * @param ttl  0x00, 0x02– 0x7F The Default TTL state
     *             0x01, 0x80– 0xFF Prohibited
     */
    public ConfigTTLSet(final Context context, final ProvisionedMeshNode provisionedMeshNode, final boolean aszmic,
                        final InternalTransportCallbacks mInternalTransportCallbacks,
                        final MeshConfigurationStatusCallbacks meshConfigurationStatusCallbacks, int ttl)  {
        super(context, provisionedMeshNode);
        this.mAszmic = aszmic ? 1 : 0;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
        this.mConfigStatusCallbacks = meshConfigurationStatusCallbacks;
        this.ttl=ttl;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_DEFAULT_TTL_SET;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final byte[] key = mProvisionedMeshNode.getDeviceKey();
        int akf = 0;
        int aid = 0;
        byte[] parameters=new byte[1];
        parameters[0]= (byte) ttl;
        final AccessMessage accessMessage = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, key, akf, aid, mAszmic,
                ConfigMessageOpCodes.CONFIG_DEFAULT_TTL_SET, parameters);
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
