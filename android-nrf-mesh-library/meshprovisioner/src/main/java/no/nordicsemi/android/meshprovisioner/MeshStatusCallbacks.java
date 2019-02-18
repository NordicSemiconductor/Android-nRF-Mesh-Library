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

package no.nordicsemi.android.meshprovisioner;

import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;

/**
 * Callbacks to notify the status of the mesh messgaes
 */
public interface MeshStatusCallbacks {

    /**
     * Notifies if a transaction has failed
     * <p>
     * As of now this is only triggered if the incomplete timer has expired for a given segmented message.
     * The incomplete timer will wait for a minimum of 10 seconds on receiving a segmented message.
     * If all segments are not received during this period, that transaction shall be considered as failed.
     * </p>
     *
     * @param dst                       unique dst address of the device
     * @param hasIncompleteTimerExpired flag that notifies if the incomplete timer had expired
     * @deprecated in favor of {@link #onTransactionFailed(int, boolean)}
     */
    @Deprecated
    void onTransactionFailed(final byte[] dst, final boolean hasIncompleteTimerExpired);

    /**
     * Notifies if a transaction has failed
     * <p>
     * As of now this is only triggered if the incomplete timer has expired for a given segmented message.
     * The incomplete timer will wait for a minimum of 10 seconds on receiving a segmented message.
     * If all segments are not received during this period, that transaction shall be considered as failed.
     * </p>
     *
     * @param dst                       unique dst address of the device
     * @param hasIncompleteTimerExpired flag that notifies if the incomplete timer had expired
     */
    void onTransactionFailed(final int dst, final boolean hasIncompleteTimerExpired);

    /**
     * Notifies if an unknown pdu was received
     *
     * @param src           address where the message originated from
     * @param accessPayload access payload of the message
     * @deprecated in favor of {@link #onUnknownPduReceived(int, byte[])}
     */
    @Deprecated
    void onUnknownPduReceived(final byte[] src, final byte[] accessPayload);

    /**
     * Notifies if an unknown pdu was received
     *
     * @param src           address where the message originated from
     * @param accessPayload access payload of the message
     */
    void onUnknownPduReceived(final int src, final byte[] accessPayload);

    /**
     * Notifies if a block acknowledgement was sent
     *
     * @param dst dst address to which the block ack was sent
     * @deprecated in favour of {@link #onBlockAcknowledgementSent(int)}
     */
    @Deprecated
    void onBlockAcknowledgementSent(final byte[] dst);

    /**
     * Notifies if a block acknowledgement was sent
     *
     * @param dst dst address to which the block ack was sent
     */
    void onBlockAcknowledgementSent(final int dst);

    /**
     * Notifies if a block acknowledgement was received
     *
     * @param src source address from which the block ack was received
     * @deprecated in favour of {@link #onBlockAcknowledgementSent(int)}
     */
    @Deprecated
    void onBlockAcknowledgementReceived(final byte[] src);

    /**
     * Notifies if a block acknowledgement was received
     *
     * @param src source address from which the block ack was received
     */
    void onBlockAcknowledgementReceived(final int src);

    /**
     * Callback to notify the mesh message has been sent
     *
     * @param dst         Destination address to be sent
     * @param meshMessage {@link MeshMessage} containing the message that was sent
     * @deprecated in favour of {@link #onMeshMessageSent(int, MeshMessage)}
     */
    @Deprecated
    void onMeshMessageSent(final byte[] dst, final MeshMessage meshMessage);

    /**
     * Callback to notify the mesh message has been sent
     *
     * @param dst         Destination address to be sent
     * @param meshMessage {@link MeshMessage} containing the message that was sent
     */
    void onMeshMessageSent(final int dst, final MeshMessage meshMessage);

    /**
     * Callback to notify that a mesh status message was received
     *
     * @param src         source address where the message originated from
     * @param meshMessage {@link MeshMessage} containing the message that was received
     * @deprecated in favour of {@link #onMeshMessageSent(int, MeshMessage)}
     */
    @Deprecated
    void onMeshMessageReceived(final byte[] src, final MeshMessage meshMessage);

    /**
     * Callback to notify that a mesh status message was received
     *
     * @param src         source address where the message originated from
     * @param meshMessage {@link MeshMessage} containing the message that was received
     */
    void onMeshMessageReceived(final int src, final MeshMessage meshMessage);

    /**
     * Callback to notify if the decryption failed of a received mesh message
     *
     * @param errorMessage Error message
     */
    void onMessageDecryptionFailed(final String meshLayer, final String errorMessage);
}
