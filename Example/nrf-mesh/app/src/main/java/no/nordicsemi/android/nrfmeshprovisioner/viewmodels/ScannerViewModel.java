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
import android.content.Context;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager;

public class ScannerViewModel extends ViewModel {

    private final NrfMeshRepository mNrfMeshRepository;
    private final ScannerRepository mScannerRepository;

    @Inject
    ScannerViewModel(final NrfMeshRepository nrfMeshRepository, final ScannerRepository scannerRepository) {
        this.mNrfMeshRepository = nrfMeshRepository;
        this.mScannerRepository = scannerRepository;
        scannerRepository.registerBroadcastReceivers();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mScannerRepository.unregisterBroadcastReceivers();
    }

    public LiveData<Void> isDeviceReady() {
        return mNrfMeshRepository.isDeviceReady();
    }

    public LiveData<String> getConnectionState() {
        return mNrfMeshRepository.getConnectionState();
    }

    public LiveData<Boolean> isConnected() {
        return mNrfMeshRepository.isConnected();
    }

    public void connect(final Context context, final ExtendedBluetoothDevice device, final boolean connectToNetwork) {
        mNrfMeshRepository.connect(context, device, connectToNetwork);
    }

    public void disconnect() {
        mNrfMeshRepository.disconnect();
    }

    public NrfMeshRepository getNrfMeshRepository() {
        return mNrfMeshRepository;
    }

    public BleMeshManager getBleMeshManager() {
        return mNrfMeshRepository.getBleMeshManager();
    }

    public MeshManagerApi getMeshManagerApi() {
        return mNrfMeshRepository.getMeshManagerApi();
    }

    /**
     * Returns an instance of the scanner repository
     */
    public ScannerRepository getScannerRepository() {
        return mScannerRepository;
    }
}
