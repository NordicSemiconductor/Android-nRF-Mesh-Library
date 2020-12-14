package no.nordicsemi.android.nrfmesh.viewmodels;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;

/**
 * ViewModel for {@link AppKeysActivity}
 */
public class EditAppKeyViewModel extends KeysViewModel {

    private ApplicationKey appKey;
    final MutableLiveData<ApplicationKey> appKeyLiveData = new MutableLiveData<>();

    @Inject
    EditAppKeyViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
    }

    /**
     * Returns the app key live data
     */
    public LiveData<ApplicationKey> getAppKeyLiveData() {
        return appKeyLiveData;
    }

    /**
     * Selects the key based on the application key index.
     * @param index key index
     */
    public void selectAppKey(final int index) {
        appKey = getNetworkLiveData().getMeshNetwork().getAppKey(index);
        appKeyLiveData.setValue(appKey);
    }

    /**
     * Sets the application key
     *
     * @param key Key
     * @return true if successful or false otherwise
     */
    public boolean setKey(@NonNull final byte[] key) {
        appKey.setKey(key);
        return updateKey();
    }

    /**
     * Sets the name.
     *
     * @param name Application key name.
     */
    public boolean setName(@NonNull final String name) {
        appKey.setName(name);
        return updateKey();
    }

    /**
     * Sets the bound net key index.
     *
     * @return true if success or false otherwise.
     */
    public boolean setBoundNetKeyIndex(final int index) {
        appKey.setBoundNetKeyIndex(index);
        return updateKey();
    }

    private boolean updateKey() {
        if (getNetworkLiveData().getMeshNetwork().updateAppKey(appKey)) {
            appKeyLiveData.postValue(appKey);
            return true;
        }
        return false;
    }
}
