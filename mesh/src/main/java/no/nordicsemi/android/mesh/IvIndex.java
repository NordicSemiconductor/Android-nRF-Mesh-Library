package no.nordicsemi.android.mesh;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Class containing the current IV Index State of the network.
 * <p>
 * Created by Roshan Rajaratnam on 21/04/2020.
 */
@SuppressWarnings("WeakerAccess")
public class IvIndex implements Parcelable {

    private final int ivIndex;
    private boolean isIvUpdateActive; // False: Normal Operation, True: IV Update in progress
    private boolean ivRecoveryFlag = false;
    @Nullable
    private Calendar transitionDate;

    /**
     * Construct the IV Index state of the mesh network
     *
     * @param ivIndex          IV Index of the network.
     * @param isIvUpdateActive If true IV Update is in progress and false the network is in Normal operation.
     * @param transitionDate   Time when the last IV Update happened
     */
    public IvIndex(final int ivIndex, final boolean isIvUpdateActive, @Nullable final Calendar transitionDate) {
        this.ivIndex = ivIndex;
        this.isIvUpdateActive = isIvUpdateActive;
        this.transitionDate = transitionDate;
    }

    protected IvIndex(Parcel in) {
        ivIndex = in.readInt();
        isIvUpdateActive = in.readByte() != 0;
        ivRecoveryFlag = in.readByte() != 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "IV Index: " + ivIndex + ", IV Update Active: " + isIvUpdateActive;
    }

    public static final Creator<IvIndex> CREATOR = new Creator<IvIndex>() {
        @Override
        public IvIndex createFromParcel(Parcel in) {
            return new IvIndex(in);
        }

        @Override
        public IvIndex[] newArray(int size) {
            return new IvIndex[size];
        }
    };

    /**
     * Returns current iv index
     */
    public int getIvIndex() {
        return ivIndex;
    }

    /**
     * Returns the current iv update flag.
     */
    public boolean isIvUpdateActive() {
        return isIvUpdateActive;
    }

    /**
     * Sets the current IV Update state for the given IV Index
     *
     * @param flag true if the IV Update is active or false otherwise.
     */
    protected void setIvUpdateActive(final boolean flag) {
        isIvUpdateActive = flag;
    }

    /**
     * Returns iv index used when transmitting messages.
     */
    public int getTransmitIvIndex() {
        return (isIvUpdateActive && ivIndex != 0) ? ivIndex - 1 : ivIndex;
    }

    public boolean getIvRecoveryFlag() {
        return ivRecoveryFlag;
    }

    public void setIvRecoveryFlag(boolean ivRecoveryFlag) {
        this.ivRecoveryFlag = ivRecoveryFlag;
    }

    @Nullable
    public Calendar getTransitionDate() {
        return transitionDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ivIndex);
        dest.writeByte((byte) (isIvUpdateActive ? 1 : 0));
        dest.writeByte((byte) (ivRecoveryFlag ? 1 : 0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IvIndex ivIndex1 = (IvIndex) o;
        return ivIndex == ivIndex1.ivIndex &&
                isIvUpdateActive == ivIndex1.isIvUpdateActive &&
                ivRecoveryFlag == ivIndex1.ivRecoveryFlag &&
                ((transitionDate == ivIndex1.transitionDate) ||
                        ((transitionDate != null && ivIndex1.transitionDate != null) &&
                                (transitionDate.getTimeInMillis() == ivIndex1.transitionDate.getTimeInMillis()))
                );
    }
}
