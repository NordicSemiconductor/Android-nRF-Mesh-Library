package no.nordicsemi.android.nrfmesh.viewmodels;

import android.text.TextUtils;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;

/**
 * ViewModel for {@link AppKeysActivity}
 */
public class AddNetKeyViewModel extends KeysViewModel {

    private final NetworkKey networkKey;
    private MutableLiveData<NetworkKey> networkKeyLiveData = new MutableLiveData<>();

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
        if (key.length != 16)
            throw new IllegalArgumentException("Key must be of length 16!");
        networkKey.setKey(key);
        networkKeyLiveData.postValue(networkKey);
    }

    /**
     * Sets the name.
     *
     * @param name name.
     */
    public void setName(@NonNull final String name) {
        if (TextUtils.isEmpty(name))
            throw new IllegalArgumentException("Name cannot be empty!");
        networkKey.setName(name);
        networkKeyLiveData.postValue(networkKey);
    }

    /**
     * Adds the network key
     */
    public boolean addNetKey() {
        return getNetworkLiveData().getMeshNetwork().addNetKey(networkKey);
    }
}
