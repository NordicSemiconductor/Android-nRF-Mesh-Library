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

package no.nordicsemi.android.meshprovisioner.configuration;

import android.os.Parcel;
import android.support.annotation.VisibleForTesting;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.nordicsemi.android.meshprovisioner.BaseMeshNode;
import no.nordicsemi.android.meshprovisioner.states.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;
import no.nordicsemi.android.meshprovisioner.utils.SparseIntArrayParcelable;

public class ProvisionedMeshNode extends BaseMeshNode {

    private SecureUtils.K2Output k2Output;

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public ProvisionedMeshNode(){

    }

    public ProvisionedMeshNode(final UnprovisionedMeshNode unprovisionedMeshNode){
        isProvisioned = unprovisionedMeshNode.isProvisioned();
        isConfigured = unprovisionedMeshNode.isConfigured();
        nodeName = unprovisionedMeshNode.getNodeName();
        networkKey = unprovisionedMeshNode.getNetworkKey();
        identityKey = unprovisionedMeshNode.getIdentityKey();
        keyIndex = unprovisionedMeshNode.getKeyIndex();
        mFlags = unprovisionedMeshNode.getFlags();
        ivIndex = unprovisionedMeshNode.getIvIndex();
        unicastAddress = unprovisionedMeshNode.getUnicastAddress();
        deviceKey = unprovisionedMeshNode.getDeviceKey();
        ttl = unprovisionedMeshNode.getTtl();
        k2Output = SecureUtils.calculateK2(networkKey, SecureUtils.K2_MASTER_INPUT);
        mTimeStampInMillis = unprovisionedMeshNode.getTimeStamp();
        mConfigurationSrc = unprovisionedMeshNode.getConfigurationSrc();
        numberOfElements = unprovisionedMeshNode.getNumberOfElements();
    }

