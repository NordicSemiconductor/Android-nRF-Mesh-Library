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

import no.nordicsemi.android.meshprovisioner.BaseMeshNode;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ExtendedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisionedNodesLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisioningStateLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.repository.MeshProvisionerRepository;

public class MeshProvisionerViewModel extends ViewModel {

    private final MeshProvisionerRepository mMeshProvisionerRepository;
    private final NrfMeshRepository mNrfMeshRepository;

    @Inject
    MeshProvisionerViewModel(final MeshProvisionerRepository mMeshProvisionerRepository, final NrfMeshRepository nrfMeshRepository) {
        this.mMeshProvisionerRepository = mMeshProvisionerRepository;
        this.mNrfMeshRepository = nrfMeshRepository;
        mMeshProvisionerRepository.registerBroadcastReceiver();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mNrfMeshRepository.clearMeshNodeLiveData();
        mMeshProvisionerRepository.unregisterBroadcastReceiver();
        mMeshProvisionerRepository.unbindService();
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

    public LiveData<Boolean> isReconnecting() {
        return mNrfMeshRepository.isReconnecting();
    }

    public boolean isProvisioningComplete() {
        return mNrfMeshRepository.isProvisioningComplete();
    }

    public ProvisioningStateLiveData getProvisioningState() {
        return mMeshProvisionerRepository.getProvisioningState();
    }

    public ProvisioningStatusLiveData getProvisioningStatus() {
        return mNrfMeshRepository.getProvisioningState();
    }

    /**
     * Connect to peripheral
     */
    public void connect(final Context context, final ExtendedBluetoothDevice device, final boolean connectToNetwork) {
        mNrfMeshRepository.connect(context, device, connectToNetwork);
    }

    /**
     * Disconnect from peripheral
     */
    public void disconnect() {
        mNrfMeshRepository.disconnect();
    }

    /**
     * Disconnect from peripheral
     */
    public void disconnect(final Context context) {
        mNrfMeshRepository.disconnect();
    }

    public ExtendedMeshNode getMeshNode() {
        return mMeshProvisionerRepository.getExtendedMeshNode();
    }

    public LiveData<BaseMeshNode> getBaseMeshNode() {
        return mNrfMeshRepository.getExtendedMeshNode();
    }

    public void identifyNode(final String address, final String nodeName) {
        mNrfMeshRepository.getMeshManagerApi().identifyNode(address, nodeName);
    }

    public void startProvisioning(final UnprovisionedMeshNode node) {
        mNrfMeshRepository.getMeshManagerApi().startProvisioning(node);
    }

    public void sendProvisioneePin(final String pin) {
        mMeshProvisionerRepository.confirmProvisioning(pin);
    }

    public ProvisioningSettingsLiveData getProvisioningSettings(){
        return mNrfMeshRepository.getProvisioningSettingsLiveData();
    }

    public LiveData<NetworkInformation> getNetworkInformationLiveData(){
        return mNrfMeshRepository.getNetworkInformationLiveData();
    }

    public ProvisionedNodesLiveData getProvisionedNodes() {
        return mMeshProvisionerRepository.getProvisionedNodesLiveData();
    }

    public void setSelectedAppKey(final int appKeyIndex, final String appkey) {
        mMeshProvisionerRepository.setSelectedAppKey(appKeyIndex, appkey);
    }

    public String getSelectedAppKey() {
        return mMeshProvisionerRepository.getSelectedAppKey();
    }

    public NrfMeshRepository getNrfMeshRepository() {
        return mNrfMeshRepository;
    }

    public BleMeshManager getBleMeshManager() {
        return mNrfMeshRepository.getBleMeshManager();
    }
}
