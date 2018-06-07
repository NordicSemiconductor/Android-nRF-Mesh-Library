package no.nordicsemi.android.meshprovisioner.control;

import android.util.Log;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public class BlockAcknowledgementMessage extends TransportControlMessage {


    private static final String TAG = BlockAcknowledgementMessage.class.getSimpleName();

    public BlockAcknowledgementMessage(final byte[] accessPayload, final int offset) {
        parseBlockAcknowledgement(accessPayload, offset);
    }

    public static Integer calculateBlockAcknowledgement(final Integer blockAck, final int segO) {
        int ack = 0;
        if (blockAck == null) {
            ack |= 1 << segO;
            return ack;
        } else {
            ack = blockAck;
            ack |= 1 << segO;
            return ack;
        }
    }

    @Override
    public TransportControlMessageState getState() {
        return TransportControlMessageState.LOWER_TRANSPORT_BLOCK_ACKNOWLEDGEMENT;
    }

    private void parseBlockAcknowledgement(final byte[] transportPayload, final int offset) {
        Log.v(TAG, "Acknowledgement received from node: " + MeshParserUtils.bytesToHex(transportPayload, false));
    }
}
