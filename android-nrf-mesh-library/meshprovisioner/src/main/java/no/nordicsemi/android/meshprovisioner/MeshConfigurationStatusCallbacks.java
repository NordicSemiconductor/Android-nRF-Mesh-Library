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

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.configuration.*;
/**
 * Callbacks to notify the status of the mesh messgaes
 */
public interface MeshConfigurationStatusCallbacks {

    /**
     * Notifies if a transaction has failed
     * <p>
     *     As of now this is only triggered if the incomplete timer has expired for a given segmented message.
     *     The incomplete timer will wait for a minimum of 10 seconds on receiving a segmented message.
     *     If all segments are not received during this period, that transaction shall be considered as failed.
     * </p>
     *
     * @param node                      mesh node that failed to handle the transaction
     * @param src                       unique src address of the device
     * @param hasIncompleteTimerExpired flag that notifies if the incomplete timer had expired
     */
    void onTransactionFailed(final ProvisionedMeshNode node, final byte[] src, final boolean hasIncompleteTimerExpired);

    /**
     * Notifies if an unknown pdu was received
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onUnknownPduReceived(final ProvisionedMeshNode node);

    /**
     * Notifies if a block acknowledgement was sent
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onBlockAcknowledgementSent(final ProvisionedMeshNode node);

    /**
     * Notifies if a block acknowledgement was received
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onBlockAcknowledgementReceived(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link ConfigCompositionDataGet} was sent
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onGetCompositionDataSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link ConfigCompositionDataStatus} was received
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onCompositionDataStatusReceived(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link ConfigAppKeyAdd} was sent
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onAppKeyAddSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link ConfigAppKeyStatus} was received
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onAppKeyStatusReceived(final ProvisionedMeshNode node, final boolean success, int status, final int netKeyIndex, final int appKeyIndex);

    /**
     * Notifies if {@link ConfigModelAppBind} was sent
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onAppKeyBindSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link ConfigModelAppStatus} was received
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onAppKeyBindStatusReceived(final ProvisionedMeshNode node, final boolean success, int status, final int elementAddress, final int appKeyIndex, final int modelIdentifier);

    /**
     * Notifies if {@link ConfigModelPublicationSet} was sent
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onPublicationSetSent(final ProvisionedMeshNode node);

    /**
     * Notifies if an {@link ConfigModelPublicationStatus} was received
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onPublicationStatusReceived(final ProvisionedMeshNode node, final boolean success, final int status, final byte[] elementAddress, final byte[] publishAddress, final int modelIdentifier);

    /**
     * Notifies if {@link ConfigModelSubscriptionAdd} was sent
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onSubscriptionAddSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link ConfigModelSubscriptionStatus} was received
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onSubscriptionStatusReceived(final ProvisionedMeshNode node, final boolean success, final int status, final byte[] elementAddress, final byte[] subscriptionAddress, final int modelIdentifier);

    /**
     * Notifies if {@link GenericOnOffStatus} was received
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onGenericOnOffStatusReceived(final ProvisionedMeshNode node, final boolean presentOnOff, final boolean targetOnOff, final int remainingTime);

    /**
     * Notifies if the mesh {@link ConfigNodeReset} was sent
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onMeshNodeResetSent(final ProvisionedMeshNode node);

    /**
     * Notifies if the mesh {@link ConfigNodeResetStatus} was received
     *
     * @param node mesh node that failed to handle the transaction
     */
    void onMeshNodeResetStatusReceived(final ProvisionedMeshNode node);
}
