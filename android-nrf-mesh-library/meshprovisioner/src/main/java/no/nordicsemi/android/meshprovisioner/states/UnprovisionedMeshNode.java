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

package no.nordicsemi.android.meshprovisioner.states;

import android.os.Parcel;

import no.nordicsemi.android.meshprovisioner.BaseMeshNode;


public final class UnprovisionedMeshNode extends BaseMeshNode {

    private static final String TAG = UnprovisionedMeshNode.class.getSimpleName();

    public UnprovisionedMeshNode() {

    }

    protected UnprovisionedMeshNode(Parcel in) {
        isProvisioned = in.readByte() != 0;
        isConfigured = in.readByte() != 0;
        nodeName = in.readString();
        provisionerPublicKeyXY = in.createByteArray();
        provisioneePublicKeyXY = in.createByteArray();
        sharedECDHSecret = in.createByteArray();
        provisionerRandom = in.createByteArray();
        provisioneeConfirmation = in.createByteArray();
        authenticationValue = in.createByteArray();
        provisioneeRandom = in.createByteArray();
        networkKey = in.createByteArray();
        identityKey = in.createByteArray();
        keyIndex = in.createByteArray();
        mFlags = in.createByteArray();
        ivIndex = in.createByteArray();
        unicastAddress = in.createByteArray();
        deviceKey = in.createByteArray();
        ttl = in.readInt();
        provisioningCapabilities = in.readParcelable(ProvisioningCapabilities.class.getClassLoader());
        numberOfElements = provisioningCapabilities.getNumberOfElements();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isProvisioned ? 1 : 0));
        dest.writeByte((byte) (isConfigured ? 1 : 0));
        dest.writeString(nodeName);
        dest.writeByteArray(provisionerPublicKeyXY);
        dest.writeByteArray(provisioneePublicKeyXY);
        dest.writeByteArray(sharedECDHSecret);
        dest.writeByteArray(provisionerRandom);
        dest.writeByteArray(provisioneeConfirmation);
        dest.writeByteArray(authenticationValue);
        dest.writeByteArray(provisioneeRandom);
        dest.writeByteArray(networkKey);
        dest.writeByteArray(identityKey);
        dest.writeByteArray(keyIndex);
        dest.writeByteArray(mFlags);
        dest.writeByteArray(ivIndex);
        dest.writeByteArray(unicastAddress);
        dest.writeByteArray(deviceKey);
        dest.writeInt(ttl);
        dest.writeParcelable(provisioningCapabilities, flags);
    }


    public static final Creator<UnprovisionedMeshNode> CREATOR = new Creator<UnprovisionedMeshNode>() {
        @Override
        public UnprovisionedMeshNode createFromParcel(Parcel in) {
            return new UnprovisionedMeshNode(in);
        }

        @Override
        public UnprovisionedMeshNode[] newArray(int size) {
            return new UnprovisionedMeshNode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public final byte[] getSharedECDHSecret() {
        return sharedECDHSecret;
    }

    public final void setSharedECDHSecret(final byte[] sharedECDHSecret) {
        this.sharedECDHSecret = sharedECDHSecret;
    }

    public final byte[] getProvisionerPublicKeyXY() {
        return provisionerPublicKeyXY;
    }

    public final void setProvisionerPublicKeyXY(final byte[] rawProvisionerKey) {
        this.provisionerPublicKeyXY = rawProvisionerKey;
    }

    public final byte[] getProvisioneePublicKeyXY() {
        return provisioneePublicKeyXY;
    }

    public final void setProvisioneePublicKeyXY(final byte[] provisioneePublicKeyXY) {
        this.provisioneePublicKeyXY = provisioneePublicKeyXY;
    }

    public final byte[] getProvisionerRandom() {
        return provisionerRandom;
    }

    public final void setProvisionerRandom(final byte[] provisionerRandom) {
        this.provisionerRandom = provisionerRandom;
    }

    public final byte[] getProvisioneeConfirmation() {
        return provisioneeConfirmation;
    }

    public final void setProvisioneeConfirmation(final byte[] provisioneeConfirmation) {
        this.provisioneeConfirmation = provisioneeConfirmation;
    }

    public final byte[] getAuthenticationValue() {
        return authenticationValue;
    }

    public final void setAuthenticationValue(final byte[] authenticationValue) {
        this.authenticationValue = authenticationValue;
    }

    public final byte[] getProvisioneeRandom() {
        return provisioneeRandom;
    }

    public final void setProvisioneeRandom(final byte[] provisioneeRandom) {
        this.provisioneeRandom = provisioneeRandom;
    }

    public final byte[] getNetworkKey() {
        return networkKey;
    }

    public final void setNetworkKey(final byte[] networkKey) {
        this.networkKey = networkKey;
    }

    protected final void setDeviceKey(final byte[] deviceKey) {
        this.deviceKey = deviceKey;
    }

    public final void setProvisionedTime(final long timeStampInMillis) {
        mTimeStampInMillis = timeStampInMillis;
    }

    protected void setProvisioningCapabilities(final ProvisioningCapabilities provisioningCapabilities) {
        numberOfElements = provisioningCapabilities.getNumberOfElements();
        this.provisioningCapabilities = provisioningCapabilities;
    }
}
