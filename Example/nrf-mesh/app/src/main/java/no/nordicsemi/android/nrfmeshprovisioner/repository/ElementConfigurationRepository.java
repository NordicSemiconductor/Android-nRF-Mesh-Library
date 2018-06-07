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

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisioningStateLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ExtendedMeshNode;

public class ElementConfigurationRepository extends BaseMeshRepository {

    private static final String TAG = ElementConfigurationRepository.class.getSimpleName();

    @Inject
    public ElementConfigurationRepository(final Context context){
        super(context);
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

    }

    @Override
    public void isReconnecting(final boolean isReconnecting) {

    }

    @Override
    public void onProvisioningStateChanged(final Intent intent) {

    }

    @Override
    public void onConfigurationStateChanged(final Intent intent) {

    }

    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    public ProvisioningStateLiveData getProvisioningState() {
        return mProvisioningStateLiveData;
    }


    public LiveData<MeshModel> getMeshModel() {
        return mMeshModel;
    }


    public ExtendedMeshNode getExtendedMeshNode() {
        return mExtendedMeshNode;
    }

    @Override
    public void setMeshNode(final ProvisionedMeshNode meshNode) {
        super.setMeshNode(meshNode);
    }

    /**
     * Selects the mesh model to be configured
     * @param meshModel updates the mesh model
     */
    public void selectModel(final MeshModel meshModel) {
        mBinder.setMeshModel(meshModel);
        mMeshModel.postValue(meshModel);
    }

    public void sendGetCompositionData() {
        mBinder.sendCompositionDataGet(mExtendedMeshNode.getMeshNode());
    }

}
