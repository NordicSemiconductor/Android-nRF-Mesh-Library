package no.nordicsemi.android.meshprovisioner.utils;

public class ParseProvisioningAlgorithm {

    private static final int FIPS_P_256_ELLIPTIC_CURVE = 1;

    /**
     * Returns the algorithm type used for provisioning
     *
     * @param type
     * @return
     */

    public static String getAlgorithmType(final int type) {
        switch (type) {
            case 0x0001:
                return "FIPS P-256 ELLIPTIC CURVE";
            case 0x0000:
            default:
                return "NONE";
        }
    }

    public static byte getAlgorithmValue(final int type) {
        switch (type) {
            case FIPS_P_256_ELLIPTIC_CURVE:
                return 0x00;
            default:
                return 0x01;
        }
    }
}
