package no.nordicsemi.android.meshprovisioner.utils;

import android.support.annotation.NonNull;

/**
 * Abstract class for bluetooth mesh addresses
 */
@SuppressWarnings("WeakerAccess")
public abstract class MeshAddress {

    private static final int UNASSIGNED_ADDRESS = 0x0000;
    private static final int START_UNICAST_ADDRESS = 0x0001;
    private static final int END_UNICAST_ADDRESS = 0x7FFF;
    private static final byte B1_VIRTUAL_ADDRESS = (byte) 0xBF;
    private static final int END_VIRTUAL_ADDRESS = 0xBFFF;
    private byte[] address;

    /**
     * Constructs a mesh address
     *
     * @param address 16-bit bluetooth mesh address
     */
    public MeshAddress(@NonNull final byte[] address) {
        if (isValidAddress(address))
            throw new IllegalArgumentException("Invalid address, an address in a mesh network must be a 16-bit value");
        this.address = address;
    }

    private boolean isValidAddress(@NonNull final byte[] address) {
        return address.length != 2;
    }

    /**
     * Checks if the address is in range
     *
     * @param address address
     * @return true if is in range or false otherwise
     */
    private boolean isAddressInRange(final int address) {
        return address >= Short.MIN_VALUE && address <= Short.MAX_VALUE;
    }

    /**
     * Validates an unassigned address
     *
     * @param address 16-bit address
     * @return true if the address is a valid unassigned address or false otherwise
     */
    public boolean isUnassignedAddress(@NonNull final byte[] address) {
        if (isValidAddress(address)) {
            return false;
        }

        return isValidUnassignedAddress(MeshParserUtils.unsignedBytesToInt(address[0], address[1]));
    }

    /**
     * Validates a unassigned address
     *
     * @param address 16-bit address
     * @return true if the address is a valid unassigned address or false otherwise
     */
    public boolean isValidUnassignedAddress(final int address) {
        return isAddressInRange(address) && (address == UNASSIGNED_ADDRESS);
    }

    /**
     * Validates a unicast address
     *
     * @param address Address in bytes
     * @return true if the address is a valid unicast address or false otherwise
     */
    public boolean isValidUnicastAddress(@NonNull final byte[] address) {
        if (isValidAddress(address)) {
            return false;
        }

        return isValidUnicastAddress(MeshParserUtils.unsignedBytesToInt(address[0], address[1]));
    }

    /**
     * Validates a unicast address
     *
     * @param address 16-bit address
     * @return true if the address is a valid unicast address or false otherwise
     */
    public boolean isValidUnicastAddress(final int address) {
        return isAddressInRange(address) && (address >= START_UNICAST_ADDRESS && address <= END_UNICAST_ADDRESS);
    }

    /**
     * Validates a virtual address
     *
     * @param address Address in bytes
     * @return true if the address is a valid virtual address or false otherwise
     */
    public boolean isValidVirtualAddress(@NonNull final byte[] address) {
        if (isValidAddress(address)) {
            return false;
        }
        return isValidVirtualAddress(MeshParserUtils.unsignedBytesToInt(address[0], address[1]));
    }

    /**
     * Validates a unicast address
     *
     * @param address 16-bit address
     * @return true if the address is a valid virtual address or false otherwise
     */
    public boolean isValidVirtualAddress(final int address) {
        if(isAddressInRange(address)) {
            final byte [] tempAddress = new byte[]{(byte) ((address >> 8) & 0xFF), (byte) (address & 0xFF)};
            final int b1 = tempAddress[0];
            if(b1 == B1_VIRTUAL_ADDRESS) {
                return address <= END_VIRTUAL_ADDRESS;
            }
        }
        return false;
    }

}
