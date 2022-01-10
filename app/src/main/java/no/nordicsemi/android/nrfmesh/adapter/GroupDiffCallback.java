package no.nordicsemi.android.nrfmesh.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import no.nordicsemi.android.mesh.Group;

public class GroupDiffCallback extends DiffUtil.ItemCallback<Group> {

    @Override
    public boolean areItemsTheSame(@NonNull final Group oldItem, @NonNull final Group newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull final Group oldItem, @NonNull final Group newItem) {
        return oldItem.equals(newItem);
    }
}