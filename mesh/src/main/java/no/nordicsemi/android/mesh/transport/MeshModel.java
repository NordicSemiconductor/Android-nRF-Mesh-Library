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

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.utils.MeshAddress;

/**
 * Base mesh model class
 * <p>
 * This class contains properties such as Model Identifier, bound keys, key indexes, subscription
 * and publication settings belonging to a mesh model.
 * </p>
 */
@SuppressWarnings({"WeakerAccess"})
public abstract class MeshModel implements Parcelable {

    @Expose
    protected int mModelId;
    @Expose
    final List<Integer> mBoundAppKeyIndexes = new ArrayList<>();
    @Expose(serialize = false)
    final Map<Integer, String> mBoundAppKeys = new LinkedHashMap<>();
    @Expose
    final List<Integer> subscriptionAddresses = new ArrayList<>();
    @Expose
    final List<UUID> labelUuids = new ArrayList<>();
    @Expose
    PublicationSettings mPublicationSettings;
    protected int currentScene;
    protected int targetScene;
    @Expose
    protected List<Integer> sceneNumbers = new ArrayList<>();

    public MeshModel(final int modelId) {
        this.mModelId = modelId;
    }

    MeshModel() {
    }

    protected MeshModel(final Parcel in) {

        final int modelId = in.readInt();
        if (modelId < Short.MIN_VALUE || modelId > Short.MAX_VALUE) {
            mModelId = modelId;
        } else {
            mModelId = (short) modelId;
        }
        in.readList(mBoundAppKeyIndexes, Integer.class.getClassLoader());
        mPublicationSettings = (PublicationSettings) in.readValue(PublicationSettings.class.getClassLoader());
        in.readList(subscriptionAddresses, Integer.class.getClassLoader());
        in.readList(labelUuids, UUID.class.getClassLoader());
        in.readList(sceneNumbers, Integer.class.getClassLoader());
    }

    /**
     * Write the mesh model in to parcel
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    protected final void parcelMeshModel(final Parcel dest, final int flags) {
        dest.writeInt(mModelId);
        dest.writeList(mBoundAppKeyIndexes);
        dest.writeValue(mPublicationSettings);
        dest.writeList(subscriptionAddresses);
        dest.writeList(labelUuids);
        dest.writeList(sceneNumbers);
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
     * Returns bound appkey indexes for this model
     */
    public List<Integer> getBoundAppKeyIndexes() {
        return Collections.unmodifiableList(mBoundAppKeyIndexes);
    }

    protected void setBoundAppKeyIndex(final int appKeyIndex) {
        if (!mBoundAppKeyIndexes.contains(appKeyIndex))
            mBoundAppKeyIndexes.add(appKeyIndex);
    }

    protected void setBoundAppKeyIndexes(@NonNull final List<Integer> indexes) {
        mBoundAppKeyIndexes.clear();
        mBoundAppKeyIndexes.addAll(indexes);
    }

    @SuppressWarnings("RedundantCollectionOperation")
    protected void removeBoundAppKeyIndex(final int appKeyIndex) {
        if (mBoundAppKeyIndexes.contains(appKeyIndex)) {
            final int position = mBoundAppKeyIndexes.indexOf(appKeyIndex);
            mBoundAppKeyIndexes.remove(position);
        }
    }

    /**
     * Returns the list of subscription addresses belonging to this model
     *
     * @return subscription addresses
     */
    public List<Integer> getSubscribedAddresses() {
        return Collections.unmodifiableList(subscriptionAddresses);
    }

    /**
     * Returns the list of label UUIDs subscribed to this model
     */
    public List<UUID> getLabelUUID() {
        return Collections.unmodifiableList(labelUuids);
    }

    /**
     * Returns the label UUID for a given virtual address
     *
     * @param address 16-bit virtual address
     */
    public UUID getLabelUUID(final int address) {
        return MeshAddress.getLabelUuid(labelUuids, address);
    }

