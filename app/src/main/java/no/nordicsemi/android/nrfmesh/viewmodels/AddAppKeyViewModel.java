package no.nordicsemi.android.nrfmesh.viewmodels;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;

/**
 * ViewModel for {@link AppKeysActivity}
 */
@HiltViewModel
public class AddAppKeyViewModel extends BaseViewModel {

    private final ApplicationKey appKey;
    final MutableLiveData<ApplicationKey> appKeyLiveData = new MutableLiveData<>();

    @Inject
    AddAppKeyViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
        appKey = getNetworkLiveData().getMeshNetwork().createAppKey();
        appKeyLiveData.setValue(appKey);
    }

    public LiveData<ApplicationKey> getAppKeyLiveData() {
        return appKeyLiveData;
    }

    /**
     * Sets the application key
     *
     * @param key Key
     */
    public void setKey(@NonNull final byte[] key) {
        appKey.setKey(key);
        appKeyLiveData.setValue(appKey);
    }

    /**
     * Sets the name.
     *
     * @param name Application key name.
     */
    public void setName(@NonNull final String name) {
        appKey.setName(name);
        appKeyLiveData.setValue(appKey);
    }

    /**
     * Sets the name.
     */
    public void setBoundNetKeyIndex(final int index) {
        appKey.setBoundNetKeyIndex(index);
        appKeyLiveData.setValue(appKey);
    }

    public boolean addAppKey() {
        return getNetworkLiveData().getMeshNetwork().addAppKey(appKey);
    }
}
