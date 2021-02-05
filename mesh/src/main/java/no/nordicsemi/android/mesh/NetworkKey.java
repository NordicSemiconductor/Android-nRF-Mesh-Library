package no.nordicsemi.android.mesh;

import android.os.Parcel;

import com.google.gson.annotations.Expose;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    public static final int NORMAL_OPERATION = 0;
    public static final int KEY_DISTRIBUTION = 1;
    public static final int USING_NEW_KEYS = 2;
    //public static final int REVOKING_OLD_KEYS   = 3; This phase is instantaneous.

    // Transitions
    public static final int USE_NEW_KEYS = 2; //Normal operation
    public static final int REVOKE_OLD_KEYS = 3; //Key Distribution

    @ColumnInfo(name = "phase")
    @Expose
    private int phase = NORMAL_OPERATION;

    @ColumnInfo(name = "security")
    @Expose
    private boolean minSecurity;

    @ColumnInfo(name = "timestamp")
    @Expose
    private long timestamp;

    @Ignore
    private byte[] identityKey;

    @Ignore
    private byte[] oldIdentityKey;

    @Ignore
    private SecureUtils.K2Output derivatives;

    @Ignore
    private SecureUtils.K2Output oldDerivatives;

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
        derivatives = SecureUtils.calculateK2(key, SecureUtils.K2_MASTER_INPUT);
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
        identityKey = in.createByteArray();
        oldIdentityKey = in.createByteArray();
        derivatives = in.readParcelable(SecureUtils.K2Output.class.getClassLoader());
        oldDerivatives = in.readParcelable(SecureUtils.K2Output.class.getClassLoader());
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
        dest.writeByteArray(identityKey);
        dest.writeByteArray(oldIdentityKey);
        dest.writeParcelable(derivatives, flags);
        dest.writeParcelable(oldDerivatives, flags);
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
        derivatives = SecureUtils.calculateK2(key, SecureUtils.K2_MASTER_INPUT);
    }

    @Override
    public void setOldKey(final byte[] oldKey) {
        super.setOldKey(oldKey);
        oldIdentityKey = SecureUtils.calculateIdentityKey(oldKey);
        oldDerivatives = SecureUtils.calculateK2(oldKey, SecureUtils.K2_MASTER_INPUT);
    }

    /**
     * Returns the NetworkKey based on the key refresh procedure phase.
     *
     * @return key
     */
    public byte[] getTxNetworkKey() {
        switch (phase) {
            case KEY_DISTRIBUTION:
                return oldKey;
            case USING_NEW_KEYS:
            default:
                return key;
        }
    }

    /**
     * Returns the NetworkKey based on the key refresh procedure phase.
     *
     * @return key
     */
    public SecureUtils.K2Output getTxDerivatives() {
        switch (phase) {
            case KEY_DISTRIBUTION:
                return oldDerivatives;
            case USING_NEW_KEYS:
            default:
                return derivatives;
        }
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
     * Returns the identity key derived from the current key
     */
    public byte[] getIdentityKey() {
        return identityKey;
    }

    /**
     * Returns the identity key derived from the Old Key
     */
    public byte[] getOldIdentityKey() {
        return oldIdentityKey;
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
     * Returns the current phase description
     */
    public String getPhaseDescription() {
        switch (phase) {
            default:
            case NORMAL_OPERATION:
                return "Normal Operation";
            case KEY_DISTRIBUTION:
                return "Key Distribution";
            case USING_NEW_KEYS:
                return "Using New Keys";
        }
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
    @IntDef({NORMAL_OPERATION, KEY_DISTRIBUTION, USING_NEW_KEYS})
    public @interface KeyRefreshPhase {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({USE_NEW_KEYS, REVOKE_OLD_KEYS})
    public @interface KeyRefreshPhaseTransition {
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
     * @throws IllegalArgumentException if a NetworkKey distribution is attempted twice with different key
     *                                  values during a single Key refresh procedure
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
            setPhase(USING_NEW_KEYS);
            timestamp = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Revokes old key by switching the phase to {@link KeyRefreshPhase KEY_DISTRIBUTION or USING_NEW_KEYS}
     *
     * @return true if successful or false otherwise.
     */
    protected boolean revokeOldKey() {
        if (phase == KEY_DISTRIBUTION || phase == USING_NEW_KEYS) {
            phase = NORMAL_OPERATION;
            timestamp = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    protected byte[] getNetworkId() {
        return SecureUtils.calculateK3(key);
    }

    @Nullable
    protected byte[] getOldNetworkId() {
        return SecureUtils.calculateK3(oldKey);
    }


    /**
     * Returns the derivatives from the network key
     *
     * @return {@link SecureUtils.K2Output}
     */
    public SecureUtils.K2Output getDerivatives() {
        return derivatives;
    }

    /**
     * Returns the derivatives from the old network key
     *
     * @return {@link SecureUtils.K2Output}
     */
    @Nullable
    public SecureUtils.K2Output getOldDerivatives() {
        return oldDerivatives;
    }
}
