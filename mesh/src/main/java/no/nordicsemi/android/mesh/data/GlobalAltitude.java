package no.nordicsemi.android.mesh.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * GlobalAltitude is an object representing the Global Altitude field of the Generic Location model.
 * It is an abstract class with three concrete implementations:
 * <ul>
 *     <li>Coordinate: A altitude position in the World Geodetic System (WGS84) coordinate system.</li>
 *     <li>GreaterThanOrEqualTo32766: Altitude is greater than or equal to 32766 meters.</li>
 *     <li>NotConfigured: Altitude is not configured.</li>
 * </ul>
 */
public abstract class GlobalAltitude {
    private static final short ENCODED_VALUE_NOT_CONFIGURED = 0x7FFF;
    private static final short ENCODED_VALUE_GREATER_THAN_OR_EQUAL_TO_32766 = 0x7FFE;

    private GlobalAltitude() {
    }

    public abstract short getEncodedValue();

    /**
     * Creates a GlobalAltitude from an encoded value.
     *
     * @param encodedValue a value encoded according to the Global Altitude field of the Generic Location model
     * @return either a Coordinate or NotConfigured instance representing the encodedValue
     */
    @NonNull
    public static GlobalAltitude of(short encodedValue) {
        if (encodedValue == ENCODED_VALUE_NOT_CONFIGURED) {
            return new NotConfigured();
        }
        if (encodedValue == ENCODED_VALUE_GREATER_THAN_OR_EQUAL_TO_32766) {
            return new GreaterThanOrEqualTo32766();
        }
        return new Coordinate(encodedValue);
    }

    /**
     * Encodes a altitude position in the World Geodetic System (WGS84) coordinate system.
     *
     * @param position a position in the range of -32768 to 32765 meters inclusive
     * @return a Coordinate instance encoding the position
     * @throws IllegalArgumentException if position is out of range
     */
    @NonNull
    public static Coordinate encode(int position) {
        return new Coordinate(position);
    }

    /**
     * Creates a NotConfigured instance representing that global altitude is not configured.
     *
     * @return a NotConfigured instance
     */
    @NonNull
    public static NotConfigured notConfigured() {
        return new NotConfigured();
    }

    /**
     * Get the decoded altitude position in the World Geodetic System (WGS84) coordinate system.
     *
     * @return the position or null if `not configured` or `greater than or equal to 32766 meters`
     */
    @Nullable
    public Integer getDecodedPosition() {
        if (this instanceof Coordinate) {
            return ((Coordinate) this).getPosition();
        }
        return null;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalAltitude that = (GlobalAltitude) o;
        return getEncodedValue() == that.getEncodedValue();
    }

    @Override
    public final int hashCode() {
        return Integer.valueOf(getEncodedValue()).hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "GlobalAltitude." + this.getClass().getSimpleName() + " encodedValue: " + Integer.toHexString(this.getEncodedValue()) + " position: " + this.getDecodedPosition();
    }

    public final static class Coordinate extends GlobalAltitude {

        private final short encodedValue;

        private Coordinate(short encodedValue) {
            if (encodedValue == ENCODED_VALUE_NOT_CONFIGURED) {
                throw new IllegalArgumentException("Encoded value can't be ´NOT_CONFIGURED´ (0x7FFF)");
            }
            if (encodedValue == ENCODED_VALUE_GREATER_THAN_OR_EQUAL_TO_32766) {
                throw new IllegalArgumentException("Encoded value can't be ´GREATER_THAN_OR_EQUAL_TO_32766´ (0x7FFE)");
            }
            this.encodedValue = encodedValue;
        }

        private Coordinate(int value) {
            if (value < -32768 || value > 32765) {
                throw new IllegalArgumentException("Altitude coordinate must be between -32768 and 32765 meters inclusive");
            }
            this.encodedValue = (short) value;
        }

        @Override
        public short getEncodedValue() {
            return encodedValue;
        }

        public int getPosition() {
            return (int) encodedValue;
        }
    }

    public final static class NotConfigured extends GlobalAltitude {
        private NotConfigured() {
        }

        @Override
        public short getEncodedValue() {
            return ENCODED_VALUE_NOT_CONFIGURED;
        }
    }

    public final static class GreaterThanOrEqualTo32766 extends GlobalAltitude {
        private GreaterThanOrEqualTo32766() {
        }

        @Override
        public short getEncodedValue() {
            return ENCODED_VALUE_GREATER_THAN_OR_EQUAL_TO_32766;
        }
    }
}
