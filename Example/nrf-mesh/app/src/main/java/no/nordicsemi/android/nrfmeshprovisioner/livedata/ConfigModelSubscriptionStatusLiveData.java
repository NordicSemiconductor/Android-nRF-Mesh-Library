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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ConfigModelSubscriptionStatusLiveData extends LiveData<ConfigModelSubscriptionStatusLiveData> {

    private int status;
    private byte[] elementAddress;
    private byte[] subscriptionAddress;
    private int modelIdentifier; //16-bit SIG Model or 32-bit Vendor Model identifier
    private boolean isSuccessful;
    private List<byte[]> subscriptionAddresses = new ArrayList<>();

    public ConfigModelSubscriptionStatusLiveData() {
    }

    public void onStatusChanged(final boolean isSuccessful, final int status, final byte[] elementAddress,
                                final byte[] subscriptionAddress, final int modelIdentifier) {

        this.isSuccessful = isSuccessful;
        this.status = status;
        this.elementAddress = elementAddress;
        this.subscriptionAddress = subscriptionAddress;
        this.modelIdentifier = modelIdentifier;
        subscriptionAddresses.add(subscriptionAddress);
        postValue(this);
    }

    public int getStatus() {
        return status;
    }

    public byte[] getElementAddress() {
        return elementAddress;
    }

    /**
     * Returns the element address as int
     * @return element address
     */
    public int getElementAddressInt(){
        return ByteBuffer.wrap(elementAddress).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    public byte[] getSubscriptionAddress() {
        return subscriptionAddress;
    }

    /**
     * Returns the model identifier which could be a 16-bit SIG Model ID or 32-bit Vendor Model ID
     * @return modelIdentifier
     */
    public int getModelIdentifier() {
        return modelIdentifier;
    }

    /**
     * Returns a boolean containing the success state
     * @return true if successful and false otherwise
     */
    public boolean isSuccessful() {
        return isSuccessful;
    }
}
