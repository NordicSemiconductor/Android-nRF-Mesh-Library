package no.nordicsemi.android.meshprovisioner.configuration;

import android.os.Parcel;
import android.support.annotation.VisibleForTesting;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.BaseMeshNode;
import no.nordicsemi.android.meshprovisioner.states.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

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
        companyIdentifier = in.readInt();
        productIdentifier = in.readInt();
        versionIdentifier = in.readInt();
        crpl = in.readInt();
        features = in.readInt();
        relayFeatureSupported = in.readByte() != 0;
        proxyFeatureSupported = in.readByte() != 0;
        friendFeatureSupported = in.readByte() != 0;
        lowPowerFeatureSupported = in.readByte() != 0;
        generatedNetworkId = in.createByteArray();
        mElements.putAll(in.readHashMap(Element.class.getClassLoader()));
        mAddedAppKeys = in.readHashMap(String.class.getClassLoader());
        mAddedAppKeyIndexes = in.readArrayList(Integer.class.getClassLoader());
        mTimeStampInMillis = in.readLong();
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
        dest.writeInt(companyIdentifier);
        dest.writeInt(productIdentifier);
        dest.writeInt(versionIdentifier);
        dest.writeInt(crpl);
        dest.writeInt(features);
        dest.writeByte((byte) (relayFeatureSupported ? 1 : 0));
        dest.writeByte((byte) (proxyFeatureSupported ? 1 : 0));
        dest.writeByte((byte) (friendFeatureSupported ? 1 : 0));
        dest.writeByte((byte) (lowPowerFeatureSupported ? 1 : 0));
        dest.writeByteArray(generatedNetworkId);
        dest.writeMap(mElements);
        dest.writeMap(mAddedAppKeys);
        dest.writeList(mAddedAppKeyIndexes);
        dest.writeLong(mTimeStampInMillis);
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
        return mElements;
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

    public final int getCompanyIdentifier() {
        return companyIdentifier;
    }

    public final int getProductIdentifier() {
        return productIdentifier;
    }

    public final int getVersionIdentifier() {
        return versionIdentifier;
    }

    public final int getCrpl() {
        return crpl;
    }

    public final int getFeatures() {
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

    public final List<Integer> getAddedAppKeyIndexes() {
        return Collections.unmodifiableList(mAddedAppKeyIndexes);
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
     * Sets the data from the {@link ConfigModelAppStatus}
     * @param configModelAppStatus Composition data status object
     */
    protected final void setConfigModelAppStatus(final ConfigModelAppStatus configModelAppStatus) {
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

    public final byte[] getConfigurationSrc() {
        return mConfigurationSrc;
    }

}
