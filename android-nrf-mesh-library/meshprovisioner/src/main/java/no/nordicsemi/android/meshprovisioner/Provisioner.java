package no.nordicsemi.android.meshprovisioner;

import com.google.gson.annotations.Expose;

import java.util.UUID;

/**
 * Class definition of a Provisioner of mesh network
 */
public class Provisioner {

    @Expose
    private String provisionerName = "nRF Mesh Provisioner";
    @Expose
    private UUID uuid = UUID.randomUUID();
    @Expose
    private AllocatedGroupRange allocatedGroupRange;
    @Expose
    private AllocatedUnicastRange allocatedUnicastRange;

    /**
     * Constructs {@link Provisioner}
     */
    Provisioner(){

    }

    /**
     * Returns the provisioner name
     * @return name
     */
    public String getProvisionerName() {
        return provisionerName;
    }

    /**
     * Sets a friendly name to a provisioner
     * @param provisionerName friendly name
     */
    public void setProvisionerName(final String provisionerName) {
        this.provisionerName = provisionerName;
    }

    /**
     * Returns a unique uuid generated for this provisioner
     * @return UUID
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Returns {@link AllocatedGroupRange} for this provisioner
     * @return allocated range of group addresses
     */
    public AllocatedGroupRange getAllocatedGroupRange() {
        return allocatedGroupRange;
    }

    /**
     * Sets {@link AllocatedGroupRange} for this provisioner
     * @param allocatedGroupRange allocated range of group addresses
     */
    public void setAllocatedGroupRange(final AllocatedGroupRange allocatedGroupRange) {
        this.allocatedGroupRange = allocatedGroupRange;
    }


    /**
     * Returns {@link AllocatedUnicastRange} for this provisioner
     * @return allocated range of unicast addresses
     */
    public AllocatedUnicastRange getAllocatedUnicastRange() {
        return allocatedUnicastRange;
    }

    /**
     * Sets {@link AllocatedGroupRange} for this provisioner
     * @param allocatedUnicastRange allocated range of unicast addresses
     */
    public void setAllocatedUnicastRange(final AllocatedUnicastRange allocatedUnicastRange) {
        this.allocatedUnicastRange = allocatedUnicastRange;
    }
}
