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

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.utils.MeshTypeConverters;
import no.nordicsemi.android.meshprovisioner.utils.SparseIntArrayParcelable;

@SuppressWarnings({"unused", "WeakerAccess"})
abstract class ProvisionedBaseMeshNode implements Parcelable {

    protected static final String TAG = ProvisionedBaseMeshNode.class.getSimpleName();
    /**Unique identifier of the mesh network*/
    @ColumnInfo(name = "mesh_uuid")
    @Expose(serialize = false, deserialize = false)
    String meshUuid;

    /**Device UUID*/
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "uuid")
    String uuid;

    @Ignore
    @Expose
    boolean isProvisioned;

    @ColumnInfo(name = "unicast_address")
    @Expose
    byte[] unicastAddress;

    @ColumnInfo(name = "name")
    @Expose
    protected String nodeName = "My Node";

    @ColumnInfo(name = "configured")
    @Expose
    boolean isConfigured;

    @ColumnInfo(name = "device_key")
    @Expose
    byte[] deviceKey;

    @ColumnInfo(name = "ttl")
    @Expose
    protected int ttl = 5;

    @ColumnInfo(name = "seq_number")
    @Expose
    int mReceivedSequenceNumber;

    @Ignore
    @Expose
    String bluetoothAddress;

    @Ignore
    @Expose(serialize = false, deserialize = false)
    String nodeIdentifier;

    @ColumnInfo(name = "cid")
    @Nullable
    @Expose
    Integer companyIdentifier = null;

    @ColumnInfo(name = "pid")
    @Nullable
    @Expose
    Integer productIdentifier = null;

    @ColumnInfo(name = "vid")
    @Nullable
    @Expose
    Integer versionIdentifier = null;

    @ColumnInfo(name = "crpl")
    @Nullable
    @Expose
    Integer crpl = null;

    @ColumnInfo(name = "relay")
    @Nullable
    @Expose
    Boolean relayFeatureSupported = null;

    @ColumnInfo(name = "proxy")
    @Nullable
    @Expose
    Boolean proxyFeatureSupported = null;

    @ColumnInfo(name = "friend")
    @Nullable
    @Expose
    Boolean friendFeatureSupported = null;

    @ColumnInfo(name = "low_power")
    @Nullable
    @Expose
    Boolean lowPowerFeatureSupported = null;

    @ColumnInfo(name = "timestamp")
    @Expose
    public long mTimeStampInMillis;

    @Embedded
    @Expose
    SparseIntArrayParcelable mSeqAuth = new SparseIntArrayParcelable();

    //Fields ignored by the entity as they have been migrated to the mesh network object
    @Ignore
    @Expose(serialize = false)
    protected byte[] networkKey;

    @Ignore
    @Expose
    List<NetworkKey> networkKeys = new ArrayList<>();

    @Ignore
    @Expose
    byte[] identityKey;

    @Ignore
    @Expose(serialize = false)
    byte[] keyIndex;

    @Deprecated
    @Ignore
    @Expose(serialize = false)
    int netKeyIndex;

    @Ignore
    @Expose
    byte[] mFlags;

    @Ignore
    @Expose
    Integer features = null;

    @TypeConverters(MeshTypeConverters.class)
    @Expose
    Map<Integer, Element> mElements = new LinkedHashMap<>();

    @Ignore
    @SerializedName("appKeys")
    @Expose
    List<Integer> mAddedAppKeyIndexes = new ArrayList<>();

    @Ignore
    @Expose(serialize = false)
    Map<Integer, String> mAddedAppKeys = new LinkedHashMap<>(); //Map containing the key as the app key index and the app key as the value

    @TypeConverters(MeshTypeConverters.class)
    @Expose
    Map<Integer, ApplicationKey> mAddedApplicationKeys = new LinkedHashMap<>(); //Map containing the key as the app key index and the app key as the value

    @Ignore
    @Expose
    byte[] generatedNetworkId;

    @Ignore
    @Expose(deserialize = false)
    private String bluetoothDeviceAddress;

    @Ignore
    @Expose
    int numberOfElements;

    @Ignore
    @Expose
    byte[] mConfigurationSrc = {0x7F, (byte) 0xFF};

    /**
     * @deprecated IV Index is a network property hence movec to {@link MeshNetwork}
     */
    @Deprecated
    @Ignore
    @Expose(serialize = false)
    protected byte[] ivIndex;

    public ProvisionedBaseMeshNode() {

    }

    public String getMeshUuid() {
        return meshUuid;
    }

    public void setMeshUuid(final String meshUuid) {
        this.meshUuid = meshUuid;
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }

    public void setUuid(@NonNull final String uuid) {
        this.uuid = uuid;
    }

    public boolean isProvisioned() {
        return isProvisioned;
    }

    public final boolean isConfigured() {
        return isConfigured;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
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

    public final byte[] getUnicastAddress() {
        return unicastAddress;
    }

    public final int getUnicastAddressInt() {
        return ByteBuffer.wrap(unicastAddress).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    public final void setUnicastAddress(final byte[] unicastAddress) {
        this.unicastAddress = unicastAddress;
    }

    public int getTtl() {
        return ttl;
    }

    public final byte[] getIdentityKey() {
        return identityKey;
    }

    /**
     * Returns the key index
     *
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

    /**
     * @deprecated IV Index is a part of the network {@link MeshNetwork#getIvIndex()}
     */
    @Deprecated
    public final byte[] getIvIndex() {
        return ivIndex;
    }

    @VisibleForTesting
    final void setIvIndex(final byte[] ivIndex) {
        this.ivIndex = ivIndex;
    }

    @Deprecated
    public void setBluetoothDeviceAddress(final String bluetoothDeviceAddress) {
        this.bluetoothDeviceAddress = bluetoothDeviceAddress;
    }

    public void setTtl(final int ttl) {
        this.ttl = ttl;
    }

    public long getTimeStamp() {
        return mTimeStampInMillis;
    }

    public void setTimeStamp(final long timestamp){
        mTimeStampInMillis = timestamp;
    }

    public final byte[] getConfigurationSrc() {
        return mConfigurationSrc;
    }

    public final void setConfigurationSrc(final byte[] src) {
        mConfigurationSrc = src;
    }
}
