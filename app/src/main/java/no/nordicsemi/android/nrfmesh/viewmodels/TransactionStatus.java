package no.nordicsemi.android.nrfmesh.viewmodels;

public class TransactionStatus {

    private int mElementAddress;
    private boolean incompleteTimerExpired;

    TransactionStatus(final int elementAddress, final boolean hasIncompleteTimerExpired) {
        this.mElementAddress = elementAddress;
        incompleteTimerExpired = hasIncompleteTimerExpired;
    }

    /**
     * Returns the element address of the failed transaction
     */
    @SuppressWarnings("unused")
    public int getElementAddress() {
        return mElementAddress;
    }

    /**
     * Returns if incomplete timer expired of the failed transaction
     */
    public boolean isIncompleteTimerExpired() {
        return incompleteTimerExpired;
    }
}
