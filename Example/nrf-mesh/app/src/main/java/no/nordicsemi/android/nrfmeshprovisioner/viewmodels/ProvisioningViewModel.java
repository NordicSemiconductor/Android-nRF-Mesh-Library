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

package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.ProvisioningActivity;

/**
 * ViewModel for {@link ProvisioningActivity}
 */
public class ProvisioningViewModel extends BaseViewModel {

    @Inject
    ProvisioningViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mNrfMeshRepository.clearProvisioningLiveData();
    }

    /**
     * Returns the LifeData {@link UnprovisionedMeshNode}
     */
    public LiveData<UnprovisionedMeshNode> getUnprovisionedMeshNode() {
        return mNrfMeshRepository.getUnprovisionedMeshNode();
    }

    /**
     * Returns true if reconnecting after provisioning is completed
     */
    public LiveData<Boolean> isReconnecting() {
        return mNrfMeshRepository.isReconnecting();
    }

    /**
     * Returns the provisioning status
     */
    public ProvisioningStatusLiveData getProvisioningStatus() {
        return mNrfMeshRepository.getProvisioningState();
    }

    /**
     * Returns true if provisioning has completed
     */
    public boolean isProvisioningComplete() {
        return mNrfMeshRepository.isProvisioningComplete();
    }

    /**
     * Returns true if the CompositionDataStatus is received
     */
    public boolean isCompositionDataStatusReceived() {
        return mNrfMeshRepository.isCompositionDataStatusReceived();
    }

    /**
     * Returns true if the DefaultTTLGet completed
     */
    public boolean isDefaultTtlReceived() {
        return mNrfMeshRepository.isDefaultTtlReceived();
    }

    /**
     * Returns true if the AppKeyAdd completed
     */
    public boolean isAppKeyAddCompleted() {
        return mNrfMeshRepository.isAppKeyAddCompleted();
    }

    /**
     * Returns true if the NetworkRetransmitSet is completed
     */
    public boolean isNetworkRetransmitSetCompleted() {
        return mNrfMeshRepository.isNetworkRetransmitSetCompleted();
    }

}
