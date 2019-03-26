package no.nordicsemi.android.meshprovisioner.utils;

/**
 * Address types
 */
@SuppressWarnings("unused")
public enum AddressType {

    UNASSIGNED_ADDRESS(0),
    UNICAST_ADDRESS(1),
    GROUP_ADDRESS(2),
    VIRTUAL_ADDRESS(3);

    private int type;

    /**
     * Constructs address type
     *
     * @param type Address type
     */
    AddressType(final int type) {
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
    public static AddressType fromValue(final int method) {
        switch (method) {
            default:
                return null;
            case 0:
                return UNASSIGNED_ADDRESS;
            case 1:
                return UNICAST_ADDRESS;
            case 2:
                return GROUP_ADDRESS;
            case 3:
                return VIRTUAL_ADDRESS;
        }
    }

    /**
     * Returns the address type name
     *
     * @param type Address type
     */
    public static String getTypeName(final AddressType type) {
        switch (type) {
            default:
            case UNASSIGNED_ADDRESS:
                return "Unassigned Address";
            case UNICAST_ADDRESS:
                return "Unicast Address";
            case GROUP_ADDRESS:
                return "Group Address";
            case VIRTUAL_ADDRESS:
                return "Virtual Address";

        }
    }
}
