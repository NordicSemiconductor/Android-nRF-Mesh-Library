package no.nordicsemi.android.meshprovisioner.configuration;

import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class MeshModel implements Parcelable {

    protected final int mModelId;
    private List<Integer> mBoundAppKeyIndexes = new ArrayList<>();
    private Map<Integer, String> mBoundAppKeys = new LinkedHashMap<>();
    protected byte[] publishAddress;
    protected byte[] appKeyIndex;
    private int credentialFlag;
    private int publishTtl;
    private int publishPeriod;
    private int publishRetransmitCount;
    private int publishRetransmitIntervalSteps;
    private List<byte[]> mSubscriptionAddress = new ArrayList<>();

    public MeshModel(final int modelId) {
        this.mModelId = modelId;
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
