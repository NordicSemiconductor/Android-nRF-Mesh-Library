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

package no.nordicsemi.android.nrfmesh.viewmodels;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import dagger.hilt.android.lifecycle.HiltViewModel;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.models.SigModelParser;
import no.nordicsemi.android.mesh.transport.ConfigBeaconGet;
import no.nordicsemi.android.mesh.transport.ConfigFriendGet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatPublicationGet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatSubscriptionGet;
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitGet;
import no.nordicsemi.android.mesh.transport.ConfigNodeIdentityGet;
import no.nordicsemi.android.mesh.transport.ConfigRelayGet;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.SceneGet;
import no.nordicsemi.android.nrfmesh.node.ConfigurationClientActivity;
import no.nordicsemi.android.nrfmesh.node.ConfigurationServerActivity;
import no.nordicsemi.android.nrfmesh.node.GenericLevelServerActivity;
import no.nordicsemi.android.nrfmesh.node.GenericModelConfigurationActivity;
import no.nordicsemi.android.nrfmesh.node.GenericOnOffServerActivity;
import no.nordicsemi.android.nrfmesh.node.VendorModelActivity;

/**
 * Generic View Model class for {@link ConfigurationServerActivity},{@link ConfigurationClientActivity},
 * {@link GenericOnOffServerActivity}, {@link GenericLevelServerActivity}, {@link VendorModelActivity},
 * {@link GenericModelConfigurationActivity}
 */
@HiltViewModel
public class ModelConfigurationViewModel extends BaseViewModel {

    @Inject
    ModelConfigurationViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mNrfMeshRepository.clearTransactionStatus();
        messageQueue.clear();
    }

    public boolean isActivityVisible() {
        return isActivityVisible;
    }

    public void setActivityVisible(final boolean visible) {
        isActivityVisible = visible;
    }

    public void prepareMessageQueue() {
        switch (getSelectedModel().getValue().getModelId()) {
            case SigModelParser.CONFIGURATION_SERVER:
                messageQueue.add(new ConfigHeartbeatPublicationGet());
                messageQueue.add(new ConfigHeartbeatSubscriptionGet());
                messageQueue.add(new ConfigRelayGet());
                messageQueue.add(new ConfigNetworkTransmitGet());
                messageQueue.add(new ConfigBeaconGet());
                messageQueue.add(new ConfigFriendGet());
                messageQueue.add(new ConfigNodeIdentityGet());
                break;
            case SigModelParser.SCENE_SERVER:
                messageQueue.add(new SceneGet(getDefaultApplicationKey()));
                break;
        }
    }

    public ApplicationKey getDefaultApplicationKey() {
        final MeshModel meshModel = getSelectedModel().getValue();
        if (meshModel != null && !meshModel.getBoundAppKeyIndexes().isEmpty()) {
            return getNetworkLiveData().getAppKeys().get(meshModel.getBoundAppKeyIndexes().get(0));
        }
        return null;
    }
}
