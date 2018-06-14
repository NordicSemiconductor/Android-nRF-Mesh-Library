package no.nordicsemi.android.meshprovisioner.configuration;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class MeshModel implements Parcelable {

    protected final int mModelId;
    protected final List<Integer> mBoundAppKeyIndexes = new ArrayList<>();
    protected final Map<Integer, String> mBoundAppKeys = new LinkedHashMap<>();
    protected byte[] publishAddress;
    protected byte[] appKeyIndex;
    protected int credentialFlag;
    protected int publishTtl;
    protected int publishPeriod;
    protected int publishRetransmitCount;
    protected int publishRetransmitIntervalSteps;
    protected List<byte[]> mSubscriptionAddress = new ArrayList<>();

    public MeshModel(final int modelId) {
        this.mModelId = modelId;
    }

    protected MeshModel(final Parcel in) {

        final int modelId = in.readInt();
        if(modelId < Short.MIN_VALUE || modelId > Short.MAX_VALUE) {
            mModelId = modelId;
        } else {
            mModelId = (short)modelId;
        }
        in.readList(mBoundAppKeyIndexes, Integer.class.getClassLoader());
        sortAppKeys(in.readHashMap(String.class.getClassLoader()));
        publishAddress = in.createByteArray();
        appKeyIndex = in.createByteArray();
        credentialFlag = in.readInt();
        publishTtl = in.readInt();
        publishPeriod = in.readInt();
        publishRetransmitIntervalSteps = in.readInt();
        in.readList(mSubscriptionAddress, byte[].class.getClassLoader());
    }

    protected final void parcelMeshModel(final Parcel dest, final int flags){
        dest.writeInt(mModelId);
        dest.writeList(mBoundAppKeyIndexes);
        dest.writeMap(mBoundAppKeys);
        dest.writeByteArray(publishAddress);
        dest.writeByteArray(appKeyIndex);
        dest.writeInt(credentialFlag);
        dest.writeInt(publishTtl);
        dest.writeInt(publishPeriod);
        dest.writeInt(publishRetransmitIntervalSteps);
        dest.writeList(mSubscriptionAddress);
    }

    private void sortAppKeys(final HashMap<Integer, String> unorderedBoundAppKeys){
        final Set<Integer> unorderedKeys =  unorderedBoundAppKeys.keySet();

        final List<Integer> orderedKeys = new ArrayList<>();
        for(int key : unorderedKeys) {
            orderedKeys.add(key);
        }
        Collections.sort(orderedKeys);
        for(int key : orderedKeys) {
            mBoundAppKeys.put(key, unorderedBoundAppKeys.get(key));
        }
    }

    /**
     * Returns the 16-bit model id which could be a SIG Model or a Vendor Model
     *
     * @return modelId
     */
    public abstract int getModelId();

    /**
     * Returns the Bluetooth SIG defined model name
     *
     * @return model name
     */
    public abstract String getModelName();

    /**
     * Returns bound appkey index
     */
    public List<Integer> getBoundAppKeyIndexes() {
        return Collections.unmodifiableList(mBoundAppKeyIndexes);
    }

    protected void setBoundAppKey(final int appKeyIndex, final String appKey) {
        if (!mBoundAppKeyIndexes.contains(appKeyIndex))
            mBoundAppKeyIndexes.add(appKeyIndex);
        mBoundAppKeys.put(appKeyIndex, appKey);
    }

    /**
     * Returns an unmodifiable map of bound app keys for this model.
     * @return LinkedHashMap containing the bound app keys for this model
     */
    public Map<Integer, String> getBoundAppkeys(){
        return Collections.unmodifiableMap(mBoundAppKeys);
    }

    public String getBoundAppKey(final int appKeyIndex) {
        return mBoundAppKeys.get(appKeyIndex);
    }

    public byte[] getPublishAddress() {
        return publishAddress;
    }

    /**
     * Returns the publish address as int
     *
     * @return element address
     */
    public int getPublishAddressInt() {
        return ByteBuffer.wrap(publishAddress).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    public int getCredentialFlag() {
        return credentialFlag;
    }

    public int getPublishTtl() {
        return publishTtl;
    }

    public int getPublishPeriod() {
        return publishPeriod;
    }

    public int getPublishRetransmitCount() {
        return publishRetransmitCount;
    }

    public int getPublishRetransmitIntervalSteps() {
        return publishRetransmitIntervalSteps;
    }


    public List<byte[]> getSubscriptionAddresses() {
        return Collections.unmodifiableList(mSubscriptionAddress);
    }

    /**
     * Sets the data from the {@link ConfigModelAppStatus}
     *
     * @param configMessage Composition data status object
     */
    protected void setSubscriptionAddress(final ConfigModelPublicationStatus configMessage) {
        publishAddress = configMessage.getPublishAddress();
        credentialFlag = configMessage.getCredentialFlag();
        publishTtl = configMessage.getPublishTtl();
        publishPeriod = configMessage.getPublishPeriod();
        publishRetransmitCount = configMessage.getPublishRetransmitCount();
        publishRetransmitIntervalSteps = configMessage.getPublishRetransmitIntervalSteps();
    }

    /**
     * Sets data from the {@link ConfigModelAppStatus}
     */
    protected void setSubscriptionAddress(final byte[] subscriptionAddress) {
        if (subscriptionAddress != null && !checkIfAlreadySubscribed(subscriptionAddress)) {
            mSubscriptionAddress.add(subscriptionAddress);
        }
    }

    /**
     * Sets data from the {@link ConfigModelAppStatus}
     */
    protected void removeSubscriptionAddress(final byte[] subscriptionAddress) {
        if (subscriptionAddress != null) {
            final int index = getIndex(subscriptionAddress);
            if (index > -1) {
                mSubscriptionAddress.remove(index);
            }
        }
    }

    private boolean checkIfAlreadySubscribed(final byte[] subscriptionAddress) {
        for (byte[] address : mSubscriptionAddress) {
            if (Arrays.equals(address, subscriptionAddress))
                return true;
        }
        return false;
    }

    private int getIndex(final byte[] subscriptionAddress) {
        int counter = 0;
        for (byte[] address : mSubscriptionAddress) {
            if (Arrays.equals(address, subscriptionAddress))
                return counter;
            counter++;
        }
        return -1;
    }
}
