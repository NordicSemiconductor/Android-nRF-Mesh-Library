package no.nordicsemi.android.meshprovisioner.utils;

import android.util.Log;

import java.util.ArrayList;

/**
 * Supported algorithm type
 */
@SuppressWarnings("unused")
public enum PublicKeyOOB {

    /**
     * Static OOB Type
     */
    PUBLIC_KEY_INFORMATION_AVAILABLE((byte)0x01);

    private static final String TAG = PublicKeyOOB.class.getSimpleName();
    private short algorithmType;

    PublicKeyOOB(final short algorithmType) {
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
     * @param rawPublicKeyType algorithm type
     * @return selected output action type
     */
    public static PublicKeyOOB getPublicKeyOOBFromBitMask(final byte rawPublicKeyType) {
        final PublicKeyOOB[] publicKeyOOBS = {PUBLIC_KEY_INFORMATION_AVAILABLE};
        final ArrayList<PublicKeyOOB> supportedAlgorithms = new ArrayList<>();
        for (PublicKeyOOB publicKeyOOB : publicKeyOOBS) {
            if ((rawPublicKeyType & publicKeyOOB.ordinal()) == publicKeyOOB.ordinal()) {
                return publicKeyOOB;
            }
        }
        return null;
    }

    /**
     * Returns the algorithm description
     *
     * @param type {@link AlgorithmType} type
     * @return Input OOB type descrption
     */
    public static String getPublicKeyInforationDescription(final PublicKeyOOB type) {
        switch (type) {
            case PUBLIC_KEY_INFORMATION_AVAILABLE:
                return "FIPS P-256 Elliptic Curve";
            default:
                return "Prohibited";
        }
    }
}
