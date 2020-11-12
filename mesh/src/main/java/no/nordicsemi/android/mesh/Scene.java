package no.nordicsemi.android.mesh;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Class definitions for creating scenes in a mesh network
 */
@Entity(tableName = "scene",
        foreignKeys = @ForeignKey(entity = MeshNetwork.class,
                parentColumns = "mesh_uuid",
                childColumns = "mesh_uuid",
                onUpdate = CASCADE, onDelete = CASCADE),
        indices = @Index("mesh_uuid"))
public class Scene implements Parcelable {
    @ColumnInfo(name = "mesh_uuid")
    @Expose
    private final String meshUuid;

    @ColumnInfo(name = "name")
    @Expose
    private String name = "nRF Scene";

    @TypeConverters(MeshTypeConverters.class)
    @Expose
    protected List<Integer> addresses = new ArrayList<>();

    @PrimaryKey
    @ColumnInfo(name = "number")
    @Expose
    private int number;

    public Scene(final int number, @NonNull final String meshUuid) {
        this.number = number;
        this.meshUuid = meshUuid;
    }

    @Ignore
    public Scene(final int number, @NonNull final List<Integer> addresses, @NonNull final String meshUuid) {
        this.number = number;
        this.addresses.addAll(addresses);
        this.meshUuid = meshUuid;
    }

    protected Scene(Parcel in) {
        meshUuid = in.readString();
        name = in.readString();
        number = in.readInt();
    }

    public static final Creator<Scene> CREATOR = new Creator<Scene>() {
        @Override
        public Scene createFromParcel(Parcel in) {
            return new Scene(in);
        }

        @Override
        public Scene[] newArray(int size) {
            return new Scene[size];
        }
    };

    public String getMeshUuid() {
        return meshUuid;
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
        return Collections.unmodifiableList(addresses);
    }

    /**
     * Sets addresses for this group
     *
     * @param addresses list of addresses
     */
    public void setAddresses(final List<Integer> addresses) {
        this.addresses.clear();
        this.addresses.addAll(addresses);
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(meshUuid);
        dest.writeString(name);
        dest.writeInt(number);
    }

    /**
     * Validates the excene number
     *
     * @param sceneNumber Scene number
     * @return true if is a valid or throws an IllegalArgument exception
     */
    public static boolean isValidSceneNumber(final int sceneNumber) {
        if (sceneNumber > 0x0000 && sceneNumber <= 0xFFFF) return true;
        throw new IllegalArgumentException("Scene number must range from 0x0001 to 0xFFFF!");
    }

    /**
     * Formats the scene number in to a 4 character hexadecimal String
     *
     * @param number Scene number
     * @param add0x  Sets "0x" as prefix if set to true or false otherwise
     */
    public static String formatSceneNumber(final int number, final boolean add0x) {
        return add0x ?
                "0x" + String.format(Locale.US, "%04X", number) :
                String.format(Locale.US, "%04X", number);
    }

    @Override
    public String toString() {
        return "Scene{" +
                "meshUuid='" + meshUuid + '\'' +
                ", name='" + name + '\'' +
                ", addresses=" + addresses +
                ", number=" + number +
                '}';
    }
}
