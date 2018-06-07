package no.nordicsemi.android.meshprovisioner.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ParseKeys {


    /**
     * Parses the mBoundAppKeyIndexes
     *
     * @return appkeyindex int
     */
    public static int getAppKeyIndexInt(final byte[] appKeyIndex) {
        return ByteBuffer.wrap(appKeyIndex).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    /**
     * Parses the netKeyIndex
     *
     * @return netKeyIndex int
     */
    public static int getNetkeyIndexInt(final byte[] netKeyIndex) {
        return ByteBuffer.wrap(netKeyIndex).order(ByteOrder.BIG_ENDIAN).getShort();
    }
}
