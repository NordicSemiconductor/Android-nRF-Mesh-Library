package no.nordicsemi.android.nrfmeshprovisioner.utils;

/**
 * Address types
 */
@SuppressWarnings("unused")
public enum AddressTypes {

    UNICAST_ADDRESS(0),
    GROUP_ADDRESS(1),
    ALL_PROXIES(2),
    ALL_FRIENDS(3),
    ALL_RELAYS(4),
    ALL_NODES(5),
    VIRTUAL_ADDRESS(6);

    private int type;

    /**
     * Constructs address type
     *
     * @param type Address type
     */
    AddressTypes(final int type) {
        this.type = type;
    }

    /**
     * Returns the address type
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the oob method used for authentication
     *
     * @param method auth method used
     */
    public static AddressTypes fromValue(final int method) {
        switch (method) {
            default:
                return null;
            case 0:
                return UNICAST_ADDRESS;
            case 1:
                return GROUP_ADDRESS;
            case 2:
                return ALL_PROXIES;
            case 3:
                return ALL_FRIENDS;
            case 4:
                return ALL_RELAYS;
            case 5:
                return ALL_NODES;
            case 6:
                return VIRTUAL_ADDRESS;
        }
    }

    /**
     * Returns the address type name
     *
     * @param type Address type
     */
    public static String getTypeName(final AddressTypes type) {
        switch (type) {
            default:
                return "Unicast Address";
            case GROUP_ADDRESS:
                return "Group Address";
            case ALL_PROXIES:
                return "All Proxies";
            case ALL_FRIENDS:
                return "All Friends";
            case ALL_RELAYS:
                return "All Relays";
            case ALL_NODES:
                return "All Nodes";
            case VIRTUAL_ADDRESS:
                return "Virtual Address";
        }
    }
}
