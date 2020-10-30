package no.nordicsemi.android.mesh.utils;

public final class CountLogStateRange extends CountLogState {

    public CountLogStateRange(final int periodLog) {
        super(periodLog);
    }

    /**
     * Returns the lower bound of the count log.
     */
    public int getLowerBound() {
        return (int) (Math.pow(2, countLog - 1));
    }

    /**
     * Returns the upper bound of the count log.
     */
    public int getUpperBound() {
        return Math.min(0xFFFE, (int) ((Math.pow(2, countLog)) - 1));
    }

    /**
     * Returns the period description as a Range.
     */
    @Override
    public String getDescription() {
        final int lowerBound = getLowerBound();
        final int upperBound = getUpperBound();
        return lowerBound + " ... " + upperBound;
    }
}
