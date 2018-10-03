package no.nordicsemi.android.meshprovisioner.meshmessagestates;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelGet;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelSet;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelStatus;
import no.nordicsemi.android.meshprovisioner.messagetypes.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messagetypes.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.transport.LowerTransportLayerCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
/**
 * State class for handling GenericLevelSet messages.
 */
public class GenericLevelSetState extends GenericMessageState implements LowerTransportLayerCallbacks {
    
    private static final String TAG = GenericLevelSetState.class.getSimpleName();
    private final GenericLevelSet mGenericLevelSet;

    /**
     * Constructs {@link GenericLevelSetState}
     *
     * @param context         Context of the application
     * @param dstAddress      Destination address to which the message must be sent to
     * @param genericLevelSet Wrapper class {@link GenericLevelSet} containing the opcode and parameters for {@link GenericLevelSet} message
     * @param callbacks       {@link InternalMeshMsgHandlerCallbacks} for internal callbacks
     * @throws IllegalArgumentException
     */
    public GenericLevelSetState(@NonNull final Context context,
                                @NonNull final byte[] dstAddress,
                                @NonNull final GenericLevelSet genericLevelSet,
                                @NonNull final InternalMeshMsgHandlerCallbacks callbacks) throws IllegalArgumentException {
        super(context, dstAddress, genericLevelSet.getMeshNode(), callbacks);
        this.mGenericLevelSet = genericLevelSet;
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
