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

package no.nordicsemi.android.meshprovisioner.provisionerstates;

import android.os.Parcelable;
import android.text.TextUtils;

import java.util.UUID;


abstract class UnprovisionedBaseMeshNode implements Parcelable {

    protected static final String TAG = UnprovisionedBaseMeshNode.class.getSimpleName();

    private int mConfigurationSrc;
    protected byte[] ivIndex;
    boolean isProvisioned;
    boolean isConfigured;
    protected String nodeName = "My Node";
    byte[] provisionerPublicKeyXY;
    byte[] provisioneePublicKeyXY;
    byte[] sharedECDHSecret;
    byte[] provisionerRandom;
    byte[] provisioneeConfirmation;
    byte[] authenticationValue;
    byte[] provisioneeRandom;
    protected byte[] networkKey;
    byte[] identityKey;
    protected int keyIndex;
    byte[] mFlags;
    protected int unicastAddress;
    byte[] deviceKey;
    protected int ttl = 5;
    private String bluetoothDeviceAddress;
    long mTimeStampInMillis;
    ProvisioningCapabilities provisioningCapabilities;
    int numberOfElements;
    UUID deviceUuid;

    UnprovisionedBaseMeshNode(final UUID uuid) {
        deviceUuid = uuid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isProvisioned() {
        return isProvisioned;
    }

    public final boolean isConfigured() {
        return isConfigured;
    }

    public final void setConfigured(final boolean configured) {
        isConfigured = configured;
    }

    public final String getNodeName() {
        return nodeName;
    }

    public final void setNodeName(final String nodeName) {
        if (!TextUtils.isEmpty(nodeName))
            this.nodeName = nodeName;
    }

    public final int getUnicastAddress() {
        return unicastAddress;
    }

    public final void setUnicastAddress(final int unicastAddress) {
        this.unicastAddress = unicastAddress;
    }

    public byte[] getDeviceKey() {
        return deviceKey;
    }

    public int getTtl() {
        return ttl;
    }

    public final byte[] getIdentityKey() {
        return identityKey;
    }

    public final int getKeyIndex() {
        return keyIndex;
    }

    public final void setKeyIndex(final int keyIndex) {
        this.keyIndex = keyIndex;
    }

    public final byte[] getFlags() {
        return mFlags;
    }

    public final void setFlags(final byte[] flags) {
        this.mFlags = flags;
    }

    public final byte[] getIvIndex() {
        return ivIndex;
    }

    public final void setIvIndex(final byte[] ivIndex) {
        this.ivIndex = ivIndex;
    }

    public void setTtl(final int ttl) {
        this.ttl = ttl;
    }

    public long getTimeStamp() {
        return mTimeStampInMillis;
    }

    public final int getConfigurationSrc() {
        return mConfigurationSrc;
    }

    public final void setConfigurationSrc(final int src) {
        mConfigurationSrc = src;
    }

    public ProvisioningCapabilities getProvisioningCapabilities() {
        return provisioningCapabilities;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public UUID getDeviceUuid(){
        return deviceUuid;
    }
}
