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

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.nordicsemi.android.meshprovisioner.Features;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.NetworkTransmitSettings;
import no.nordicsemi.android.meshprovisioner.utils.RelaySettings;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;
import no.nordicsemi.android.meshprovisioner.utils.SparseIntArrayParcelable;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@SuppressWarnings({"WeakerAccess", "unused", "deprecation"})
@Entity(tableName = "nodes",
        foreignKeys = @ForeignKey(entity = MeshNetwork.class,
                parentColumns = "mesh_uuid",
                childColumns = "mesh_uuid",
                onUpdate = CASCADE, onDelete = CASCADE),
        indices = @Index("mesh_uuid"))
public final class ProvisionedMeshNode extends ProvisionedBaseMeshNode {

    @Ignore
    @Expose
    private SecureUtils.K2Output k2Output;

    public static final Creator<ProvisionedMeshNode> CREATOR = new Creator<ProvisionedMeshNode>() {
        @Override
        public ProvisionedMeshNode createFromParcel(Parcel in) {
            return new ProvisionedMeshNode(in);
        }

        @Override
        public ProvisionedMeshNode[] newArray(int size) {
            return new ProvisionedMeshNode[size];
        }
    };

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public ProvisionedMeshNode() {
    }

    @Ignore
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public ProvisionedMeshNode(final UnprovisionedMeshNode unprovisionedMeshNode) {
        uuid = unprovisionedMeshNode.getDeviceUuid().toString();
        isConfigured = unprovisionedMeshNode.isConfigured();
        nodeName = unprovisionedMeshNode.getNodeName();
        networkKey = unprovisionedMeshNode.getNetworkKey();
        final NetworkKey networkKey = new NetworkKey(unprovisionedMeshNode.getKeyIndex(), unprovisionedMeshNode.getNetworkKey());
        mAddedNetworkKeys.add(networkKey);
        identityKey = unprovisionedMeshNode.getIdentityKey();
        mFlags = unprovisionedMeshNode.getFlags();
        unicastAddress = unprovisionedMeshNode.getUnicastAddress();
        deviceKey = unprovisionedMeshNode.getDeviceKey();
        ttl = unprovisionedMeshNode.getTtl();
        k2Output = SecureUtils.calculateK2(networkKey.getKey(), SecureUtils.K2_MASTER_INPUT);
        mTimeStampInMillis = unprovisionedMeshNode.getTimeStamp();
        numberOfElements = unprovisionedMeshNode.getNumberOfElements();
    }

