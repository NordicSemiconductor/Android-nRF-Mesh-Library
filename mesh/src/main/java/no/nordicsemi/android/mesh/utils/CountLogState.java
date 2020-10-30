package no.nordicsemi.android.mesh.utils;


public abstract class CountLogState {

    public int countLog;

    CountLogState(final int countLog) {
        this.countLog = countLog;
    }

    /**
     * Returns the description
     */
    public abstract String getDescription();
}