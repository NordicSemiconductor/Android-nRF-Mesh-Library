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

package no.nordicsemi.android.mesh.transport;

import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import no.nordicsemi.android.mesh.Features;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.MeshTypeConverters;
import no.nordicsemi.android.mesh.NodeKey;
import no.nordicsemi.android.mesh.SecureNetworkBeacon;
import no.nordicsemi.android.mesh.utils.NetworkTransmitSettings;
import no.nordicsemi.android.mesh.utils.RelaySettings;
import no.nordicsemi.android.mesh.utils.SparseIntArrayParcelable;

@SuppressWarnings({"WeakerAccess"})
abstract class ProvisionedBaseMeshNode implements Parcelable {

    public static final int DISABLED = 0;
    public static final int ENABLED = 1;
    public static final int UNSUPPORTED = 2;
    public static final int LOW = 0; //Low security
    public static final int HIGH = 1; //High security
    protected static final String TAG = ProvisionedBaseMeshNode.class.getSimpleName();
    @ColumnInfo(name = "timestamp")
    @Expose
    public long mTimeStampInMillis;
    @ColumnInfo(name = "name")
    @Expose
    protected String nodeName = "My Node";
    @ColumnInfo(name = "ttl")
    @Expose
    protected Integer ttl = 5;
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
    int sequenceNumber = 0;
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
    @Embedded
    @Expose
    Features nodeFeatures = null;
    @Ignore
    @Expose
    SparseIntArrayParcelable mSeqAuth = new SparseIntArrayParcelable();
    @TypeConverters(MeshTypeConverters.class)
    @SerializedName("netKeys")
    @ColumnInfo(name = "netKeys")
    @Expose
    List<NodeKey> mAddedNetKeys = new ArrayList<>();
    @TypeConverters(MeshTypeConverters.class)
    @SerializedName("appKeys")
    @ColumnInfo(name = "appKeys")
    @Expose
    List<NodeKey> mAddedAppKeys = new ArrayList<>();
    @Ignore
    @Expose
    byte[] mFlags;
    @ColumnInfo(name = "elements")
    @TypeConverters(MeshTypeConverters.class)
    @Expose
    Map<Integer, Element> mElements = new LinkedHashMap<>();
    @ColumnInfo(name = "excluded")
    boolean excluded = false;
    @Ignore
    @Expose(serialize = false, deserialize = false)
    @NodeIdentityState
    int nodeIdentityState;

    @RestrictTo(RestrictTo.Scope.LIBRARY)
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

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public final void setNodeName(final String nodeName) {
        if (!TextUtils.isEmpty(nodeName))
            this.nodeName = nodeName;
    }

    public final int getUnicastAddress() {
        return unicastAddress;
    }

    /**
     * Sets the unicast address of the node
     * <p>This is to be used only by the library</p>
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public final void setUnicastAddress(final int unicastAddress) {
        this.unicastAddress = unicastAddress;
    }

    /**
     * Returns the number of elements in the node
     */
    public int getNumberOfElements() {
        return mElements.size();
    }

    /**
     * Returns the unicast address used by the last element in the node
     */
    public int getLastUnicastAddress() {
        final int elementCount = getNumberOfElements();
        return elementCount == 1 ? unicastAddress : (unicastAddress + (elementCount - 1));
    }

    public final Integer getTtl() {
        return ttl;
    }

    public final void setTtl(final Integer ttl) {
        this.ttl = ttl;
    }

    public final byte[] getFlags() {
        return mFlags;
    }

    public final void setFlags(final byte[] flags) {
        this.mFlags = flags;
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
     * Returns the {@link SecureNetworkBeacon} beacon of this node
     */
    public Boolean isSecureNetworkBeaconSupported() {
        return secureNetworkBeaconSupported;
    }

    /**
     * Sets the {@link SecureNetworkBeacon} beacon for this node
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setSecureNetworkBeaconSupported(final Boolean enable) {
        this.secureNetworkBeaconSupported = enable;
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

    /**
     * Returns true if the node is marked as excluded.
     *
     * @return true if marked as excluded or false otherwise.
     */
    public boolean isExcluded() {
        return excluded;
    }

    /**
     * Returns the node {@link NodeIdentityState}.
     */
    @NodeIdentityState
    public int getNodeIdentityState() {
        return nodeIdentityState;
    }

    /**
     * Marks a node as excluded. Note that to exclude a node from a network, users must call
     * {@link MeshNetwork#excludeNode(ProvisionedMeshNode)}
     *
     * @param excluded true if the node is to be excluded or false otherwise
     */
    public void setExcluded(final boolean excluded) {
        this.excluded = excluded;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOW, HIGH})
    public @interface SecurityState {
    }

    /**
     * Secure Network Beacon state determines if a node is periodically broadcasting Secure Network Beacons.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DISABLED, ENABLED})
    public @interface SecureNetworkBeaconState {
    }

    /**
     * The Node Identity state determines if a node is advertising with Node Identity messages on a subnet.
     * If the Mesh Proxy Service is exposed, the node can be configured to advertise with Node Identity on a subnet.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DISABLED, ENABLED, UNSUPPORTED})
    public @interface NodeIdentityState {
    }
}
