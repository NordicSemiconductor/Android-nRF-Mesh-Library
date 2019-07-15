package no.nordicsemi.android.meshprovisioner;

import com.google.gson.annotations.Expose;

import java.util.List;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Class definitions for creating scenes in a mesh network
 */
@SuppressWarnings("unused")
@Entity(tableName = "scene",
        foreignKeys = @ForeignKey(entity = MeshNetwork.class,
                parentColumns = "mesh_uuid",
                childColumns = "mesh_uuid",
                onUpdate = CASCADE, onDelete = CASCADE),
        indices = @Index("mesh_uuid"))
public class Scene {
    @ColumnInfo(name = "mesh_uuid")
    @Expose
    private String meshUuid;

    @ColumnInfo(name = "name")
    @Expose
    private String name = "nRF Scene";

    @TypeConverters(MeshTypeConverters.class)
    @Expose
    private List<Integer> addresses;

    @PrimaryKey
    @ColumnInfo(name = "number")
    @Expose
    private int number;

    public Scene(final int number, final List<Integer> addresses, final String meshUuid) {
        this.number = number;
        this.addresses = addresses;
        this.meshUuid = meshUuid;
    }

    public String getMeshUuid() {
        return meshUuid;
    }

    public void setMeshUuid(final String meshUuid) {
        this.meshUuid = meshUuid;
    }

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
    public List<Integer> getAddresses() {
        return addresses;
    }

    /**
     * Sets addresses for this group
     *
     * @param addresses list of addresses
     */
    public void setAddresses(final List<Integer> addresses) {
        this.addresses = addresses;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValidScene(final int scene) {
        return scene >= 0x0000 && scene <= 0xFFFF;
    }
}
