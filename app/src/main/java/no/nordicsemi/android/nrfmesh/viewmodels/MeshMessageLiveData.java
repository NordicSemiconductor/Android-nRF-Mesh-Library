package no.nordicsemi.android.nrfmesh.viewmodels;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import no.nordicsemi.android.mesh.transport.MeshMessage;

public class MeshMessageLiveData extends SingleLiveEvent<MeshMessage> {

    @Override
    public void postValue(final MeshMessage value) {
        super.postValue(value);
    }

    @Override
    @MainThread
    public void observe(@NonNull final LifecycleOwner owner, @NonNull final Observer<? super MeshMessage> observer) {
        super.observe(owner, observer);
    }
}