    protected ProvisionedMeshNode(Parcel in) {
        isProvisioned = in.readByte() != 0;
        isConfigured = in.readByte() != 0;
        nodeName = in.readString();
        networkKey = in.createByteArray();
        identityKey = in.createByteArray();
        keyIndex = in.createByteArray();
        mFlags = in.createByteArray();
        ivIndex = in.createByteArray();
        unicastAddress = in.createByteArray();
        deviceKey = in.createByteArray();
        ttl = in.readInt();
        mReceivedSequenceNumber = in.readInt();
        bluetoothAddress = in.readString();
        k2Output = in.readParcelable(SecureUtils.K2Output.class.getClassLoader());
        nodeIdentifier = in.readString();
        companyIdentifier = (Integer) in.readValue(Integer.class.getClassLoader());
        productIdentifier = (Integer) in.readValue(Integer.class.getClassLoader());
        versionIdentifier = (Integer) in.readValue(Integer.class.getClassLoader());
        crpl = (Integer) in.readValue(Integer.class.getClassLoader());
        features = (Integer) in.readValue(Integer.class.getClassLoader());
        relayFeatureSupported = in.readByte() != 0;
        proxyFeatureSupported = in.readByte() != 0;
        friendFeatureSupported = in.readByte() != 0;
        lowPowerFeatureSupported = in.readByte() != 0;
        generatedNetworkId = in.createByteArray();
        sortElements(in.readHashMap(Element.class.getClassLoader()));
        mAddedAppKeys = in.readHashMap(String.class.getClassLoader());
        mAddedAppKeyIndexes = in.readArrayList(Integer.class.getClassLoader());
        mTimeStampInMillis = in.readLong();
        mConfigurationSrc = in.createByteArray();
        mSeqAuth = in.readParcelable(SparseIntArrayParcelable.class.getClassLoader());
        numberOfElements = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isProvisioned ? 1 : 0));
        dest.writeByte((byte) (isConfigured ? 1 : 0));
        dest.writeString(nodeName);
        dest.writeByteArray(networkKey);
        dest.writeByteArray(identityKey);
        dest.writeByteArray(keyIndex);
        dest.writeByteArray(mFlags);
        dest.writeByteArray(ivIndex);
        dest.writeByteArray(unicastAddress);
        dest.writeByteArray(deviceKey);
        dest.writeInt(ttl);
        dest.writeInt(mReceivedSequenceNumber);
        dest.writeString(bluetoothAddress);
        dest.writeParcelable(k2Output, flags);
        dest.writeString(nodeIdentifier);
        dest.writeValue(companyIdentifier);
        dest.writeValue(productIdentifier);
        dest.writeValue(versionIdentifier);
        dest.writeValue(crpl);
        dest.writeValue(features);
        dest.writeByte((byte) (relayFeatureSupported ? 1 : 0));
        dest.writeByte((byte) (proxyFeatureSupported ? 1 : 0));
        dest.writeByte((byte) (friendFeatureSupported ? 1 : 0));
        dest.writeByte((byte) (lowPowerFeatureSupported ? 1 : 0));
        dest.writeByteArray(generatedNetworkId);
        dest.writeMap(mElements);
        dest.writeMap(mAddedAppKeys);
        dest.writeList(mAddedAppKeyIndexes);
        dest.writeLong(mTimeStampInMillis);
        dest.writeByteArray(mConfigurationSrc);
        dest.writeParcelable(mSeqAuth, flags);
        dest.writeInt(numberOfElements);
    }


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

    @Override
    public int describeContents() {
        return 0;
    }

    public final int getTtl() {
        return ttl;
    }

    public final void setTtl(final int ttl) {
        this.ttl = ttl;
    }

    public final Map<Integer, Element> getElements() {
        return Collections.unmodifiableMap(mElements);
    }

    public final byte[] getDeviceKey() {
        return deviceKey;
    }

    protected void setDeviceKey(final byte[] deviceKey) {
        this.deviceKey = deviceKey;
    }

    public final int getSequenceNumber() {
        return mReceivedSequenceNumber;
    }

    public final void setSequenceNumber(final int receivedSequenceNumber) {
        mReceivedSequenceNumber = receivedSequenceNumber;
    }

    public SecureUtils.K2Output getK2Output() {
        return k2Output;
    }

    public final void setK2Ouput(final SecureUtils.K2Output k2Output) {
        this.k2Output = k2Output;
    }

    public final Integer getCompanyIdentifier() {
        return companyIdentifier;
    }

    public final Integer getProductIdentifier() {
        return productIdentifier;
    }

    public final Integer getVersionIdentifier() {
        return versionIdentifier;
    }

    public final Integer getCrpl() {
        return crpl;
    }

    public final Integer getFeatures() {
        return features;
    }

    public final boolean isRelayFeatureSupported() {
        return relayFeatureSupported;
    }

    public final boolean isProxyFeatureSupported() {
        return proxyFeatureSupported;
    }

    public final boolean isFriendFeatureSupported() {
        return friendFeatureSupported;
    }

    public final boolean isLowPowerFeatureSupported() {
        return lowPowerFeatureSupported;
    }

    public final void setBluetoothDeviceAddress(final String address) {
        this.bluetoothAddress = address;
    }

    public final String getNodeIdentifier() {
        return nodeIdentifier;
    }

    public final void setNodeIdentifier(final String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    public final Map<Integer, String> getAddedAppKeys() {
        return Collections.unmodifiableMap(mAddedAppKeys);
    }

    protected final void setAddedAppKey(final int index, final String appKey) {
        this.mAddedAppKeys.put(index, appKey);
    }

    public final byte[] getGeneratedNetworkId() {
        return generatedNetworkId;
    }

    public final void setGeneratedNetworkId(final byte[] advertisedNetworkId) {
        this.generatedNetworkId = advertisedNetworkId;
    }

    /**
     * Sets the data from the {@link ConfigCompositionDataStatus}
     * @param configCompositionDataStatus Composition data status object
     */
    protected final void setCompositionData(final ConfigCompositionDataStatus configCompositionDataStatus) {
        if (configCompositionDataStatus != null) {
            companyIdentifier = configCompositionDataStatus.getCompanyIdentifier();
            productIdentifier = configCompositionDataStatus.getProductIdentifier();
            versionIdentifier = configCompositionDataStatus.getVersionIdentifier();
            crpl = configCompositionDataStatus.getCrpl();
            features = configCompositionDataStatus.getFeatures();
            relayFeatureSupported = configCompositionDataStatus.isRelayFeatureSupported();
            proxyFeatureSupported = configCompositionDataStatus.isProxyFeatureSupported();
            friendFeatureSupported = configCompositionDataStatus.isFriendFeatureSupported();
            lowPowerFeatureSupported = configCompositionDataStatus.isLowPowerFeatureSupported();
            mElements.putAll(configCompositionDataStatus.getElements());
        }
    }

    /**
     * Sets the bound app key data from the {@link ConfigModelAppStatus}
     * @param configModelAppStatus ConfigModelAppStatus contaiing the bound app key information
     */
    protected final void setAppKeyBindStatus(final ConfigModelAppStatus configModelAppStatus) {
        if (configModelAppStatus != null) {
            if (configModelAppStatus.isSuccessful()) {
                final Element element = mElements.get(configModelAppStatus.getElementAddressInt());
                final int modelIdentifier = configModelAppStatus.getModelIdentifierInt();
                final MeshModel model = element.getMeshModels().get(modelIdentifier);
                final int appKeyIndex = configModelAppStatus.getAppKeyIndexInt();
                final String appKey = mAddedAppKeys.get(appKeyIndex);
                model.setBoundAppKey(appKeyIndex, appKey);
            }
        }
    }

    /**
     * Sets the unbind app key data from the {@link ConfigModelAppStatus}
     * @param configModelAppStatus ConfigModelAppStatus containing the unbound app key information
     */
    protected final void setAppKeyUnbindStatus(final ConfigModelAppStatus configModelAppStatus) {
        if (configModelAppStatus != null) {
            if (configModelAppStatus.isSuccessful()) {
                final Element element = mElements.get(configModelAppStatus.getElementAddressInt());
                final int modelIdentifier = configModelAppStatus.getModelIdentifierInt();
                final MeshModel model = element.getMeshModels().get(modelIdentifier);
                final int appKeyIndex = configModelAppStatus.getAppKeyIndexInt();
                final String appKey = mAddedAppKeys.get(appKeyIndex);
                model.removeBoundAppKey(appKeyIndex, appKey);
            }

        }
    }

    private void sortElements(final HashMap<Integer, Element> unorderedElements){
        final Set<Integer> unorderedKeys =  unorderedElements.keySet();

        final List<Integer> orderedKeys = new ArrayList<>(unorderedKeys);
        Collections.sort(orderedKeys);
        for(int key : orderedKeys) {
            mElements.put(key, unorderedElements.get(key));
        }
    }

    public void setSeqAuth(final byte[] src, final int seqAuth) {
        final int srcAddress = ((src[0] << 8) & 0xFF) | src[1];
        mSeqAuth.put(srcAddress, seqAuth);
    }

    public Integer getSeqAuth(final byte[] src) {
        if(mSeqAuth.size() == 0) {
            return null;
        }

        final int srcAddress = ((src[0] & 0xFF) << 8) | (src[1] & 0xFF) ;
        return (int) mSeqAuth.get(srcAddress);
    }
}
