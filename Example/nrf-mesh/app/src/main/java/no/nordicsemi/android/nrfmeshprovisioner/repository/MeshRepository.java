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

package no.nordicsemi.android.nrfmeshprovisioner.repository;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;

import java.util.Map;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisionedNodesLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisioningLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.service.MeshService;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;

public class MeshRepository extends BaseMeshRepository {

    @Inject
    public MeshRepository(final Context context) {
        super(context);
    }

    public void stopService(){
        final Intent intent = new Intent(mContext, MeshService.class);
        mContext.stopService(intent);
    }

    @Override
    public void onConnectionStateChanged(final String connectionState) {

    }

    @Override
    public void isDeviceConnected(final boolean isConnected) {
        mIsConnected.postValue(isConnected);
    }

    @Override
    public void onDeviceReady(final boolean isReady) {
        mOnDeviceReady.postValue(isReady);
    }

    @Override
    public void isReconnecting(final boolean isReconnecting) {
        mIsReconnecting.postValue(isReconnecting);
    }

    @Override
    public void onProvisioningStateChanged(final Intent intent) {
        //Do nothing as we don't do any provisioning related tasks here
    }

    @Override
    public void onConfigurationMessageStateChanged(final Intent intent) {
        //Do nothing as we don't do any configuration related tasks here
    }

    /**
     * Disconnect from peripheral
     */
    public void disconnect() {
        mBinder.disconnect();
    }

    public ProvisioningLiveData getProvisioningData(){
        return mProvisioningLiveData;
    }

    /**
     * Returns the {@link ProvisionedNodesLiveData} object
     * @return provisioned nodes live data
     */
    public ProvisionedNodesLiveData getProvisionedNodesLiveData(){
        return mProvisionedNodesLiveData;
    }

    public String getNetworkId() {
        return mBinder.getNetworkId();
    }

    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    public boolean isConnectedToMesh() {
        return mBinder != null && mBinder.isConnected();
    }

    public void saveApplicationKeys(final Map<Integer, String> appKeys) {
        Utils.saveApplicationKeys(mContext, appKeys);
    }

    public void refreshProvisionedNodes(){
        if(mBinder != null) {
            Map<Integer, ProvisionedMeshNode> nodes = mBinder.getProvisionedNodes();
            mProvisionedNodesLiveData.updateProvisionedNodes(nodes);
        }
    }

    public boolean setConfiguratorSrc(final byte[] configuratorSrc) {
        if(mMeshManagerApi != null) {
            if(mMeshManagerApi.setConfiguratorSrc(configuratorSrc)) {
                mConfigurationSrc.postValue(configuratorSrc);
                return true;
            }
        }
        return false;
    }
}
