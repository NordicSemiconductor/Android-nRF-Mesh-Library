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

import no.nordicsemi.android.meshprovisioner.control.TransportControlMessage;

public class ControlMessage extends Message {

    private byte[] transportControlPdu;
    private TransportControlMessage transportControlMessage;

    public ControlMessage() {
        this.ctl = 1;
    }

    @Override
    public Map<Integer, byte[]> getNetworkPdu() {
        return networkPdu;
    }

    @Override
    public void setNetworkPdu(final HashMap<Integer, byte[]> pdu) {
        networkPdu = pdu;
    }

    public byte[] getTransportControlPdu() {
        return transportControlPdu;
    }

    public void setTransportControlPdu(final byte[] transportControlPdu) {
        this.transportControlPdu = transportControlPdu;
    }

    public HashMap<Integer, byte[]> getLowerTransportControlPdu() {
        return super.getLowerTransportControlPdu();
    }

    public void setLowerTransportControlPdu(final HashMap<Integer, byte[]> segmentedAccessMessages) {
        super.setLowerTransportControlPdu(segmentedAccessMessages);
    }

    public TransportControlMessage getTransportControlMessage() {
        return transportControlMessage;
    }

    public void setTransportControlMessage(final TransportControlMessage transportControlMessage) {
        this.transportControlMessage = transportControlMessage;
    }
}
