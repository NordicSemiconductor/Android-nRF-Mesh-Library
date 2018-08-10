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

public interface MeshConfigurationStatusCallbacks {

    void onUnknownPduReceived(final ProvisionedMeshNode node);

    void onBlockAcknowledgementSent(final ProvisionedMeshNode node);

    void onBlockAcknowledgementReceived(final ProvisionedMeshNode node);

    void onGetCompositionDataSent(final ProvisionedMeshNode node);

    void onCompositionDataStatusReceived(final ProvisionedMeshNode node);

    void onAppKeyAddSent(final ProvisionedMeshNode node);

    void onAppKeyStatusReceived(final ProvisionedMeshNode node, final boolean success, int status, final int netKeyIndex, final int appKeyIndex);

    void onAppKeyBindSent(final ProvisionedMeshNode node);

    void onAppKeyBindStatusReceived(final ProvisionedMeshNode node, final boolean success, int status, final int elementAddress, final int appKeyIndex, final int modelIdentifier);

    void onPublicationSetSent(final ProvisionedMeshNode node);

    void onPublicationStatusReceived(final ProvisionedMeshNode node, final boolean success, final int status, final byte[] elementAddress, final byte[] publishAddress, final int modelIdentifier);

    void onSubscriptionAddSent(final ProvisionedMeshNode node);

    void onSubscriptionStatusReceived(final ProvisionedMeshNode node, final boolean success, final int status, final byte[] elementAddress, final byte[] subscriptionAddress, final int modelIdentifier);

    void onGenericOnOffStatusReceived(final ProvisionedMeshNode node, final boolean presentOnOff, final boolean targetOnOff, final int remainingTime);

    void onMeshNodeResetSent(final ProvisionedMeshNode node);

    void onMeshNodeResetStatusReceived(final ProvisionedMeshNode node);
}
