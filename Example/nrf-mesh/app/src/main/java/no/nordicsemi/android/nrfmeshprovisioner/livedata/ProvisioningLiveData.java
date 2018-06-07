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

import java.util.Map;

import no.nordicsemi.android.meshprovisioner.ProvisioningSettings;

public class ProvisioningLiveData extends LiveData<ProvisioningLiveData>  {

    private ProvisioningSettings mProvisioningSettings;
    private String selectedAppKey;

    public ProvisioningLiveData(final ProvisioningSettings provisioningSettings){
        this.mProvisioningSettings = provisioningSettings;
        postValue(this);
    }

    public void refreshProvisioningData(final ProvisioningSettings provisioningSettings) {
        this.mProvisioningSettings = provisioningSettings;
        postValue(this);
    }

    public void setNodeName(final String nodeName) {
        if(nodeName != null && !nodeName.isEmpty()) {
            mProvisioningSettings.setNodeName(nodeName);
            postValue(this);
        }
    }

    public String getNodeName() {
        return mProvisioningSettings.getNodeName();
    }

    public String getNetworkName() {
        return mProvisioningSettings.getNetworkName();
    }

    public void setNetworkName(final String name) {
        mProvisioningSettings.setNetworkName(name);
        postValue(this);
    }

    public String getNetworkKey() {
        return mProvisioningSettings.getNetworkKey();
    }

    public void setNetworkKey(final String networkKey) {
        mProvisioningSettings.setNetworkKey(networkKey);
        postValue(this);
    }

    public Map<Integer, String> getAppKeys() {
        return mProvisioningSettings.getAppKeys();
    }

    public int getKeyIndex() {
        return mProvisioningSettings.getKeyIndex();
    }

    public void setKeyIndex(final int keyIndex) {
        mProvisioningSettings.setKeyIndex(keyIndex);
        postValue(this);
    }

    public int getIvIndex() {
        return mProvisioningSettings.getIvIndex();
    }

    public void setIvIndex(final int ivIndex) {
        mProvisioningSettings.setIvIndex(ivIndex);
        postValue(this);
    }

    public int getUnicastAddress() {
        return mProvisioningSettings.getUnicastAddress();
    }

    public void setUnicastAddress(final int unicastAddress) {
        mProvisioningSettings.setUnicastAddress(unicastAddress);
        postValue(this);
    }

    public int getFlags() {
        return mProvisioningSettings.getFlags();
    }

    public void setFlags(final int flags) {
        mProvisioningSettings.setFlags(flags);
        postValue(this);
    }

    public int getGlobalTtl() {
        return mProvisioningSettings.getGlobalTtl();
    }

    public void setGlobalTtl(final int globalTtl) {
        mProvisioningSettings.setGlobalTtl(globalTtl);
        postValue(this);
    }

    public void update(final ProvisioningSettings provisioningSettings) {
        this.mProvisioningSettings = provisioningSettings;
        postValue(this);
    }

    public String getSelectedAppKey() {
        selectedAppKey = mProvisioningSettings.getAppKeys().get(0);
        return selectedAppKey;
    }

    public void setSelectedAppKey(final String appKey){
        this.selectedAppKey = appKey;
        postValue(this);
    }
}
