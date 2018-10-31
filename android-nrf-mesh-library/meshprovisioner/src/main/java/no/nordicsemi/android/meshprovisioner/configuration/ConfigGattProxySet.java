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
 * The GATT Proxy state indicates if the Mesh Proxy Service
 */
public class ConfigGattProxySet extends ConfigMessage {


    private static final String TAG = ConfigGattProxySet.class.getSimpleName();

    private final int mAszmic;
    private int gattProxy;

    /**
     *
     * @param context
     * @param provisionedMeshNode
     * @param aszmic
     * @param mInternalTransportCallbacks
     * @param meshConfigurationStatusCallbacks
     * @param gattProxy 0x00 The Mesh Proxy Service is running, Proxy feature is disabled
     *               0x01 The Mesh Proxy Service is running, Proxy feature is enabled
     *               0x02 The Mesh Proxy Service is not supported, Proxy feature is not supported
     *               0x03–0xFF Prohibited
     */
    public ConfigGattProxySet(final Context context, final ProvisionedMeshNode provisionedMeshNode, final boolean aszmic,
                              final InternalTransportCallbacks mInternalTransportCallbacks,
                              final MeshConfigurationStatusCallbacks meshConfigurationStatusCallbacks, int gattProxy)  {
        super(context, provisionedMeshNode);
        this.mAszmic = aszmic ? 1 : 0;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
        this.mConfigStatusCallbacks = meshConfigurationStatusCallbacks;
        this.gattProxy=gattProxy;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.CONFIG_GATT_PROXY_SET;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final byte[] key = mProvisionedMeshNode.getDeviceKey();
        int akf = 0;
        int aid = 0;
        byte[] parameters=new byte[1];
        parameters[0]= (byte) gattProxy;
        final AccessMessage accessMessage = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, key, akf, aid, mAszmic,
                ConfigMessageOpCodes.CONFIG_GATT_PROXY_SET, parameters);
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
