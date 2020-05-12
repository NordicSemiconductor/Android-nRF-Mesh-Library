package no.nordicsemi.android.mesh.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseIntArray;

public class SparseIntArrayParcelable extends SparseIntArray implements Parcelable {

    public static Creator<SparseIntArrayParcelable> CREATOR = new Creator<SparseIntArrayParcelable>() {
        @Override
        public SparseIntArrayParcelable createFromParcel(Parcel source) {
            SparseIntArrayParcelable read = new SparseIntArrayParcelable();
            int size = source.readInt();

            int[] keys = new int[size];
            int[] values = new int[size];

            source.readIntArray(keys);
            source.readIntArray(values);

            for (int i = 0; i < size; i++) {
                read.put(keys[i], values[i]);
            }

            return read;
        }

        @Override
        public SparseIntArrayParcelable[] newArray(int size) {
            return new SparseIntArrayParcelable[size];
        }
    };

    public SparseIntArrayParcelable() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int[] keys = new int[size()];
        int[] values = new int[size()];

        for (int i = 0; i < size(); i++) {
            keys[i] = keyAt(i);
            values[i] = valueAt(i);
        }

        dest.writeInt(size());
        dest.writeIntArray(keys);
        dest.writeIntArray(values);
    }
}
