package no.nordicsemi.android.mesh.utils;


public abstract class PeriodLogState {

    public int periodLog;

    PeriodLogState(final int periodLog) {
        this.periodLog = periodLog;
    }

    /**
     * Returns the period description
     */
    public abstract String getPeriodDescription();
}