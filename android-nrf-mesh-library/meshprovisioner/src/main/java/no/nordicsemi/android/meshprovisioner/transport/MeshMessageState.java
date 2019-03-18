package no.nordicsemi.android.meshprovisioner.transport;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshStatusCallbacks;
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

    final MeshTransport mMeshTransport;
    private final InternalMeshMsgHandlerCallbacks meshMessageHandlerCallbacks;
    MeshMessage mMeshMessage;
    int mSrc;
    int mDst;
    protected InternalTransportCallbacks mInternalTransportCallbacks;
    MeshStatusCallbacks mMeshStatusCallbacks;
    protected Message message;

    /**
     * Constructs the base mesh message state class
     *
     * @param meshMessage   {@link MeshMessage} Mesh message
     * @param meshTransport {@link MeshTransport} Mesh transport
     * @param callbacks     {@link InternalMeshMsgHandlerCallbacks} Internal mesh message handler callbacks
     */
    MeshMessageState(@Nullable final MeshMessage meshMessage,
                     @NonNull final MeshTransport meshTransport,
                     @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        this.mMeshMessage = meshMessage;
        if (meshMessage != null) {
            this.message = meshMessage.getMessage();
        }
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
    abstract MessageState getState();

    /**
     * Returns the mesh transport
     */
    MeshTransport getMeshTransport() {
        return mMeshTransport;
    }

    /**
     * Returns the mesh message relating to the state
     */
    public MeshMessage getMeshMessage() {
        return mMeshMessage;
    }

    /**
     * Starts sending the mesh pdu
     */
    public void executeSend() {
        if (message.getNetworkPdu().size() > 0) {
            for (int i = 0; i < message.getNetworkPdu().size(); i++) {
                mInternalTransportCallbacks.onMeshPduCreated(mDst, message.getNetworkPdu().get(i));
            }

            if (mMeshStatusCallbacks != null) {
                mMeshStatusCallbacks.onMeshMessageProcessed(mDst, mMeshMessage);
            }
        }
    }

    /**
     * Re-sends the mesh pdu segments that were lost in flight
     *
     * @param retransmitPduIndexes list of indexes of the messages to be
     */
    final void executeResend(final List<Integer> retransmitPduIndexes) {
        if (message.getNetworkPdu().size() > 0 && !retransmitPduIndexes.isEmpty()) {
            for (int i = 0; i < retransmitPduIndexes.size(); i++) {
                final int segO = retransmitPduIndexes.get(i);
                if (message.getNetworkPdu().get(segO) != null) {
                    final byte[] pdu = message.getNetworkPdu().get(segO);
                    Log.v(TAG, "Resending segment " + segO + " : " + MeshParserUtils.bytesToHex(pdu, false));
                    final Message retransmitMeshMessage = mMeshTransport.createRetransmitMeshMessage(message, segO);
                    mInternalTransportCallbacks.onMeshPduCreated(mDst, retransmitMeshMessage.getNetworkPdu().get(segO));
                }
            }
        }
    }

    @Override
    public void onIncompleteTimerExpired() {
        Log.v(TAG, "Incomplete timer has expired, all segments were not received!");
        if (meshMessageHandlerCallbacks != null) {
            meshMessageHandlerCallbacks.onIncompleteTimerExpired(mDst);

            if (mMeshStatusCallbacks != null) {
                mMeshStatusCallbacks.onTransactionFailed(mDst, true);
            }
        }
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        //We don't send acknowledgements here
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkPdu().get(0), false));
        mInternalTransportCallbacks.onMeshPduCreated(message.getDst(), message.getNetworkPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementProcessed(message.getDst(), controlMessage);
    }

    public enum MessageState {

        //Proxy configuration message
        PROXY_CONFIG_MESSAGE_STATE(500),

        //Configuration message States
        CONFIG_MESSAGE_STATE(501),

        //Application message States
        GENERIC_MESSAGE_STATE(502),
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
