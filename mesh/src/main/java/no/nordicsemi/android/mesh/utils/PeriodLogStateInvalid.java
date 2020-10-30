package no.nordicsemi.android.mesh.utils;

public final class PeriodLogStateInvalid extends PeriodLogState {

    public PeriodLogStateInvalid(final int periodLog) {
        super(periodLog);
    }

    /**
     * Returns the period value.
     */
    public int getValue() {
        return periodLog;
    }

    @Override
    public String getPeriodDescription() {
        return "Invalid";
    }
}
