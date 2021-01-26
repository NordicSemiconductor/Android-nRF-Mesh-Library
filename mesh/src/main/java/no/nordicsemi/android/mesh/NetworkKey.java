package no.nordicsemi.android.mesh;

import android.os.Parcel;

import com.google.gson.annotations.Expose;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Wrapper class for network key
 */
@SuppressWarnings({"WeakerAccess"})
@Entity(tableName = "network_key",
        foreignKeys = @ForeignKey(
                entity = MeshNetwork.class,
                parentColumns = "mesh_uuid",
                childColumns = "mesh_uuid",
                onUpdate = CASCADE,
                onDelete = CASCADE),
        indices = @Index("mesh_uuid"))
public final class NetworkKey extends MeshKey {

    // Key refresh phases
    public static final int PHASE_0 = 0; //Normal operation
    public static final int PHASE_1 = 1; //Key Distribution
    public static final int PHASE_2 = 2; //Switching to new keys
    public static final int PHASE_3 = 3; //Revoking old keys

    @ColumnInfo(name = "phase")
    @Expose
    private int phase = PHASE_0;

    @ColumnInfo(name = "security")
    @Expose
    private boolean minSecurity;

    @ColumnInfo(name = "timestamp")
    @Expose
    private long timestamp;

    @Ignore
    private byte[] identityKey;

    /**
     * Constructs a NetworkKey object with a given key index and network key
     *
     * @param keyIndex 12-bit network key index
     * @param key      16-byte network key
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public NetworkKey(final int keyIndex, @NonNull final byte[] key) {
        super(keyIndex, key);
        name = "Network Key " + (keyIndex + 1);
        identityKey = SecureUtils.calculateIdentityKey(key);
        timestamp = System.currentTimeMillis();
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

    /**
     * Returns the key refresh phase of the network key
     *
     * @return int phase
     */
    @KeyRefreshPhase
    public int getPhase() {
        return phase;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setPhase(@KeyRefreshPhase final int phase) {
        this.phase = phase;
    }

    @Override
    public void setKey(@NonNull final byte[] key) {
        super.setKey(key);
        identityKey = SecureUtils.calculateIdentityKey(key);
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
     * Sets  the minimum security.
     *
     * @param minSecurity true if minimum security or false if insecure.
     */
    public void setMinSecurity(final boolean minSecurity) {
        this.minSecurity = minSecurity;
    }

    /**
     * Returns the identity key derived
     */
    public byte[] getIdentityKey() {
        return identityKey;
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
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PHASE_0, PHASE_1, PHASE_2, PHASE_3})
    public @interface KeyRefreshPhase {
    }

    @NonNull
    @Override
    public NetworkKey clone() throws CloneNotSupportedException {
        return (NetworkKey) super.clone();
    }

    /**
     * Updates the currently used {@link #key} with the newKey  and sets the currently used key as the {@link #oldKey}
     *
     * @param newKey New NetworkKey value
     * @return true if successful or false otherwise
     * @throws IllegalArgumentException if a NetworkKey update is attempted twice
     */
    protected boolean distributeKey(@NonNull final byte[] newKey) throws IllegalArgumentException {
        if (valid(newKey)) {
            if (phase == 0 || phase == 1) {
                phase = 1;
                timestamp = System.currentTimeMillis();
                return super.distributeKey(newKey);
            } else {
                throw new IllegalArgumentException("A NetworkKey can only be updated once during a Key Refresh Procedure.");
            }
        }
        return false;
    }

    /**
     * Switch to New Key.
     *
     * @return true if successful or false otherwise.
     */
    protected boolean switchToNewKey() {
        if (phase == 1) {
            setPhase(PHASE_2);
            timestamp = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Revokes old key by switching the phase to {@link KeyRefreshPhase PHASE_3 or PHASE_0}
     *
     * @return true if successful or false otherwise.
     */
    protected boolean revokeOldKey() {
        if (phase == PHASE_1 || phase == PHASE_2 || phase == PHASE_3) {
            phase = PHASE_0;
            timestamp = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
