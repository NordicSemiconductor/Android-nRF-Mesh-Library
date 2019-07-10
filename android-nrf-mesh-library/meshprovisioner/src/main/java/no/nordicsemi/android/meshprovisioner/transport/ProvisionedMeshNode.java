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

import android.os.Parcel;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.Features;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.NetworkKey;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.NetworkTransmitSettings;
import no.nordicsemi.android.meshprovisioner.utils.RelaySettings;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;
import no.nordicsemi.android.meshprovisioner.utils.SparseIntArrayParcelable;

import static androidx.room.ForeignKey.CASCADE;

@SuppressWarnings({"WeakerAccess", "unused"})
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

    /**
     * Constructor to be used only by hte library
     *
     * @param unprovisionedMeshNode {@link UnprovisionedMeshNode}
     */
    @Ignore
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public ProvisionedMeshNode(final UnprovisionedMeshNode unprovisionedMeshNode) {
        uuid = unprovisionedMeshNode.getDeviceUuid().toString();
        isConfigured = unprovisionedMeshNode.isConfigured();
        nodeName = unprovisionedMeshNode.getNodeName();
        mAddedNetKeyIndexes.add(unprovisionedMeshNode.getKeyIndex());
        mFlags = unprovisionedMeshNode.getFlags();
        unicastAddress = unprovisionedMeshNode.getUnicastAddress();
        deviceKey = unprovisionedMeshNode.getDeviceKey();
        ttl = unprovisionedMeshNode.getTtl();
        final NetworkKey networkKey = new NetworkKey(unprovisionedMeshNode.getKeyIndex(), unprovisionedMeshNode.getNetworkKey());
        k2Output = SecureUtils.calculateK2(networkKey.getKey(), SecureUtils.K2_MASTER_INPUT);
        mTimeStampInMillis = unprovisionedMeshNode.getTimeStamp();
        numberOfElements = unprovisionedMeshNode.getNumberOfElements();
    }

    /**
     * Constructor to be used only by the library
     *
     * @param provisioner {@link Provisioner}
     * @param netKeys     List of {@link NetworkKey}
     * @param appKeys     List of {@link ApplicationKey}
     */
    @SuppressWarnings("ConstantConditions")
    @Ignore
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public ProvisionedMeshNode(@NonNull final Provisioner provisioner,
                               @NonNull final List<NetworkKey> netKeys,
                               @NonNull final List<ApplicationKey> appKeys) {
        this.meshUuid = provisioner.getMeshUuid();
        uuid = provisioner.getProvisionerUuid();
        isConfigured = true;
        nodeName = provisioner.getProvisionerName();
        for (ApplicationKey key : appKeys) {
            mAddedAppKeyIndexes.add(key.getKeyIndex());
        }
        if (provisioner.getProvisionerAddress() != null)
            unicastAddress = provisioner.getProvisionerAddress();
        sequenceNumber = provisioner.getSequenceNumber();
        deviceKey = SecureUtils.generateRandomNumber();
        ttl = provisioner.getGlobalTtl();
        mTimeStampInMillis = System.currentTimeMillis();
        numberOfElements = 1;
        final MeshModel model = SigModelParser.getSigModel(SigModelParser.CONFIGURATION_CLIENT);
        final HashMap<Integer, MeshModel> models = new HashMap<>();
        models.put(model.getModelId(), model);
        final Element element = new Element(unicastAddress, 0, models);
        final HashMap<Integer, Element> elements = new HashMap<>();
        elements.put(unicastAddress, element);
        mElements = elements;
    }

    @Ignore
    protected ProvisionedMeshNode(Parcel in) {
        uuid = in.readString();
        isConfigured = in.readByte() != 1;
        nodeName = in.readString();
        mAddedNetKeyIndexes = in.readArrayList(Integer.class.getClassLoader());
        mFlags = in.createByteArray();
        unicastAddress = in.readInt();
        deviceKey = in.createByteArray();
        ttl = (Integer) in.readValue(Integer.class.getClassLoader());
        numberOfElements = in.readInt();
        sequenceNumber = in.readInt();
        k2Output = in.readParcelable(SecureUtils.K2Output.class.getClassLoader());
        companyIdentifier = (Integer) in.readValue(Integer.class.getClassLoader());
        productIdentifier = (Integer) in.readValue(Integer.class.getClassLoader());
        versionIdentifier = (Integer) in.readValue(Integer.class.getClassLoader());
        crpl = (Integer) in.readValue(Integer.class.getClassLoader());
        nodeFeatures = (Features) in.readValue(Features.class.getClassLoader());
        generatedNetworkId = in.createByteArray();
        sortElements(in.readHashMap(Element.class.getClassLoader()));
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
        dest.writeList(mAddedNetKeyIndexes);
        dest.writeByteArray(mFlags);
        dest.writeInt(unicastAddress);
        dest.writeByteArray(deviceKey);
        dest.writeValue(ttl);
        dest.writeInt(numberOfElements);
        dest.writeInt(sequenceNumber);
        dest.writeParcelable(k2Output, flags);
        dest.writeValue(companyIdentifier);
        dest.writeValue(productIdentifier);
        dest.writeValue(versionIdentifier);
        dest.writeValue(crpl);
        dest.writeValue(nodeFeatures);
        dest.writeByteArray(generatedNetworkId);
        dest.writeMap(mElements);
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

    public final int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the sequence number
     * <p>This is only meant to be used internally within the library, hence the Restricted</p>
     *
     * @param sequenceNumber sequence number of the node
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public final void setSequenceNumber(final int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
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
    public final List<Integer> getAddedNetKeyIndexes() {
        return Collections.unmodifiableList(mAddedNetKeyIndexes);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public final void setAddedNetKeyIndexes(final List<Integer> addedNetKeyIndexes) {
        mAddedNetKeyIndexes = addedNetKeyIndexes;
    }

    /**
     * Adds a NetKey index that was added to the node
     *
     * @param index NetKey index
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    protected final void setAddedNetKeyIndex(final int index) {
        if (!mAddedNetKeyIndexes.contains(index)) {
            this.mAddedNetKeyIndexes.add(index);
        }
    }

    /**
     * Returns the list of added AppKey indexes to the node
     */
    public final List<Integer> getAddedAppKeyIndexes() {
        return Collections.unmodifiableList(mAddedAppKeyIndexes);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public final void setAddedAppKeyIndexes(final List<Integer> addedAppKeyIndexes) {
        mAddedAppKeyIndexes = addedAppKeyIndexes;
    }

    /**
     * Adds an AppKey index that was added to the node
     *
     * @param index AppKey index
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    protected final void setAddedAppKeyIndex(final int index) {
        if (!mAddedAppKeyIndexes.contains(index)) {
            this.mAddedAppKeyIndexes.add(index);
        }
    }

    /**
     * Sets the data from the {@link ConfigCompositionDataStatus}
     *
     * @param configCompositionDataStatus Composition data status object
     */
    protected final void setCompositionData(@NonNull final ConfigCompositionDataStatus configCompositionDataStatus) {
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
            if (element != null) {
                final int modelIdentifier = configModelAppStatus.getModelIdentifier();
                final MeshModel model = element.getMeshModels().get(modelIdentifier);
                if (model != null) {
                    final int appKeyIndex = configModelAppStatus.getAppKeyIndex();
                    model.setBoundAppKeyIndex(appKeyIndex);
                }
            }
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
            if (element != null) {
                final int modelIdentifier = configModelAppStatus.getModelIdentifier();
                final MeshModel model = element.getMeshModels().get(modelIdentifier);
                final int appKeyIndex = configModelAppStatus.getAppKeyIndex();
                if (model != null) {
                    model.removeBoundAppKeyIndex(appKeyIndex);
                }
            }
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

    public boolean isExist(final int modelId) {
        for (Map.Entry<Integer, Element> elementEntry : mElements.entrySet()) {
            final Element element = elementEntry.getValue();
            for (Map.Entry<Integer, MeshModel> modelEntry : element.getMeshModels().entrySet()) {
                final MeshModel model = modelEntry.getValue();
                if (model != null && model.getModelId() == modelId) {
                    return true;
                }
            }
        }
        return false;
    }
}
