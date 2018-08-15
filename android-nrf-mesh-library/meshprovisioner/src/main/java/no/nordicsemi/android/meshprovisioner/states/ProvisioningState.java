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

package no.nordicsemi.android.meshprovisioner.states;

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

    public abstract boolean parseData(final byte[] data);

    public enum State {
        PROVISIONING_INVITE(0), PROVISIONING_CAPABILITIES(1), PROVISIONING_START(2), PROVISIONING_PUBLIC_KEY(3),
        PROVISINING_INPUT_COMPLETE(4), PROVISIONING_CONFIRMATION(5), PROVISINING_RANDOM(6),
        PROVISINING_DATA(7), PROVISINING_COMPLETE(8), PROVISINING_FAILED(9);


        private int state;


        State(final int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

    }
}