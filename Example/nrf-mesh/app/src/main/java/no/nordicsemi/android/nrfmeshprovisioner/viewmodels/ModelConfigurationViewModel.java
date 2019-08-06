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

import java.util.LinkedList;
import java.util.Queue;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.nrfmeshprovisioner.node.ConfigurationClientActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.ConfigurationServerActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.GenericLevelServerActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.GenericOnOffServerActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.ModelConfigurationActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.VendorModelActivity;

/**
 * Generic View Model class for {@link ConfigurationServerActivity},{@link ConfigurationClientActivity},
 * {@link GenericOnOffServerActivity}, {@link GenericLevelServerActivity}, {@link VendorModelActivity},
 * {@link ModelConfigurationActivity}
 */
public class ModelConfigurationViewModel extends BaseViewModel {

    private Queue<MeshMessage> messageQueue = new LinkedList<>();

    @Inject
    ModelConfigurationViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mNrfMeshRepository.clearTransactionStatus();
        messageQueue.clear();
    }

    public Queue<MeshMessage> getMessageQueue() {
        return messageQueue;
    }

    public void removeMessage() {
        if (!messageQueue.isEmpty())
            messageQueue.remove();
    }

    public boolean isActivityVisibile() {
        return isActivityVisibile;
    }

    public void setActivityVisible(final boolean visible){
        isActivityVisibile = visible;
    }
}
