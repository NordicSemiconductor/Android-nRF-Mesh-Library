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

import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.meshmessagestates.MeshModel;
import no.nordicsemi.android.meshprovisioner.meshmessagestates.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.messages.ConfigModelAppStatus;
import no.nordicsemi.android.meshprovisioner.messages.ConfigModelPublicationStatus;
import no.nordicsemi.android.meshprovisioner.messages.ConfigModelSubscriptionStatus;
import no.nordicsemi.android.meshprovisioner.messages.GenericLevelStatus;
import no.nordicsemi.android.meshprovisioner.messages.GenericOnOffStatus;
import no.nordicsemi.android.meshprovisioner.messages.VendorModelMessageStatus;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.SingleLiveEvent;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.TransactionFailedLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.repository.ModelConfigurationRepository;

public class ModelConfigurationViewModel extends ViewModel {

    private final ModelConfigurationRepository mModelConfigurationRepository;
    private final NrfMeshRepository mNrfMeshRepository;

    @Inject
    ModelConfigurationViewModel(final ModelConfigurationRepository configurationRepository, final NrfMeshRepository nrfMeshRepository) {
        this.mModelConfigurationRepository = configurationRepository;
        this.mNrfMeshRepository = nrfMeshRepository;
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
     */
    public NrfMeshRepository getNrfMeshRepository() {
        return mNrfMeshRepository;
    }

    /**
     * Returns the mesh manager api
     */
    public MeshManagerApi getMeshManagerApi() {
        return mNrfMeshRepository.getMeshManagerApi();
    }

    public MeshMessageLiveData getMeshMessageLiveData() {
        return mNrfMeshRepository.getMeshMessageLiveData();
    }

    public ExtendedMeshNode getSelectedMeshNode(){
        return mNrfMeshRepository.getSelectedMeshNode();
    }

    public ExtendedElement getSelectedElement(){
        return mNrfMeshRepository.getSelectedElement();
    }

    public ExtendedMeshModel getSelectedModel(){
        return mNrfMeshRepository.getSelectedModel();
    }

    public LiveData<TransactionFailedLiveData> getTransactionStatus() {
        return mModelConfigurationRepository.getTransactionFailedLiveData();
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

    public SingleLiveEvent<GenericOnOffStatus> getGenericOnOffState() {
        return mModelConfigurationRepository.getGenericOnOffState();
    }

    public SingleLiveEvent<VendorModelMessageStatus> getVendorModelState() {
        return mModelConfigurationRepository.getVendorModelState();
    }

    public void sendVendorModelUnacknowledgedMessage(final ProvisionedMeshNode node, final MeshModel model, final int appKeyIndex, final int opcode, final byte[] parameters){
        mModelConfigurationRepository.sendVendorModelUnacknowledgedMessage(node, model, appKeyIndex, opcode, parameters);
    }

    public void sendVendorModelAcknowledgedMessage(final ProvisionedMeshNode node, final MeshModel model, final int appKeyIndex, final int opcode, final byte[] parameters){
        mModelConfigurationRepository.sendVendorModelAcknowledgedMessage(node, model, appKeyIndex, opcode, parameters);
    }

    public LiveData<GenericLevelStatus> getGenericLevelState() {
        return mModelConfigurationRepository.getGenericLevelState();
    }

    public void sendGenericLevelGet(final ProvisionedMeshNode node) {
        mModelConfigurationRepository.sendGenericLevelGet(node);
    }

    public void sendGenericLevelSet(final ProvisionedMeshNode node, final int level, final Integer transitionSteps, final Integer transitionResolution, final Integer delay) {
        mModelConfigurationRepository.sendGenericLevelSet(node, level, transitionSteps, transitionResolution, delay);
    }

    public void sendGenericLevelSetUnacknowledged(final ProvisionedMeshNode node, final int level, final Integer transitionSteps, final Integer transitionResolution, final Integer delay) {
        mModelConfigurationRepository.sendGenericLevelSetUnacknowledged(node, level, transitionSteps, transitionResolution, delay);
    }
}
