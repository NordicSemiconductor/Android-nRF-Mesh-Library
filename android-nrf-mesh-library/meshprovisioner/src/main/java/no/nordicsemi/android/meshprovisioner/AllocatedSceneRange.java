package no.nordicsemi.android.meshprovisioner;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Class definition for allocating group range for provisioners.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Entity(tableName = "allocated_scene_range",
        foreignKeys = @ForeignKey(entity = Provisioner.class,
                parentColumns = "provisioner_uuid",
                childColumns = "provisioner_uuid",
                onUpdate = CASCADE, onDelete =
                CASCADE),
indices = @Index("provisioner_uuid"))
public class AllocatedSceneRange {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    int id;

    @ColumnInfo(name = "provisioner_uuid")
    String provisionerUuid;

    @ColumnInfo(name = "first_scene")
    @Expose
    private int firstScene;

    @ColumnInfo(name = "last_scene")
    @Expose
    private int lastScene;

    @Ignore
    public AllocatedSceneRange() {

    }

    /**
     * Constructs {@link AllocatedSceneRange} for provisioner
     *
     * @param firstScene high address of group range
     * @param lastScene  low address of group range
     */
    public AllocatedSceneRange(final int firstScene, final int lastScene) {
        this.firstScene = firstScene;
        this.lastScene = lastScene;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Returns the provisionerUuid of the Mesh network
     * @return String provisionerUuid
     */
    public String getProvisionerUuid() {
        return provisionerUuid;
    }

    /**
     * Sets the provisionerUuid of the mesh network to this application key
     * @param provisionerUuid mesh network provisionerUuid
     */
    public void setProvisionerUuid(final String provisionerUuid) {
        this.provisionerUuid = provisionerUuid;
    }

    /**
     * Returns the low address of the allocated group address
     *
     * @return low address
     */
    public int getLastScene() {
        return lastScene;
    }

    /**
     * Sets the low address of the allocated group address
     *
     * @param lastScene of the group range
     */
    public void setLastScene(final int lastScene) {
        this.lastScene = lastScene;
    }

    /**
     * Returns the high address of the allocated group range
     *
     * @return firstScene of the group range
     */
    public int getFirstScene() {
        return firstScene;
    }

    /**
     * Sets the high address of the group address
     *
     * @param firstScene of the group range
     */
    public void setFirstScene(final int firstScene) {
        this.firstScene = firstScene;
    }
}
