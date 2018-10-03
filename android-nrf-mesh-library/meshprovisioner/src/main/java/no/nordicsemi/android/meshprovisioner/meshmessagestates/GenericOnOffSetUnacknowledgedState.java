package no.nordicsemi.android.meshprovisioner.meshmessagestates;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelStatus;
import no.nordicsemi.android.meshprovisioner.messages.GenericOnOffSetUnacknowledged;
import no.nordicsemi.android.meshprovisioner.messagetypes.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.Message;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling GenericOnOffSetState messages.
 */
public class GenericOnOffSetUnacknowledgedState extends GenericMessageState {

    private static final String TAG = GenericOnOffSetUnacknowledgedState.class.getSimpleName();

    private final GenericOnOffSetUnacknowledged mGenericOnOffSetUnacknowledged;

    /**
     * Constructs {@link GenericOnOffSetState}
     *
     * @param context                       Context of the application
     * @param dstAddress                    Destination address to which the message must be sent to
     * @param genericOnOffSetUnacknowledged Wrapper class {@link GenericOnOffSetUnacknowledged} containing the opcode and parameters for {@link GenericOnOffSetUnacknowledged} message
     * @param callbacks                     {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException
     */
    public GenericOnOffSetUnacknowledgedState(@NonNull final Context context,
                                              @NonNull final byte[] dstAddress,
                                              @NonNull final GenericOnOffSetUnacknowledged genericOnOffSetUnacknowledged,
                                              @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, genericOnOffSetUnacknowledged.getMeshNode(), callbacks);
        this.mGenericOnOffSetUnacknowledged = genericOnOffSetUnacknowledged;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_ON_OFF_SET_UNACKNOWLEDGED_STATE;
    }

    @Override
    protected boolean parseMeshPdu(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof AccessMessage) {
                final GenericLevelStatus genericLevelStatus = new GenericLevelStatus(mNode, (AccessMessage) message);
                //TODO handle GenericLevelSet status message
                mInternalTransportCallbacks.updateMeshNode(mNode);
                return true;
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
        final byte[] key = mGenericOnOffSetUnacknowledged.getAppKey();
        final int akf = mGenericOnOffSetUnacknowledged.getAkf();
        final int aid = mGenericOnOffSetUnacknowledged.getAid();
        final int aszmic = mGenericOnOffSetUnacknowledged.getAszmic();
        final int opCode = mGenericOnOffSetUnacknowledged.getOpCode();
        final byte[] parameters = mGenericOnOffSetUnacknowledged.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, mDstAddress, key, akf, aid, aszmic, opCode, parameters);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public void executeSend() {
        Log.v(TAG, "Sending Generic OnOff set unacknowledged: ");
        super.executeSend();

        if (!mPayloads.isEmpty()) {
            if (mMeshStatusCallbacks != null)
                mMeshStatusCallbacks.onGenericOnOffSetUnacknowledgedSent(mNode);
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
