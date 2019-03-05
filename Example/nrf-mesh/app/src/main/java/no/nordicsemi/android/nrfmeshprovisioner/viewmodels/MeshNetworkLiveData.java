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

import no.nordicsemi.android.meshprovisioner.MeshNetwork;
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

    /**
     * Returns the primary network key in the mesh network
     */
    public NetworkKey getPrimaryNetworkKey() {
        return meshNetwork.getPrimaryNetworkKey();
    }

    /**
     * Sets primary network key
     *
     * @param networkKey network key
     */
    public void setPrimaryNetworkKey(final String networkKey) {
        meshNetwork.addNetKey(0, networkKey);
        postValue(this);
    }

    /**
     * Returns the app keys list
     */
    public List<ApplicationKey> getAppKeys() {
        return meshNetwork.getAppKeys();
    }

    /**
     * Returns the network key index
     */
    public int getKeyIndex() {
        return meshNetwork.getNetKeys().get(0).getKeyIndex();
    }

    /**
     * Set network key index
     *
     * @param keyIndex network key index
     */
    public void setKeyIndex(final int keyIndex) {
        meshNetwork.getNetKeys().get(0).setKeyIndex(keyIndex);
        postValue(this);
    }

    /**
     * Returns the IV Index used for provisioning
     *
     * @return iv index
     */
    public int getIvIndex() {
        return meshNetwork.getIvIndex();
    }

    /**
     * Set IV Index
     *
     * @param ivIndex 24-bit iv index
     */
    public void setIvIndex(final int ivIndex) {
        meshNetwork.setIvIndex(ivIndex);
        postValue(this);
    }

    /**
     * Returns unicast address
     *
     * @return 16-bit unicast address
     */
    public int getUnicastAddress() {
        final byte[] unicast = AddressUtils.getUnicastAddressBytes(meshNetwork.getUnicastAddress());
        return AddressUtils.getUnicastAddressInt(unicast);
    }

    /**
     * Set unicast address, this would be the address assigned to an unprovisioned node.
     *
     * @param unicastAddress 16-bit unicast address
     */
    public void setUnicastAddress(final int unicastAddress) {
        meshNetwork.assignUnicastAddress(unicastAddress);
        postValue(this);
    }

    public byte[] getProvisionerAddress() {
        return AddressUtils.getUnicastAddressBytes(meshNetwork.getProvisionerAddress());
    }

    public boolean setProvisionerAddress(final int address) {
        final boolean flag = meshNetwork.setProvisionerAddress(address);
        if (flag) {
            postValue(this);
        }
        return flag;
    }

    /**
     * Provisioning flags
     */
    public int getFlags() {
        return meshNetwork.getProvisioningFlags();
    }

    /**
     * Provisioning flags
     *
     * @param flags provisioning flags
     */
    public void setFlags(final int flags) {
        postValue(this);
    }

    /**
     * Returns the global ttl set for the messages sent by the provisioner
     */
    public int getGlobalTtl() {
        return meshNetwork.getGlobalTtl();
    }

    /**
     * Sets a global ttl value that would be used on all messages sent from the provisioner
     *
     * @param globalTtl ttl value
     */
    public void setGlobalTtl(final int globalTtl) {
        meshNetwork.setGlobalTtl(globalTtl);
        postValue(this);
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

    public void resetSelectedAppKey(){
        this.selectedAppKey = null;
    }

    /**
     * Adds an application key to the next available index in the global app key list
     * @param applicationKey key {@link ApplicationKey}
     */
    public void addAppKey(final String applicationKey) {
        if (meshNetwork != null) {
            meshNetwork.addAppKey(applicationKey);
        }
        postValue(this);
    }

    /**
     * Adds an application key to the mesh network
     */
    public void addAppKey(final ApplicationKey applicationKey) {
        if (meshNetwork != null) {
            meshNetwork.addAppKey(applicationKey);
        }
        postValue(this);
    }

    /**
     * Update the application key in a particular position
     * @param keyIndex update app key in given key index
     * @param applicationKey app key
     */
    public void updateAppKey(final int keyIndex, final String applicationKey) {
        if (meshNetwork != null) {
            meshNetwork.updateAppKey(keyIndex, applicationKey);
        }
        postValue(this);
    }

    /**
     * Remove app key from the list of application keys in the mesh network
     * @param appKey key {@link ApplicationKey}
     */
    public void removeAppKey(final ApplicationKey appKey) {
        if (meshNetwork != null) {
            meshNetwork.removeAppKey(appKey);
        }
        postValue(this);
    }

    /**
     * Returns the network name
     */
    public String getNetworkName() {
        return meshNetwork.getMeshName();
    }

    /**
     * Set the network name of the mesh network
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
