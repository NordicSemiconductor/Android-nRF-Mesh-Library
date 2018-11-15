package no.nordicsemi.android.meshprovisioner;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.meshprovisioner.utils.MeshTypeConverters;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Class definition of a Provisioner of mesh network
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Entity(tableName = "provisioner",
        foreignKeys = @ForeignKey(entity = MeshNetwork.class,
                parentColumns = "mesh_uuid",
                childColumns = "mesh_uuid",
                onUpdate = CASCADE, onDelete = CASCADE),
        indices = @Index("mesh_uuid"))
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class Provisioner {

    @ColumnInfo(name = "mesh_uuid")
    @Expose
    private String meshUuid;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "provisioner_uuid")
    @Expose
    private String provisionerUuid;

    @ColumnInfo(name = "name")
    @Expose
    private String provisionerName = "nRF Mesh Provisioner";

    @TypeConverters(MeshTypeConverters.class)
    @Expose
    private List<AllocatedGroupRange> allocatedGroupRange = new ArrayList<>();

    @TypeConverters(MeshTypeConverters.class)
    @Expose
    private List<AllocatedUnicastRange> allocatedUnicastRange = new ArrayList<>();

    @TypeConverters(MeshTypeConverters.class)
    @Expose
    private List<AllocatedSceneRange> allocatedSceneRange = new ArrayList<>();

    @ColumnInfo(name = "sequence_number")
    @Expose
    private int sequenceNumber;

    @ColumnInfo(name = "provisioner_address")
    @Expose
    private byte[] provisionerAddress = {0x07, (byte) 0xFF};

    @ColumnInfo(name = "unicast_address")
    @Expose
    private byte[] unicastAddress;

    @ColumnInfo(name = "global_ttl")
    @Expose
    private int globalTtl;

    @ColumnInfo(name = "last_selected")
    @Expose
    private boolean lastSelected;

    /**
     * Constructs {@link Provisioner}
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Provisioner() {

    }

    /**
     * Returns the provisionerUuid of the Mesh network
     *
     * @return String provisionerUuid
     */
    public String getMeshUuid() {
        return meshUuid;
    }

    /**
     * Sets the provisionerUuid of the mesh network to this application key
     *
     * @param uuid mesh network provisionerUuid
     */
    public void setMeshUuid(final String uuid) {
        meshUuid = uuid;
    }

    /**
     * Returns the provisioner name
     *
     * @return name
     */
    public String getProvisionerName() {
        return provisionerName;
    }

    /**
     * Sets a friendly name to a provisioner
     *
     * @param provisionerName friendly name
     */
    public void setProvisionerName(final String provisionerName) {
        this.provisionerName = provisionerName;
    }

    /**
     * Returns the provisionerUuid
     *
     * @return UUID
     */
    @NonNull
    public String getProvisionerUuid() {
        return provisionerUuid;
    }

    public void setProvisionerUuid(@NonNull final String provisionerUuid) {
        this.provisionerUuid = provisionerUuid;
    }

    /**
     * Returns {@link AllocatedGroupRange} for this provisioner
     *
     * @return allocated range of group addresses
     */
    public List<AllocatedGroupRange> getAllocatedGroupRange() {
        return allocatedGroupRange;
    }

    /**
     * Sets {@link AllocatedGroupRange} for this provisioner
     *
     * @param allocatedGroupRange allocated range of group addresses
     */
    public void setAllocatedGroupRange(final List<AllocatedGroupRange> allocatedGroupRange) {
        this.allocatedGroupRange = allocatedGroupRange;
    }

    /**
     * Returns {@link AllocatedUnicastRange} for this provisioner
     *
     * @return allocated range of unicast addresses
     */
    public List<AllocatedUnicastRange> getAllocatedUnicastRange() {
        return allocatedUnicastRange;
    }

    /**
     * Sets {@link AllocatedGroupRange} for this provisioner
     *
     * @param allocatedUnicastRange allocated range of unicast addresses
     */
    public void setAllocatedUnicastRange(final List<AllocatedUnicastRange> allocatedUnicastRange) {
        this.allocatedUnicastRange = allocatedUnicastRange;
    }

    /**
     * Returns {@link AllocatedSceneRange} for this provisioner
     *
     * @return allocated range of unicast addresses
     */
    public List<AllocatedSceneRange> getAllocatedSceneRange() {
        return allocatedSceneRange;
    }

    /**
     * Sets {@link AllocatedSceneRange} for this provisioner
     *
     * @param allocatedSceneRange allocated range of unicast addresses
     */
    public void setAllocatedSceneRange(final List<AllocatedSceneRange> allocatedSceneRange) {
        this.allocatedSceneRange = allocatedSceneRange;
    }


    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(final int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public byte[] getProvisionerAddress() {
        return provisionerAddress;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setProvisionerAddress(final byte[] provisionerAddress) {
        this.provisionerAddress = provisionerAddress;
    }

    /**
     * Returns the next available unicast address
     *
     * @return unicast address
     */
    public byte[] getUnicastAddress() {
        return unicastAddress;
    }

    /**
     * Sets the next available unicast address
     *
     * @param unicastAddress address
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setUnicastAddress(final byte[] unicastAddress) {
        this.unicastAddress = unicastAddress;
    }

    public int getGlobalTtl() {
        return globalTtl;
    }

    public void setGlobalTtl(final int globalTtl) {
        this.globalTtl = globalTtl;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public boolean isLastSelected() {
        return lastSelected;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setLastSelected(final boolean lastSelected) {
        this.lastSelected = lastSelected;
    }

    public int incrementSequenceNumber() {
        sequenceNumber = sequenceNumber + 1;
        return sequenceNumber;
    }
}
