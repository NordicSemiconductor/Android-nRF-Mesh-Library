package no.nordicsemi.android.mesh.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Wrapper class for addresses
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class AddressArray implements Parcelable {

    final byte[] address = new byte[2];

    /**
     * Constructs the AddressArray
     *
     * @param b1 address byte1
     * @param b2 address byte2
     */
    public AddressArray(final byte b1, final byte b2) {
        address[0] = b1;
        address[1] = b2;
    }

    protected AddressArray(Parcel in) {
        final byte[] address = in.createByteArray();
        if (address != null) {
            this.address[0] = address[0];
            this.address[1] = address[1];
        }
    }

    public static final Creator<AddressArray> CREATOR = new Creator<AddressArray>() {
        @Override
        public AddressArray createFromParcel(Parcel in) {
            return new AddressArray(in);
        }

        @Override
        public AddressArray[] newArray(int size) {
            return new AddressArray[size];
        }
    };

    /**
     * Returns address used in the filter message
     */
    public byte[] getAddress() {
        return address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeByteArray(address);
    }
}
