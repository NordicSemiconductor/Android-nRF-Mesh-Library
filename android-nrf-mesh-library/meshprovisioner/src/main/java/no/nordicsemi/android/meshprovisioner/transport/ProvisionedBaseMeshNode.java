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

package no.nordicsemi.android.meshprovisioner.transport;

import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.utils.SparseIntArrayParcelable;


abstract class ProvisionedBaseMeshNode implements Parcelable {

    protected static final String TAG = ProvisionedBaseMeshNode.class.getSimpleName();

    @Expose
    byte[] mConfigurationSrc = {0x7F, (byte) 0xFF};
    @Expose
    protected byte[] ivIndex;
    @Expose
    boolean isProvisioned;
    @Expose
    boolean isConfigured;
    @Expose
    protected String nodeName = "My Node";
    @Expose(serialize = false)
    protected byte[] networkKey;
    @Expose
    List<NetworkKey> networkKeys = new ArrayList<>();
    @Expose
    byte[] identityKey;
    @Expose(serialize = false)
    byte[] keyIndex;
    @Expose(serialize = false)
    int netKeyIndex;
    @Expose
    byte[] mFlags;
    @Expose
    protected byte[] unicastAddress;
    @Expose
    byte[] deviceKey;
    @Expose
    protected int ttl = 5;
    @Expose
    int mReceivedSequenceNumber;
    @Expose
    String bluetoothAddress;
    @Expose
    String nodeIdentifier;
    @Expose
    protected Integer companyIdentifier = null;
    @Expose
    Integer productIdentifier = null;
    @Expose
    Integer versionIdentifier = null;
    @Expose
    Integer crpl = null;
    @Expose
    Integer features = null;
    @Expose
    boolean relayFeatureSupported;
    @Expose
    boolean proxyFeatureSupported;
    @Expose
    boolean friendFeatureSupported;
    @Expose
    boolean lowPowerFeatureSupported;
    @Expose
    final Map<Integer, Element> mElements = new LinkedHashMap<>();
    @SerializedName("appKeys")
    @Expose
    List<Integer> mAddedAppKeyIndexes = new ArrayList<>();
    @Expose
    Map<Integer, String> mAddedAppKeys = new LinkedHashMap<>(); //Map containing the key as the app key index and the app key as the value
    @Expose
    Map<Integer, ApplicationKey> mAddedApplicationKeys = new LinkedHashMap<>(); //Map containing the key as the app key index and the app key as the value
    @Expose
    byte[] generatedNetworkId;
    @Expose
    private String bluetoothDeviceAddress;
    @Expose
    long mTimeStampInMillis;
    @Expose
    SparseIntArrayParcelable mSeqAuth = new SparseIntArrayParcelable();
    @Expose
    int numberOfElements;

    ProvisionedBaseMeshNode() {

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

    /**
     * Returns the key index
     * @return network key index
     * @deprecated Use {@link #getNetworkKeys()} instead
     */
    @Deprecated
    public final byte[] getKeyIndex() {
        return keyIndex;
    }

    public List<NetworkKey> getNetworkKeys() {
        return networkKeys;
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

    public int getNumberOfElements() {
        return numberOfElements;
    }

}
