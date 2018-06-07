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

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ExtendedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.MeshNodeStates;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_APP_KEY_INDEX;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_CONFIGURATION_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_ELEMENT_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_IS_SUCCESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_MODEL_ID;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_PUBLISH_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_STATUS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_SUBSCRIPTION_ADDRESS;

public class ModelConfigurationRepository extends BaseMeshRepository {

    private static final String TAG = ModelConfigurationRepository.class.getSimpleName();

    public ModelConfigurationRepository(final Context context) {
        super(context);
    }

    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    @Override
    public void onConnectionStateChanged(final String connectionState) {

    }

    @Override
    public void isDeviceConnected(final boolean isConnected) {

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
        handleConfigurationStates(intent);
    }

    private void handleConfigurationStates(final Intent intent){
        final int state = intent.getExtras().getInt(EXTRA_CONFIGURATION_STATE);
        final MeshNodeStates.MeshNodeStatus status = MeshNodeStates.MeshNodeStatus.fromStatusCode(state);
        final ProvisionedMeshNode node = mBinder.getMeshNode();
        final MeshModel model = mBinder.getMeshModel();
        switch (status) {
            case COMPOSITION_DATA_GET_SENT:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state);
                mExtendedMeshNode = new ExtendedMeshNode(node);
                break;
            case COMPOSITION_DATA_STATUS_RECEIVED:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state);
                mExtendedMeshNode.updateMeshNode(node);
                break;
            case SENDING_BLOCK_ACKNOWLEDGEMENT:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state);
                mExtendedMeshNode.updateMeshNode(node);
                break;
            case BLOCK_ACKNOWLEDGEMENT_RECEIVED:
                mExtendedMeshNode.updateMeshNode(node);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state);
                break;
            case SENDING_APP_KEY_ADD:
                mExtendedMeshNode.updateMeshNode(node);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state);
                break;
            case APP_KEY_STATUS_RECEIVED:
                if(intent.getExtras() != null) {
                    final int statusCode = intent.getExtras().getInt(EXTRA_STATUS);
                    mExtendedMeshNode.updateMeshNode(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state, statusCode);
                }
                break;
            case APP_BIND_SENT:
                break;
            case APP_BIND_STATUS_RECEIVED:
                if (intent.getExtras() != null) {
                    final boolean success = intent.getExtras().getBoolean(EXTRA_IS_SUCCESS);
                    final int statusCode = intent.getExtras().getInt(EXTRA_STATUS);
                    final int elementAddress = intent.getExtras().getInt(EXTRA_ELEMENT_ADDRESS);
                    final int appKeyIndex = intent.getExtras().getInt(EXTRA_APP_KEY_INDEX);
                    final int modelId = intent.getExtras().getInt(EXTRA_MODEL_ID);
                    mExtendedMeshNode.updateMeshNode(node);
                    mAppKeyBindStatus.onStatusChanged(success, statusCode, elementAddress, appKeyIndex, modelId);
                }
                break;
            case PUBLISH_ADDRESS_SET_SENT:
                break;
            case PUBLISH_ADDRESS_STATUS_RECEIVED:
                if (intent.getExtras() != null) {
                    final boolean success = intent.getExtras().getBoolean(EXTRA_IS_SUCCESS);
                    final int statusCode = intent.getExtras().getInt(EXTRA_STATUS);
                    final byte[] elementAddress = intent.getExtras().getByteArray(EXTRA_ELEMENT_ADDRESS);
                    final byte[] publishAddress = intent.getExtras().getByteArray(EXTRA_PUBLISH_ADDRESS);
                    final int modelId = intent.getExtras().getInt(EXTRA_MODEL_ID);
                    mExtendedMeshNode.updateMeshNode(node);
                    mConfigModelPublicationStatus.onStatusChanged(success, statusCode, elementAddress, publishAddress, modelId);
                }
                break;
            case SUBSCRIPTION_ADD_SENT:
                break;
            case SUBSCRIPTION_STATUS_RECEIVED:
                if (intent.getExtras() != null) {
                    final boolean success = intent.getExtras().getBoolean(EXTRA_IS_SUCCESS);
                    final int statusCode = intent.getExtras().getInt(EXTRA_STATUS);
                    final byte[] elementAddress = intent.getExtras().getByteArray(EXTRA_ELEMENT_ADDRESS);
                    final byte[] subscriptionAddress = intent.getExtras().getByteArray(EXTRA_SUBSCRIPTION_ADDRESS);
                    final int modelId = intent.getExtras().getInt(EXTRA_MODEL_ID);
                    mExtendedMeshNode.updateMeshNode(node);
                    mMeshModel.postValue(model);
                    mConfigModelSubscriptionStatus.onStatusChanged(success, statusCode, elementAddress, subscriptionAddress, modelId);
                }
                break;
        }
    }

    @Override
    public void setElement(final Element element) {
        super.setElement(element);
    }

    @Override
    public void setModel(final ProvisionedMeshNode node, final int elementAddress, final int modelId) {
        super.setModel(node, elementAddress, modelId);
    }

    /**
     * Binds appkey to model
     * @param appKeyIndex index of the application key that has already been added to the mesh node
     */
    public void bindAppKey(final int appKeyIndex) {
        mBinder.sendBindAppKey(mExtendedMeshNode.getMeshNode(), mElement.getValue().getElementAddress(), mMeshModel.getValue(), appKeyIndex);
    }

    public void sendConfigModelPublishAddressSet(final byte[] publishAddress) {
        final ProvisionedMeshNode node = mExtendedMeshNode.getMeshNode();
        final Element element = mElement.getValue();
        final MeshModel model = mMeshModel.getValue();
        final int appKeyIndex = 0;//model.getBoundAppKeyIndexes().get(0);
        mBinder.sendConfigModelPublishAddressSet(node, element, model, appKeyIndex, publishAddress);
    }

    public void sendConfigModelSubscriptionAdd(final byte[] subscriptionAddress) {
        final ProvisionedMeshNode node = mExtendedMeshNode.getMeshNode();
        final Element element = mElement.getValue();
        final MeshModel model = mMeshModel.getValue();
        mBinder.sendConfigModelSubscriptionAdd(node, element, model, subscriptionAddress);
    }

    public void sendConfigModelSubscriptionDelete(final byte[] subscriptionAddress) {
        final ProvisionedMeshNode node = mExtendedMeshNode.getMeshNode();
        final Element element = mElement.getValue();
        final MeshModel model = mMeshModel.getValue();
        mBinder.sendConfigModelSubscriptionDelete(node, element, model, subscriptionAddress);
    }
}