    @Ignore
    protected ProvisionedMeshNode(Parcel in) {
        uuid = in.readString();
        isConfigured = in.readByte() != 1;
        nodeName = in.readString();
        mAddedNetworkKeyIndexes = in.readArrayList(Integer.class.getClassLoader());
        mAddedNetworkKeys = in.readArrayList(NetworkKey.class.getClassLoader());
        mFlags = in.createByteArray();
        unicastAddress = in.readInt();
        deviceKey = in.createByteArray();
        ttl = (Integer) in.readValue(Integer.class.getClassLoader());
        numberOfElements = in.readInt();
        mReceivedSequenceNumber = in.readInt();
        k2Output = in.readParcelable(SecureUtils.K2Output.class.getClassLoader());
        companyIdentifier = (Integer) in.readValue(Integer.class.getClassLoader());
        productIdentifier = (Integer) in.readValue(Integer.class.getClassLoader());
        versionIdentifier = (Integer) in.readValue(Integer.class.getClassLoader());
        crpl = (Integer) in.readValue(Integer.class.getClassLoader());
        nodeFeatures = (Features) in.readValue(Features.class.getClassLoader());
        generatedNetworkId = in.createByteArray();
        sortElements(in.readHashMap(Element.class.getClassLoader()));
        mAddedApplicationKeys = in.readHashMap(ApplicationKey.class.getClassLoader());
        mAddedAppKeyIndexes = in.readArrayList(Integer.class.getClassLoader());
        mTimeStampInMillis = in.readLong();
        mSeqAuth = in.readParcelable(SparseIntArrayParcelable.class.getClassLoader());
        secureNetworkBeaconSupported = (Boolean) in.readValue(Boolean.class.getClassLoader());
        networkTransmitSettings = in.readParcelable(NetworkTransmitSettings.class.getClassLoader());
        relaySettings = in.readParcelable(RelaySettings.class.getClassLoader());
        blackListed = in.readInt() != 1;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeByte((byte) (isConfigured ? 1 : 0));
        dest.writeString(nodeName);
        dest.writeList(mAddedNetworkKeyIndexes);
        dest.writeList(mAddedNetworkKeys);
        dest.writeByteArray(mFlags);
        dest.writeInt(unicastAddress);
        dest.writeByteArray(deviceKey);
        dest.writeValue(ttl);
        dest.writeInt(numberOfElements);
        dest.writeInt(mReceivedSequenceNumber);
        dest.writeParcelable(k2Output, flags);
        dest.writeValue(companyIdentifier);
        dest.writeValue(productIdentifier);
        dest.writeValue(versionIdentifier);
        dest.writeValue(crpl);
        dest.writeValue(nodeFeatures);
        dest.writeByteArray(generatedNetworkId);
        dest.writeMap(mElements);
        dest.writeMap(mAddedApplicationKeys);
        dest.writeList(mAddedAppKeyIndexes);
        dest.writeLong(mTimeStampInMillis);
        dest.writeParcelable(mSeqAuth, flags);
        dest.writeValue(secureNetworkBeaconSupported);
        dest.writeParcelable(networkTransmitSettings, flags);
        dest.writeParcelable(relaySettings, flags);
        dest.writeInt((blackListed ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public final Map<Integer, Element> getElements() {
        return mElements;
    }

    /**
     * Check if an unicast address is the address of an element
     *
     * @param unicastAddress the address to check
     * @return if this address is the address of an element
     */
    public final boolean hasUnicastAddress(final int unicastAddress) {
        if (unicastAddress == getUnicastAddress())
            return true;
        for (Element element : mElements.values()) {
            if (element.getElementAddress() == unicastAddress)
                return true;
        }
        return false;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public final void setElements(final Map<Integer, Element> elements) {
        mElements = elements;
    }

    public final byte[] getDeviceKey() {
        return deviceKey;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setDeviceKey(final byte[] deviceKey) {
        this.deviceKey = deviceKey;
    }

    public final int getReceivedSequenceNumber() {
        return mReceivedSequenceNumber;
    }

    /**
     * Sets the received sequence number
     * <p>This is only meant to be used internally within the library, hence the Restricted annotation</p>
     *
     * @param receivedSequenceNumber sequence number of the message received from a node
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public final void setReceivedSequenceNumber(final int receivedSequenceNumber) {
        mReceivedSequenceNumber = receivedSequenceNumber;
    }

    public final SecureUtils.K2Output getK2Output() {
        return k2Output;
    }

    final void setK2Output(final SecureUtils.K2Output k2Output) {
        this.k2Output = k2Output;
    }

    public final Integer getCompanyIdentifier() {
        return companyIdentifier;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public final void setCompanyIdentifier(final Integer companyIdentifier) {
        this.companyIdentifier = companyIdentifier;
    }

    public final Integer getProductIdentifier() {
        return productIdentifier;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public final void setProductIdentifier(final Integer productIdentifier) {
        this.productIdentifier = productIdentifier;
    }

    public final Integer getVersionIdentifier() {
        return versionIdentifier;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public final void setVersionIdentifier(final Integer versionIdentifier) {
        this.versionIdentifier = versionIdentifier;
    }

    public final Integer getCrpl() {
        return crpl;
    }

    public final void setCrpl(final Integer crpl) {
        this.crpl = crpl;
    }

    /**
     * @deprecated Use {@link #getNodeFeatures()} instead
     */
    @Deprecated
    public final Integer getFeatures() {
        return features;
    }

    /**
     * Returns the {@link Features} of the node
     */
    public final Features getNodeFeatures() {
        return nodeFeatures;
    }

    /**
     * Set {@link Features} of the node
     *
     * @param features feature set supported by the node
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public final void setNodeFeatures(final Features features) {
        this.nodeFeatures = features;
    }

    /**
     * @deprecated use {@link #getNodeFeatures()} instead
     */
    @Deprecated
    public final Boolean isRelayFeatureSupported() {
        return relayFeatureSupported;
    }

    /**
     * @deprecated use {@link #getNodeFeatures()} instead
     */
    public final Boolean isProxyFeatureSupported() {
        return proxyFeatureSupported;
    }

    /**
     * @deprecated use {@link #getNodeFeatures()} instead
     */
    public final Boolean isFriendFeatureSupported() {
        return friendFeatureSupported;
    }

    /**
     * @deprecated use {@link #getNodeFeatures()} instead
     */
    public final Boolean isLowPowerFeatureSupported() {
        return lowPowerFeatureSupported;
    }

    /**
     * Sets the low power feature supported state
     *
     * @deprecated use {@link Features#getLowPower()} to get the enumerated states of the features
     */
    @Deprecated
    public final void setLowPowerFeatureSupported(final Boolean supported) {
        lowPowerFeatureSupported = supported;
    }

    /**
     * Returns the number of elements in the node
     */
    public int getNumberOfElements() {
        if (numberOfElements > mElements.size()) {
            return numberOfElements;
        }

        return mElements.size();
    }

    /**
     * Returns the list of Network keys added to this node
     */
    public List<NetworkKey> getAddedNetworkKeys() {
        return mAddedNetworkKeys;
    }

    public final Map<Integer, ApplicationKey> getAddedApplicationKeys() {
        return mAddedApplicationKeys;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public final void setAddedApplicationKeys(final Map<Integer, ApplicationKey> applicationKeys) {
        mAddedApplicationKeys = applicationKeys;
    }

    protected final void setAddedAppKey(final int index, final ApplicationKey appKey) {
        this.mAddedApplicationKeys.put(index, appKey);
    }

    /**
     * Sets the data from the {@link ConfigCompositionDataStatus}
     *
     * @param configCompositionDataStatus Composition data status object
     */
    protected final void setCompositionData(@NonNull final ConfigCompositionDataStatus configCompositionDataStatus) {
        if (configCompositionDataStatus != null) {
            companyIdentifier = configCompositionDataStatus.getCompanyIdentifier();
            productIdentifier = configCompositionDataStatus.getProductIdentifier();
            versionIdentifier = configCompositionDataStatus.getVersionIdentifier();
            crpl = configCompositionDataStatus.getCrpl();
            final boolean relayFeatureSupported = configCompositionDataStatus.isRelayFeatureSupported();
            final boolean proxyFeatureSupported = configCompositionDataStatus.isProxyFeatureSupported();
            final boolean friendFeatureSupported = configCompositionDataStatus.isFriendFeatureSupported();
            final boolean lowPowerFeatureSupported = configCompositionDataStatus.isLowPowerFeatureSupported();
            nodeFeatures = new Features(friendFeatureSupported ? Features.ENABLED : Features.UNSUPPORTED,
                    lowPowerFeatureSupported ? Features.ENABLED : Features.UNSUPPORTED,
                    proxyFeatureSupported ? Features.ENABLED : Features.UNSUPPORTED,
                    relayFeatureSupported ? Features.ENABLED : Features.UNSUPPORTED);
            mElements.putAll(configCompositionDataStatus.getElements());
        }
    }

    private int getFeatureState(final Boolean feature) {
        if (feature != null && feature) {
            return 2;
        }
        return 0;
    }

    /**
     * Sets the bound app key data from the {@link ConfigModelAppStatus}
     *
     * @param configModelAppStatus ConfigModelAppStatus containing the bound app key information
     */
    protected final void setAppKeyBindStatus(@NonNull final ConfigModelAppStatus configModelAppStatus) {
        if (configModelAppStatus.isSuccessful()) {
            final Element element = mElements.get(configModelAppStatus.getElementAddress());
            final int modelIdentifier = configModelAppStatus.getModelIdentifier();
            final MeshModel model = element.getMeshModels().get(modelIdentifier);
            final int appKeyIndex = configModelAppStatus.getAppKeyIndex();
            final ApplicationKey appKey = mAddedApplicationKeys.get(appKeyIndex);
            model.setBoundAppKey(appKeyIndex, appKey);
        }
    }

    /**
     * Sets the unbind app key data from the {@link ConfigModelAppStatus}
     *
     * @param configModelAppStatus ConfigModelAppStatus containing the unbound app key information
     */
    protected final void setAppKeyUnbindStatus(@NonNull final ConfigModelAppStatus configModelAppStatus) {
        if (configModelAppStatus.isSuccessful()) {
            final Element element = mElements.get(configModelAppStatus.getElementAddress());
            final int modelIdentifier = configModelAppStatus.getModelIdentifier();
            final MeshModel model = element.getMeshModels().get(modelIdentifier);
            final int appKeyIndex = configModelAppStatus.getAppKeyIndex();
            model.removeBoundAppKey(appKeyIndex);
        }

    }

    private void sortElements(final HashMap<Integer, Element> unorderedElements) {
        final Set<Integer> unorderedKeys = unorderedElements.keySet();

        final List<Integer> orderedKeys = new ArrayList<>(unorderedKeys);
        Collections.sort(orderedKeys);
        for (int key : orderedKeys) {
            mElements.put(key, unorderedElements.get(key));
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    void setSeqAuth(final int src, final int seqAuth) {
        mSeqAuth.put(src, seqAuth);
    }

    public Integer getSeqAuth(final int src) {
        if (mSeqAuth.size() == 0) {
            return null;
        }

        return mSeqAuth.get(src);
    }

    /**
     * Method for migrating old network key data
     */
    @SuppressWarnings("unused")
    private void tempMigrateNetworkKey() {
        if (networkKey != null) {
            netKeyIndex = MeshParserUtils.removeKeyIndexPadding(keyIndex);
            NetworkKey netKey = new NetworkKey(netKeyIndex, networkKey);
            mAddedNetworkKeys.add(netKey);
        }
    }

    /**
     * Method for migrating old Application key data
     */
    @SuppressWarnings("unused")
    private void tempMigrateAddedApplicationKeys() {
        for (Map.Entry<Integer, String> entry : mAddedAppKeys.entrySet()) {
            if (entry.getValue() != null) {
                final ApplicationKey applicationKey = new ApplicationKey(entry.getKey(), MeshParserUtils.toByteArray(entry.getValue()));
                mAddedApplicationKeys.put(applicationKey.getKeyIndex(), applicationKey);
            }
        }
    }

    /**
     * Method for migrating old Application key data
     */
    @SuppressWarnings("unused")
    private void tempMigrateBoundApplicationKeys() {
        for (Map.Entry<Integer, Element> elementEntry : mElements.entrySet()) {
            if (elementEntry.getValue() != null) {
                final Element element = elementEntry.getValue();
                for (Map.Entry<Integer, MeshModel> modelEntry : element.getMeshModels().entrySet()) {
                    if (modelEntry.getValue() != null) {
                        final MeshModel meshModel = modelEntry.getValue();
                        for (Map.Entry<Integer, String> appKeyEntry : meshModel.getBoundAppkeys().entrySet()) {
                            final int keyIndex = appKeyEntry.getKey();
                            final byte[] key = MeshParserUtils.toByteArray(appKeyEntry.getValue());
                            final ApplicationKey applicationKey = new ApplicationKey(keyIndex, key);
                            //meshModel.mBoundApplicationKeys.put(keyIndex, applicationKey);
                        }
                    }
                }
            }
        }
    }

    /**
     * Method for migrating old Application key data
     */
    @SuppressWarnings("unused")
    private void tempMigrateSubscriptions() {
        for (Map.Entry<Integer, Element> elementEntry : mElements.entrySet()) {
            if (elementEntry.getValue() != null) {
                final Element element = elementEntry.getValue();
                for (Map.Entry<Integer, MeshModel> modelEntry : element.getMeshModels().entrySet()) {
                    if (modelEntry.getValue() != null) {
                        final MeshModel meshModel = modelEntry.getValue();
                        meshModel.mSubscriptionAddress.addAll(meshModel.getSubscriptionAddresses());
                    }
                }
            }
        }
    }
}
