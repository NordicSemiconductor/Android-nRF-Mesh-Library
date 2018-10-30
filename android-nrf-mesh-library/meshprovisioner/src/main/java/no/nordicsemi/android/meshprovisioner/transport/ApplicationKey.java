package no.nordicsemi.android.meshprovisioner.transport;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

/**
 * Wrapper class for application key
 */
@SuppressWarnings("WeakerAccess")
public final class ApplicationKey implements Parcelable {

    @Expose
    final int keyIndex;
    @Expose
    final byte[] key;
    /**
     * Constructs a ApplicationKey object with a given key index and network key
     *
     * @param keyIndex 12-bit app key index
     * @param key      16-byte app key
     */
    public ApplicationKey(final int keyIndex, final byte[] key) {
        this.key = key;
        this.keyIndex = keyIndex;
    }

    protected ApplicationKey(Parcel in) {
        keyIndex = in.readInt();
        key = in.createByteArray();
    }

    public static final Creator<ApplicationKey> CREATOR = new Creator<ApplicationKey>() {
        @Override
        public ApplicationKey createFromParcel(Parcel in) {
            return new ApplicationKey(in);
        }

        @Override
        public ApplicationKey[] newArray(int size) {
            return new ApplicationKey[size];
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
