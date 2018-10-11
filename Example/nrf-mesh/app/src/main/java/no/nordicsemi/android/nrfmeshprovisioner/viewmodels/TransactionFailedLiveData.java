package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

public class TransactionFailedLiveData extends SingleLiveEvent<TransactionFailedLiveData> {

    private int mElementAddress;
    private boolean incompleteTimerExpired;

    TransactionFailedLiveData() {
    }

    public void onTransactionFailed(final int elementAddress, final boolean hasIncompleteTimerExpired) {
        this.mElementAddress = elementAddress;
        this.incompleteTimerExpired = hasIncompleteTimerExpired;
        postValue(this);
    }

    public int getElementAddress() {
        return mElementAddress;
    }

    public boolean isIncompleteTimerExpired() {
        return incompleteTimerExpired;
    }
}
