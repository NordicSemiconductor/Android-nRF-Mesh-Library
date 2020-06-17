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

package no.nordicsemi.android.mesh.models;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.utils.HeartbeatPublication;
import no.nordicsemi.android.mesh.utils.HeartbeatSubscription;

@SuppressWarnings("WeakerAccess")
public class ConfigurationServerModel extends SigModel {

    @Expose
    @SerializedName("heartbeatPub")
    private HeartbeatPublication heartbeatPublication = null;
    @Expose
    @SerializedName("heartbeatSub")
    private HeartbeatSubscription heartbeatSubscription = null;

    public static final Creator<ConfigurationServerModel> CREATOR = new Creator<ConfigurationServerModel>() {
        @Override
        public ConfigurationServerModel createFromParcel(final Parcel source) {
            return new ConfigurationServerModel(source);
        }

        @Override
        public ConfigurationServerModel[] newArray(final int size) {
            return new ConfigurationServerModel[size];
        }
    };

    public ConfigurationServerModel(final int modelId) {
        super(modelId);
    }

    private ConfigurationServerModel(Parcel in) {
        super(in);
    }

    @Override
    public String getModelName() {
        return "Configuration Server";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.parcelMeshModel(dest, flags);
    }

    /**
     * Returns the Heartbeat publication.
     */
    @Nullable
    public HeartbeatPublication getHeartbeatPublication() {
        return heartbeatPublication;
    }

    /**
     * Sets the Heartbeat publication.
     *
     * @param heartbeatPublication Heartbeat publication.
     */
    public void setHeartbeatPublication(final HeartbeatPublication heartbeatPublication) {
        this.heartbeatPublication = heartbeatPublication;
    }

    /**
     * Returns the Heartbeat subscription.
     */
    public HeartbeatSubscription getHeartbeatSubscription() {
        return heartbeatSubscription;
    }

    /**
     * Sets the Heartbeat subscription.
     *
     * @param heartbeatSubscription Heartbeat subscription.
     */
    public void setHeartbeatSubscription(final HeartbeatSubscription heartbeatSubscription) {
        this.heartbeatSubscription = heartbeatSubscription;
    }
}
