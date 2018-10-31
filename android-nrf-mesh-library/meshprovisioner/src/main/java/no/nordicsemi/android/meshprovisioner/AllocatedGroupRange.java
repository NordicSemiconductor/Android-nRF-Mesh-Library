package no.nordicsemi.android.meshprovisioner;

import com.google.gson.annotations.Expose;

/**
 * Class definition for allocating group range for provisioners.
 */
public class AllocatedGroupRange {

    @Expose
    private int highAddress;
    @Expose
    private int lowAdddress;

    /**
     * Constructs {@link AllocatedGroupRange} for provisioner
     *
     * @param lowAdddress low address of group range
     * @param highAddress high address of group range
     */
    public AllocatedGroupRange(final int lowAdddress, final int highAddress) {
        this.lowAdddress = lowAdddress;
        this.highAddress = highAddress;
    }

    /**
     * Returns the low address of the allocated group address
     *
     * @return low address
     */
    public int getLowAdddress() {
        return lowAdddress;
    }

    /**
     * Sets the low address of the allocated group address
     *
     * @param lowAdddress of the group range
     */
    public void setLowAdddress(final int lowAdddress) {
        this.lowAdddress = lowAdddress;
    }

    /**
     * Returns the high address of the allocated group range
     *
     * @return highAddress of the group range
     */
    public int getHighAddress() {
        return highAddress;
    }

    /**
     * Sets the high address of the group address
     *
     * @param highAddress of the group range
     */
    public void setHighAddress(final int highAddress) {
        this.highAddress = highAddress;
    }
}
