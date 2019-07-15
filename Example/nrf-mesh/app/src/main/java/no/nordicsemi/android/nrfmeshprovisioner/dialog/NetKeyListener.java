package no.nordicsemi.android.nrfmeshprovisioner.dialog;

import androidx.annotation.NonNull;
import no.nordicsemi.android.meshprovisioner.NetworkKey;

public interface NetKeyListener {

    void onKeyUpdated(@NonNull final NetworkKey key);

    void onKeyNameUpdated(@NonNull final String nodeName);
}
