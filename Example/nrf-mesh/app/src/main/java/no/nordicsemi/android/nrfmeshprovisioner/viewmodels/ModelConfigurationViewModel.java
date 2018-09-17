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

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.AppKeyBindStatusLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ConfigModelPublicationStatusLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ConfigModelSubscriptionStatusLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ExtendedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.GenericOnOffStatusUpdate;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.TransactionFailedLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.repository.ModelConfigurationRepository;

public class ModelConfigurationViewModel extends ViewModel {

    private final ModelConfigurationRepository mModelConfigurationRepository;

    @Inject
    ModelConfigurationViewModel(final ModelConfigurationRepository configurationRepository) {
        this.mModelConfigurationRepository = configurationRepository;
        mModelConfigurationRepository.registerBroadcastReceiver();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mModelConfigurationRepository.unbindService();
        mModelConfigurationRepository.unregisterBroadcastReceiver();
    }

    public LiveData<Boolean> isConnected() {
        return mModelConfigurationRepository.isConnected();
    }

    /**
     * Returns the Mesh repository
     *
     * @return mesh repository
     */
    public ModelConfigurationRepository getMeshRepository() {
        return mModelConfigurationRepository;
    }

    /**
     * Returns the selected mesh model
     *
     * @return meshmodel live data object
     */
    public LiveData<MeshModel> getMeshModel() {
        return mModelConfigurationRepository.getMeshModel();
    }

    public ExtendedMeshNode getExtendedMeshNode() {
        return mModelConfigurationRepository.getExtendedMeshNode();
    }

    public void sendBindAppKey(final int appKeyIndex) {
        mModelConfigurationRepository.sendBindAppKey(appKeyIndex);
    }

    public void sendUnbindAppKey(final int appKeyIndex) {
        mModelConfigurationRepository.sendUnbindAppKey(appKeyIndex);
    }

    public LiveData<TransactionFailedLiveData> getTransactionStatus() {
        return mModelConfigurationRepository.getTransactionFailedLiveData();
    }

    public LiveData<AppKeyBindStatusLiveData> getAppKeyBindStatusLiveData() {
        return mModelConfigurationRepository.getAppKeyBindStatus();
    }

    public LiveData<ConfigModelPublicationStatusLiveData> getConfigModelPublicationStatusLiveData() {
        return mModelConfigurationRepository.getConfigModelPublicationStatus();
    }

    public LiveData<ConfigModelSubscriptionStatusLiveData> getConfigModelSubscriptionStatusLiveData() {
        return mModelConfigurationRepository.getConfigModelSubscriptionStatus();
    }

    public void sendConfigModelPublicationSet(final byte[] publishAddress, final int appKeyIndex, final boolean credentialFlag, final int publishTtl,
                                              final int publicationSteps, final int resolution, final int publishRetransmitCount, final int publishRetransmitIntervalSteps) {
        mModelConfigurationRepository.sendConfigModelPublicationSet(publishAddress, appKeyIndex, credentialFlag, publishTtl, publicationSteps, resolution, publishRetransmitCount, publishRetransmitIntervalSteps);
    }
    public void sendConfigModelSubscriptionAdd(final byte[] subscriptionAddress) {
        mModelConfigurationRepository.sendConfigModelSubscriptionAdd(subscriptionAddress);
    }

    public void sendConfigModelSubscriptionDelete(final byte[] subscriptionAddress) {
        mModelConfigurationRepository.sendConfigModelSubscriptionDelete(subscriptionAddress);
    }

    public void setModel(final ProvisionedMeshNode meshNode, final int elementAddress, final int modelId) {
        mModelConfigurationRepository.setModel(meshNode, elementAddress, modelId);
    }

    /**
     * Send generic on off set to mesh node
     *
     * @param node                 mesh node to send generic on off message
     * @param transitionSteps      the number of steps
     * @param transitionResolution the resolution for the number of steps
     * @param delay                message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     * @param state                on off state
     */
    public void sendGenericOnOff(final ProvisionedMeshNode node, final Integer transitionSteps, final Integer transitionResolution, final Integer delay, final boolean state) {
        mModelConfigurationRepository.sendGenericOnOffSet(node, transitionSteps, transitionResolution, delay, state);
    }

    /**
     * Send generic on off get to mesh node
     *
     * @param node mesh node to send generic on off get
     */
    public void sendGenericOnOffGet(final ProvisionedMeshNode node) {
        mModelConfigurationRepository.sendGenericOnOffGet(node);
    }

    public LiveData<GenericOnOffStatusUpdate> getGenericOnOffState() {
        return mModelConfigurationRepository.getGenericOnOffState();
    }

    public LiveData<byte[]> getVendorModelState() {
        return mModelConfigurationRepository.getVendorModelState();
    }

    public void sendVendorModelUnacknowledgedMessage(final ProvisionedMeshNode node, final MeshModel model, final int appKeyIndex, final int opcode, final byte[] parameters){
        mModelConfigurationRepository.sendVendorModelUnacknowledgedMessage(node, model, appKeyIndex, opcode, parameters);
    }

    public void sendVendorModelAcknowledgedMessage(final ProvisionedMeshNode node, final MeshModel model, final int appKeyIndex, final int opcode, final byte[] parameters){
        mModelConfigurationRepository.sendVendorModelAcknowledgedMessage(node, model, appKeyIndex, opcode, parameters);
    }
}
