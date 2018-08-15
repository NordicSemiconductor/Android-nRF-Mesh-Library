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

package no.nordicsemi.android.nrfmeshprovisioner.livedata;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import java.util.ArrayList;

import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.utils.ProvisioningProgress;

public class ProvisioningStateLiveData extends LiveData<ProvisioningStateLiveData> {

    private final ArrayList<ProvisioningProgress> mProvisioningProgress = new ArrayList<>();
    private ProvisioningProgress provisionerState;

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
        PROVISIONING_AUTHENTICATION_INPUT_WAITING(5),
        PROVISIONING_AUTHENTICATION_INPUT_ENTERED(6),
        PROVISIONING_INPUT_COMPLETE(7),
        PROVISIONING_CONFIRMATION_SENT(8),
        PROVISIONING_CONFIRMATION_RECEIVED(9),
        PROVISIONING_RANDOM_SENT(10),
        PROVISIONING_RANDOM_RECEIVED(11),
        PROVISIONING_DATA_SENT(12),
        PROVISIONING_COMPLETE(13),
        PROVISIONING_FAILED(14),
        COMPOSITION_DATA_GET_SENT(15),
        COMPOSITION_DATA_STATUS_RECEIVED(16),
        SENDING_BLOCK_ACKNOWLEDGEMENT(17),
        SENDING_APP_KEY_ADD(18),
        BLOCK_ACKNOWLEDGEMENT_RECEIVED(19),
        APP_KEY_STATUS_RECEIVED(20),
        APP_BIND_SENT(21),
        APP_BIND_STATUS_RECEIVED(22),
        PUBLISH_ADDRESS_SET_SENT(23),
        PUBLISH_ADDRESS_STATUS_RECEIVED(24);

        private int state;

        ProvisioningLiveDataState(final int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public static ProvisioningLiveDataState fromStatusCode(final int statusCode){
            for(ProvisioningLiveDataState state : ProvisioningLiveDataState.values()){
                if(state.getState() == statusCode){
                    return state;
                }
            }
            throw new IllegalStateException("Invalid state");
        }
    }

    public ArrayList<ProvisioningProgress> getStateList(){
        return mProvisioningProgress;
    }


    public ProvisioningProgress getProvisionerProgress(){
        if(mProvisioningProgress.size() == 0)
            return null;
        return mProvisioningProgress.get(mProvisioningProgress.size() - 1);
    }

    public void onMeshNodeStateUpdated(final Context context, final int provisionerState) {
        final ProvisioningProgress provisioningProgress;
        final ProvisioningLiveDataState state = ProvisioningLiveDataState.fromStatusCode(provisionerState);
        switch (state){
            case PROVISIONING_INVITE:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.sending_prov_invite), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_CAPABILITIES:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.prov_capabilities_received), R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_START:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.sending_prov_start), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_PUBLIC_KEY_SENT:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.sending_prov_public_key), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_PUBLIC_KEY_RECEIVED:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.prov_public_key_received), R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_AUTHENTICATION_INPUT_WAITING:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.prov_user_authentication_waiting), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_AUTHENTICATION_INPUT_ENTERED:
                break;
            case PROVISIONING_INPUT_COMPLETE:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.sending_prov_input_complete), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_CONFIRMATION_SENT:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.sending_prov_confirmation), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_CONFIRMATION_RECEIVED:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.prov_confirmation_received), R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_RANDOM_SENT:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.sending_prov_random), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_RANDOM_RECEIVED:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.provisionee_random_received), R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_DATA_SENT:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.sending_prov_data), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_COMPLETE:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.prov_complete_received), R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            /*case PROVISIONING_FAILED:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.prov_failed_received), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);*/
            default:
                break;
            case COMPOSITION_DATA_GET_SENT:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.sending_prov_composition_data_get), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case COMPOSITION_DATA_STATUS_RECEIVED:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.prov_composition_data_status_received), R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case SENDING_BLOCK_ACKNOWLEDGEMENT:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.prov_sending_block_acknowledgement), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case BLOCK_ACKNOWLEDGEMENT_RECEIVED:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.prov_block_acknowledgement_received), R.drawable.ic_arrow_back_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case SENDING_APP_KEY_ADD:
                provisioningProgress = new ProvisioningProgress(state, context.getString(R.string.prov_sending_app_key_add), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
        }
        postValue(this);
    }

    public void onMeshNodeStateUpdated(final Context context, final int provisionerState, final int statusCode) {
        final ProvisioningProgress provisioningProgress;
        final ProvisioningLiveDataState state = ProvisioningLiveDataState.fromStatusCode(provisionerState);
        switch (state) {
            case PROVISIONING_FAILED:
                provisioningProgress = new ProvisioningProgress(state, statusCode, context.getString(R.string.prov_failed_received), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case APP_KEY_STATUS_RECEIVED:
                provisioningProgress = new ProvisioningProgress(state, statusCode, context.getString(R.string.prov_app_key_status_received), R.drawable.ic_arrow_forward_black_alpha);
                mProvisioningProgress.add(provisioningProgress);
                break;
        }
        postValue(this);
    }
}
