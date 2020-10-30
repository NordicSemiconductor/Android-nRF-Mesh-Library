package no.nordicsemi.android.mesh.utils;

public final class CountLogStateInvalid extends CountLogState {

    public CountLogStateInvalid(final int periodLog) {
        super(periodLog);
    }

    /**
     * Returns the period value.
     */
    public int getValue() {
        return countLog;
    }

    @Override
    public String getDescription() {
        return "Invalid";
    }
}
