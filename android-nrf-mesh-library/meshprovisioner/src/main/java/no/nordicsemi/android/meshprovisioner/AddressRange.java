package no.nordicsemi.android.meshprovisioner;

import com.google.gson.annotations.Expose;

public abstract class AddressRange extends Range {

    @Expose
    int lowAddress;

    @Expose
    int highAddress;

    /**
     * Returns the low address of the allocated group address
     *
     * @return low address
     */
    public abstract int getLowAddress();

    /**
     * Returns the high address of the allocated group range
     *
     * @return highAddress of the group range
     */
    public abstract int getHighAddress();

}
