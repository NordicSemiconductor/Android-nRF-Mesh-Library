package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

@SuppressWarnings("unchecked")
public class TransactionStatusLiveData extends SingleLiveEvent<TransactionStatusLiveData> {

    private int mElementAddress;
    private boolean incompleteTimerExpired;

    TransactionStatusLiveData() {
    }

    void onTransactionFailed(final byte[] elementAddress, final boolean hasIncompleteTimerExpired) {
        this.mElementAddress = MeshParserUtils.bytesToInt(elementAddress);
        incompleteTimerExpired = hasIncompleteTimerExpired;
    }

    void onTransactionFailed(final int elementAddress, final boolean hasIncompleteTimerExpired) {
        this.mElementAddress = elementAddress;
        incompleteTimerExpired = hasIncompleteTimerExpired;
    }

    public int getElementAddress() {
        return mElementAddress;
    }

    public boolean isIncompleteTimerExpired() {
        return incompleteTimerExpired;
    }
}
