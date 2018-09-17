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

package no.nordicsemi.android.meshprovisioner;

import android.os.Parcelable;
import android.text.TextUtils;
import android.util.SparseIntArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.states.ProvisioningCapabilities;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;
import no.nordicsemi.android.meshprovisioner.utils.SparseIntArrayParcelable;


public abstract class BaseMeshNode implements Parcelable {

    protected static final String TAG = BaseMeshNode.class.getSimpleName();

    protected byte[] mConfigurationSrc = {0x7F, (byte) 0xFF};
    protected byte[] ivIndex;
    protected boolean isProvisioned;
    protected boolean isConfigured;
    protected String nodeName = "My Node";
    protected byte[] provisionerPublicKeyXY;
    protected byte[] provisioneePublicKeyXY;
    protected byte[] sharedECDHSecret;
    protected byte[] provisionerRandom;
    protected byte[] provisioneeConfirmation;
    protected byte[] authenticationValue;
    protected byte[] provisioneeRandom;
    protected byte[] networkKey;
    protected byte[] identityKey;
    protected byte[] keyIndex;
    protected byte[] mFlags;
    protected byte[] unicastAddress;
    protected byte[] deviceKey;
    protected int ttl = 5;
    protected int mReceivedSequenceNumber;
    protected String bluetoothAddress;
    protected String nodeIdentifier;
    protected Integer companyIdentifier = null;
    protected Integer productIdentifier = null;
    protected Integer versionIdentifier = null;
    protected Integer crpl = null;
    protected Integer features = null;
    protected boolean relayFeatureSupported;
    protected boolean proxyFeatureSupported;
    protected boolean friendFeatureSupported;
    protected boolean lowPowerFeatureSupported;
    protected final Map<Integer, Element> mElements = new LinkedHashMap<>();
    protected List<Integer> mAddedAppKeyIndexes = new ArrayList<>();
    protected Map<Integer, String> mAddedAppKeys = new LinkedHashMap<>(); //Map containing the key as the app key index and the app key as the value
    protected byte[] generatedNetworkId;
    private String bluetoothDeviceAddress;
    protected long mTimeStampInMillis;
    protected SparseIntArrayParcelable mSeqAuth = new SparseIntArrayParcelable();
    protected ProvisioningCapabilities provisioningCapabilities;
    protected int numberOfElements;

    protected BaseMeshNode() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isProvisioned() {
        return isProvisioned;
    }

    public final void setIsProvisioned(final boolean isProvisioned) {
        identityKey = SecureUtils.calculateIdentityKey(networkKey);
        this.isProvisioned = isProvisioned;
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

    protected final void setNodeName(final String nodeName) {
        if (!TextUtils.isEmpty(nodeName))
            this.nodeName = nodeName;
    }

    public final byte[] getUnicastAddress() {
        return unicastAddress;
    }

    public final int getUnicastAddressInt() {
        return ByteBuffer.wrap(unicastAddress).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    public final void setUnicastAddress(final byte[] unicastAddress) {
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

    public final byte[] getKeyIndex() {
        return keyIndex;
    }

    public final void setKeyIndex(final byte[] keyIndex) {
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

    public void setBluetoothDeviceAddress(final String bluetoothDeviceAddress) {
        this.bluetoothDeviceAddress = bluetoothDeviceAddress;
    }

    public void setTtl(final int ttl) {
        this.ttl = ttl;
    }

    public long getTimeStamp() {
        return mTimeStampInMillis;
    }

    public final byte[] getConfigurationSrc() {
        return mConfigurationSrc;
    }

    public final void setConfigurationSrc(final byte[] src) {
        mConfigurationSrc = src;
    }

    public ProvisioningCapabilities getProvisioningCapabilities() {
        return provisioningCapabilities;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }
}
