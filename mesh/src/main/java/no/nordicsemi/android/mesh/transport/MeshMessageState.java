package no.nordicsemi.android.mesh.transport;

import android.util.Log;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.InternalTransportCallbacks;
import no.nordicsemi.android.mesh.MeshStatusCallbacks;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * This generic class handles the mesh messages received or sent.
 * <p>
 * This class handles sending, resending and parsing mesh messages. Each message sent by the library has its own state.
 * {@link ConfigMessageState} and {@link ApplicationMessageState} extends this class based on the type of the message.
 * Currently the library supports basic Configuration and Generic Messages.
 * </p>
 */
abstract class MeshMessageState implements LowerTransportLayerCallbacks {

    private static final String TAG = MeshMessageState.class.getSimpleName();

    MeshMessage mMeshMessage;
    final MeshTransport mMeshTransport;
    private final InternalMeshMsgHandlerCallbacks meshMessageHandlerCallbacks;
    protected InternalTransportCallbacks mInternalTransportCallbacks;
    MeshStatusCallbacks mMeshStatusCallbacks;
    int mSrc;
    int mDst;
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
        if (message.getNetworkLayerPdu().size() > 0) {
            for (int i = 0; i < message.getNetworkLayerPdu().size(); i++) {
                mInternalTransportCallbacks.onMeshPduCreated(mDst, message.getNetworkLayerPdu().get(i));
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
        if (message.getNetworkLayerPdu().size() > 0 && !retransmitPduIndexes.isEmpty()) {
            for (int i = 0; i < retransmitPduIndexes.size(); i++) {
                final int segO = retransmitPduIndexes.get(i);
                if (message.getNetworkLayerPdu().get(segO) != null) {
                    final byte[] pdu = message.getNetworkLayerPdu().get(segO);
                    Log.v(TAG, "Resending segment " + segO + " : " + MeshParserUtils.bytesToHex(pdu, false));
                    final Message retransmitMeshMessage = mMeshTransport.createRetransmitMeshMessage(message, segO);
                    mInternalTransportCallbacks.onMeshPduCreated(mDst, retransmitMeshMessage.getNetworkLayerPdu().get(segO));
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
    public int getTtl() {
        return message.getTtl();
    }

    @Override
    public void sendSegmentAcknowledgementMessage(final ControlMessage controlMessage) {
        //We don't send acknowledgements here
        final ControlMessage message = mMeshTransport.createSegmentBlockAcknowledgementMessage(controlMessage);
        Log.v(TAG, "Sending acknowledgement: " + MeshParserUtils.bytesToHex(message.getNetworkLayerPdu().get(0), false));
        mInternalTransportCallbacks.onMeshPduCreated(message.getDst(), message.getNetworkLayerPdu().get(0));
        mMeshStatusCallbacks.onBlockAcknowledgementProcessed(message.getDst(), controlMessage);
    }

    public enum MessageState {

        //Proxy configuration message
        PROXY_CONFIG_MESSAGE_STATE(500),
        //Configuration message States
        CONFIG_MESSAGE_STATE(501),
        //Application message States
        APPLICATION_MESSAGE_STATE(502),
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
