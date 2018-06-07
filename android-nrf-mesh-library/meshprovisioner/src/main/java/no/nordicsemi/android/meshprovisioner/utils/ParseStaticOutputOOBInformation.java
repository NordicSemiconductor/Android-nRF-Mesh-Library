package no.nordicsemi.android.meshprovisioner.utils;

public class ParseStaticOutputOOBInformation {

    private static final int STATIC_OOB_INFO_UNAVAILABLE = 0x0000;
    private static final int STATIC_OOB_INFO_AVAILABLE = 0x0001;

    public static String getStaticOOBActionInformationAvailability(final int type) {
        switch (type) {
            case STATIC_OOB_INFO_UNAVAILABLE:
                return "Static OOB Actions unavailable";
            case STATIC_OOB_INFO_AVAILABLE:
                return "Static OOB Actions available";
            default:
                return "Unknown";
        }
    }
}