    /**
     * Sets the data from the {@link ConfigModelPublicationStatus}
     *
     * @param status publication set status
     */
    protected void setPublicationStatus(@NonNull final ConfigModelPublicationStatus status,
                                        @Nullable final UUID labelUUID) {
        if (status.isSuccessful()) {
            if (!MeshAddress.isValidUnassignedAddress(status.getPublishAddress())) {
                mPublicationSettings = new PublicationSettings(status.getPublishAddress(),
                        labelUUID,
                        status.getAppKeyIndex(),
                        status.getCredentialFlag(),
                        status.getPublishTtl(),
                        status.getPublicationSteps(),
                        status.getPublicationResolution(),
                        status.getPublishRetransmitCount(),
                        status.getPublishRetransmitIntervalSteps());
            } else {
                mPublicationSettings = null;
            }
        }
    }

    /**
     * Updates the data from the {@link ConfigModelPublicationStatus}
     *
     * @param status publication set status
     */
    protected void updatePublicationStatus(@NonNull final ConfigModelPublicationStatus status) {
        if (status.isSuccessful()) {
            if (mPublicationSettings != null) {
                mPublicationSettings.setPublishAddress(status.getPublishAddress());
                if (!MeshAddress.isValidVirtualAddress(status.getPublishAddress())) {
                    mPublicationSettings.setLabelUUID(null);
                }
                mPublicationSettings.setAppKeyIndex(status.getAppKeyIndex());
                mPublicationSettings.setCredentialFlag(status.getCredentialFlag());
                mPublicationSettings.setPublishTtl(status.getPublishTtl());
                mPublicationSettings.setPublicationSteps(status.getPublicationSteps());
                mPublicationSettings.setPublicationResolution(status.getPublicationResolution());
                mPublicationSettings.setPublishRetransmitCount(status.getPublishRetransmitCount());
                mPublicationSettings.setPublishRetransmitIntervalSteps(status.getPublishRetransmitIntervalSteps());
            }
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
     * @param subscriptionAddress Subscription address
     */
    protected void addSubscriptionAddress(final int subscriptionAddress) {
        if (!subscriptionAddresses.contains(subscriptionAddress)) {
            subscriptionAddresses.add(subscriptionAddress);
        }
    }

    /**
     * Sets the subscription address in a mesh model
     *
     * @param labelUuid Label uuid of the the subscription address
     * @param address   Subscription address
     */
    protected void addSubscriptionAddress(@NonNull final UUID labelUuid, final int address) {
        if (!labelUuids.contains(labelUuid)) {
            labelUuids.add(labelUuid);
        }

        if (!subscriptionAddresses.contains(address)) {
            subscriptionAddresses.add(address);
        }
    }

    /**
     * Removes the subscription address in a mesh model
     *
     * @param address Subscription address
     */
    protected void removeSubscriptionAddress(@NonNull final Integer address) {
        subscriptionAddresses.remove(address);
    }

    /**
     * Removes the subscription address in a mesh model
     *
     * @param labelUuid Label UUID
     * @param address   Subscription address
     */
    protected void removeSubscriptionAddress(@NonNull final UUID labelUuid,
                                             @NonNull final Integer address) {
        labelUuids.remove(labelUuid);
        removeSubscriptionAddress(address);
    }

    /**
     * Removes all the subscription addresses in a mesh model
     */
    protected void removeAllSubscriptionAddresses() {
        labelUuids.clear();
        subscriptionAddresses.clear();
    }

    /**
     * Overwrites the subscription addresses in a mesh model by clearing the existing addresses and adding a new address
     *
     * @param subscriptionAddress Subscription address
     */
    protected void overwriteSubscriptionAddress(@NonNull final Integer subscriptionAddress) {
        subscriptionAddresses.clear();
        addSubscriptionAddress(subscriptionAddress);
    }

    /**
     * Overwrites the subscription addresses in a mesh model by clearing the existing addresses and adding a new address
     *
     * @param labelUuid Label UUID
     * @param address   Subscription address
     */
    protected void overwriteSubscriptionAddress(@NonNull final UUID labelUuid,
                                                @NonNull final Integer address) {
        labelUuids.clear();
        addSubscriptionAddress(labelUuid, address);
        overwriteSubscriptionAddress(address);
    }

    /**
     * Update the subscription addresses list
     *
     * @param addresses List of subscription addresses
     */
    protected void updateSubscriptionAddressesList(@NonNull final List<Integer> addresses) {
        subscriptionAddresses.clear();
        subscriptionAddresses.addAll(addresses);
    }
}
