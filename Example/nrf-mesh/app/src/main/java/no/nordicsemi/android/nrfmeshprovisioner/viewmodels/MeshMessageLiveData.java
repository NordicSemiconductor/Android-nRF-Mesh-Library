package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;

import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;

public class MeshMessageLiveData extends SingleLiveEvent<MeshMessage> {

    @Override
    public void postValue(final MeshMessage value) {
        super.postValue(value);
    }

    @Override
    public void observe(final LifecycleOwner owner, final Observer<MeshMessage> observer) {
        super.observe(owner, observer);
    }
}
