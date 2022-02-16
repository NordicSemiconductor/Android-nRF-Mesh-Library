package no.nordicsemi.android.nrfmesh.provisioners;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.Range;

public interface RangeListener {

    void addRange(@NonNull final Range range);

    void updateRange(@NonNull final Range range, final Range newRange);
}
