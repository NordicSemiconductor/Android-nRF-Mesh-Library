/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.mesh.control;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class BlockAcknowledgementMessage extends TransportControlMessage {

    private static final String TAG = BlockAcknowledgementMessage.class.getSimpleName();

    public BlockAcknowledgementMessage(final byte[] acknowledgementPayload) {

    }

    /**
     * Calculates the block acknowledgement payload.
     * <p>
     * This method will set the segO bit to 1
     * </p>
     *
     * @param blockAck block acknowledgement payload to be sent
     * @param segO     segment index
     */
    public static Integer calculateBlockAcknowledgement(final Integer blockAck, final int segO) {
        int ack = 0;
        if (blockAck == null) {
            ack |= 1 << segO;
            Log.v(TAG, "Block ack value: " + Integer.toString(ack, 16));
            return ack;
        } else {
            ack = blockAck;
            ack |= 1 << segO;
            Log.v(TAG, "Block ack value: " + Integer.toString(ack, 16));
            return ack;
        }
    }

    /**
     * Calculates the block acknowledgement payload.
     * <p>
     * This method will set the segO bit to 1
     * </p>
     *
     * @param segN number of segments
     */
    public static int calculateBlockAcknowledgement(final int segN) {
        final int segmentCount = segN + 1;
        int ack = 0;
        for (int i = 0; i < segmentCount; i++) {
            ack |= 1 << i;
        }
        return ack;
    }

    @Override
    public TransportControlMessageState getState() {
        return TransportControlMessageState.LOWER_TRANSPORT_BLOCK_ACKNOWLEDGEMENT;
    }

    /**
     * Parses the block acknowledgement payload
     * <p>
     * This method will iterate though the block acknowledgement to find out which segments needs to be retransmitted.
     * </p>
     *
     * @param blockAcknowledgement acknowledgement payload received
     * @param segmentCount         number of segments
     */
    public static ArrayList<Integer> getSegmentsToBeRetransmitted(final byte[] blockAcknowledgement, final int segmentCount) {
        final ArrayList<Integer> retransmitSegments = new ArrayList<>();
        final int blockAck = ByteBuffer.wrap(blockAcknowledgement).order(ByteOrder.BIG_ENDIAN).getInt();
        for (int i = 0; i < segmentCount; i++) {
            int bit = (blockAck >> i) & 1;
            if (bit == 1) {
                Log.v(TAG, "Segment " + i + " of " + (segmentCount - 1) + " received by peer");
            } else {
                retransmitSegments.add(i);
                Log.v(TAG, "Segment " + i + " of " + (segmentCount - 1) + " not received by peer");
            }
        }
        return retransmitSegments;
    }

    /**
     * Checks if all segments are received based on the segment count
     *
     * @param blockAcknowledgement acknowledgement payload received
     * @param segN                 number of segments
     */
    public static boolean hasAllSegmentsBeenReceived(final Integer blockAcknowledgement, final int segN) {
        if (blockAcknowledgement == null)
            return false;
        Log.v(TAG, "Block ack: " + blockAcknowledgement);
        final int blockAck = blockAcknowledgement;
        int setBitCount = 0;
        for (int i = 0; i < segN; i++) {
            int bit = (blockAck >> i) & 1;
            if (bit == 1) {
                setBitCount++;
            }
        }
        Log.v(TAG, "bit count: " + setBitCount);
        return setBitCount == segN + 1; //Since segN is 0 based add 1 as the bit count represents the number of segments
    }
}
