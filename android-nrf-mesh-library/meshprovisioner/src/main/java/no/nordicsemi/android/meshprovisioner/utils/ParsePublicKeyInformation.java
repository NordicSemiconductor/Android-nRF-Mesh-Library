package no.nordicsemi.android.meshprovisioner.utils;

public class ParsePublicKeyInformation {

    private static final int PULIC_KEY_INFORMATION_UNAVAILABLE = 0x0000;
    private static final int PULIC_KEY_INFORMATION_AVAILABLE = 0x0001;

    public static String getPublicKeyInformation(final int type) {
        switch (type) {
            case PULIC_KEY_INFORMATION_UNAVAILABLE:
                return "Public key information unavailable";
            case PULIC_KEY_INFORMATION_AVAILABLE:
                return "Public key information available";
            default:
                return "Unknown";
        }
    }
}
