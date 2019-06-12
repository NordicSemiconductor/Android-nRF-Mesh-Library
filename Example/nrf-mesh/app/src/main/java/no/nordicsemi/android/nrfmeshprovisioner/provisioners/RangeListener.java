package no.nordicsemi.android.nrfmeshprovisioner.provisioners;

import androidx.annotation.NonNull;
import no.nordicsemi.android.meshprovisioner.Range;

public interface RangeListener {

    void addRange(@NonNull final Range range);

}
