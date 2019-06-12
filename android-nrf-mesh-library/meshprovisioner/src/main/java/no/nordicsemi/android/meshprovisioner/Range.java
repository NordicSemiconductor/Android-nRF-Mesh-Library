package no.nordicsemi.android.meshprovisioner;

import android.os.Parcelable;

import androidx.room.Ignore;

@SuppressWarnings("WeakerAccess")
public abstract class Range implements Parcelable {

    @Ignore
    protected int lowerBound;

    @Ignore
    protected int upperBound;

    public abstract int getLowerBound();

    public abstract int getUpperBound();
}
