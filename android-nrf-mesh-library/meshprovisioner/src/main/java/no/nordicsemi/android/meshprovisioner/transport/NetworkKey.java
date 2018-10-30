package no.nordicsemi.android.meshprovisioner.transport;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Wrapper class for network key
 */
@SuppressWarnings("WeakerAccess")
public final class NetworkKey implements Parcelable {

    final int keyIndex;
    final byte[] key;

    /**
     * Constructs a NetworkKey object with a given key index and network key
     *
     * @param keyIndex 12-bit network key index
     * @param key      16-byte network key
     */
    public NetworkKey(final int keyIndex, final byte[] key) {
        this.key = key;
        this.keyIndex = keyIndex;
    }

    protected NetworkKey(Parcel in) {
        keyIndex = in.readInt();
        key = in.createByteArray();
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

    public byte[] getKey() {
        return key;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(keyIndex);
        dest.writeByteArray(key);
    }
}
