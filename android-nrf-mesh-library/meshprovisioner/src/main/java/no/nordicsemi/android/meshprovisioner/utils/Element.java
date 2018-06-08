package no.nordicsemi.android.meshprovisioner.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.models.SigModel;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;

public class Element implements Parcelable {

    private final byte[] elementAddress;
    private final int locationDescriptor;
    private final int sigModelCount;
    private final int vendorModelCount;
    private final Map<Integer, MeshModel> meshModels;

    public Element(final byte[] elementAddress, final int locationDescriptor, final int sigModelCount, final int vendorModelCount, final Map<Integer, MeshModel> models) {
        this.elementAddress = elementAddress;
        this.locationDescriptor = locationDescriptor;
        this.sigModelCount = sigModelCount;
        this.vendorModelCount = vendorModelCount;
        this.meshModels = models;
    }

    protected Element(Parcel in) {
        elementAddress = in.createByteArray();
        locationDescriptor = in.readInt();
        sigModelCount = in.readInt();
        vendorModelCount = in.readInt();
        meshModels = in.readHashMap(MeshModel.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(elementAddress);
        dest.writeInt(locationDescriptor);
        dest.writeInt(sigModelCount);
        dest.writeInt(vendorModelCount);
        dest.writeMap(meshModels);
    }

    public static final Creator<Element> CREATOR = new Creator<Element>() {
        @Override
        public Element createFromParcel(Parcel in) {
            return new Element(in);
        }

        @Override
        public Element[] newArray(int size) {
            return new Element[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public byte[] getElementAddress() {
        return elementAddress;
    }

    public int getLocationDescriptor() {
        return locationDescriptor;
    }

    public int getSigModelCount() {
        return sigModelCount;
    }

    public int getVendorModelCount() {
        return vendorModelCount;
    }

    /**
     * Returns a list of sig models avaialable in this element
     * @return List containing sig models
     */
    public Map<Integer, MeshModel> getMeshModels() {
        return Collections.unmodifiableMap(meshModels);
    }

    public int getElementAddressInt() {
        return ByteBuffer.wrap(elementAddress).order(ByteOrder.BIG_ENDIAN).getShort();
    }
}
