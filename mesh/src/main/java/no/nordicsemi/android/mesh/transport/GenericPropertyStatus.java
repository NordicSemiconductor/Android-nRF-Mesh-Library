package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.utils.ArrayUtils;
import no.nordicsemi.android.mesh.utils.BitReader;

public class GenericPropertyStatus extends ApplicationStatusMessage  implements Parcelable {

    private final int opCode;
    private short propertyId;
    private byte userAccess;
    private byte[] propertyValue;

    private static final Creator<GenericPropertyStatus> CREATOR = new Creator<GenericPropertyStatus>() {
        @Override
        public GenericPropertyStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new GenericPropertyStatus(message);
        }

        @Override
        public GenericPropertyStatus[] newArray(int size) {
            return new GenericPropertyStatus[size];
        }
    };

    public GenericPropertyStatus(@NonNull final AccessMessage message) {
        super(message);
        this.opCode = message.getOpCode();
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        BitReader bitReader = new BitReader(mParameters);
        // We have to shift the second byte of the property.
        int firstPropertyByte = bitReader.getBits(8);
        int secondPropertyByte = bitReader.getBits(8);
        propertyId = (short) (secondPropertyByte << 8 | firstPropertyByte);
        userAccess = (byte) bitReader.getBits(8);
        propertyValue = bitReader.getRemainingBytes();
    }

    @Override
    public int getOpCode() {
        return opCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final AccessMessage message = (AccessMessage) mMessage;
        dest.writeParcelable(message, flags);
    }

    public short getPropertyId() {
        return propertyId;
    }

    public byte getUserAccess() {
        return userAccess;
    }

    public byte[] getPropertyValue() {
        return propertyValue;
    }
}
