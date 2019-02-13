package no.nordicsemi.android.meshprovisioner.utils;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Class for group addresses in a mesh network
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class GroupAddress extends Address {

    //Group address start and end defines the address range that can be used to create groups
    public static final int START_GROUP_ADDRESS = 0xC000;
    public static final int END_GROUP_ADDRESS = 0xFEFF;

    //Fixed group addresses
    public static final int ALL_PROXIES_ADDRESS = 0xFFFC;
    public static final int ALL_FRIENDS_ADDRESS = 0xFFFD;
    public static final int ALL_RELAYS_ADDRESS = 0xFFFE;
    public static final int ALL_NODES_ADDRESS = 0xFFFF;

    /**
     * Constructs a group address
     *
     * @param address 16-bit unicast address
     */
    public GroupAddress(@NonNull final byte[] address) throws IllegalArgumentException {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid group address, group address must be a 16-bit value, and must range from 0xC000 to 0xFEFF");
        }
        this.address = MeshParserUtils.bytesToInt(address);
    }

    /**
     * Constructs a group address
     *
     * @param address 16-bit unicast address
     */
    public GroupAddress(final int address) throws IllegalArgumentException {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid group address, group address must be a 16-bit value, and must range from 0xC000 to 0xFEFF");
        }
        this.address = address;
    }

    @Override
    protected boolean isValidAddress(@NonNull final byte[] address) {
        return isValidGroupAddress(address);
    }

    @Override
    protected boolean isValidAddress(final int address) {
        return isValidGroupAddress(address);
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

    /**
     * Validates an all proxies address
     *
     * @param address 16-bit address
     * @return true if the address is a valid or false otherwise
     */
    public static boolean isAllProxiesAddress(@NonNull final byte[] address) {
        return isAddressInRange(address) && isAllProxiesAddress(MeshParserUtils.bytesToInt(address));
    }

    /**
     * Validates an all proxies address
     *
     * @param address 16-bit address
     * @return true if the address is a valid or false otherwise
     */
    public static boolean isAllProxiesAddress(final int address) {
        return isAddressInRange(address) && address == ALL_PROXIES_ADDRESS;
    }

    /**
     * Validates an all friends address
     *
     * @param address 16-bit address
     * @return true if the address is a valid or false otherwise
     */
    public static boolean isAllFriendsAddress(@NonNull final byte[] address) {
        return isAddressInRange(address) && isAllFriendsAddress(MeshParserUtils.bytesToInt(address));
    }

    /**
     * Validates an all friends address
     *
     * @param address 16-bit address
     * @return true if the address is a valid or false otherwise
     */
    public static boolean isAllFriendsAddress(final int address) {
        return isAddressInRange(address) && address == ALL_FRIENDS_ADDRESS;
    }

    /**
     * Validates an all relays address
     *
     * @param address 16-bit address
     * @return true if the address is a valid or false otherwise
     */
    public static boolean isAllRelaysAddress(@NonNull final byte[] address) {
        return isAddressInRange(address) && isAllRelaysAddress(MeshParserUtils.bytesToInt(address));
    }

    /**
     * Validates an all relays address
     *
     * @param address 16-bit address
     * @return true if the address is a valid or false otherwise
     */
    public static boolean isAllRelaysAddress(final int address) {
        return isAddressInRange(address) && address == ALL_RELAYS_ADDRESS;
    }

    /**
     * Validates an all nodes address
     *
     * @param address 16-bit address
     * @return true if the address is a valid or false otherwise
     */
    public static boolean isAllNodesAddress(@NonNull final byte[] address) {
        return isAddressInRange(address) && isAllRelaysAddress(MeshParserUtils.bytesToInt(address));
    }

    /**
     * Validates an all nodes address
     *
     * @param address 16-bit address
     * @return true if the address is a valid or false otherwise
     */
    public static boolean isAllNodesAddress(final int address) {
        return isAddressInRange(address) && address == ALL_NODES_ADDRESS;
    }

    public static final Creator<GroupAddress> CREATOR = new Creator<GroupAddress>() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public GroupAddress createFromParcel(Parcel in) {
            return new GroupAddress(in.readInt());
        }

        @Override
        public GroupAddress[] newArray(int size) {
            return new GroupAddress[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(address);
    }
}
