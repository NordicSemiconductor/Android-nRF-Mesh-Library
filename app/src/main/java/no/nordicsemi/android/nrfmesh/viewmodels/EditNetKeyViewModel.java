package no.nordicsemi.android.nrfmesh.viewmodels;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;

/**
 * ViewModel for {@link AppKeysActivity}
 */
public class EditNetKeyViewModel extends KeysViewModel {

    private NetworkKey networkKey;
    private MutableLiveData<NetworkKey> networkKeyLiveData = new MutableLiveData<>();

    @Inject
    EditNetKeyViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
    }

    public LiveData<NetworkKey> getNetworkKeyLiveData() {
        return networkKeyLiveData;
    }

    /**
     * Selects the Network Key based on the given key index
     *
     * @param index Key index
     */
    public void selectNetKey(final int index) {
        networkKey = getNetworkLiveData().getMeshNetwork().getNetKey(index);
        networkKeyLiveData.setValue(networkKey);
    }

    /**
     * Sets the application key
     *
     * @param key Key
     * @return true if successful or false otherwise
     */
    public boolean setKey(@NonNull final byte[] key) {
        if (key.length != 16)
            throw new IllegalArgumentException("Key must be of length 16!");
        networkKey.setKey(key);
        return updateKey();
    }

    /**
     * Sets the name.
     *
     * @param name Application key name.
     */
    public boolean setName(@NonNull final String name) {
        networkKey.setName(name);
        return updateKey();
    }

    private boolean updateKey() {
        if (getNetworkLiveData().getMeshNetwork().updateNetKey(networkKey)) {
            networkKeyLiveData.postValue(networkKey);
            return true;
        }
        return false;
    }

}
