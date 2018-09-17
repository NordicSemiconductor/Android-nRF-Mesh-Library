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

package no.nordicsemi.android.meshprovisioner.messages;

import java.util.HashMap;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.configuration.ConfigMessageState;

public class AccessMessage extends Message {

    private byte[] accessPdu;
    private byte[] transportPdu;
    private ConfigMessageState configMessage;

    public AccessMessage() {
        this.ctl = 0;
    }

    @Override
    public Map<Integer, byte[]> getNetworkPdu() {
        return networkPdu;
    }

    @Override
    public void setNetworkPdu(final HashMap<Integer, byte[]> pdu) {
        networkPdu = pdu;
    }

    public byte[] getAccessPdu() {
        return accessPdu;
    }

    public void setAccessPdu(final byte[] accessPdu) {
        this.accessPdu = accessPdu;
    }

    public byte[] getUpperTransportPdu() {
        return transportPdu;
    }

    public void setUpperTransportPdu(final byte[] transportPdu) {
        this.transportPdu = transportPdu;
    }

    public HashMap<Integer, byte[]> getLowerTransportAccessPdu() {
        return super.getLowerTransportAccessPdu();
    }

    public void setLowerTransportAccessPdu(final HashMap<Integer, byte[]> lowerTransportAccessPdu) {
        super.setLowerTransportAccessPdu(lowerTransportAccessPdu);
    }

    public ConfigMessageState getConfigMessage() {
        return configMessage;
    }

    public void setConfigMessage(final ConfigMessageState configMessage) {
        this.configMessage = configMessage;
    }
}
