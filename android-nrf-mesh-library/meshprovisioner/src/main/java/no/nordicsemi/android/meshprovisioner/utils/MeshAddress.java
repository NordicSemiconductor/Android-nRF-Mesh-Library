package no.nordicsemi.android.meshprovisioner.utils;

import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * Abstract class for bluetooth mesh addresses
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class MeshAddress {

    //Group address start and end defines the address range that can be used to create groups
    public static final int START_GROUP_ADDRESS = 0xC000;
    public static final int END_GROUP_ADDRESS = 0xFEFF;

    //Fixed group addresses
    public static final int ALL_PROXIES_ADDRESS = 0xFFFC;
    public static final int ALL_FRIENDS_ADDRESS = 0xFFFD;
    public static final int ALL_RELAYS_ADDRESS = 0xFFFE;
    public static final int ALL_NODES_ADDRESS = 0xFFFF;

    public static final int UNASSIGNED_ADDRESS = 0x0000;
    private static final int START_UNICAST_ADDRESS = 0x0001;
    private static final int END_UNICAST_ADDRESS = 0x7FFF;
    private static final byte B1_VIRTUAL_ADDRESS = (byte) 0xBF;
    private static final int END_VIRTUAL_ADDRESS = 0xBFFF;

    public static boolean isAddressInRange(@NonNull final byte[] address) {
        return address.length != 2;
    }

    /**
     * Checks if the address is in range
     *
     * @param address address
     * @return true if is in range or false otherwise
     */
    public static boolean isAddressInRange(final int address) {
        return address == (address & 0xFFFF);
    }

    /**
     * Validates an unassigned address
     *
     * @param address 16-bit address
     * @return true if the address is a valid unassigned address or false otherwise
     */
    public boolean isUnassignedAddress(@NonNull final byte[] address) {
        if (isAddressInRange(address)) {
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
    public static boolean isValidUnicastAddress(@NonNull final byte[] address) {
        if (isAddressInRange(address)) {
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
    public static boolean isValidUnicastAddress(final int address) {
        return isAddressInRange(address) && (address >= START_UNICAST_ADDRESS && address <= END_UNICAST_ADDRESS);
    }

    /**
     * Validates a virtual address
     *
     * @param address Address in bytes
     * @return true if the address is a valid virtual address or false otherwise
     */
    public boolean isValidVirtualAddress(@NonNull final byte[] address) {
        if (isAddressInRange(address)) {
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


    private static boolean isValidGroupAddress(final byte[] address){
        if(!isAddressInRange(address))
            return false;

        final int b0 = MeshParserUtils.unsignedByteToInt(address[0]);
        final int b1 = MeshParserUtils.unsignedByteToInt(address[1]);

        final boolean groupRange = b0 >= 0xC0 && b0 <= 0xFF;
        final boolean rfu = b0 == 0xFF && b1 >= 0x00 && b1 <= 0xFB;
        final boolean allNodes = b0 == 0xFF && b1 == 0xFF;

        return groupRange && !rfu && !allNodes;
    }

    /**
     * Validates a unicast address
     *
     * @param address 16-bit address
     * @return true if the address is valid and false otherwise
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isValidGroupAddress(final int address) {
        if(!isAddressInRange(address))
            return false;

        final int b0 = address >> 8 & 0xFF;
        final int b1 = address & 0xFF;

        final boolean groupRange = b0 >= 0xC0 && b0 <= 0xFF;
        final boolean rfu = b0 == 0xFF && b1 >= 0x00 && b1 <= 0xFB;
        final boolean allNodes = b0 == 0xFF && b1 == 0xFF;

        return groupRange && !rfu && !allNodes;
    }

    public static String formatAddress(final int address, final boolean add0x) {
        return add0x ? "0x" + String.format(Locale.US, "%04X", address) : String.format(Locale.US, "%04X", address);
    }
}
