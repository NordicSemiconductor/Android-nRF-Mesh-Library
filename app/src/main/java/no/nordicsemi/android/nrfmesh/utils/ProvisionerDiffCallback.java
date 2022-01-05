package no.nordicsemi.android.nrfmesh.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import no.nordicsemi.android.mesh.Provisioner;

public class ProvisionerDiffCallback extends DiffUtil.ItemCallback<Provisioner> {

    @Override
    public boolean areItemsTheSame(@NonNull final Provisioner oldItem, @NonNull final Provisioner newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull final Provisioner oldItem, @NonNull final Provisioner newItem) {
        return oldItem.equals(newItem);
    }
}