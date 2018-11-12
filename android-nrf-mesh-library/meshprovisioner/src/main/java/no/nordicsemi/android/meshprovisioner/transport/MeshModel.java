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
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RestrictTo;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.nordicsemi.android.meshprovisioner.Subscription;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.PublicationSettings;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.support.annotation.RestrictTo.Scope;


@SuppressWarnings({"WeakerAccess", "unused"})
@Entity(tableName = "models",
        foreignKeys = {
                @ForeignKey(
                        entity = ProvisionedMeshNode.class,
                        parentColumns = "uuid",
                        childColumns = "uuid",
                        onUpdate = CASCADE,
                        onDelete = CASCADE)},
        indices = {@Index("parent_address"), @Index("uuid")})
public class MeshModel implements Parcelable {

    /**
     * Used as a unique key in the database
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "parent_address")
    byte[] parentAddress;

    @ColumnInfo(name = "uuid")
    String uuid;

    @ColumnInfo(name = "model_id")
    @Expose
    protected int mModelId;

    @Ignore
    @Expose
    final List<Integer> mBoundAppKeyIndexes = new ArrayList<>();

    @Ignore
    @Expose(serialize = false)
    final Map<Integer, String> mBoundAppKeys = new LinkedHashMap<>();

    @Ignore
    @Expose
    final Map<Integer, ApplicationKey> mBoundApplicationKeys = new LinkedHashMap<>();

    @Embedded
    @Expose
    PublicationSettings mPublicationSettings;

    @Ignore
    @Expose(serialize = false)
    final List<byte[]> mSubscriptionAddress = new ArrayList<>();

    @Ignore
    @Expose
    final List<Subscription> mSubscriptions = new ArrayList<>();

    @Ignore
    MeshModel() {
    }

    @RestrictTo(Scope.LIBRARY)
    public MeshModel(final int modelId) {
        this.mModelId = modelId;
    }

    protected MeshModel(Parcel in) {
        id = in.readInt();
        parentAddress = in.createByteArray();
        mModelId = in.readInt();
        mPublicationSettings = in.readParcelable(PublicationSettings.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        parcelMeshModel(dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MeshModel> CREATOR = new Creator<MeshModel>() {
        @Override
        public MeshModel createFromParcel(Parcel in) {
            return new MeshModel(in);
        }

        @Override
        public MeshModel[] newArray(int size) {
            return new MeshModel[size];
        }
    };

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
    public int getModelId() {
        return mModelId;
    }

    @RestrictTo(Scope.LIBRARY_GROUP)
    public void setModelId(final int modelId) {
        mModelId = modelId;
    }

    /**
     * Returns the address of the element this model belongs to
     */
    public byte[] getParentAddress() {
        return parentAddress;
    }

    @RestrictTo(Scope.LIBRARY_GROUP)
    public void setParentAddress(final byte[] parentAddress) {
        this.parentAddress = parentAddress;
    }

    /**
     * Returns the unique device uuid of the node to which this model belongs to
     */
    public String getUuid() {
        return uuid;
    }

    @RestrictTo(Scope.LIBRARY_GROUP)
    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    /**
     * Returns the Bluetooth SIG defined model name
     *
     * @return model name
     */
    public String getModelName() {
        return "MeshModel";
    }

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
        return Collections.unmodifiableMap(mBoundApplicationKeys);
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
     * Returns the list of {@link Subscription} addresses belonging to this model
     *
     * @return subscription addresses
     */
    public List<Subscription> getSubscriptions() {
        return Collections.unmodifiableList(mSubscriptions);
    }

    /**
     * Checks if a model contains group addresses
     *
     * @return
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

    public void setPublicationSettings(final PublicationSettings publicationSettings) {
        mPublicationSettings = publicationSettings;
    }

    /**
     * Sets the subscription address in a mesh model
     *
     * @param subscriptionAddress subscription address
     */
    protected void addSubscriptionAddress(final byte[] subscriptionAddress) {
        if (subscriptionAddress != null && !isAlreadySubscribed(subscriptionAddress)) {
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

    /**
     * Sets the subscription address in a mesh model
     *
     * @param subscription subscribe address
     */
    protected void addSubscription(final Subscription subscription) {
        if (subscription != null && !isAlreadySubscribed(subscription)) {
            mSubscriptions.add(subscription);
        }
    }

    /**
     * Removes the subscription address in a mesh model
     *
     * @param subscription subscribe address
     */
    protected void removeSubscription(final Subscription subscription) {
        if (subscription != null) {
            final int index = getSubscriptionIndex(subscription);
            if (index > -1) {
                mSubscriptions.remove(index);
            }
        }
    }

    private boolean isAlreadySubscribed(final byte[] subscriptionAddress) {
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


    private boolean isAlreadySubscribed(final Subscription subscription) {
        for (Subscription subscription1 : mSubscriptions) {
            if (subscription.getAddress() == subscription1.getAddress())
                return true;
        }
        return false;
    }

    private int getSubscriptionIndex(final Subscription subscription) {
        int counter = 0;
        for (int i = 0; i < mSubscriptions.size(); i++) {
            if (subscription.getAddress() == mSubscriptions.get(i).getAddress())
                return i;
        }

        return -1;
    }

    private void parcelMeshModel(final Parcel dest, final int flags) {
        dest.writeInt(mModelId);
        dest.writeList(mBoundAppKeyIndexes);
        dest.writeMap(mBoundApplicationKeys);
        dest.writeParcelable(mPublicationSettings, flags);
        dest.writeList(mSubscriptionAddress);
    }

}
