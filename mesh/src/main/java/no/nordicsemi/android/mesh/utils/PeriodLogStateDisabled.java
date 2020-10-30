package no.nordicsemi.android.mesh.utils;

public final class PeriodLogStateDisabled extends PeriodLogState {

    public PeriodLogStateDisabled(final int periodLog) {
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
        return "Disabled";
    }
}
