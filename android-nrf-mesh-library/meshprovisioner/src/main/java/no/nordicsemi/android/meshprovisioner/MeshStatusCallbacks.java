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

import android.support.annotation.NonNull;

import no.nordicsemi.android.meshprovisioner.meshmessagestates.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.meshmessagestates.*;
import no.nordicsemi.android.meshprovisioner.messages.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.messages.ConfigCompositionDataStatus;
import no.nordicsemi.android.meshprovisioner.messages.ConfigModelAppBind;
import no.nordicsemi.android.meshprovisioner.messages.ConfigModelAppStatus;
import no.nordicsemi.android.meshprovisioner.messages.ConfigModelAppUnbind;
import no.nordicsemi.android.meshprovisioner.messages.ConfigModelPublicationSet;
import no.nordicsemi.android.meshprovisioner.messages.ConfigModelPublicationStatus;
import no.nordicsemi.android.meshprovisioner.messages.ConfigNodeResetStatus;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelStatus;
import no.nordicsemi.android.meshprovisioner.messages.GenericOnOffStatus;
import no.nordicsemi.android.meshprovisioner.messages.VendorModelMessageStatus;

/**
 * Callbacks to notify the status of the mesh messgaes
 */
public interface MeshStatusCallbacks {

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
    void onTransactionFailed(final ProvisionedMeshNode node, final int src, final boolean hasIncompleteTimerExpired);

    /**
     * Notifies if an unknown pdu was received
     *
     * @param node mesh node that the message was received from
     */
    void onUnknownPduReceived(final ProvisionedMeshNode node);

    /**
     * Notifies if a block acknowledgement was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onBlockAcknowledgementSent(final ProvisionedMeshNode node);

    /**
     * Notifies if a block acknowledgement was received
     *
     * @param node mesh node that the message was received from
     */
    void onBlockAcknowledgementReceived(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link ConfigCompositionDataGetState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onGetCompositionDataSent(@NonNull final ProvisionedMeshNode node);

    /**
     * Callback returning the {@link ConfigCompositionDataStatus} message
     *
     * @param compositionDataStatus composition data status message
     */
    void onCompositionDataStatusReceived(@NonNull final ConfigCompositionDataStatus compositionDataStatus);

    /**
     * Notifies if {@link ConfigAppKeyAddState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onAppKeyAddSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link ConfigAppKeyStatus} was received
     *
     * @param status
     */
    void onAppKeyStatusReceived(final ConfigAppKeyStatus status);

    /**
     * Notifies if {@link ConfigModelAppBindState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onAppKeyBindSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link ConfigModelAppUnbindState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onAppKeyUnbindSent(final ProvisionedMeshNode node);

    /**
     * Callback returning the status for {@link ConfigModelAppBind} or {@link ConfigModelAppUnbind} message
     *
     * @param status {@link ConfigModelAppStatus} containing the status
     */
    void onAppKeyBindStatusReceived(@NonNull final ConfigModelAppStatus status);

    /**
     * Notifies if {@link ConfigModelPublicationSetState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onPublicationSetSent(final ProvisionedMeshNode node);

    /**
     * Callback returning the status for  {@link ConfigModelPublicationSet} message.
     *
     * @param status {@link ConfigModelPublicationStatus} containing hte status
     */
    void onPublicationStatusReceived(@NonNull final ConfigModelPublicationStatus status);

    /**
     * Notifies if {@link ConfigModelSubscriptionAddState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onSubscriptionAddSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link ConfigModelSubscriptionDeleteState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onSubscriptionDeleteSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link ConfigModelSubscriptionStatusState} was received
     *
     * @param node mesh node that the message was received from
     */
    void onSubscriptionStatusReceived(final ProvisionedMeshNode node, final boolean success, final int status, final byte[] elementAddress, final byte[] subscriptionAddress, final int modelIdentifier);

    /**
     * Notifies if the mesh {@link ConfigNodeResetState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onMeshNodeResetSent(final ProvisionedMeshNode node);

    /**
     * Notifies if the mesh {@link ConfigNodeResetStatus} was received.
     *
     * @param configNodeResetStatus config node reset status message
     */
    void onMeshNodeResetStatusReceived(@NonNull final ConfigNodeResetStatus configNodeResetStatus);

    /**
     * Notifies if {@link GenericOnOffGetState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onGenericOnOffGetSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link GenericOnOffSetState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onGenericOnOffSetSent(final ProvisionedMeshNode node, final boolean presentOnOff, final boolean targetOnOff, final int remainingTime);

    /**
     * Notifies if {@link GenericOnOffSetUnacknowledgedState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onGenericOnOffSetUnacknowledgedSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link GenericOnOffStatus} was received
     *  @param node mesh node that the message was received from
     * @param targetOnOff
     * @param transitionSteps
     * @param transitionResolution
     */
    void onGenericOnOffStatusReceived(final ProvisionedMeshNode node, final boolean presentOnOff, final Boolean targetOnOff, final int transitionSteps, final int transitionResolution);


    /**
     * Notifies if {@link GenericLevelGetState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onGenericLevelGetSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link GenericLevelSetState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onGenericLevelSetSent(final ProvisionedMeshNode node, final boolean presentOnOff, final boolean targetOnOff, final int remainingTime);

    /**
     * Notifies if {@link GenericLevelSetUnacknowledgedState} was sent
     *
     * @param node mesh node that the message was sent to
     */
    void onGenericLevelSetUnacknowledgedSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link GenericLevelStatus} was received
     * @param node mesh node that the message was received from
     * @param targetLevel
     * @param transitionSteps
     * @param transitionResolution
     */
    void onGenericLevelStatusReceived(final ProvisionedMeshNode node, final int presentLevel, final int targetLevel, final int transitionSteps, final int transitionResolution);

    /**
     * Notifies if {@link VendorModelMessageUnackedState} was received
     *
     * @param node mesh node that the message was received from
     */
    void onUnacknowledgedVendorModelMessageSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link VendorModelMessageState} was received
     *
     * @param node mesh node that the message was received from
     */
    void onAcknowledgedVendorModelMessageSent(final ProvisionedMeshNode node);

    /**
     * Notifies if {@link VendorModelMessageStatus} was received
     *
     * @param node mesh node that the message was received from
     */
    void onVendorModelMessageStatusReceived(final ProvisionedMeshNode node, final byte[] pdu);
}
