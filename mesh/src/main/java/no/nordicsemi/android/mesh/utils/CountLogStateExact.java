package no.nordicsemi.android.mesh.utils;

public final class CountLogStateExact extends CountLogState {

    public CountLogStateExact(final int countLog) {
        super(countLog);
    }

    /**
     * Returns the period value.
     */
    public short getValue() {
        if (countLog == 0x00 || countLog == 0x01)
            return (short) countLog;
        else if (countLog == 0xFF || countLog == 0x11)
            return (short) 0xFFFF;
        else throw new IllegalArgumentException("Invalid value!");
    }

    @Override
    public String getDescription() {
        return String.valueOf(getValue());
    }
}
