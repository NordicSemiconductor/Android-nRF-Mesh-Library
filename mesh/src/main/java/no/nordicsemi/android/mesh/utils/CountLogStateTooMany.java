package no.nordicsemi.android.mesh.utils;

public final class CountLogStateTooMany extends CountLogState {

    public CountLogStateTooMany(final int countLog) {
        super(countLog);
    }

    /**
     * Returns the period value.
     */
    public short getValue() {
        return (short) countLog;
    }


    @Override
    public String getDescription() {
        return "More than 0xFFFE";
    }
}
