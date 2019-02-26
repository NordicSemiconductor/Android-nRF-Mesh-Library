package no.nordicsemi.android.meshprovisioner.utils;

import android.util.Log;

import java.util.ArrayList;

/**
 * Supported algorithm type
 */
@SuppressWarnings("unused")
public enum AlgorithmType {

    /**
     * Static OOB Type
     */
    NONE((short) 0x0000),
    FIPS_P_256_ELLIPTIC_CURVE((short) 0x0001);

    private static final String TAG = AlgorithmType.class.getSimpleName();
    private short algorithmType;

    AlgorithmType(final short algorithmType) {
        this.algorithmType = algorithmType;
    }

    /**
     * Returns the algorithm oob type value
     */
    public short getAlgorithmType() {
        return algorithmType;
    }

    /**
     * Parses the output oob action value
     *
     * @param algorithmTypeValue algorithm type
     * @return selected output action type
     */
    public static ArrayList<AlgorithmType> getAlgorithmTypeFromBitMask(final short algorithmTypeValue) {
        final AlgorithmType[] algorithmTypes = {FIPS_P_256_ELLIPTIC_CURVE};
        final ArrayList<AlgorithmType> supportedAlgorithms = new ArrayList<>();
        for (AlgorithmType algorithmType : algorithmTypes) {
            if ((algorithmTypeValue & algorithmType.ordinal()) == algorithmType.ordinal()) {
                supportedAlgorithms.add(algorithmType);
                Log.v(TAG, "Supported output oob action type: " + getAlgorithmTypeDescription(algorithmType));
            }
        }
        return supportedAlgorithms;
    }

    /**
     * Returns the algorithm description
     *
     * @param type {@link AlgorithmType} type
     * @return Input OOB type descrption
     */
    public static String getAlgorithmTypeDescription(final AlgorithmType type) {
        switch (type) {
            case FIPS_P_256_ELLIPTIC_CURVE:
                return "FIPS P-256 Elliptic Curve";
            default:
                return "Unknown";
        }
    }
}
