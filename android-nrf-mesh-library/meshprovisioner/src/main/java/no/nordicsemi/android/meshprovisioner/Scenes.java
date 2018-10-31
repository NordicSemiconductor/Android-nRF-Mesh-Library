package no.nordicsemi.android.meshprovisioner;

import com.google.gson.annotations.Expose;

/**
 * Class definitions for creating scenes in a mesh network
 */
public class Scenes {

    @Expose
    private String name;
    @Expose
    private int[] addresses;
    @Expose
    private int number;

    /**
     * Friendly name of the scene
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a friendly name to a scene
     *
     * @param name friendly name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the address of the scene
     *
     * @return 2 byte address
     */
    public int[] getAddresses() {
        return addresses;
    }

    /**
     * Sets addresses for this grou
     * @param addresses
     */
    public void setAddresses(final int[] addresses) {
        this.addresses = addresses;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }
}
