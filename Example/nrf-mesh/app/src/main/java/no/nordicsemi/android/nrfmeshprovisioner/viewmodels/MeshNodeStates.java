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

public class MeshNodeStates {

    public enum MeshNodeStatus {
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
        APP_UNBIND_SENT(22),
        APP_BIND_STATUS_RECEIVED(23),
        PUBLISH_ADDRESS_SET_SENT(24),
        PUBLISH_ADDRESS_STATUS_RECEIVED(25),
        SUBSCRIPTION_ADD_SENT(26),
        SUBSCRIPTION_DELETE_SENT(27),
        SUBSCRIPTION_STATUS_RECEIVED(28),
        NODE_RESET_STATUS_RECEIVED(29);

        private final int state;

        MeshNodeStatus(final int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public static MeshNodeStatus fromStatusCode(final int statusCode){
            for(MeshNodeStatus state : MeshNodeStatus.values()){
                if(state.getState() == statusCode){
                    return state;
                }
            }
            throw new IllegalStateException("Invalid state");
        }
    }
}
