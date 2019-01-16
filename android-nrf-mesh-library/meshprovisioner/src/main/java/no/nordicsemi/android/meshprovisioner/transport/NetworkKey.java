package no.nordicsemi.android.meshprovisioner.transport;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.google.gson.annotations.Expose;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import no.nordicsemi.android.meshprovisioner.MeshNetwork;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Wrapper class for network key
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Entity(tableName = "network_key",
        foreignKeys = @ForeignKey(
                entity = MeshNetwork.class,
                parentColumns = "mesh_uuid",
                childColumns = "mesh_uuid",
                onUpdate = CASCADE,
                onDelete = CASCADE),
        indices = @Index("mesh_uuid"))
public final class NetworkKey implements Parcelable {

    // Key refresh phases
    public static final int PHASE_0 = 0; //Distribution of new keys
    public static final int PHASE_1 = 1; //Switching to the new keys
    public static final int PHASE_2 = 2; //Revoking the old keys
    public static final Creator<NetworkKey> CREATOR = new Creator<NetworkKey>() {
        @Override
        public NetworkKey createFromParcel(Parcel in) {
            return new NetworkKey(in);
        }

        @Override
        public NetworkKey[] newArray(int size) {
            return new NetworkKey[size];
        }
    };
    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "mesh_uuid")
    @Expose
    String meshUuid;

    @ColumnInfo(name = "index")
    @Expose
    private int keyIndex;

    @ColumnInfo(name = "name")
    @Expose
    private String name = "Network Key";

    @ColumnInfo(name = "key")
    @Expose
    private byte[] key;

    @ColumnInfo(name = "phase")
    @Expose
    private int phase = PHASE_0;

    @ColumnInfo(name = "security")
    @Expose
    private boolean minSecurity;

    @ColumnInfo(name = "old_key")
    @Expose
    private byte[] oldKey;

    @ColumnInfo(name = "timestamp")
    @Expose
    private long timestamp = 0x0;

    /**
     * Constructs a NetworkKey object with a given key index and network key
     *
     * @param keyIndex 12-bit network key index
     * @param key      16-byte network key
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public NetworkKey(final int keyIndex, final byte[] key) {
        this.key = key;
        this.keyIndex = keyIndex;
    }

    protected NetworkKey(Parcel in) {
        meshUuid = in.readString();
        keyIndex = in.readInt();
        name = in.readString();
        key = in.createByteArray();
        phase = in.readInt();
        minSecurity = in.readByte() != 0;
        oldKey = in.createByteArray();
        timestamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(meshUuid);
        dest.writeInt(keyIndex);
        dest.writeString(name);
        dest.writeByteArray(key);
        dest.writeInt(phase);
        dest.writeByte((byte) (minSecurity ? 1 : 0));
        dest.writeByteArray(oldKey);
        dest.writeLong(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getMeshUuid() {
        return meshUuid;
    }

    public void setMeshUuid(final String meshUuid) {
        this.meshUuid = meshUuid;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public int getId() {
        return id;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Returns the friendly name assigned to a network key
     *
     * @return String containing the a friendly name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a friendly name to a network key
     *
     * @param name friendly name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the network key
     *
     * @return 16-byte network key
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * Sets a network key
     *
     * @param key 16-byte network key
     */
    public void setKey(@NonNull final byte[] key) {
        this.key = key;
    }

    /**
     * Returns the key refresh phase of the network key
     *
     * @return int phase
     */
    @KeyRefreshPhases
    public int getPhase() {
        return phase;
    }

    public void setPhase(@KeyRefreshPhases final int phase) {
        this.phase = phase;
    }

    /**
     * Returns the network key index
     *
     * @return key index
     */
    public int getKeyIndex() {
        return keyIndex;
    }

    /**
     * Sets a network key index
     *
     * @param keyIndex network key index
     */
    public void setKeyIndex(final int keyIndex) {
        this.keyIndex = keyIndex;
    }

    /**
     * Uses min security
     *
     * @return true if minimum security or false otherwise
     */
    public boolean isMinSecurity() {
        return minSecurity;
    }

    /**
     * Set security
     *
     * @param minSecurity boolean security true if min false otherwise
     */
    public void setMinSecurity(final boolean minSecurity) {
        this.minSecurity = minSecurity;
    }

    /**
     * Returns the old network key
     *
     * @return old key
     */
    public byte[] getOldKey() {
        return oldKey;
    }

    /**
     * Set the old key
     *
     * @param oldKey old network key
     */
    public void setOldKey(final byte[] oldKey) {
        this.oldKey = oldKey;
    }

    /**
     * Returns the timestamp of the phase change
     *
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp when the the phase change happened
     *
     * @param timestamp timestamp
     */
    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PHASE_0, PHASE_1, PHASE_2})
    public @interface KeyRefreshPhases {
    }
}
