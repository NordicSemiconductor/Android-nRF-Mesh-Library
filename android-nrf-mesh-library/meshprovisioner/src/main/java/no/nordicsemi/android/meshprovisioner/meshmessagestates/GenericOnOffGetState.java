package no.nordicsemi.android.meshprovisioner.meshmessagestates;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelGet;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelStatus;
import no.nordicsemi.android.meshprovisioner.messages.GenericOnOffGet;
import no.nordicsemi.android.meshprovisioner.messages.GenericOnOffStatus;
import no.nordicsemi.android.meshprovisioner.messagetypes.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;


/**
 * State class for handling GenericLevelGet messages.
 */
public class GenericOnOffGetState extends GenericMessageState {

    private static final String TAG = GenericOnOffGetState.class.getSimpleName();
    private final GenericOnOffGet mGenericOnOffGet;

    /**
     * Constructs GenericLevelGetState
     *
     * @param context         Context of the application
     * @param dstAddress      Destination address to which the message must be sent to
     * @param genericOnOffGet Wrapper class {@link GenericOnOffGet} containing the opcode and parameters for {@link GenericOnOffGet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException
     */
    public GenericOnOffGetState(@NonNull final Context context,
                                @NonNull final byte[] dstAddress,
                                @NonNull final GenericOnOffGet genericOnOffGet,
                                @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, genericOnOffGet.getMeshNode(), callbacks);
        this.mGenericOnOffGet = genericOnOffGet;
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
                if(message.getOpCode() == ApplicationMessageOpCodes.GENERIC_ON_OFF_STATUS) {
                    final GenericOnOffStatus genericOnOffStatus = new GenericOnOffStatus(mNode, (AccessMessage) message);
                    mInternalTransportCallbacks.updateMeshNode(mNode);
                    mMeshStatusCallbacks.onGenericOnOffStatusReceived(genericOnOffStatus);
                    return true;
                } else {
                    Log.v(TAG, "Unknown pdu received! " + MeshParserUtils.bytesToHex(((AccessMessage) message).getAccessPdu(), false));
                }
            } else {
                parseControlMessage((ControlMessage) message, mPayloads.size());
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
        final byte[] key = mGenericOnOffGet.getAppKey();
        final int akf = mGenericOnOffGet.getAkf();
        final int aid = mGenericOnOffGet.getAid();
        final int aszmic = mGenericOnOffGet.getAszmic();
        final int opCode = mGenericOnOffGet.getOpCode();
        final byte[] parameters = mGenericOnOffGet.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, mDstAddress, key, akf, aid, aszmic, opCode, parameters);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending Generic OnOff get");
        super.executeSend();

        if (!mPayloads.isEmpty()) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onGenericOnOffGetSent(mNode);
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mNode, message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mNode);
    }
}
