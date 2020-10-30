package no.nordicsemi.android.mesh.utils;

public final class PeriodLogStateExact extends PeriodLogState {

    public PeriodLogStateExact(final int periodLog) {
        super(periodLog);
    }

    /**
     * Returns the period value.
     */
    public int getValue() {
        if (periodLog == 0x01)
            return 1;
        else if (periodLog == 0x11)
            return 0xFFFF;
        else throw new IllegalArgumentException("Invalid value!");
    }

    @Override
    public String getPeriodDescription() {
        return "Exact";
    }
}
