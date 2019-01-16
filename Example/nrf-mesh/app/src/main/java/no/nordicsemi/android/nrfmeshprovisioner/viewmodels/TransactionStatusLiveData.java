package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

@SuppressWarnings("unchecked")
public class TransactionStatusLiveData extends SingleLiveEvent<TransactionStatusLiveData> {

    private byte[] mElementAddress;
    private boolean incompleteTimerExpired;

    TransactionStatusLiveData() {
    }

    void onTransactionFailed(final byte[] elementAddress, final boolean hasIncompleteTimerExpired) {
        this.mElementAddress = elementAddress;
        this.incompleteTimerExpired = hasIncompleteTimerExpired;
        postValue(this);
    }

    public byte[] getElementAddress() {
        return mElementAddress;
    }

    public boolean isIncompleteTimerExpired() {
        return incompleteTimerExpired;
    }
}
