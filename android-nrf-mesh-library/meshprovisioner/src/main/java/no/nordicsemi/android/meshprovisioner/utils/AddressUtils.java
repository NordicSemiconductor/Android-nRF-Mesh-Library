package no.nordicsemi.android.meshprovisioner.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AddressUtils {

    private static final String TAG = AddressUtils.class.getSimpleName();


    /**
     * Increments the unicast address by 1
     *
     * @param elementAddress
     * @return
     */
    public static byte[] incrementUnicastAddress(final byte[] elementAddress) {
        int address = getUnicastAddressInt(elementAddress);
        address++;
        return new byte[]{(byte) ((address >> 8) & 0xFF), (byte) (address & 0xFF)};
    }

    /**
     * Returns the unicast address as int
     *
     * @param unicastAddress unicast address
     * @return unicast address
     */
    public static int getUnicastAddressInt(final byte[] unicastAddress) {
        return ByteBuffer.wrap(unicastAddress).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    /**
     * Returns the unicast address as int
     *
     * @param unicastAddress unicast address
     * @return unicast address
     */
    public static byte[] getUnicastAddressBytes(final int unicastAddress) {
        return new byte[]{(byte) ((unicastAddress >> 8) & 0xFF), (byte) (unicastAddress & 0xFF)};
    }
}
