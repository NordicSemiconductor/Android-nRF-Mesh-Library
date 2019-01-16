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

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Contains the default information network and node name. Changing the network name will save the data in to the shared preferences
 */
@SuppressWarnings("unused")
public class NetworkInformation {

    private String networkName = "nRF Mesh Network";
    private String nodeName = "nRF Mesh Node";
    private String NETWORK_NAME_PREFS = "NETWORK_NAME_PREFS";
    private String NETWORK_NAME = "NETWORK_NAME";

    private NetworkInformationListener mListener;

    /**
     * Interface that listens to changes made on  network information.
     */
    interface NetworkInformationListener {
        void onNetworkInformationUpdated(final NetworkInformation networkInformation);
    }

    public NetworkInformation(final Context context) {
        loadNetworkName(context);
    }

    /**
     * Set the network information listener
     */
    void setNetworkInformationListener(final NetworkInformationListener listener) {
        mListener = listener;
    }

    /**
     * Remove the network information listener
     */
    void removeNetworkInformationListener() {
        mListener = null;
    }

    public void refreshProvisioningData() {
        networkName = "nRF Mesh Network";
        nodeName = "nRF Mesh Node";
        if (mListener != null) {
            mListener.onNetworkInformationUpdated(this);
        }
    }

    /**
     * Sets the node name
     *
     * @param nodeName node name
     */
    public void setNodeName(final String nodeName) {
        if (nodeName != null && !nodeName.isEmpty()) {
            this.nodeName = nodeName;
            if (mListener != null) {
                mListener.onNetworkInformationUpdated(this);
            }
        }
    }

    /**
     * Returns the node name
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Returns the network name
     */
    public String getNetworkName() {
        return networkName;
    }

    /**
     * Sets the  network name. This will be saved locally to shared preferences.
     *
     * @param context application context
     * @param networkName    network name
     */
    public void setNetworkName(final Context context, final String networkName) {
        this.networkName = networkName;
        saveNetworkName(context);
        if (mListener != null) {
            mListener.onNetworkInformationUpdated(this);
        }
    }

    /**
     * Loads the saved network name from the preferences.
     *
     * @param context application context
     */
    private void loadNetworkName(final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(NETWORK_NAME_PREFS, Context.MODE_PRIVATE);
        networkName = preferences.getString(NETWORK_NAME, networkName);
    }

    /**
     * Saves the network name to shared preferences.
     *
     * @param context Application context
     */
    private void saveNetworkName(final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(NETWORK_NAME_PREFS, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(NETWORK_NAME, networkName);
        editor.apply();
    }
}
