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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.Map;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisionedNodesLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisioningLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.repository.MeshRepository;
import no.nordicsemi.android.nrfmeshprovisioner.repository.ScannerRepository;

public class SharedViewModel extends ViewModel {

    private final ScannerRepository mScannerRepository;
    private final MeshRepository mMeshRepository;

    @Inject
    SharedViewModel(final ScannerRepository scannerRepository, final MeshRepository meshRepository) {
        this.mScannerRepository = scannerRepository;
        this.mMeshRepository = meshRepository;
        scannerRepository.registerBroadcastReceivers();
        mMeshRepository.registerBroadcastReceiver();
    }

    public ProvisioningLiveData getProvisioningData() {
        return mMeshRepository.getProvisioningData();
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        mMeshRepository.disconnect();
        mScannerRepository.unregisterBroadcastReceivers();
        mMeshRepository.unregisterBroadcastReceiver();
        mMeshRepository.unbindService();
        mMeshRepository.stopService();
    }

    public ScannerRepository getScannerRepository() {
        return mScannerRepository;
    }


    public MeshRepository getMeshRepository() {
        return mMeshRepository;
    }

    public ProvisionedNodesLiveData getProvisionedNodesLiveData() {
        return mMeshRepository.getProvisionedNodesLiveData();
    }

    public void saveApplicationKeys(final Map<Integer, String> appKeys){
        mMeshRepository.saveApplicationKeys(appKeys);
    }

    public LiveData<Boolean> isConnected() {
        return mMeshRepository.isConnected();
    }

    public void disconnect() {
        mMeshRepository.disconnect();
    }

    public String getNetworkId() {
        return mMeshRepository.getNetworkId();
    }

    public boolean isConenctedToMesh() {
        return mMeshRepository.isConnectedToMesh();
    }

    public void refreshProvisionedNodes() {
        mMeshRepository.refreshProvisionedNodes();
    }

    public void setMeshNode(final ProvisionedMeshNode meshNode) {
        mMeshRepository.setMeshNode(meshNode);
    }

    public void resetMeshNetwork() {
        mMeshRepository.resetMeshNetwork();
    }

    public void refreshProvisioningData() {
        mMeshRepository.refreshProvisioningData();
    }

    public LiveData<byte[]> getConfigurationSrcLiveData() {
        return mMeshRepository.getConfigurationSrc();
    }

    public boolean setConfiguratorSrouce(final byte[] configuratorSource) {
        return mMeshRepository.setConfiguratorSrc(configuratorSource);
    }
}
