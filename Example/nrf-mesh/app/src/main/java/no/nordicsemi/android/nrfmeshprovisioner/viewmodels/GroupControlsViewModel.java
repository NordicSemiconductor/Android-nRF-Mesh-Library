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

import java.util.List;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager;

public class GroupControlsViewModel extends ViewModel {

    private final NrfMeshRepository mNrfMeshRepository;
    private final ScannerRepository mScannerRepository;

    @Inject
    GroupControlsViewModel(final NrfMeshRepository nrfMeshRepository, final ScannerRepository scannerRepository) {
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

    public LiveData<Group> getSelectedGroup() {
        return mNrfMeshRepository.getSelectedGroup();
    }
    /**
     * Returns the provisioned nodes as a live data object.
     */
    public LiveData<List<ProvisionedMeshNode>> getProvisionedNodes() {
        return mNrfMeshRepository.getProvisionedNodes();
    }

    /**
     * Returns if currently connected to the mesh network.
     *
     * @return true if connected and false otherwise
     */
    public LiveData<Boolean> isConnectedToProxy() {
        return mNrfMeshRepository.isConnectedToProxy();
    }

    public MeshNetworkLiveData getMeshNetworkLiveData() {
        return mNrfMeshRepository.getMeshNetworkLiveData();
    }

    public LiveData<MeshMessage> getMeshMessageLiveData() {
        return mNrfMeshRepository.getMeshMessageLiveData();
    }

    /**
     * Set the mesh node to be configured
     *
     * @param meshNode provisioned mesh node
     */
    public void setSelectedMeshNode(final ProvisionedMeshNode meshNode) {
        mNrfMeshRepository.setSelectedMeshNode(meshNode);
    }

    /**
     * Set the element to be configured
     *
     * @param element {@link Element}
     */
    public void setSelectedElement(final Element element) {
        mNrfMeshRepository.setSelectedElement(element);
    }

    /**
     * Get selected model
     */
    public LiveData<MeshModel> getSelectedModel() {
        return mNrfMeshRepository.getSelectedModel();
    }

    /**
     * Set the mesh model to be configured
     *
     * @param model {@link MeshModel}
     */
    public void setSelectedModel(final MeshModel model) {
        mNrfMeshRepository.setSelectedModel(model);
    }
    /**
     * Get selected mesh node
     *
     * @return {@link ExtendedMeshNode} element
     */
    public LiveData<ProvisionedMeshNode> getSelectedMeshNode() {
        return mNrfMeshRepository.getSelectedMeshNode();
    }
}
