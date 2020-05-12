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
import android.os.Parcelable;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.utils.CompanyIdentifiers;

@SuppressWarnings("unused")
public class VendorModel extends MeshModel {

    private static final String TAG = VendorModel.class.getSimpleName();
    private final short companyIdentifier;
    private final String companyName;

    public static final Parcelable.Creator<VendorModel> CREATOR = new Parcelable.Creator<VendorModel>() {
        @Override
        public VendorModel createFromParcel(final Parcel source) {
            return new VendorModel(source);
        }

        @Override
        public VendorModel[] newArray(final int size) {
            return new VendorModel[size];
        }
    };

    public VendorModel(final int modelIdentifier) {
        super(modelIdentifier);
        final ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(modelIdentifier);
        this.companyIdentifier = buffer.getShort(0);
        this.companyName = CompanyIdentifiers.getCompanyName(companyIdentifier);
        Log.v(TAG, "Company name: " + companyName);
    }

    private VendorModel(final Parcel source) {
        super(source);
        final ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(mModelId);
        this.companyIdentifier = buffer.getShort(0);
        this.companyName = CompanyIdentifiers.getCompanyName(companyIdentifier);
    }

    @Override
    public int getModelId() {
        return mModelId;
    }

    @Override
    public String getModelName() {
        return "Vendor Model";
    }

    public int getCompanyIdentifier() {
        return companyIdentifier;
    }

    public String getCompanyName() {
        return companyName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.parcelMeshModel(dest, flags);
    }

}
