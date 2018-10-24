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
import android.support.annotation.NonNull;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.ProvisioningSettings;

/**
 * LiveData class for storing {@link ProvisioningSettings}
 */
public class ProvisioningSettingsLiveData extends LiveData<ProvisioningSettingsLiveData> {

    private ProvisioningSettings mProvisioningSettings;
    private String selectedAppKey;

    ProvisioningSettingsLiveData(@NonNull final ProvisioningSettings provisioningSettings) {
        mProvisioningSettings = provisioningSettings;
        postValue(this);
    }

    public ProvisioningSettings getProvisioningSettings() {
        return mProvisioningSettings;
    }

    /**
     * Refresh provisioning settings
     *
     * @param provisioningSettings provisioning settings
     */
    void refresh(final ProvisioningSettings provisioningSettings) {
        this.mProvisioningSettings = provisioningSettings;
        postValue(this);
    }

    /**
     * Returns the network key used for provisioning
     */
    public String getNetworkKey() {
        return mProvisioningSettings.getNetworkKey();
    }

    /**
     * Set network key
     *
     * @param networkKey network key
     */
    public void setNetworkKey(final String networkKey) {
        mProvisioningSettings.setNetworkKey(networkKey);
        postValue(this);
    }

    /**
     * Returns the app keys list
     */
    public List<String> getAppKeys() {
        return mProvisioningSettings.getAppKeys();
    }

    /**
     * Returns the network key index
     */
    public int getKeyIndex() {
        return mProvisioningSettings.getKeyIndex();
    }

    /**
     * Set network key index
     * @param keyIndex network key index
     */
    public void setKeyIndex(final int keyIndex) {
        mProvisioningSettings.setKeyIndex(keyIndex);
        postValue(this);
    }

    /**
     * Returns the IV Index used for provisioning
     * @return iv index
     */
    public int getIvIndex() {
        return mProvisioningSettings.getIvIndex();
    }

    /**
     * Set IV Index
     * @param ivIndex 24-bit iv index
     */
    public void setIvIndex(final int ivIndex) {
        mProvisioningSettings.setIvIndex(ivIndex);
        postValue(this);
    }

    /**
     * Returns unicast address
     * @return 16-bit unicast address
     */
    public int getUnicastAddress() {
        return mProvisioningSettings.getUnicastAddress();
    }

    /**
     * Set unicast address, this would be the address used for a node during the provisioning process.
     * @param unicastAddress 16-bit unicast address
     */
    public void setUnicastAddress(final int unicastAddress) {
        mProvisioningSettings.setUnicastAddress(unicastAddress);
        postValue(this);
    }

    /**
     * Provisioning flags
     */
    public int getFlags() {
        return mProvisioningSettings.getFlags();
    }

    /**
     * Provisioning flags
     * @param flags provisioning flags
     */
    public void setFlags(final int flags) {
        mProvisioningSettings.setFlags(flags);
        postValue(this);
    }

    /**
     * Returns the global ttl set for the messages sent by the provisioner
     */
    public int getGlobalTtl() {
        return mProvisioningSettings.getGlobalTtl();
    }

    /**
     * Sets a global ttl value that would be used on all messages sent from the provisioner
     * @param globalTtl ttl value
     */
    public void setGlobalTtl(final int globalTtl) {
        mProvisioningSettings.setGlobalTtl(globalTtl);
        postValue(this);
    }

    /**
     * Return the selected app key to be added during the provisioning process.
     * @return app key
     */
    public String getSelectedAppKey() {
        if (selectedAppKey == null)
            selectedAppKey = mProvisioningSettings.getAppKeys().get(0);
        return selectedAppKey;
    }

    /**
     * Set the selected app key to be added during the provisioning process.
     * @return app key
     */
    public void setSelectedAppKey(final String appKey) {
        this.selectedAppKey = appKey;
        postValue(this);
    }

    public void addAppKey(final String applicationKey) {
        if (mProvisioningSettings != null) {
            mProvisioningSettings.addAppKey(applicationKey);
        }
        postValue(this);
    }

    public void addAppKey(final int position, final String applicationKey) {
        if (mProvisioningSettings != null) {
            mProvisioningSettings.addAppKey(position, applicationKey);
        }
        postValue(this);
    }

    public void updateAppKey(final int position, final String applicationKey) {
        if (mProvisioningSettings != null) {
            mProvisioningSettings.updateAppKey(position, applicationKey);
        }
        postValue(this);
    }

    public void removeAppKey(final String appKey) {
        if (mProvisioningSettings != null) {
            mProvisioningSettings.removeAppKey(appKey);
        }
        postValue(this);
    }
}
