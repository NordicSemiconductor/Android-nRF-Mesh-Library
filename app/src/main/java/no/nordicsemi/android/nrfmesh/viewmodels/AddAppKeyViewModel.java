package no.nordicsemi.android.nrfmesh.viewmodels;

import android.text.TextUtils;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;

/**
 * ViewModel for {@link AppKeysActivity}
 */
public class AddAppKeyViewModel extends KeysViewModel {

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
        if (key.length != 16)
            throw new IllegalArgumentException("Key must be of length 16!");
        appKey.setKey(key);
        appKeyLiveData.postValue(appKey);
    }

    /**
     * Sets the name.
     *
     * @param name Application key name.
     */
    public void setName(@NonNull final String name) {
        if (TextUtils.isEmpty(name))
            throw new IllegalArgumentException("Name cannot be empty!");
        appKey.setName(name);
        appKeyLiveData.postValue(appKey);
    }

    /**
     * Sets the name.
     */
    public void setBoundNetKeyIndex(final int index) {
        appKey.setBoundNetKeyIndex(index);
        appKeyLiveData.postValue(appKey);
    }

    public boolean save() {
        return getNetworkLiveData().getMeshNetwork().addAppKey(appKey);
    }
}
