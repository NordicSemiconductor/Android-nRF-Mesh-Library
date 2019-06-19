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

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;

/**
 * LiveData class for storing {@link MeshNetwork}
 */
public class MeshNetworkLiveData extends LiveData<MeshNetworkLiveData> {

    private MeshNetwork meshNetwork;
    private ApplicationKey selectedAppKey;
    private String nodeName;

    MeshNetworkLiveData() {

    }

    /**
     * Loads the mesh network information in to live data
     *
     * @param meshNetwork provisioning settings
     */
    void loadNetworkInformation(@NonNull final MeshNetwork meshNetwork) {
        this.meshNetwork = meshNetwork;
        postValue(this);
    }

    public MeshNetwork getMeshNetwork() {
        return meshNetwork;
    }

    /**
     * Refreshes the mesh network information
     *
     * @param meshNetwork provisioning settings
     */
    void refresh(@NonNull final MeshNetwork meshNetwork) {
        this.meshNetwork = meshNetwork;
        postValue(this);
    }

    public List<NetworkKey> getNetworkKeys() {
        return meshNetwork.getNetKeys();
    }

    /**
     * Returns the app keys list
     */
    public List<ApplicationKey> getAppKeys() {
        return meshNetwork.getAppKeys();
    }

    /**
     * Returns the list of {@link Provisioner}
     */
    public List<Provisioner> getProvisioners() {
        return meshNetwork.getProvisioners();
    }

    public Provisioner getProvisioner() {
        return meshNetwork.getSelectedProvisioner();
    }

    /**
     * Return the selected app key to be added during the provisioning process.
     *
     * @return app key
     */
    public ApplicationKey getSelectedAppKey() {
        if (selectedAppKey == null)
            selectedAppKey = meshNetwork.getAppKeys().get(0);
        return selectedAppKey;
    }

    /**
     * Set the selected app key to be added during the provisioning process.
     */
    public void setSelectedAppKey(final ApplicationKey appKey) {
        this.selectedAppKey = appKey;
        postValue(this);
    }

    public void resetSelectedAppKey() {
        this.selectedAppKey = null;
    }

    /**
     * Adds an application key to the next available index in the global app key list
     *
     * @param appKey key {@link ApplicationKey}
     */
    public boolean addAppKey(@NonNull final String appKey) throws IllegalArgumentException {
        if (meshNetwork != null) {
            return meshNetwork.addAppKey(appKey);
        }
        return false;
    }

    /**
     * Adds an application key to the mesh network
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean addAppKey(final ApplicationKey applicationKey) {
        if (meshNetwork != null) {
            return meshNetwork.addAppKey(applicationKey);
        }
        return false;
    }

    /**
     * Update the application key in a particular position
     *
     * @param keyIndex       update app key in given key index
     * @param applicationKey app key
     */
    public boolean updateAppKey(final int keyIndex, final String applicationKey) throws IllegalArgumentException {
        if (meshNetwork != null) {
            return meshNetwork.updateAppKey(keyIndex, applicationKey);
        }
        return false;
    }

    /**
     * Remove app key from the list of application keys in the mesh network
     *
     * @param appKey key {@link ApplicationKey}
     */
    public boolean removeAppKey(@NonNull final ApplicationKey appKey) throws IllegalArgumentException {
        if (meshNetwork != null) {
            return meshNetwork.removeAppKey(appKey);
        }
        return false;
    }

    /**
     * Returns the network name
     */
    public String getNetworkName() {
        return meshNetwork.getMeshName();
    }

    /**
     * Set the network name of the mesh network
     *
     * @param name network name
     */
    public void setNetworkName(final String name) {
        meshNetwork.setMeshName(name);
        postValue(this);
    }

    /**
     * Sets the node name
     *
     * @param nodeName node name
     */
    public void setNodeName(final String nodeName) {
        if (nodeName != null && !nodeName.isEmpty()) {
            this.nodeName = nodeName;
            postValue(this);
        }
    }

    /**
     * Returns the node name
     */
    public String getNodeName() {
        return nodeName;
    }
}
