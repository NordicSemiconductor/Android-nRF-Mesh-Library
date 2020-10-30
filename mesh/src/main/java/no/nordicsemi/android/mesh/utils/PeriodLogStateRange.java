package no.nordicsemi.android.mesh.utils;

public final class PeriodLogStateRange extends PeriodLogState {

    public PeriodLogStateRange(final int periodLog) {
        super(periodLog);
    }

    /**
     * Returns the lower bound of the period.
     */
    public int getLowerBound() {
        return (int) (Math.pow(2, periodLog - 1));
    }

    /**
     * Returns the upper bound of the period.
     */
    public int getUpperBound() {
        return (int) Math.pow(2, periodLog) - 1;
    }

    /**
     * Returns the period description as a Range.
     */
    @Override
    public String getPeriodDescription() {
        final int lowerBound = getLowerBound();
        final int upperBound = getUpperBound();
        return periodToTime(lowerBound) + " ... " + periodToTime(upperBound);
    }

    /**
     * Converts the perio to time
     * @param seconds PeriodLog
     */
    public static String periodToTime(final int seconds) {
        if(seconds == 1)
            return seconds + " second";
        else if (seconds > 1 && seconds < 60) {
            return seconds + " seconds";
        } else if (seconds >= 60 && seconds < 3600) {
            return seconds / 60 + " min " + (seconds % 60) + " sec";
        } else if (seconds >= 3600 && seconds <= 65535) {
            return seconds / 3600 + " h " + ((seconds % 3600) / 60) + " min " + (seconds % 3600 % 60) + " sec";
        } else
            return seconds / 3600 + " h " + ((seconds % 3600) / 60) + " min " + ((seconds % 3600 % 60) - 1) + " sec";
    }
}
