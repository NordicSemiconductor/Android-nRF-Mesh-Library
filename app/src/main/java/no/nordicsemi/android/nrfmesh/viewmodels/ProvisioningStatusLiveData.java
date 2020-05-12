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

package no.nordicsemi.android.nrfmesh.viewmodels;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.utils.ProvisionerStates;

public class ProvisioningStatusLiveData extends LiveData<ProvisioningStatusLiveData> {

    private final ArrayList<ProvisionerProgress> mProvisioningProgress = new ArrayList<>();

    public void clear() {
        mProvisioningProgress.clear();
        postValue(this);
    }

    public ArrayList<ProvisionerProgress> getStateList() {
        return mProvisioningProgress;
    }


    public ProvisionerProgress getProvisionerProgress() {
        if (mProvisioningProgress.size() == 0)
            return null;
        return mProvisioningProgress.get(mProvisioningProgress.size() - 1);
    }

    void onMeshNodeStateUpdated(final ProvisionerStates state) {
        final ProvisionerProgress provisioningProgress;
        switch (state) {
            case PROVISIONING_INVITE:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning invite...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_CAPABILITIES:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning capabilities received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_START:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning start...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_PUBLIC_KEY_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning public key...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_PUBLIC_KEY_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning public key received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_AUTHENTICATION_STATIC_OOB_WAITING:
            case PROVISIONING_AUTHENTICATION_OUTPUT_OOB_WAITING:
            case PROVISIONING_AUTHENTICATION_INPUT_OOB_WAITING:
                provisioningProgress = new ProvisionerProgress(state, "Waiting for user authentication input...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_AUTHENTICATION_INPUT_ENTERED:
                provisioningProgress = new ProvisionerProgress(state, "OOB authentication entered...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_INPUT_COMPLETE:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning input complete received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_CONFIRMATION_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning confirmation...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_CONFIRMATION_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning confirmation received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_RANDOM_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning random...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_RANDOM_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning random received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_DATA_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning data...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_COMPLETE:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning complete received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_FAILED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning failed received...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
            default:
                break;
            case COMPOSITION_DATA_GET_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending composition data get...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case COMPOSITION_DATA_STATUS_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Composition data status received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case SENDING_DEFAULT_TTL_GET:
                provisioningProgress = new ProvisionerProgress(state, "Sending default TLL get...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case DEFAULT_TTL_STATUS_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Default TTL status received...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case SENDING_APP_KEY_ADD:
                provisioningProgress = new ProvisionerProgress(state, "Sending app key add...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case APP_KEY_STATUS_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "App key status received...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case SENDING_NETWORK_TRANSMIT_SET:
                provisioningProgress = new ProvisionerProgress(state, "Sending network transmit set...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case NETWORK_TRANSMIT_STATUS_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Network transmit status received...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case SENDING_BLOCK_ACKNOWLEDGEMENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending block acknowledgements", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case BLOCK_ACKNOWLEDGEMENT_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Receiving block acknowledgements", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONER_UNASSIGNED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioner unassigned...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
        }
        postValue(this);
    }
}
