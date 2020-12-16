package no.nordicsemi.android.nrfmesh.viewmodels;

import androidx.annotation.NonNull;
import androidx.hilt.lifecycle.ViewModelInject;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;

/**
 * ViewModel for {@link AppKeysActivity}
 */
public class ScenesViewModel extends BaseViewModel {

    @ViewModelInject
    ScenesViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
    }
}
