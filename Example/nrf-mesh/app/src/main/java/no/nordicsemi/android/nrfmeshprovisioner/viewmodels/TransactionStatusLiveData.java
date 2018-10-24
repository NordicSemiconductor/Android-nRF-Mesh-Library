package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

@SuppressWarnings("unchecked")
public class TransactionStatusLiveData extends SingleLiveEvent<TransactionStatusLiveData> {

    private int mElementAddress;
    private boolean incompleteTimerExpired;

    TransactionStatusLiveData() {
    }

    void onTransactionFailed(final int elementAddress, final boolean hasIncompleteTimerExpired) {
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
