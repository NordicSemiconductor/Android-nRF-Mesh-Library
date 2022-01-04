package no.nordicsemi.android.nrfmesh.keys.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import no.nordicsemi.android.mesh.ApplicationKey;

public class ApplicationKeyDiffCallback extends DiffUtil.ItemCallback<ApplicationKey> {

    @Override
    public boolean areItemsTheSame(@NonNull final ApplicationKey oldItem, @NonNull final ApplicationKey newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull final ApplicationKey oldItem, @NonNull final ApplicationKey newItem) {
        return oldItem.equals(newItem);
    }
}
