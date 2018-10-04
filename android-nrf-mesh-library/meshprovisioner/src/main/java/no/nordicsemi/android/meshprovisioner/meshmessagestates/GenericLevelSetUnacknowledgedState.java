package no.nordicsemi.android.meshprovisioner.meshmessagestates;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelSet;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelSetUnacknowledged;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelStatus;
import no.nordicsemi.android.meshprovisioner.messagetypes.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.Message;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * State class for handling unacknowledged GenericLevelSet messages.
 */
public class GenericLevelSetUnacknowledgedState extends GenericMessageState {

    private static final String TAG = GenericLevelSetState.class.getSimpleName();
    private final GenericLevelSetUnacknowledged mGenericLevelSet;

    /**
     * Constructs {@link GenericLevelSetUnacknowledgedState}
     *
     * @param context                Context of the application
     * @param dstAddress             Destination address to which the message must be sent to
     * @param genericLevelSetUnacked Wrapper class {@link GenericLevelSetUnacknowledged} containing the opcode and parameters for {@link GenericLevelSetUnacknowledged} message
     * @param callbacks              {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException
     */
    public GenericLevelSetUnacknowledgedState(@NonNull final Context context,
                                              @NonNull final byte[] dstAddress,
                                              @NonNull final GenericLevelSetUnacknowledged genericLevelSetUnacked,
                                              @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, genericLevelSetUnacked.getMeshNode(), callbacks);
        this.mGenericLevelSet = genericLevelSetUnacked;
        createAccessMessage();
    }

    @Override
    public MessageState getState() {
        return MessageState.GENERIC_LEVEL_SET_STATE;
    }

    @Override
    public boolean parseMeshPdu(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof ControlMessage) {
                parseControlMessage((ControlMessage) message, mPayloads.size());
                return true;
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
        final byte[] key = mGenericLevelSet.getAppKey();
        final int akf = mGenericLevelSet.getAkf();
        final int aid = mGenericLevelSet.getAid();
        final int aszmic = mGenericLevelSet.getAszmic();
        final int opCode = mGenericLevelSet.getOpCode();
        final byte[] parameters = mGenericLevelSet.getParameters();
        message = mMeshTransport.createMeshMessage(mNode, mSrc, mDstAddress, key, akf, aid, aszmic, opCode, parameters);
        mPayloads.putAll(message.getNetworkPdu());
    }

    @Override
    public final void executeSend() {
        Log.v(TAG, "Sending Generic Level set acknowledged ");
        super.executeSend();
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mNode, message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mNode);
    }
}
