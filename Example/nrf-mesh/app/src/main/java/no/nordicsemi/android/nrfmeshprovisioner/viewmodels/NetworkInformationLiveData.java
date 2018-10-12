package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import android.arch.lifecycle.LiveData;

/**
 * This class is used to observes the updates happening in the {@link NetworkInformation}
 */
public class NetworkInformationLiveData extends LiveData<NetworkInformation> {

    private NetworkInformation mNetworkInformation;

    private final NetworkInformation.NetworkInformationListener mListener = new NetworkInformation.NetworkInformationListener() {
        @Override
        public void onNetworkInformationUpdated(final NetworkInformation networkInformation) {
            mNetworkInformation = networkInformation;
            postValue(networkInformation);
        }
    };

    NetworkInformationLiveData (final NetworkInformation networkInformation){
        this.mNetworkInformation = networkInformation;
        postValue(networkInformation);
    }

    @Override
    protected void onActive() {
        mNetworkInformation.setNetworkInformationListener(mListener);
    }

    @Override
    protected void onInactive() {
        mNetworkInformation.removeNetworkInformationListener();
    }
}
