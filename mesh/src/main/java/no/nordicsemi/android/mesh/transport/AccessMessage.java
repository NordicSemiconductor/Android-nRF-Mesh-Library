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
import android.os.ParcelUuid;
import android.util.SparseArray;

import java.util.UUID;

import androidx.annotation.NonNull;

@SuppressWarnings("WeakerAccess")
public final class AccessMessage extends Message {

    private UUID label;                                // Label UUID for destination address
    protected SparseArray<byte[]> lowerTransportAccessPdu = new SparseArray<>();
    private byte[] accessPdu;
    private byte[] transportPdu;

    public static final Creator<AccessMessage> CREATOR = new Creator<AccessMessage>() {
        @Override
        public AccessMessage createFromParcel(final Parcel source) {
            return new AccessMessage(source);
        }

        @Override
        public AccessMessage[] newArray(final int size) {
            return new AccessMessage[size];
        }
    };

    public AccessMessage() {
        this.ctl = 0;
    }

    protected AccessMessage(final Parcel source) {
        super(source);
        final ParcelUuid parcelUuid = source.readParcelable(ParcelUuid.class.getClassLoader());
        if (parcelUuid != null) {
            label = parcelUuid.getUuid();
        }
        lowerTransportAccessPdu = readSparseArrayToParcelable(source);
        accessPdu = source.createByteArray();
        transportPdu = source.createByteArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(new ParcelUuid(label), flags);
        writeSparseArrayToParcelable(dest, lowerTransportAccessPdu);
        dest.writeByteArray(accessPdu);
        dest.writeByteArray(transportPdu);
    }

    @Override
    public int getCtl() {
        return ctl;
    }

    public UUID getLabel() {
        return label;
    }

    public void setLabel(@NonNull final UUID label) {
        this.label = label;
    }

    public final byte[] getAccessPdu() {
        return accessPdu;
    }

    public final void setAccessPdu(final byte[] accessPdu) {
        this.accessPdu = accessPdu;
    }

    public final byte[] getUpperTransportPdu() {
        return transportPdu;
    }

    public final void setUpperTransportPdu(final byte[] transportPdu) {
        this.transportPdu = transportPdu;
    }

    public final SparseArray<byte[]> getLowerTransportAccessPdu() {
        return lowerTransportAccessPdu;
    }

    public final void setLowerTransportAccessPdu(final SparseArray<byte[]> lowerTransportAccessPdu) {
        this.lowerTransportAccessPdu = lowerTransportAccessPdu;
    }
}
