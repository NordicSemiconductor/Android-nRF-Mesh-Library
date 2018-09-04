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
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.util.Map;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.states.UnprovisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ExtendedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisionedNodesLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisioningStateLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.service.MeshService;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.MeshNodeStates;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_CONNECT_TO_UNPROVISIONED_NODE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_APP_KEY_INDEX;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_CONFIGURATION_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DATA;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DEVICE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_ELEMENT_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_IS_SUCCESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_MODEL_ID;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_PROVISIONING_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_PUBLISH_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_STATUS;

public class MeshProvisionerRepository extends BaseMeshRepository {

    private static final String TAG = MeshProvisionerRepository.class.getSimpleName();


    @Inject
    public MeshProvisionerRepository(final Context context){
        super(context);
        mExtendedMeshNode = new ExtendedMeshNode(new UnprovisionedMeshNode());
    }

    /**
     * Returns the {@link ProvisionedNodesLiveData} object
     * @return provisioned nodes live data
     */
    public ProvisionedNodesLiveData getProvisionedNodesLiveData(){
        return mProvisionedNodesLiveData;
    }

    public LiveData<Boolean> isDeviceReady() {
        return mOnDeviceReady;
    }

    public LiveData<String> getConnectionState() {
        return mConnectionState;
    }

    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    public LiveData<Boolean> isReconnecting() {
        return mIsReconnecting;
    }

    public boolean isProvisioningComplete() {
        return mIsProvisioningComplete;
    }

    public ProvisioningStateLiveData getProvisioningState() {
        return mProvisioningStateLiveData;
    }

    public ExtendedMeshNode getExtendedMeshNode() {
        return mExtendedMeshNode.getValue();
    }

    @Override
    public void onConnectionStateChanged(final String connectionState) {
        mConnectionState.postValue(connectionState);
    }

    @Override
    public void isDeviceConnected(final boolean isConnected) {
        mIsConnected.postValue(isConnected);
        if(isConnected) {
            mIsReconnecting.postValue(false);
        }
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
        handleProvisioningStates(intent);
    }

    @Override
    public void onConfigurationMessageStateChanged(final Intent intent) {
        handleConfigurationStates(intent);
    }

    private void handleProvisioningStates(final Intent intent){
        final int provisionerState = intent.getExtras().getInt(EXTRA_PROVISIONING_STATE);
        final MeshNodeStates.MeshNodeStatus status = MeshNodeStates.MeshNodeStatus.fromStatusCode(provisionerState);
        switch (status) {
            case PROVISIONING_INVITE:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_CAPABILITIES:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                mExtendedMeshNode.updateMeshNode(mBinder.getMeshNode());
                break;
            case PROVISIONING_START:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_PUBLIC_KEY_SENT:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_PUBLIC_KEY_RECEIVED:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_AUTHENTICATION_INPUT_WAITING:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_AUTHENTICATION_INPUT_ENTERED:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_INPUT_COMPLETE:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_CONFIRMATION_SENT:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_CONFIRMATION_RECEIVED:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_RANDOM_SENT:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_RANDOM_RECEIVED:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_DATA_SENT:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_COMPLETE:
                mIsProvisioningComplete = true;
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;
            case PROVISIONING_FAILED:
                final int statusCode = intent.getIntExtra(EXTRA_DATA, 7);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState, statusCode);
                break;
            default:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, provisionerState);
                break;

        }
    }

    private void handleConfigurationStates(final Intent intent){
        final int state = intent.getExtras().getInt(EXTRA_CONFIGURATION_STATE);
        final MeshNodeStates.MeshNodeStatus status = MeshNodeStates.MeshNodeStatus.fromStatusCode(state);
        final ProvisionedMeshNode node = (ProvisionedMeshNode) mBinder.getMeshNode();
        switch (status) {
            case COMPOSITION_DATA_GET_SENT:
                mProvisioningStateLiveData.onMeshNodeStateUpdated(mContext, state);
                mExtendedMeshNode.updateMeshNode(node);
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
        }
    }

    /**
     * Connect to peripheral.
     * We use the start service with the bluetooth device as an extra within the intent in case the service is not bound yet.
     * @param device bluetooth device
     */
    public void connect(final ExtendedBluetoothDevice device) {
        final Intent intent = new Intent(mContext, MeshService.class);
        intent.setAction(ACTION_CONNECT_TO_UNPROVISIONED_NODE);
        intent.putExtra(EXTRA_DEVICE, device);
        mContext.startService(intent);
    }

    public void identifyNode(final String nodeName){
        mBinder.identifyNode(nodeName);
    }

    public void startProvisioning() {
        mBinder.startProvisioning();
    }

    public void confirmProvisioning(final String pin) {
        mBinder.confirmProvisioning(pin);
    }

    public BluetoothDevice getBluetoothDevice() {
        return mBinder.getBluetoothDevice();
    }

    public void saveApplicationKeys(final Map<Integer, String> appKeys) {
        Utils.saveApplicationKeys(mContext, appKeys);
    }

}
