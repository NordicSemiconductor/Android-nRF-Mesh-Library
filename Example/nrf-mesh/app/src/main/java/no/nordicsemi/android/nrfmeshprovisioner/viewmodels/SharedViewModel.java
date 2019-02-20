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
import android.net.Uri;

import java.util.List;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

public class SharedViewModel extends ViewModel {

    private final ScannerRepository mScannerRepository;
    private final NrfMeshRepository nRFMeshRepository;

    @Inject
    SharedViewModel(final ScannerRepository scannerRepository, final NrfMeshRepository nrfMeshRepository) {
        mScannerRepository = scannerRepository;
        nRFMeshRepository = nrfMeshRepository;
        scannerRepository.registerBroadcastReceivers();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        nRFMeshRepository.disconnect();
        mScannerRepository.unregisterBroadcastReceivers();
    }

    public MeshManagerApi getMeshManagerApi(){
        return nRFMeshRepository.getMeshManagerApi();
    }

    public void importMeshNetwork(final Uri uri){
        nRFMeshRepository.importMeshNetwork(uri);
    }

    public MeshNetworkLiveData getMeshNetworkLiveData() {
        return nRFMeshRepository.getMeshNetworkLiveData();
    }

    public LiveData<List<Group>> getGroups(){
        return nRFMeshRepository.getGroups();
    }

    /**
     * Returns an instance of the scanner repository
     */
    public ScannerRepository getScannerRepository() {
        return mScannerRepository;
    }

    public NrfMeshRepository getnRFMeshRepository() {
        return nRFMeshRepository;
    }

    /**
     * Returns the provisioned nodes as a live data object.
     */
    public LiveData<List<ProvisionedMeshNode>> getProvisionedNodes() {
        return nRFMeshRepository.getProvisionedNodes();
    }

    /**
     * Returns if currently connected to a peripheral device.
     *
     * @return true if connected and false otherwise
     */
    public LiveData<Boolean> isConnected() {
        return nRFMeshRepository.isConnected();
    }

    /**
     * Disconnect from peripheral
     */
    public void disconnect() {
        nRFMeshRepository.disconnect();
    }

    /**
     * Returns if currently connected to the mesh network.
     *
     * @return true if connected and false otherwise
     */
    public LiveData<Boolean> isConnectedToProxy() {
        return nRFMeshRepository.isConnectedToProxy();
    }

    /**
     * Set the mesh node to be configured
     *
     * @param meshNode provisioned mesh node
     */
    public void setSelectedMeshNode(final ProvisionedMeshNode meshNode) {
        nRFMeshRepository.setSelectedMeshNode(meshNode);
    }

    /**
     * Reset mesh network
     */
    public void resetMeshNetwork() {
        nRFMeshRepository.resetMeshNetwork();
    }

    public LiveData<String> getNetworkLoadState(){
        return nRFMeshRepository.getNetworkLoadState();
    }
    public LiveData<String> getNetworkExportState(){
        return nRFMeshRepository.getNetworkExportState();
    }

    public LiveData<Integer> getConnectedMeshNodeAddress(){
        return nRFMeshRepository.getConnectedMeshNodeAddress();
    }

    public void setSelectedGroup(final int address){
        nRFMeshRepository.setSelectedGroup(address);
    }
}
