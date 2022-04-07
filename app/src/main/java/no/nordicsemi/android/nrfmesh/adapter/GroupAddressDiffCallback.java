package no.nordicsemi.android.nrfmesh.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class GroupAddressDiffCallback extends DiffUtil.ItemCallback<Integer> {

    @Override
    public boolean areItemsTheSame(@NonNull final Integer oldItem, @NonNull final Integer newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull final Integer oldItem, @NonNull final Integer newItem) {
        return oldItem.equals(newItem);
    }
}