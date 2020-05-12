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

package no.nordicsemi.android.mesh.provisionerstates;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public abstract class ProvisioningState {

    static final byte TYPE_PROVISIONING_INVITE = 0x00;
    static final byte TYPE_PROVISIONING_CAPABILITIES = 0x01;
    static final byte TYPE_PROVISIONING_START = 0x02;
    static final byte TYPE_PROVISIONING_PUBLIC_KEY = 0x03;
    static final byte TYPE_PROVISIONING_INPUT_COMPLETE = 0x04;
    static final byte TYPE_PROVISIONING_CONFIRMATION = 0x05;
    static final byte TYPE_PROVISIONING_RANDOM_CONFIRMATION = 0x06;
    static final byte TYPE_PROVISIONING_DATA = 0x07;
    static final byte TYPE_PROVISIONING_COMPLETE = 0x08;

    public ProvisioningState() {
    }

    public abstract State getState();

    public abstract void executeSend();

    public abstract boolean parseData(@NonNull final byte[] data);

    public enum State {
        PROVISIONING_INVITE(0), PROVISIONING_CAPABILITIES(1), PROVISIONING_START(2), PROVISIONING_PUBLIC_KEY(3),
        PROVISIONING_INPUT_COMPLETE(4), PROVISIONING_CONFIRMATION(5), PROVISIONING_RANDOM(6),
        PROVISIONING_DATA(7), PROVISIONING_COMPLETE(8), PROVISIONING_FAILED(9);

        private int state;

        State(final int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

    }

    public enum States {
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
        SENDING_DEFAULT_TTL_GET(19),
        DEFAULT_TTL_STATUS_RECEIVED(20),
        SENDING_APP_KEY_ADD(21),
        APP_KEY_STATUS_RECEIVED(22),
        SENDING_NETWORK_TRANSMIT_SET(23),
        NETWORK_TRANSMIT_STATUS_RECEIVED(24),
        SENDING_BLOCK_ACKNOWLEDGEMENT(98),
        BLOCK_ACKNOWLEDGEMENT_RECEIVED(99);

        private int state;

        States(final int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public static States fromStatusCode(final int statusCode) {
            for (States state : States.values()) {
                if (state.getState() == statusCode) {
                    return state;
                }
            }
            throw new IllegalStateException("Invalid state");
        }
    }
}