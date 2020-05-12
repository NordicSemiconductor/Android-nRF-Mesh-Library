package no.nordicsemi.android.nrfmesh.provisioners;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.Range;

public interface RangeListener {

    void addRange(@NonNull final Range range);

}
