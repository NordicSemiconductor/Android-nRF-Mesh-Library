package no.nordicsemi.android.meshprovisioner.utils;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Class for unassigned addresses in a mesh network
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class UnassignedAddress extends Address {

    private static final int UNASSIGNED_ADDRESS = 0x0000;

    /**
     * Constructs a UnassignedAddress
     *
     * @param address 16-bit unicast address
     */
    public UnassignedAddress(@NonNull final byte[] address) throws IllegalArgumentException {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid unassigned address, unassigned address must be a 16-bit value, and must be 0x0000");
        }
        this.address = address;
    }

    /**
     * Constructs a UnassignedAddress
     *
     * @param address 16-bit unicast address
     */
    public UnassignedAddress(final int address) throws IllegalArgumentException {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid unassigned address, unassigned address must be a 16-bit value, and must be 0x0000");
        }
        this.address = MeshParserUtils.intToBytes(address);
    }

    /**
     * Validates a unassigned address
     *
     * @param address Address in bytes
     * @return true if the address is valid and false otherwise
     */
    private boolean isValidUnassignedAddress(@NonNull final byte[] address) {
        if (address.length != 2) {
            return false;
        }
        final int tempAddress = MeshParserUtils.unsignedBytesToInt(address[0], address[1]);
        return isValidUnassignedAddress(tempAddress);
    }

    @Override
    protected boolean isValidAddress(@NonNull final byte[] address) {
        return isAddressInRange(address) && isValidAddress(MeshParserUtils.bytesToInt(address));
    }

    @Override
    protected boolean isValidAddress(final int address) {
        return isAddressInRange(address) && isValidUnassignedAddress(address);
    }

    /**
     * Validates a unassigned address
     *
     * @param address 16-bit address
     * @return true if the address is valid and false otherwise
     */
    private boolean isValidUnassignedAddress(final int address) {
        return address == UNASSIGNED_ADDRESS;
    }

    public static final Creator<UnassignedAddress> CREATOR = new Creator<UnassignedAddress>() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public UnassignedAddress createFromParcel(Parcel in) {
            return new UnassignedAddress(in.createByteArray());
        }

        @Override
        public UnassignedAddress[] newArray(int size) {
            return new UnassignedAddress[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeByteArray(address);
    }
}
