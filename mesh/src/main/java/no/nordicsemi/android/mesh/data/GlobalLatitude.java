package no.nordicsemi.android.mesh.data;

import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.pow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * GlobalLatitude is an object representing the Global Latitude field of the Generic Location model.
 * It is an abstract class with two concrete implementations:
 * <ul>
 *     <li>Coordinate: A latitude position in the World Geodetic System (WGS84) coordinate system.</li>
 *     <li>NotConfigured: Latitude is not configured.</li>
 * </ul>
 */
public abstract class GlobalLatitude {
    private static final int ENCODED_VALUE_NOT_CONFIGURED = 0x80000000;

    private GlobalLatitude() {
    }

    public abstract int getEncodedValue();

    /**
     * Creates a GlobalLatitude from an encoded value.
     *
     * @param encodedValue a value encoded according to the Global Latitude field of the Generic Location model
     * @return either a Coordinate or NotConfigured instance representing the encodedValue
     */
    @NonNull
    public static GlobalLatitude of(int encodedValue) {
        if (encodedValue == ENCODED_VALUE_NOT_CONFIGURED) {
            return new NotConfigured();
        }
        return new Coordinate(encodedValue);
    }

    /**
     * Encodes a latitude position in the World Geodetic System (WGS84) coordinate system.
     *
     * @param position a position in the range of -90° to 90° inclusive
     * @return a Coordinate instance encoding the position
     * @throws IllegalArgumentException if position is out of range
     */
    @NonNull
    public static Coordinate encode(double position) {
        return new Coordinate(position);
    }

    /**
     * Creates a NotConfigured instance representing that global latitude is not configured.
     *
     * @return a NotConfigured instance
     */
    @NonNull
    public static NotConfigured notConfigured() {
        return new NotConfigured();
    }

    /**
     * Get the decoded latitude position in the World Geodetic System (WGS84) coordinate system.
     *
     * @return the position or null if `not configured`
     */
    @Nullable
    public Double getDecodedPosition() {
        if (this instanceof Coordinate) {
            return ((Coordinate) this).getPosition();
        }
        return null;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalLatitude that = (GlobalLatitude) o;
        return getEncodedValue() == that.getEncodedValue();
    }

    @Override
    public final int hashCode() {
        return Integer.valueOf(getEncodedValue()).hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "GlobalLatitude." + this.getClass().getSimpleName() + " encodedValue: " + Integer.toHexString(this.getEncodedValue()) + " position: " + this.getDecodedPosition();
    }

    public final static class Coordinate extends GlobalLatitude {

        private final int encodedValue;

        private Coordinate(int encodedValue) {
            if (encodedValue == ENCODED_VALUE_NOT_CONFIGURED) {
                throw new IllegalArgumentException("Encoded value can't be ´NOT CONFIGURED´ (0x80000000)");
            }
            this.encodedValue = encodedValue;
        }

        private Coordinate(double value) {
            if (value < -90 || value > 90) {
                throw new IllegalArgumentException("Latitude coordinate must be between -90 and 90 degrees inclusive");
            }
            int n = (int) floor((value / 90) * (pow(2, 31) - 1));
            this.encodedValue = max(0x80000001, n);
        }

        @Override
        public int getEncodedValue() {
            return encodedValue;
        }

        public double getPosition() {
            return ((double) this.encodedValue) / (pow(2, 31) - 1) * 90;
        }
    }

    public final static class NotConfigured extends GlobalLatitude {
        private NotConfigured() {
        }

        @Override
        public int getEncodedValue() {
            return ENCODED_VALUE_NOT_CONFIGURED;
        }
    }
}
