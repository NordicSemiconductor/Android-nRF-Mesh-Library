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
import no.nordicsemi.android.nrfmeshprovisioner.ConfigurationServerActivity;

/**
 * View Model class for {@link ConfigurationServerActivity}
 */
public class ModelConfigurationViewModel extends ViewModel {

    private final NrfMeshRepository mNrfMeshRepository;

    @Inject
    ModelConfigurationViewModel(final NrfMeshRepository nrfMeshRepository) {
        this.mNrfMeshRepository = nrfMeshRepository;
    }

    public LiveData<Boolean> isConnectedToProxy() {
        return mNrfMeshRepository.isConnectedToProxy();
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

    /**
     * Returns an observable live data object containing the mesh message received
     *
     * @return {@link MeshMessageLiveData}
     */
    public MeshMessageLiveData getMeshMessageLiveData() {
        return mNrfMeshRepository.getMeshMessageLiveData();
    }

    /**
     * Get selected mesh node
     *
     * @return {@link ExtendedMeshNode} element
     */
    public ExtendedMeshNode getSelectedMeshNode() {
        return mNrfMeshRepository.getSelectedMeshNode();
    }

    /**
     * Get selected element
     *
     * @return {@link ExtendedElement} element
     */
    public ExtendedElement getSelectedElement() {
        return mNrfMeshRepository.getSelectedElement();
    }

    /**
     * Get selected model
     *
     * @return {@link ExtendedMeshModel} element
     */
    public ExtendedMeshModel getSelectedModel() {
        return mNrfMeshRepository.getSelectedModel();
    }

    /**
     * Returns an observable live data object containing the transaction status.
     *
     * @return {@link TransactionStatusLiveData}
     */
    public TransactionStatusLiveData getTransactionStatus() {
        return mNrfMeshRepository.getTransactionStatusLiveData();
    }
}
