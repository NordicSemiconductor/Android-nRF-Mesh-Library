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

package no.nordicsemi.android.mesh;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import no.nordicsemi.android.mesh.utils.SecureUtils;

final class ProvisioningSettings extends NetworkSettings {

    private static final String APPLICATION_KEYS = "APPLICATION_KEYS";
    private static final String PROVISIONING_DATA = "PROVISIONING_DATA";
    private static final String NETWORK_KEY = "NETWORK_KEY";
    private static final String UNICAST_ADDRESS = "UNICAST_ADDRESS";
    private static final String KEY_INDEX = "KEY_INDEX";
    private static final String IV_INDEX = "IV_INDEX";
    private static final String FLAGS = "FLAGS";
    private static final String GLOBAL_TTL = "GLOBAL_TTL";
    private final Context mContext;

    ProvisioningSettings(final Context context) {
        this.mContext = context;
        generateProvisioningData();
    }

    /**
     * Generates initial provisioning data
     */
    void generateProvisioningData() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        networkKey = preferences.getString(NETWORK_KEY, SecureUtils.generateRandomNetworkKey());
        unicastAddress = preferences.getInt(UNICAST_ADDRESS, 1);
        keyIndex = preferences.getInt(KEY_INDEX, 0);
        ivIndex = preferences.getInt(IV_INDEX, 0);
        flags = preferences.getInt(FLAGS, 0);
        globalTtl = preferences.getInt(GLOBAL_TTL, 5);
        addAppKeys();
    }

    /**
     * Clear provisioning data
     */
    void clearProvisioningData() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    private void addAppKeys() {
        final SharedPreferences preferences = mContext.getSharedPreferences(APPLICATION_KEYS, Context.MODE_PRIVATE);
        final Map<String, ?> keys = preferences.getAll();
        if (!keys.isEmpty()) {
            appKeys.clear();
            for (int i = 0; i < keys.size(); i++) {
                appKeys.add(i, String.valueOf(keys.get(String.valueOf(i))));
            }
        } else {
            appKeys.add(SecureUtils.generateRandomApplicationKey().toUpperCase(Locale.US));
            appKeys.add(SecureUtils.generateRandomApplicationKey().toUpperCase(Locale.US));
            appKeys.add(SecureUtils.generateRandomApplicationKey().toUpperCase(Locale.US));
        }
        saveApplicationKeys();
    }

    /**
     * Returns the network key
     */
    public final String getNetworkKey() {
        return networkKey;
    }

    /**
     * Set network key
     * @param networkKey network key
     */
    public final void setNetworkKey(final String networkKey) {
        this.networkKey = networkKey;
        saveNetworkKey();
    }

    /**
     * Returns an unmodifiable list of application keys available in the provisioning settings
     *
     * @return Map of application keys where the key is used as the application key index for a given application key in the map
     */
    public final List<String> getAppKeys() {
        return Collections.unmodifiableList(appKeys);
    }

    /**
     * Adds an application key to the application keys list
     *
     * @param applicationKey application key to be added in the specified position
     */
    public final void addAppKey(final String applicationKey) throws IllegalArgumentException {
        /*if (this.appKeys.contains(applicationKey))
            throw new IllegalArgumentException("App key already exists");*/

        this.appKeys.add(applicationKey);
        saveApplicationKeys();
    }

    /**
     * Adds an application key to the application keys list
     *
     * @param position       Position would be used as the key for the application key. Also during configuration steps position value would be used as the index for the application key index.
     * @param applicationKey application key to be added in the specified position
     */
    public final void addAppKey(final int position, final String applicationKey) {
        /*if (this.appKeys.contains(applicationKey))
            throw new IllegalArgumentException("App key already exists");*/

        this.appKeys.add(position, applicationKey);
        saveApplicationKeys();
    }

    /**
     * Updates an application key
     *
     * @param position       Position would be used as the key for the application key. Also during configuration steps position value would be used as the index for the application key index.
     * @param applicationKey application key to be added in the specified position
     */
    public final void updateAppKey(final int position, final String applicationKey) {
        if (this.appKeys.contains(applicationKey))
            throw new IllegalArgumentException("App key already exists");

        this.appKeys.set(position, applicationKey);
        saveApplicationKeys();
    }

    /**
     * Removes the specified app key from the app key
     *
     * @param appKey App key to be removed
     */
    @SuppressWarnings("RedundantCollectionOperation")
    public final void removeAppKey(final String appKey) {
        if (appKeys.contains(appKey)) {
            final int index = appKeys.indexOf(appKey);
            appKeys.remove(index);
            saveApplicationKeys();
        }
    }

    /**
     * Return network key index used
     */
    public final int getKeyIndex() {
        return keyIndex;
    }

    /**
     * Set key index
     * @param keyIndex key index of the network key
     */
    public final void setKeyIndex(final int keyIndex) {
        this.keyIndex = keyIndex;
        saveKeyIndex();
    }

    /**
     * Returns the IV Index
     */
    public final int getIvIndex() {
        return ivIndex;
    }

    /**
     * Set IV index of the network
     */
    public final void setIvIndex(final int ivIndex) {
        this.ivIndex = ivIndex;
        saveIvIndex();
    }

    /**
     * Return the unicast address
     */
    public final int getUnicastAddress() {
        return unicastAddress;
    }

    /**
     * Set unicast address
     */
    public final void setUnicastAddress(final int unicastAddress) {
        //TODO implement a unicast address database to ensure addresses are not missed out or misused
        this.unicastAddress = unicastAddress;
        saveUnicastAddress();
    }

    /**
     * Return the flags
     */
    public final int getFlags() {
        return flags;
    }

    public final void setFlags(final int flags) {
        this.flags = flags;
        saveFlags();
    }

    public final int getGlobalTtl() {
        return globalTtl;
    }

    public final void setGlobalTtl(final int globalTtl) {
        this.globalTtl = globalTtl;
        saveGlobalTtl();
    }

    private void saveNetworkKey() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(NETWORK_KEY, networkKey);
        editor.apply();
    }

    private void saveUnicastAddress() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(UNICAST_ADDRESS, unicastAddress);
        editor.apply();
    }

    private void saveKeyIndex() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_INDEX, keyIndex);
        editor.apply();
    }

    private void saveIvIndex() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(IV_INDEX, ivIndex);
        editor.apply();
    }

    private void saveFlags() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(FLAGS, flags);
        editor.apply();
    }

    private void saveGlobalTtl() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(GLOBAL_TTL, globalTtl);
        editor.apply();
    }

    private void saveApplicationKeys() {
    }
}
