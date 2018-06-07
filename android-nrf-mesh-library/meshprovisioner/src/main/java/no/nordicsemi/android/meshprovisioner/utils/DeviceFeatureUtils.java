package no.nordicsemi.android.meshprovisioner.utils;

public class DeviceFeatureUtils {

    /**
     * Checks if relay feature is supported by node;
     *
     * @param feature 16-bit feature value
     * @return true if relay bit = 1 and false if relay bit = 0
     */
    public static final boolean supportsRelayFeature(final int feature) {
        return ((feature & (1 << 0)) > 0);
    }

    /**
     * Checks if proxy feature is supported by node;
     *
     * @param feature 16-bit feature value
     * @return true if proxy bit = 1 and false if proxy bit = 0
     */
    public static final boolean supportsProxyFeature(final int feature) {
        return ((feature & (1 << 1)) > 0);
    }

    /**
     * Checks if friend feature is supported by node;
     *
     * @param feature 16-bit feature value
     * @return true if friend bit = 1 and false if friend bit = 0
     */
    public static final boolean supportsFriendFeature(final int feature) {
        return ((feature & (1 << 2)) > 0);
    }

    /**
     * Checks if low power feature is supported by node;
     *
     * @param feature 16-bit feature value
     * @return true if low power bit = 1 and false if low power bit = 0
     */
    public static final boolean supportsLowPowerFeature(final int feature) {
        return ((feature & (1 << 3)) > 0);
    }
}
