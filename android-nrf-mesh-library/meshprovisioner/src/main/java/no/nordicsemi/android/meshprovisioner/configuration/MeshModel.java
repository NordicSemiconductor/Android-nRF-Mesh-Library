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
    protected byte[] publishAppKeyIndex;
    protected int credentialFlag;
    protected int publishTtl = 255;
    protected int publishPeriod = 0;
    protected int publishRetransmitCount = 1;
    protected int publishRetransmitIntervalSteps = 1;
    protected List<byte[]> mSubscriptionAddress = new ArrayList<>();
    private int publicationSteps = 0;
    private int publicationResolution = 0b00;

    public MeshModel(final int modelId) {
        this.mModelId = modelId;
    }

    protected MeshModel(final Parcel in) {

        final int modelId = in.readInt();
        if (modelId < Short.MIN_VALUE || modelId > Short.MAX_VALUE) {
            mModelId = modelId;
        } else {
            mModelId = (short) modelId;
        }
        in.readList(mBoundAppKeyIndexes, Integer.class.getClassLoader());
        sortAppKeys(in.readHashMap(String.class.getClassLoader()));
        publishAddress = in.createByteArray();
        publishAppKeyIndex = in.createByteArray();
        credentialFlag = in.readInt();
        publishTtl = in.readInt();
        publishPeriod = in.readInt();
        publicationSteps =  publishPeriod >> 6;
        publicationResolution = publishPeriod & 0x03;
        publishRetransmitIntervalSteps = in.readInt();
        in.readList(mSubscriptionAddress, byte[].class.getClassLoader());
    }

    protected final void parcelMeshModel(final Parcel dest, final int flags) {
        dest.writeInt(mModelId);
        dest.writeList(mBoundAppKeyIndexes);
        dest.writeMap(mBoundAppKeys);
        dest.writeByteArray(publishAddress);
        dest.writeByteArray(publishAppKeyIndex);
        dest.writeInt(credentialFlag);
        dest.writeInt(publishTtl);
        dest.writeInt(publishPeriod);
        dest.writeInt(publishRetransmitIntervalSteps);
        dest.writeList(mSubscriptionAddress);
    }

    private void sortAppKeys(final HashMap<Integer, String> unorderedBoundAppKeys) {
        final Set<Integer> unorderedKeys = unorderedBoundAppKeys.keySet();

        final List<Integer> orderedKeys = new ArrayList<>(unorderedKeys);
        Collections.sort(orderedKeys);
        for (int key : orderedKeys) {
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

    protected void removeBoundAppKey(final int appKeyIndex, final String appKey) {
        if (mBoundAppKeyIndexes.contains(appKeyIndex)) {
            final int position = mBoundAppKeyIndexes.indexOf(appKeyIndex);
            mBoundAppKeyIndexes.remove(position);
        }
        mBoundAppKeys.remove(appKeyIndex);
    }

    /**
     * Returns an unmodifiable map of bound app keys for this model.
     *
     * @return LinkedHashMap containing the bound app keys for this model
     */
    public Map<Integer, String> getBoundAppkeys() {
        return Collections.unmodifiableMap(mBoundAppKeys);
    }

    public String getBoundAppKey(final int appKeyIndex) {
        return mBoundAppKeys.get(appKeyIndex);
    }

    public byte[] getPublishAddress() {
        return publishAddress;
    }


    /**
     * Get publish app key index
     */
    public byte[] getPublishAppKeyIndex() {
        return publishAppKeyIndex;
    }

    /**
     * Get publish app key index
     */
    public Integer getPublishAppKeyIndexInt() {
        if(publishAppKeyIndex != null)
            return (int)ByteBuffer.wrap(publishAppKeyIndex).order(ByteOrder.BIG_ENDIAN).getShort();

        return null;
    }

    /**
     * Set publish app key index
     *
     * @param publishAppKeyIndex app key index used for publication settings
     */
    protected void setPublishAppKeyIndex(final byte[] publishAppKeyIndex) {
        this.publishAppKeyIndex = publishAppKeyIndex;
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
        return publishTtl & 0xFF;
    }

    public int getPublicationSteps() {
        return publicationSteps;
    }

    public int getPublicationResolution() {
        return publicationResolution;
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
     * @param publicationStatus Composition data status object
     */
    protected void setPublicationStatus(final ConfigModelPublicationStatus publicationStatus) {
        publishAddress = publicationStatus.getPublishAddress();
        publishAppKeyIndex = publicationStatus.getPublicationAppKeyIndex();
        credentialFlag = publicationStatus.getCredentialFlag();
        publishTtl = publicationStatus.getPublishTtl();
        publishPeriod = publicationStatus.getPublishPeriod();
        publicationSteps =  publishPeriod >> 6;
        publicationResolution = publishPeriod & 0x03;
        publishRetransmitCount = publicationStatus.getPublishRetransmitCount();
        publishRetransmitIntervalSteps = publicationStatus.getPublishRetransmitIntervalSteps();
    }

    /**
     * Sets data from the {@link ConfigModelAppStatus}
     */
    protected void setPublicationStatus(final byte[] subscriptionAddress) {
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
