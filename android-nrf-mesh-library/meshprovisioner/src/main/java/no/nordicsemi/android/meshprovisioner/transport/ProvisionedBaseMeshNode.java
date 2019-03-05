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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.Features;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.SecureNetworkBeacon;
import no.nordicsemi.android.meshprovisioner.utils.MeshTypeConverters;
import no.nordicsemi.android.meshprovisioner.utils.NetworkTransmitSettings;
import no.nordicsemi.android.meshprovisioner.utils.ProxyFilter;
import no.nordicsemi.android.meshprovisioner.utils.RelaySettings;
import no.nordicsemi.android.meshprovisioner.utils.SparseIntArrayParcelable;

@SuppressWarnings({"unused", "WeakerAccess", "deprecation"})
abstract class ProvisionedBaseMeshNode implements Parcelable {

    public static final int LOW = 0; //Low security
    public static final int HIGH = 1; //High security
    protected static final String TAG = ProvisionedBaseMeshNode.class.getSimpleName();
    @ColumnInfo(name = "timestamp")
    @Expose
    public long mTimeStampInMillis;
    @TypeConverters(MeshTypeConverters.class)
    @Expose
    public List<NetworkKey> mAddedNetworkKeys = new ArrayList<>();
    @ColumnInfo(name = "name")
    @Expose
    protected String nodeName = "My Node";
    @ColumnInfo(name = "ttl")
    @Expose
    protected Integer ttl = 5;
    //Fields ignored by the entity as they have been migrated to the mesh network object
    @Deprecated
    @Ignore
    @Expose(serialize = false)
    protected byte[] networkKey;
    /**
     * @deprecated IV Index is a network property hence movec to {@link MeshNetwork}
     */
    @Deprecated
    @Ignore
    @Expose(serialize = false)
    protected byte[] ivIndex;
    @ColumnInfo(name = "blacklisted")
    @Expose
    protected boolean blackListed = false;
    @ColumnInfo(name = "secureNetworkBeacon")
    @Expose
    protected Boolean secureNetworkBeaconSupported;
    @Embedded
    @Expose
    protected NetworkTransmitSettings networkTransmitSettings;
    @Embedded
    @Expose
    protected RelaySettings relaySettings;
    /**
     * Unique identifier of the mesh network
     */
    @ColumnInfo(name = "mesh_uuid")
    @Expose(serialize = false, deserialize = false)
    String meshUuid;
    /**
     * Device UUID
     */
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "uuid")
    String uuid;
    @ColumnInfo(name = "security")
    @Expose
    int security = LOW;
    @ColumnInfo(name = "unicast_address")
    @Expose
    int unicastAddress;
    @ColumnInfo(name = "configured")
    @Expose
    boolean isConfigured;
    @ColumnInfo(name = "device_key")
    @Expose
    byte[] deviceKey;
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
    @Deprecated
    @Ignore
    @Expose(serialize = false)
    Boolean relayFeatureSupported = null;
    @Deprecated
    @Ignore
    @Expose(serialize = false)
    Boolean proxyFeatureSupported = null;
    @Deprecated
    @Ignore
    @Expose(serialize = false)
    Boolean friendFeatureSupported = null;
    @Deprecated
    @Ignore
    @Expose(serialize = false)
    Boolean lowPowerFeatureSupported = null;
    @Embedded
    @Expose
    Features nodeFeatures = null;
    @Embedded
    @Expose
    SparseIntArrayParcelable mSeqAuth = new SparseIntArrayParcelable();
    @Ignore
    @Expose(serialize = false)
    List<Integer> mAddedNetworkKeyIndexes = new ArrayList<>();
    @Ignore
    @Expose
    byte[] identityKey;
    @Deprecated
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
    /**
     * @deprecated use {@link Features} instead
     */
    @Deprecated
    @Ignore
    @Expose(deserialize = false)
    Integer features = null;
    @TypeConverters(MeshTypeConverters.class)
    @Expose
    Map<Integer, Element> mElements = new LinkedHashMap<>();
    @Ignore
    @SerializedName("appKeys")
    @Expose(serialize = false)
    List<Integer> mAddedAppKeyIndexes = new ArrayList<>();
    @Deprecated
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
    @Expose
    int numberOfElements;
    @Ignore
    @Expose(deserialize = false)
    protected String bluetoothDeviceAddress;

    @Ignore
    @Expose(serialize = false)
    private ProxyFilter proxyFilter;

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

    public final int getUnicastAddress() {
        return unicastAddress;
    }

    public final void setUnicastAddress(final int unicastAddress) {
        this.unicastAddress = unicastAddress;
    }

    public final Integer getTtl() {
        return ttl;
    }

    public final void setTtl(final Integer ttl) {
        this.ttl = ttl;
    }

    public final byte[] getIdentityKey() {
        return identityKey;
    }

    /**
     * Returns the key index
     *
     * @return network key index
     * @deprecated Use {@link ProvisionedMeshNode#getAddedNetworkKeys()} instead
     */
    @Deprecated
    public final byte[] getKeyIndex() {
        return keyIndex;
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

    public long getTimeStamp() {
        return mTimeStampInMillis;
    }

    public void setTimeStamp(final long timestamp) {
        mTimeStampInMillis = timestamp;
    }

    /**
     * Returns the {@link SecurityState} of the node
     */
    @SecurityState
    public int getSecurity() {
        return security;
    }

    /**
     * Set security state of the node {@link SecurityState}
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setSecurity(@SecurityState final int security) {
        this.security = security;
    }

    /**
     * Returns true if the node is blacklisted or false otherwise
     */
    public boolean isBlackListed() {
        return blackListed;
    }

    /**
     * Blacklist a node
     *
     * @param blackListed true if blacklisted
     */
    public void setBlackListed(final boolean blackListed) {
        this.blackListed = blackListed;
    }

    /**
     * Returns the {@link SecureNetworkBeacon} beacon of this node
     */
    public Boolean isSecureNetworkBeaconSupported() {
        return secureNetworkBeaconSupported;
    }

    /**
     * Sets the {@link SecureNetworkBeacon} beacon for this node
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setSecureNetworkBeaconSupported(final Boolean secureNetworkBeacon) {
        this.secureNetworkBeaconSupported = secureNetworkBeacon;
    }

    /**
     * Returns {@link NetworkTransmitSettings} of this node
     */
    public NetworkTransmitSettings getNetworkTransmitSettings() {
        return networkTransmitSettings;
    }

    /**
     * Sets {@link NetworkTransmitSettings} of this node
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setNetworkTransmitSettings(final NetworkTransmitSettings networkTransmitSettings) {
        this.networkTransmitSettings = networkTransmitSettings;
    }

    /**
     * Returns {@link RelaySettings} of this node
     */
    public RelaySettings getRelaySettings() {
        return relaySettings;
    }

    /**
     * Sets {@link NetworkTransmitSettings} of this node
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setRelaySettings(final RelaySettings relaySettings) {
        this.relaySettings = relaySettings;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public List<Integer> getAddedAppKeyIndexes() {
        return mAddedAppKeyIndexes;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOW, HIGH})
    public @interface SecurityState {
    }

    /**
     * Returns the {@link ProxyFilter} set on the node
     */
    @Nullable
    public ProxyFilter getProxyFilter() {
        return proxyFilter;
    }

    /**
     * Sets the {@link ProxyFilter} settings on the node
     * <p>
     * Please note that this is not persisted within the node since the filter is reinitialized to a whitelist filter upon connecting to a proxy node.
     * Therefore after setting a proxy filter and disconnecting users will have to manually
     * <p/>
     */
    public void setProxyFilter(@Nullable final ProxyFilter proxyFilter) {
        this.proxyFilter = proxyFilter;
    }
}
