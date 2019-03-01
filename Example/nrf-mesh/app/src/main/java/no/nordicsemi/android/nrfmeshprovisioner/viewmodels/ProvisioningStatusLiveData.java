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

import java.util.ArrayList;

import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningState;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class ProvisioningStatusLiveData extends LiveData<ProvisioningStatusLiveData> {

    private final ArrayList<ProvisionerProgress> mProvisioningProgress = new ArrayList<>();

    public void clear() {
        mProvisioningProgress.clear();
        postValue(this);
    }

    public enum ProvisioningLiveDataState {
        PROVISIONING_INVITE(0),
        PROVISIONING_CAPABILITIES(1),
        PROVISIONING_START(2),
        PROVISIONING_PUBLIC_KEY_SENT(3),
        PROVISIONING_PUBLIC_KEY_RECEIVED(4),
        PROVISIONING_AUTHENTICATION_INPUT_OOB_WAITING(5),
        PROVISIONING_AUTHENTICATION_OUTPUT_OOB_WAITING(6),
        PROVISIONING_AUTHENTICATION_STATIC_OOB_WAITING(7),
        PROVISIONING_AUTHENTICATION_INPUT_ENTERED(8),
        PROVISIONING_INPUT_COMPLETE(9),
        PROVISIONING_CONFIRMATION_SENT(10),
        PROVISIONING_CONFIRMATION_RECEIVED(11),
        PROVISIONING_RANDOM_SENT(12),
        PROVISIONING_RANDOM_RECEIVED(13),
        PROVISIONING_DATA_SENT(14),
        PROVISIONING_COMPLETE(15),
        PROVISIONING_FAILED(16),
        COMPOSITION_DATA_GET_SENT(17),
        COMPOSITION_DATA_STATUS_RECEIVED(18),
        SENDING_BLOCK_ACKNOWLEDGEMENT(19),
        SENDING_APP_KEY_ADD(20),
        BLOCK_ACKNOWLEDGEMENT_RECEIVED(21),
        APP_KEY_STATUS_RECEIVED(22);

        private final int state;

        ProvisioningLiveDataState(final int state) {
            this.state = state;
        }

        int getState() {
            return state;
        }

        static ProvisioningLiveDataState fromStatusCode(final int statusCode) {
            for (ProvisioningLiveDataState state : ProvisioningLiveDataState.values()) {
                if (state.getState() == statusCode) {
                    return state;
                }
            }
            throw new IllegalStateException("Invalid state");
        }
    }

    public ArrayList<ProvisionerProgress> getStateList() {
        return mProvisioningProgress;
    }


    public ProvisionerProgress getProvisionerProgress() {
        if (mProvisioningProgress.size() == 0)
            return null;
        return mProvisioningProgress.get(mProvisioningProgress.size() - 1);
    }

    void onMeshNodeStateUpdated(final ProvisioningState.States provisionerState) {
        final ProvisionerProgress provisioningProgress;
        final ProvisioningLiveDataState state = ProvisioningLiveDataState.fromStatusCode(provisionerState.getState());
        switch (provisionerState) {
            case PROVISIONING_INVITE:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning invite...", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_CAPABILITIES:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning capabilities received...", R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_START:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning start...", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_PUBLIC_KEY_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning public key...", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_PUBLIC_KEY_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning public key received...", R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_AUTHENTICATION_STATIC_OOB_WAITING:
            case PROVISIONING_AUTHENTICATION_OUTPUT_OOB_WAITING:
            case PROVISIONING_AUTHENTICATION_INPUT_OOB_WAITING:
                provisioningProgress = new ProvisionerProgress(state, "Waiting for user authentication input...", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_AUTHENTICATION_INPUT_ENTERED:
                provisioningProgress = new ProvisionerProgress(state, "OOB authentication entered...", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_INPUT_COMPLETE:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning input complete received...", R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_CONFIRMATION_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning confirmation...", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_CONFIRMATION_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning confirmation received...", R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_RANDOM_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning random...", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_RANDOM_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning random received...", R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_DATA_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning data...", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_COMPLETE:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning complete received...", R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_FAILED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning failed received...", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
            default:
                break;
            case COMPOSITION_DATA_GET_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending composition data get...", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case COMPOSITION_DATA_STATUS_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Composition data status received...", R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case SENDING_BLOCK_ACKNOWLEDGEMENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending block acknowledgements", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case BLOCK_ACKNOWLEDGEMENT_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Receiving block acknowledgements", R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case SENDING_APP_KEY_ADD:
                provisioningProgress = new ProvisionerProgress(state, "Sending app key add...", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case APP_KEY_STATUS_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "App key status received...", R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
        }
        postValue(this);
    }
}
