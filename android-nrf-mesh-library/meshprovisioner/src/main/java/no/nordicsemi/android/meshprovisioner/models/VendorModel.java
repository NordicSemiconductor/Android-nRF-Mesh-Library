package no.nordicsemi.android.meshprovisioner.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.utils.CompanyIdentifiers;

public class VendorModel extends MeshModel {
    public static final Parcelable.Creator<VendorModel> CREATOR = new Parcelable.Creator<VendorModel>() {
        @Override
        public VendorModel createFromParcel(final Parcel source) {
            return new VendorModel(source.readInt());
        }

        @Override
        public VendorModel[] newArray(final int size) {
            return new VendorModel[size];
        }
    };
    private final short companyIdentifier;
    private final String companyName;

    public VendorModel(final int modelIdentifier) {
        super(modelIdentifier);
        final ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(modelIdentifier);
        buffer.position(2);
        this.companyIdentifier = buffer.getShort();
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
        dest.writeInt(mModelId);
    }

}
