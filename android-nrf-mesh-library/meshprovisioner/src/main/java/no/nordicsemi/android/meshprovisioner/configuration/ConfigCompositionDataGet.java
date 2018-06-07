package no.nordicsemi.android.meshprovisioner.configuration;

import android.content.Context;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshConfigurationStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;

public class ConfigCompositionDataGet extends ConfigMessage {

    private static final String TAG = ConfigCompositionDataStatus.class.getSimpleName();
    private int mAszmic;
    private int akf = 0;
    private int aid = 0;

    public ConfigCompositionDataGet(final Context context, final ProvisionedMeshNode provisionedMeshNode, final int aszmic, final InternalTransportCallbacks internalTransportCallbacks,
                                    final MeshConfigurationStatusCallbacks meshConfigurationStatusCallbacks) {
        super(context, provisionedMeshNode);
        this.mAszmic = aszmic == 1 ? 1 : 0;
        this.mInternalTransportCallbacks = internalTransportCallbacks;
        this.mConfigStatusCallbacks = meshConfigurationStatusCallbacks;
        createAccessMessage();
    }

    @Override
    public ConfigMessageState getState() {
        return ConfigMessageState.COMPOSITION_DATA_GET;
    }


    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        final AccessMessage accessMessage = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, mProvisionedMeshNode.getDeviceKey(),
                akf, aid, mAszmic, ConfigMessageOpCodes.CONFIG_COMPOSITION_DATA_GET,
                new byte[]{(byte) 0xFF});
        mPayloads.putAll(accessMessage.getNetworkPdu());

    }

    /**
     * Starts sending the mesh pdu
     */
    public void executeSend() {
        if (!mPayloads.isEmpty()) {
            for (int i = 0; i < mPayloads.size(); i++) {
                if (mInternalTransportCallbacks != null) {
                    mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, mPayloads.get(i));
                }
            }

            if (mConfigStatusCallbacks != null)
                mConfigStatusCallbacks.onGetCompositionDataSent(mProvisionedMeshNode);
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        //We don't send acks here
    }

    /**
     * Returns the source address of the message i.e. where it originated from
     *
     * @return source address
     */
    public byte[] getSrc() {
        return mSrc;
    }
}
