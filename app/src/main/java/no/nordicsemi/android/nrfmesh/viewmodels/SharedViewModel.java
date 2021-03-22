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

import java.io.OutputStream;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import no.nordicsemi.android.nrfmesh.GroupsFragment;
import no.nordicsemi.android.nrfmesh.NetworkFragment;
import no.nordicsemi.android.nrfmesh.ProxyFilterFragment;
import no.nordicsemi.android.nrfmesh.SettingsFragment;
import no.nordicsemi.android.nrfmesh.utils.NetworkExportUtils;

/**
 * ViewModel for {@link NetworkFragment}, {@link GroupsFragment}, {@link ProxyFilterFragment}, {@link SettingsFragment}
 */
@HiltViewModel
public class SharedViewModel extends BaseViewModel implements NetworkExportUtils.NetworkExportCallbacks {

    private final ScannerRepository mScannerRepository;
    private final SingleLiveEvent<String> networkExportState = new SingleLiveEvent<>();

    @Inject
    SharedViewModel(@NonNull final NrfMeshRepository nrfMeshRepository, @NonNull final ScannerRepository scannerRepository) {
        super(nrfMeshRepository);
        mScannerRepository = scannerRepository;
        scannerRepository.registerBroadcastReceivers();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mNrfMeshRepository.disconnect();
        mScannerRepository.unregisterBroadcastReceivers();
    }

    /**
     * Returns network load state
     */
    public LiveData<String> getNetworkLoadState() {
        return mNrfMeshRepository.getNetworkLoadState();
    }

    public LiveData<String> getNetworkExportState() {
        return networkExportState;
    }

    /**
     * Sets the selected group
     *
     * @param address Address of the group
     */
    public void setSelectedGroup(final int address) {
        mNrfMeshRepository.setSelectedGroup(address);
    }

    public void exportMeshNetwork(@NonNull final OutputStream stream) {
        NetworkExportUtils.exportMeshNetwork(getMeshManagerApi(), stream, this);
    }

    public void exportMeshNetwork() {
        final String fileName = getNetworkLiveData().getNetworkName() + ".json";
        NetworkExportUtils.exportMeshNetwork(getMeshManagerApi(), NrfMeshRepository.EXPORT_PATH, fileName, this);
    }

    @Override
    public void onNetworkExported() {
        networkExportState.postValue(getNetworkLiveData().getMeshNetwork().getMeshName() + " has been successfully exported.");
    }

    @Override
    public void onNetworkExportFailed(@NonNull final String error) {
        networkExportState.postValue(error);
    }
}
