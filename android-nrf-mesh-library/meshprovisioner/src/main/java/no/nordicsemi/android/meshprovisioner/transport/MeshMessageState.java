package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshStatusCallbacks;
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
        this.mSrc = mNode.getConfigurationSrc();
        this.meshMessageHandlerCallbacks = callbacks;
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
     */
    public SparseArray<byte[]> getNetworkPdu() {
        return message.getNetworkPdu();
    }

    /**
     * Starts sending the mesh pdu
     */
    public void executeSend() {
        if (message.getNetworkPdu().size() > 0) {
            for (int i = 0; i < message.getNetworkPdu().size(); i++) {
                mInternalTransportCallbacks.sendMeshPdu(mNode, message.getNetworkPdu().get(i));
            }
        }
    }

    /**
     * Re-sends the mesh pdu segments that were lost in flight
     */
    final void executeResend(final List<Integer> retransmitPduIndexes) {
        if (message.getNetworkPdu().size() > 0 && !retransmitPduIndexes.isEmpty()) {
            for (int i = 0; i < retransmitPduIndexes.size(); i++) {
                final int segO = retransmitPduIndexes.get(i);
                if (message.getNetworkPdu().get(segO) != null) {
                    final byte[] pdu = message.getNetworkPdu().get(segO);
                    Log.v(TAG, "Resending segment " + segO + " : " + MeshParserUtils.bytesToHex(pdu, false));
                    final Message retransmitMeshMessage = mMeshTransport.createRetransmitMeshMessage(message, segO);
                    mInternalTransportCallbacks.sendMeshPdu(mNode, retransmitMeshMessage.getNetworkPdu().get(segO));
                }
            }
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

            meshMessageHandlerCallbacks.onIncompleteTimerExpired(true);

            if (mMeshStatusCallbacks != null) {
                final int srcAddress = AddressUtils.getUnicastAddressInt(mSrc);
                mMeshStatusCallbacks.onTransactionFailed(mNode, srcAddress, true);
            }
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        //We don't send acks here
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.sendMeshPdu(mNode, message.getNetworkPdu().get(0));
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
