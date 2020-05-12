package no.nordicsemi.android.mesh.utils;

/**
 * Supported algorithm type
 */
@SuppressWarnings("unused")
public enum PublicKeyOOB {

    /**
     * Static OOB Type
     */
    PUBLIC_KEY_INFORMATION_AVAILABLE((byte) 0x01);

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
     * Returns the PublicKeyOOB
     *
     * @param rawPublicKeyType raw algorithm type received
     */
    public static PublicKeyOOB getPublicKeyOOBFromBitMask(final byte rawPublicKeyType) {
        final PublicKeyOOB[] publicKeyOOBS = {PUBLIC_KEY_INFORMATION_AVAILABLE};
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
     * @return Input OOB type description
     */
    public static String getPublicKeyInformationDescription(final PublicKeyOOB type) {
        switch (type) {
            case PUBLIC_KEY_INFORMATION_AVAILABLE:
                return "FIPS P-256 Elliptic Curve";
            default:
                return "Prohibited";
        }
    }
}
