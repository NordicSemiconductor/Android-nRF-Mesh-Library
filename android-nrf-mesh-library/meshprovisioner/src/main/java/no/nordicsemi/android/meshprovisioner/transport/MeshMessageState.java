package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.control.BlockAcknowledgementMessage;
import no.nordicsemi.android.meshprovisioner.control.TransportControlMessage;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * This generic class handles the mesh messages received or sent.
 * <p>
 * This class handles sending, resending and parsing mesh messages. Each message sent by the library has its own state.
 * {@link ConfigMessageState} and {@link GenericMessageState} extends this class based on the type of the message.
 * Currently the library supports basic Configuration and Generic Messages.
 * </p>
 */
abstract class MeshMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = MeshMessageState.class.getSimpleName();

    protected final Context mContext;
    protected final MeshMessage mMeshMessage;
    protected final ProvisionedMeshNode mNode;
    final MeshTransport mMeshTransport;
    private final List<Integer> mRetransmitPayloads = new ArrayList<>();
    final byte[] mSrc;
    protected InternalTransportCallbacks mInternalTransportCallbacks;
    protected MeshStatusCallbacks mMeshStatusCallbacks;
    private final InternalMeshMsgHandlerCallbacks meshMessageHandlerCallbacks;
    protected AccessMessage message;
    private boolean isIncompleteTimerExpired;

    MeshMessageState(final Context context, final MeshMessage meshMessage, final MeshTransport meshTransport, final InternalMeshMsgHandlerCallbacks callbacks) {
        this.mContext = context;
        this.mMeshMessage = meshMessage;
        this.message = meshMessage.getMessage();
        this.mNode = meshMessage.getMeshNode();
        this.meshMessageHandlerCallbacks = callbacks;
        this.mSrc = mNode.getConfigurationSrc();
        this.mMeshTransport = meshTransport;
        this.mMeshTransport.setLowerTransportLayerCallbacks(this);
    }

    /**
     * Set transport callbacks
     *
     * @param callbacks callbacks
     */
    void setTransportCallbacks(final InternalTransportCallbacks callbacks) {
        this.mInternalTransportCallbacks = callbacks;
    }

    /**
     * Set mesh status call backs
     *
     * @param callbacks callbacks
     */
    void setStatusCallbacks(final MeshStatusCallbacks callbacks) {
        this.mMeshStatusCallbacks = callbacks;
    }

    /**
     * Returns the current mesh state
     */
    public abstract MessageState getState();

    /**
     * Returns the mesh message relating to the state
     */
    public MeshMessage getMeshMessage() {
        return mMeshMessage;
    }

    /**
     * Returns the network pdu
     * @return
     */
    public SparseArray<byte[]> getNetworkPdu() {
        return message.getNetworkPdu();
    }

    /**
     * Checks if the message has to be retransmitted.
     *
     * @return true if retransmission is required or false otherwise
     */
    final boolean isRetransmissionRequired() {
        return !mRetransmitPayloads.isEmpty();
    }

    /**
     * Starts sending the mesh pdu
     */
    public void executeSend() {
        if (message.getNetworkPdu().size() > 0) {
            for (int i = 0; i < message.getNetworkPdu().size(); i++) {
                mInternalTransportCallbacks.sendPdu(mNode, message.getNetworkPdu().get(i));
            }
        }
    }

    /**
     * Re-sends the mesh pdu segments that were lost in flight
     */
    public void executeResend() {
        if (message.getNetworkPdu().size() > 0 && !mRetransmitPayloads.isEmpty()) {
            for (int i = 0; i < mRetransmitPayloads.size(); i++) {
                final int segO = mRetransmitPayloads.get(i);
                if (message.getNetworkPdu().get(segO) != null) {
                    final byte[] pdu = message.getNetworkPdu().get(segO);
                    Log.v(TAG, "Resending segment " + segO + " : " + MeshParserUtils.bytesToHex(pdu, false));
                    final Message retransmitMeshMessage = mMeshTransport.createRetransmitMeshMessage(message, segO);
                    mInternalTransportCallbacks.sendPdu(mNode, retransmitMeshMessage.getNetworkPdu().get(segO));
                }
            }
        }
    }

    /**
     * Parses control message and returns the underlying configuration message
     *
     * @param controlMessage control message to be passed
     * @param segmentCount   number of segments
     */
    final void parseControlMessage(final ControlMessage controlMessage, final int segmentCount) {
        final TransportControlMessage transportControlMessage = controlMessage.getTransportControlMessage();
        switch (transportControlMessage.getState()) {
            case LOWER_TRANSPORT_BLOCK_ACKNOWLEDGEMENT:
                Log.v(TAG, "Acknowledgement payload: " + MeshParserUtils.bytesToHex(controlMessage.getTransportControlPdu(), false));
                mRetransmitPayloads.clear();
                mRetransmitPayloads.addAll(BlockAcknowledgementMessage.getSegmentsToBeRetransmitted(controlMessage.getTransportControlPdu(), segmentCount));
                mMeshStatusCallbacks.onBlockAcknowledgementReceived(mNode);
                break;
            default:
                Log.v(TAG, "Unexpected control message received, ignoring message");
                mMeshStatusCallbacks.onUnknownPduReceived(mNode, AddressUtils.getUnicastAddressInt(controlMessage.getSrc()), controlMessage.getTransportControlPdu());
                break;
        }
    }

    public ProvisionedMeshNode getMeshNode() {
        return mNode;
    }

    boolean isSegmented() {
        return message.getNetworkPdu().size() > 1;
    }

    @Override
    public void onIncompleteTimerExpired() {
        Log.v(TAG, "Incomplete timer has expired, all segments were not received!");
        isIncompleteTimerExpired = true;
        if (meshMessageHandlerCallbacks != null) {

            final byte[] src = mSrc; //The destination of the message sent would be src address of the device
            meshMessageHandlerCallbacks.onIncompleteTimerExpired(mNode, src, true);

            if (mMeshStatusCallbacks != null) {
                final int srcAddress = AddressUtils.getUnicastAddressInt(src);
                mMeshStatusCallbacks.onTransactionFailed(mNode, srcAddress, true);
            }
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        //We don't send acks here
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendPdu(mNode, message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementSent(mNode);
    }

    public boolean isIncompleteTimerExpired() {
        return isIncompleteTimerExpired;
    }

    public enum MessageState {
        //Configuration message States
        COMPOSITION_DATA_GET_STATE(0),
        APP_KEY_ADD_STATE(1),
        CONFIG_MODEL_APP_BIND_STATE(2),
        CONFIG_MODEL_APP_UNBIND_STATE(3),
        CONFIG_MODEL_PUBLICATION_SET_STATE(4),
        CONFIG_MODEL_SUBSCRIPTION_ADD_STATE(5),
        CONFIG_MODEL_SUBSCRIPTION_DELETE_STATE(6),
        CONFIG_NODE_RESET_STATE(7),

        //Application message States
        GENERIC_ON_OFF_GET_STATE(200),
        GENERIC_ON_OFF_SET_STATE(201),
        GENERIC_ON_OFF_SET_UNACKNOWLEDGED_STATE(202),

        GENERIC_LEVEL_GET_STATE(203),
        GENERIC_LEVEL_SET_STATE(204),
        GENERIC_LEVEL_SET_UNACKNOWLEDGED_STATE(205),

        VENDOR_MODEL_ACKNOWLEDGED_STATE(1000),
        VENDOR_MODEL_UNACKNOWLEDGED_STATE(1001);

        private int state;

        MessageState(final int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }
    }
}
