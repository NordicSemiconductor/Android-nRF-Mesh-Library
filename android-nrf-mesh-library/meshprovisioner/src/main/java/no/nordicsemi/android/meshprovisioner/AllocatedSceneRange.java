package no.nordicsemi.android.meshprovisioner;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Class definition for allocating group range for provisioners.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Entity(tableName = "allocated_scene_range")
public class AllocatedSceneRange {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    int id;

    @ForeignKey(entity = Provisioner.class, parentColumns = "uuid", childColumns = "provisioner_uuid", onUpdate = CASCADE, onDelete = CASCADE)
    @ColumnInfo(name = "provisioner_uuid")
    String uuid;

    @ColumnInfo(name = "first_scene")
    @Expose
    private int firstScene;

    @ColumnInfo(name = "last_scene")
    @Expose
    private int lastScene;

    public AllocatedSceneRange(){

    }
    /**
     * Constructs {@link AllocatedSceneRange} for provisioner
     *
     * @param firstScene high address of group range
     * @param lastScene  low address of group range
     */
    @Ignore
    public AllocatedSceneRange(final int firstScene, final int lastScene) {
        this.firstScene = firstScene;
        this.lastScene = lastScene;
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
