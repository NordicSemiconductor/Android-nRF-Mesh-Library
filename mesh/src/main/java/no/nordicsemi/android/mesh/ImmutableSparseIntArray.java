/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.nordicsemi.android.mesh;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;

/**
 * Unmodifiable version of {@link SparseArray}.
 */
@SuppressWarnings("WeakerAccess")
public final class ImmutableSparseIntArray implements Parcelable {
    private static final String TAG = "ImmutableSparseIntArray";
    private SparseIntArray array;

    public ImmutableSparseIntArray(@NonNull SparseIntArray array) {
        this.array = array;
    }

    protected ImmutableSparseIntArray(Parcel source) {
        array = new SparseIntArray();
        int size = source.readInt();

        int[] keys = new int[size];
        int[] values = new int[size];

        source.readIntArray(keys);
        source.readIntArray(values);

        for (int i = 0; i < size; i++) {
            array.put(keys[i], values[i]);
        }
    }

    public static final Creator<ImmutableSparseIntArray> CREATOR = new Creator<ImmutableSparseIntArray>() {
        @Override
        public ImmutableSparseIntArray createFromParcel(Parcel in) {
            return new ImmutableSparseIntArray(in);
        }

        @Override
        public ImmutableSparseIntArray[] newArray(int size) {
            return new ImmutableSparseIntArray[size];
        }
    };

    protected SparseIntArray getArray() {
        return array;
    }

    public int size() {
        return array.size();
    }

    public int get(int key) {
        return array.get(key);
    }

    public int get(int key, int valueIfKeyNotFound) {
        return array.get(key, valueIfKeyNotFound);
    }

    public int keyAt(int index) {
        return array.keyAt(index);
    }

    public int valueAt(int index) {
        return array.valueAt(index);
    }

    public int indexOfValue(int value) {
        return array.indexOfValue(value);
    }

    protected void put(final int key, final int value) {
        array.put(key, value);
    }

    protected void delete(final int key) {
        array.delete(key);
    }

    protected void clear() {
        array.clear();
    }

    @Override
    public String toString() {
        return array.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
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
