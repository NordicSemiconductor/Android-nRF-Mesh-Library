package no.nordicsemi.android.mesh.utils;

public abstract class Heartbeat {
    public static final int DO_NOT_SEND_PERIODICALLY = 0x00;
    public static final int PERIOD_MIN = 0x01;
    public static final int PERIOD_MAX = 0x11;
    public static final int COUNT_MIN = 0x01;
    public static final int COUNT_MAX = 0x11;
    public static final int SEND_INDEFINITELY = 0xFF;
    public static final int DEFAULT_PUBLICATION_TTL = 0x05;

    Heartbeat() {

    }
}
