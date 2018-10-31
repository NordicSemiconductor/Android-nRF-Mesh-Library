package no.nordicsemi.android.meshprovisioner;

import com.google.gson.annotations.Expose;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * Defines a group in a mesh network
 */
@SuppressWarnings("unused")
public class Group {

    @Expose
    private int groupAddress;
    @Expose
    private int parentAddress;

    /**
     * Constructs a mesh group
     *
     * @param groupAddress  groupAddress of the group
     * @param parentAddress parent address
     */
    public Group(final int groupAddress, final int parentAddress) {
        this.groupAddress = groupAddress;
        if (groupAddress == parentAddress) {
            throw new IllegalArgumentException("Address cannot match parent adddress");
        }
        this.parentAddress = parentAddress;
    }

    /**
     * Returns the group address
     *
     * @return 2 byte group address
     */
    public int getGroupAddress() {
        return groupAddress;
    }

    /**
     * Sets a group address
     *
     * @param groupAddress 2 byte group address
     */
    public void setGroupAddress(final int groupAddress) {
        if (!MeshParserUtils.isValidGroupAddress(groupAddress)) {
            throw new IllegalArgumentException("Invalid group address");
        }
        this.groupAddress = groupAddress;
    }

    /**
     * Returns the parent address a group is associated with
     *
     * @return parent address
     */
    public int getParentAddress() {
        return parentAddress;
    }

    /**
     * Sets the parent address to which this group belongs to
     */
    public void setParentAddress(final int parentAddress) {
        this.parentAddress = parentAddress;
    }
}
