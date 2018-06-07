package no.nordicsemi.android.meshprovisioner.configuration;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshConfigurationStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * This class handles binding application keys to a specific model where the mode could be,
 * a 16-bit Bluetooth SigModel or a 32-bit Vendor Model
 */
public final class ConfigModelAppBind extends ConfigMessage {

    private static final String TAG = ConfigModelAppBind.class.getSimpleName();

    private static final int SIG_MODEL_APP_KEY_BIND_PARAMS_LENGTH = 6;
    private static final int VENDOR_MODEL_APP_KEY_BIND_PARAMS_LENGTH = 8;

    private final int akf = 0;
    private final int aid = 0;

    private final int mAszmic;
    private final byte[] mElementAddress;
    private final int mModelIdentifier;
    private final int mAppKeyIndex;

    public ConfigModelAppBind(final Context context,
                              final ProvisionedMeshNode meshNode,
                              final int aszmic,
                              final byte[] elementAddress, final int modelIdentifier,
                              final int appKeyIndex) {
        super(context, meshNode);
        this.mAszmic = aszmic == 1 ? 1 : 0;
        this.mElementAddress = elementAddress;
        this.mModelIdentifier = modelIdentifier;
        this.mAppKeyIndex = appKeyIndex;
        createAccessMessage();
    }

    public void setTransportCallbacks(final InternalTransportCallbacks callbacks) {
        this.mInternalTransportCallbacks = callbacks;
    }

    public void setConfigurationStatusCallbacks(final MeshConfigurationStatusCallbacks callbacks) {
        this.mConfigStatusCallbacks = callbacks;
    }

    @Override
    public ConfigMessageState getState() {
        return ConfigMessageState.CONFIG_MODEL_APP_BIND;
    }

    /**
     * Creates the access message to be sent to the node
     */
    private void createAccessMessage() {
        ByteBuffer paramsBuffer;
        byte[] parameters;
        final byte[] applicationKeyIndex = MeshParserUtils.addKeyIndexPadding(mAppKeyIndex);
        //We check if the model identifier value is within the range of a 16-bit value here. If it is then it is a sigmodel
        if (mModelIdentifier >= Short.MIN_VALUE && mModelIdentifier <= Short.MAX_VALUE) {
            paramsBuffer = ByteBuffer.allocate(SIG_MODEL_APP_KEY_BIND_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.put(mElementAddress[1]);
            paramsBuffer.put(mElementAddress[0]);
            paramsBuffer.put(applicationKeyIndex[1]);
            paramsBuffer.put(applicationKeyIndex[0]);
            paramsBuffer.putShort((short) mModelIdentifier);
            parameters = paramsBuffer.array();
        } else {
            paramsBuffer = ByteBuffer.allocate(VENDOR_MODEL_APP_KEY_BIND_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.put(mElementAddress[1]);
            paramsBuffer.put(mElementAddress[0]);
            paramsBuffer.put(applicationKeyIndex[1]);
            paramsBuffer.put(applicationKeyIndex[0]);
            final byte[] modelIdentifier = new byte[]{(byte) ((mModelIdentifier >> 24) & 0xFF), (byte) ((mModelIdentifier >> 16) & 0xFF), (byte) ((mModelIdentifier >> 8) & 0xFF), (byte) (mModelIdentifier & 0xFF)};
            paramsBuffer.put(modelIdentifier[1]);
            paramsBuffer.put(modelIdentifier[0]);
            paramsBuffer.put(modelIdentifier[3]);
            paramsBuffer.put(modelIdentifier[2]);
            parameters = paramsBuffer.array();
        }

        final byte[] key = mProvisionedMeshNode.getDeviceKey();
        final AccessMessage accessMessage = mMeshTransport.createMeshMessage(mProvisionedMeshNode, mSrc, key, akf, aid, mAszmic, ConfigMessageOpCodes.CONFIG_MODEL_APP_BIND, parameters);
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
                mConfigStatusCallbacks.onAppKeyBindSent(mProvisionedMeshNode);
        }
    }

    public void parseData(final byte[] pdu) {
        parseMessage(pdu);
    }

    private void parseMessage(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof AccessMessage) {
                final byte[] accessPayload = ((AccessMessage) message).getAccessPdu();
                Log.v(TAG, "Unexpected access message received: " + MeshParserUtils.bytesToHex(accessPayload, false));
            } else {
                final ControlMessage controlMessage = (ControlMessage) message;
                Log.v(TAG, "Control message received: " + MeshParserUtils.bytesToHex(pdu, false));
                parseControlMessage(controlMessage);
            }
        } else {
            Log.v(TAG, "Message reassembly may not be complete yet");
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, message.getNetworkPdu().get(0));
        mConfigStatusCallbacks.onBlockAcknowledgementSent(mProvisionedMeshNode);
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
