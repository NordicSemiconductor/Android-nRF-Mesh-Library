package no.nordicsemi.android.nrfmesh.viewmodels;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import dagger.hilt.android.lifecycle.HiltViewModel;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;

/**
 * ViewModel for {@link AppKeysActivity}
 */
@HiltViewModel
public class NetKeysViewModel extends BaseViewModel {

    @Inject
    NetKeysViewModel(@NonNull final NrfMeshRepository nrfMeshRepository) {
        super(nrfMeshRepository);
    }
}
