package no.nordicsemi.android.nrfmesh.viewmodels;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;

/**
 * ViewModel for {@link AppKeysActivity}
 */
@HiltViewModel
public class EditNetKeyViewModel extends BaseViewModel {

    private NetworkKey networkKey;
    private final MutableLiveData<NetworkKey> networkKeyLiveData = new MutableLiveData<>();

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
    public boolean setKey(@NonNull final String key) {
        if (getNetworkLiveData().getMeshNetwork().updateNetKey(networkKey, key)) {
            selectNetKey(networkKey.getKeyIndex());
            return true;
        }
        return false;
    }

    /**
     * Sets the name.
     *
     * @param name Application key name.
     */
    public boolean setName(@NonNull final String name) {
        networkKey.setName(name);
        if (getNetworkLiveData().getMeshNetwork().updateNetKey(networkKey)) {
            selectNetKey(networkKey.getKeyIndex());
            return true;
        }
        return false;
    }
}
