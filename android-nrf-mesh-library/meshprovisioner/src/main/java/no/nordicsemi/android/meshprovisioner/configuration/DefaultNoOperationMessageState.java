package no.nordicsemi.android.meshprovisioner.configuration;

import android.content.Context;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.ControlMessage;
import no.nordicsemi.android.meshprovisioner.messages.Message;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public class DefaultNoOperationMessageState extends MeshMessageState {

    private static final String TAG = DefaultNoOperationMessageState.class.getSimpleName();

    public DefaultNoOperationMessageState(final Context context, final ProvisionedMeshNode provisionedMeshNode,
                                          final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, provisionedMeshNode, callbacks);
    }

    @Override
    public MessageState getState() {
        return null;
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mProvisionedMeshNode, message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mProvisionedMeshNode);
    }

    @Override
    public boolean parseMeshPdu(final byte[] pdu) {
        final Message message = mMeshTransport.parsePdu(mSrc, pdu);
        if (message != null) {
            if (message instanceof AccessMessage) {
                final byte[] accessPayload = ((AccessMessage) message).getAccessPdu();

                final int opCodeLength = ((accessPayload[0] & 0xF0) >> 6);
                switch (opCodeLength) {
                    case 1:
                        break;
                    case 2:
                        if(message.getOpCode() == ApplicationMessageOpCodes.GENERIC_ON_OFF_STATUS) {
                            final GenericOnOffStatus genericOnOffStatus = new GenericOnOffStatus(mContext, mProvisionedMeshNode,
                                    meshMessageHandlerCallbacks);
                            genericOnOffStatus.setTransportCallbacks(mInternalTransportCallbacks);
                            genericOnOffStatus.setStatusCallbacks(mMeshStatusCallbacks);
                            genericOnOffStatus.parseGenericOnOffStatusMessage((AccessMessage) message);
                        } else {
                            Log.v(TAG, "Unknown Access PDU Received: " + MeshParserUtils.bytesToHex(accessPayload, false));
                        }
                        break;
                    case 3:
                        Log.v(TAG, "Vendor model Access PDU Received: " + MeshParserUtils.bytesToHex(accessPayload, false));
                        mMeshStatusCallbacks.onUnknownPduReceived(mProvisionedMeshNode);
                        break;
                    default:
                        Log.v(TAG, "Unknown Access PDU Received: " + MeshParserUtils.bytesToHex(accessPayload, false));
                        mMeshStatusCallbacks.onUnknownPduReceived(mProvisionedMeshNode);
                        break;
                }
                return true;
            } else {
                parseControlMessage((ControlMessage) message, mPayloads.size());
            }
        } else {
            Log.v(TAG, "Message reassembly may not be completed yet!");
        }
        return false;
    }

}
