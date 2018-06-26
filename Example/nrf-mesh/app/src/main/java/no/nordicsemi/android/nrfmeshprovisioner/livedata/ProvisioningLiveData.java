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
import android.content.SharedPreferences;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.ProvisioningSettings;

public class ProvisioningLiveData extends LiveData<ProvisioningLiveData>  {

    private ProvisioningSettings mProvisioningSettings;
    String networkName = "nRF Mesh Network";
    private String nodeName = "nRF Mesh Node";
    private String selectedAppKey;
    private String NETWORK_NAME_PREFS = "NETWORK_NAME_PREFS";
    private String NETWORK_NAME = "NETWORK_NAME";

    public ProvisioningLiveData(){
        postValue(this);
    }

    public ProvisioningSettings getProvisioningSettings() {
        return mProvisioningSettings;
    }

    public void loadProvisioningData(final Context context, final ProvisioningSettings provisioningSettings){
        this.mProvisioningSettings = provisioningSettings;
        loadNetworkName(context);
        postValue(this);
    }

    public void refreshProvisioningData(final ProvisioningSettings provisioningSettings) {
        this.mProvisioningSettings = provisioningSettings;
        networkName = "nRF Mesh Network";
        nodeName = "nRF Mesh Node";
        postValue(this);
    }

    public void setNodeName(final String nodeName) {
        if(nodeName != null && !nodeName.isEmpty()) {
            this.nodeName = nodeName;
            postValue(this);
        }
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(final Context context, final String name) {
        this.networkName = name;
        saveNetworkName(context);
        postValue(this);
    }

    public String getNetworkKey() {
        return mProvisioningSettings.getNetworkKey();
    }

    public void setNetworkKey(final String networkKey) {
        mProvisioningSettings.setNetworkKey(networkKey);
        postValue(this);
    }

    public List<String> getAppKeys() {
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
        if(selectedAppKey == null)
            selectedAppKey = mProvisioningSettings.getAppKeys().get(0);
        return selectedAppKey;
    }

    public void setSelectedAppKey(final String appKey){
        this.selectedAppKey = appKey;
        postValue(this);
    }

    private void loadNetworkName(final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(NETWORK_NAME_PREFS, Context.MODE_PRIVATE);
        networkName = preferences.getString(NETWORK_NAME, networkName);
    }

    private void saveNetworkName(final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(NETWORK_NAME_PREFS, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(NETWORK_NAME, networkName);
        editor.apply();
    }

    public void addAppKey(final String applicationKey) {
        if(mProvisioningSettings != null) {
            mProvisioningSettings.addAppKey(applicationKey);
        }
        postValue(this);
    }

    public void addAppKey(final int position, final String applicationKey) {
        if(mProvisioningSettings != null) {
            mProvisioningSettings.addAppKey(position, applicationKey);
        }
        postValue(this);
    }

    public void updateAppKey(final int position, final String applicationKey) {
        if(mProvisioningSettings != null) {
            mProvisioningSettings.updateAppKey(position, applicationKey);
        }
        postValue(this);
    }

    public void removeAppKey(final String appKey) {
        if(mProvisioningSettings != null) {
            mProvisioningSettings.removeAppKey(appKey);
        }
        postValue(this);
    }
}
