package no.nordicsemi.android.nrfmesh.dialog;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.NetworkKey;

public interface NetKeyListener {

    void onKeyUpdated(@NonNull final NetworkKey key);

    void onKeyNameUpdated(@NonNull final String nodeName);
}
