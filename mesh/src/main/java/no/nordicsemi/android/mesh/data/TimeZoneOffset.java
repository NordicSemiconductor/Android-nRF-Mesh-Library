package no.nordicsemi.android.mesh.data;

import androidx.annotation.NonNull;

public final class TimeZoneOffset {
    private final byte encodedValue;

    private TimeZoneOffset(byte encodedValue) {
        this.encodedValue = encodedValue;
    }

    /**
     * Creates a TimeZoneOffset from an encoded value.
     *
     * @param encodedValue a value encoded according to the Time Zone Offset Current / New field of the Time model
     * @return a TimeZoneOffset instance representing the encodedValue
     */
    @NonNull
    public static TimeZoneOffset of(byte encodedValue) {
        return new TimeZoneOffset(encodedValue);
    }

    /**
     * Encodes a TimeZoneOffset from hours. The encoding is an approximation and will clamp hours to
     * [-64, 47.75] with a resolution of 0.25 hours.
     *
     * @param hours the time offset in hours
     * @return a TimeZoneOffset instance approximating the hour offset.
     */
    @NonNull
    public static TimeZoneOffset encode(double hours) {
        return new TimeZoneOffset((byte) Math.max(0, Math.min(255, Math.round(hours * 4 + 64))));
    }

    public byte getEncodedValue() {
        return encodedValue;
    }

    public double getHours() {
        return ((double) (encodedValue & 0xFF) - 64) / 4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeZoneOffset that = (TimeZoneOffset) o;
        return encodedValue == that.encodedValue;
    }

    @Override
    public int hashCode() {
        return Byte.valueOf(encodedValue).hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "TimeZoneOffset{" +
                "encodedValue=" + encodedValue +
                " hours=" + getHours() +
                '}';
    }
}
