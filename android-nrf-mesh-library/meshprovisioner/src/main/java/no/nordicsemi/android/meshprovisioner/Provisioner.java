package no.nordicsemi.android.meshprovisioner;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;

import java.util.List;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Class definition of a Provisioner of mesh network
 */
@SuppressWarnings("unused")
@Entity(tableName = "provisioner",
        foreignKeys = @ForeignKey(entity = MeshNetwork.class,
                parentColumns = "mesh_uuid",
                childColumns = "mesh_uuid",
                onUpdate = CASCADE, onDelete = CASCADE),
        indices = @Index("mesh_uuid"))
public class Provisioner {

    @ColumnInfo(name = "mesh_uuid")
    @Expose
    private String meshUuid;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "uuid")
    @Expose
    private String uuid;

    @ColumnInfo(name = "name")
    @Expose
    private String provisionerName = "nRF Mesh Provisioner";

    @Ignore
    @Expose
    private List<AllocatedGroupRange> allocatedGroupRange;

    @Ignore
    @Expose
    private List<AllocatedUnicastRange> allocatedUnicastRange;

    @Ignore
    @Expose
    private List<AllocatedSceneRange> allocatedSceneRange;

    /**
     * Constructs {@link Provisioner}
     */
    Provisioner() {

    }

    String getMeshUuid() {
        return meshUuid;
    }

    void setMeshUuid(final String meshUuid) {
        this.meshUuid = meshUuid;
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
     * Returns a unique uuid generated for this provisioner
     *
     * @return UUID
     */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
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
}
