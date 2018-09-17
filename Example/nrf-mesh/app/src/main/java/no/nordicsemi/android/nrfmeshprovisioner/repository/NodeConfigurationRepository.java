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
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.MeshNodeStates;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_APP_KEY_INDEX;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_CONFIGURATION_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_IS_SUCCESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_NET_KEY_INDEX;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_STATUS;

public class NodeConfigurationRepository extends BaseMeshRepository {

    private static final String TAG = NodeConfigurationRepository.class.getSimpleName();

    @Inject
    public NodeConfigurationRepository(final Context context){
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
    public void onConfigurationMessageStateChanged(final Intent intent) {
        handleConfigurationStates(intent);
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
        mBinder.sendCompositionDataGet((ProvisionedMeshNode) mExtendedMeshNode.getMeshNode());
    }

    private void handleConfigurationStates(final Intent intent){
        final int state = intent.getExtras().getInt(EXTRA_CONFIGURATION_STATE);
        final MeshNodeStates.MeshNodeStatus status = MeshNodeStates.MeshNodeStatus.fromStatusCode(state);
        final ProvisionedMeshNode node = (ProvisionedMeshNode) mBinder.getMeshNode();
        switch (status) {
            case COMPOSITION_DATA_GET_SENT:
                break;
            case COMPOSITION_DATA_STATUS_RECEIVED:
                //Update the live data upon receiving a broadcast
                mCompositionDataStatus.onStatusChanged(true);
                mExtendedMeshNode.updateMeshNode(node);
                break;
            case SENDING_BLOCK_ACKNOWLEDGEMENT:
                break;
            case BLOCK_ACKNOWLEDGEMENT_RECEIVED:
                break;
            case SENDING_APP_KEY_ADD:
                break;
            case APP_KEY_STATUS_RECEIVED:
                if(intent.getExtras() != null) {
                    final boolean success = intent.getExtras().getBoolean(EXTRA_IS_SUCCESS);
                    final int statusCode = intent.getExtras().getInt(EXTRA_STATUS);
                    final int netKeyIndex = intent.getExtras().getInt(EXTRA_NET_KEY_INDEX);
                    final int appKeyIndex = intent.getExtras().getInt(EXTRA_APP_KEY_INDEX);
                    mAppKeyStatus.onStatusChanged(success, statusCode, netKeyIndex, appKeyIndex);
                }
                break;
            case NODE_RESET_STATUS_RECEIVED:
                break;
        }

        //Update the live data upon receiving a broadcast
        mExtendedMeshNode.updateMeshNode(node);
    }

    public void resetMeshNode(final ProvisionedMeshNode provisionedMeshNode) {
        mBinder.resetMeshNode(provisionedMeshNode);
    }

    @Override
    protected void onTransactionStateReceived(final Intent intent) {
        super.onTransactionStateReceived(intent);
    }
}
