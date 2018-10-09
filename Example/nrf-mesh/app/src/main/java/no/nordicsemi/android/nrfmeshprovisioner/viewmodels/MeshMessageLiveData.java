package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;


import no.nordicsemi.android.meshprovisioner.messages.MeshMessage;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.SingleLiveEvent;

public class MeshMessageLiveData extends SingleLiveEvent<MeshMessage> {

    @Override
    public void postValue(final MeshMessage value) {
        super.postValue(value);
    }
}
