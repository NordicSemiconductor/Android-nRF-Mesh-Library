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
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.PublicationSettings;


@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class MeshModel implements Parcelable {

    @Expose
    final List<Integer> mBoundAppKeyIndexes = new ArrayList<>();
    @Expose(serialize = false)
    final Map<Integer, String> mBoundAppKeys = new LinkedHashMap<>();
    @Expose
    final Map<Integer, ApplicationKey> mBoundApplicationKeys = new LinkedHashMap<>();
    @Expose
    final List<byte[]> mSubscriptionAddress = new ArrayList<>();
    @Expose
    protected int mModelId;
    @Expose
    PublicationSettings mPublicationSettings;

    public MeshModel(final int modelId) {
        this.mModelId = modelId;
    }

    MeshModel() {

    }

    @SuppressWarnings("unchecked")
    protected MeshModel(final Parcel in) {

        final int modelId = in.readInt();
        if (modelId < Short.MIN_VALUE || modelId > Short.MAX_VALUE) {
            mModelId = modelId;
        } else {
            mModelId = (short) modelId;
        }
        in.readList(mBoundAppKeyIndexes, Integer.class.getClassLoader());
        sortAppKeys(in.readHashMap(ApplicationKey.class.getClassLoader()));
        try {
            mPublicationSettings = (PublicationSettings) in.readValue(PublicationSettings.class.getClassLoader());
            in.readList(mSubscriptionAddress, byte[].class.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Write the mesh model in to parcel
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @SuppressWarnings("unused")
    protected final void parcelMeshModel(final Parcel dest, final int flags) {
        dest.writeInt(mModelId);
        dest.writeList(mBoundAppKeyIndexes);
        dest.writeMap(mBoundApplicationKeys);
        dest.writeValue(mPublicationSettings);
        dest.writeList(mSubscriptionAddress);
    }

    /**
     * Sorts the app keys as the order is not maintained when parcelled.
     *
     * @param unorderedBoundAppKeys app keys
     */
    private void sortAppKeys(final HashMap<Integer, ApplicationKey> unorderedBoundAppKeys) {
        final Set<Integer> unorderedKeys = unorderedBoundAppKeys.keySet();

        final List<Integer> orderedKeys = new ArrayList<>(unorderedKeys);
        Collections.sort(orderedKeys);
        for (int key : orderedKeys) {
            mBoundApplicationKeys.put(key, unorderedBoundAppKeys.get(key));
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

    protected void setBoundAppKey(final int appKeyIndex, final ApplicationKey appKey) {
        if (!mBoundAppKeyIndexes.contains(appKeyIndex))
            mBoundAppKeyIndexes.add(appKeyIndex);
        mBoundApplicationKeys.put(appKeyIndex, appKey);
    }

    @SuppressWarnings("RedundantCollectionOperation")
    protected void removeBoundAppKey(final int appKeyIndex) {
        if (mBoundAppKeyIndexes.contains(appKeyIndex)) {
            final int position = mBoundAppKeyIndexes.indexOf(appKeyIndex);
            mBoundAppKeyIndexes.remove(position);
        }
        mBoundApplicationKeys.remove(appKeyIndex);
    }

    /**
     * Returns an unmodifiable map of bound app keys for this model.
     *
     * @return LinkedHashMap containing the bound app keys for this model
     * @deprecated use {@link #getBoundApplicationKeys}
     */
    @Deprecated
    public Map<Integer, String> getBoundAppkeys() {
        return Collections.unmodifiableMap(mBoundAppKeys);
    }


    /**
     * Returns an unmodifiable map of bound app keys for this model.
     *
     * @return LinkedHashMap containing the bound app keys for this model
     */
    public Map<Integer, ApplicationKey> getBoundApplicationKeys() {
        return (mBoundApplicationKeys);
    }

    public ApplicationKey getBoundAppKey(final int appKeyIndex) {
        return mBoundApplicationKeys.get(appKeyIndex);
    }

    /**
     * Returns the list of subscription addresses belonging to this model
     *
     * @return subscription addresses
     */
    public List<byte[]> getSubscriptionAddresses() {
        return Collections.unmodifiableList(mSubscriptionAddress);
    }

    /**
     * Checks if a model contains group addresses
     *
     * @return true if has group addresses and false otherwise
     */
    public boolean hasGroupAddresses() {
        for (byte[] address : mSubscriptionAddress) {
            if (MeshParserUtils.isValidGroupAddress(address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the list of group addresses the model may have subscribed to
     */
    public List<byte[]> getGroupAddresses() {
        final List<byte[]> addresses = new ArrayList<>();
        for (byte[] address : mSubscriptionAddress) {
            if (MeshParserUtils.isValidGroupAddress(address)) {
                addresses.add(address);
            }
        }
        return addresses;
    }

    /**
     * Returns the list of non-group addresses (unicast addresses) the model may have subscribed to
     */
    public List<byte[]> getNonGroupAddresses() {
        final List<byte[]> addresses = new ArrayList<>();
        for (byte[] address : mSubscriptionAddress) {
            if (!MeshParserUtils.isValidGroupAddress(address)) {
                addresses.add(address);
            }
        }
        return addresses;
    }

    /**
     * Sets the data from the {@link ConfigModelPublicationStatus}
     *
     * @param status publication set status
     */
    protected void setPublicationStatus(final ConfigModelPublicationStatus status) {
        if (status.isSuccessful()) {
            //mPublicationSettings = new PublicationSettings(publicationStatus);
            mPublicationSettings = new PublicationSettings(status.getPublishAddress(),
                    status.getAppKeyIndex(),
                    status.getCredentialFlag(),
                    status.getPublishTtl(),
                    status.getPublicationSteps(),
                    status.getPublicationResolution(),
                    status.getPublishRetransmitCount(),
                    status.getPublishRetransmitIntervalSteps());
        }
    }

    /**
     * Returns the publication settings used in this model
     *
     * @return publication settings
     */
    public PublicationSettings getPublicationSettings() {
        return mPublicationSettings;
    }

    /**
     * Sets the subscription address in a mesh model
     *
     * @param subscriptionAddress subscription address
     */
    protected void addSubscriptionAddress(final byte[] subscriptionAddress) {
        if (subscriptionAddress != null && !checkIfAlreadySubscribed(subscriptionAddress)) {
            mSubscriptionAddress.add(subscriptionAddress);
        }
    }

    /**
     * Removes the subscription address in a mesh model
     *
     * @param subscriptionAddress subscription address
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
