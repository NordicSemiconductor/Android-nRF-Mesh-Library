package no.nordicsemi.android.meshprovisioner.utils;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Class for virtual addresses in a mesh network
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class VirtualAddress extends Address {

    private static final byte B1_VIRTUAL_ADDRESS = (byte) 0x80;
    public static final int END_VIRTUAL_ADDRESS = 0xBFFF;

    /**
     * Constructs a virtual address
     *
     * @param address 16-bit virtual address
     */
    public VirtualAddress(@NonNull final byte[] address) throws IllegalArgumentException {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid virtual address, a virtual address must be a 16-bit value, and must range from 0x8000 to 0xBFFF");
        }
        this.address = address;
    }

    /**
     * Constructs a virtual address
     *
     * @param address 16-bit virtual address
     */
    public VirtualAddress(final int address) throws IllegalArgumentException {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid virtual address, virtual address must be a 16-bit value, and must range from 0x0001 to 0x7FFF");
        }
        this.address = MeshParserUtils.intToBytes(address);
    }

    @Override
    protected boolean isValidAddress(@NonNull final byte[] address) {
        return isAddressInRange(address) && isValidAddress(MeshParserUtils.bytesToInt(address));
    }

    @Override
    protected boolean isValidAddress(final int address) {
        return isAddressInRange(address) && isValidVirtualAddress(address);
    }

    /**
     * Validates a virtual address
     *
     * @param address 16-bit address
     * @return true if the address is a valid virtual address or false otherwise
     */
    private boolean isValidVirtualAddress(final int address) {
        if(isAddressInRange(address)) {
            final byte [] tempAddress = new byte[]{(byte) ((address >> 8) & 0xFF), (byte) (address & 0xFF)};
            final int b1 = tempAddress[0];
            if(b1 == B1_VIRTUAL_ADDRESS) {
                return address <= END_VIRTUAL_ADDRESS;
            }
        }
        return false;
    }

    public static final Creator<VirtualAddress> CREATOR = new Creator<VirtualAddress>() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public VirtualAddress createFromParcel(Parcel in) {
            return new VirtualAddress(in.createByteArray());
        }

        @Override
        public VirtualAddress[] newArray(int size) {
            return new VirtualAddress[size];
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
