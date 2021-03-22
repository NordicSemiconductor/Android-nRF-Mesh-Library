package no.nordicsemi.android.nrfmesh.viewmodels;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;

/**
 * ViewModel for {@link AppKeysActivity}
 */
@HiltViewModel
public class AddNetKeyViewModel extends BaseViewModel {

    private final NetworkKey networkKey;
    private final MutableLiveData<NetworkKey> networkKeyLiveData = new MutableLiveData<>();

    @Inject
    AddNetKeyViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
        networkKey = getNetworkLiveData().getMeshNetwork().createNetworkKey();
        networkKeyLiveData.setValue(networkKey);
    }

    /**
     * Returns the LiveData object containing the NetworkKey.
     */
    public MutableLiveData<NetworkKey> getNetworkKeyLiveData() {
        return networkKeyLiveData;
    }

    /**
     * Sets the Network Key.
     *
     * @param key Key
     */
    public void setKey(@NonNull final byte[] key) {
        networkKey.setKey(key);
        networkKeyLiveData.setValue(networkKey);
    }

    /**
     * Sets the name.
     *
     * @param name name.
     */
    public void setName(@NonNull final String name) {
        networkKey.setName(name);
        networkKeyLiveData.setValue(networkKey);
    }

    /**
     * Adds the network key
     */
    public boolean addNetKey() {
        return getNetworkLiveData().getMeshNetwork().addNetKey(networkKey);
    }
}
