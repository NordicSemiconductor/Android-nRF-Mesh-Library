package no.nordicsemi.android.meshprovisioner;

import com.google.gson.annotations.Expose;

/**
 * Class definition for allocating unicast range for provisioners.
 */
public class AllocatedUnicastRange {

    @Expose
    private int highAddress;
    @Expose
    private int lowAdddress = 0xC000;

    /**
     * Constructs {@link AllocatedUnicastRange} for provisioner
     *
     * @param lowAdddress low address of unicast range
     * @param highAddress high address of unicast range
     */
    public AllocatedUnicastRange(final int lowAdddress, final int highAddress) {
        this.lowAdddress = lowAdddress;
        this.highAddress = highAddress;
    }

    /**
     * Returns the low address of the allocated unicast address
     *
     * @return low address
     */
    public int getLowAdddress() {
        return lowAdddress;
    }

    /**
     * Sets the low address of the allocated unicast address
     *
     * @param lowAdddress of the unicast range
     */
    public void setLowAdddress(final int lowAdddress) {
        this.lowAdddress = lowAdddress;
    }

    /**
     * Returns the high address of the allocated unicast range
     *
     * @return highAddress of the group range
     */
    public int getHighAddress() {
        return highAddress;
    }

    /**
     * Sets the high address of the allocated unicast address
     *
     * @param highAddress of the group range
     */
    public void setHighAddress(final int highAddress) {
        this.highAddress = highAddress;
    }
}
