package no.nordicsemi.android.nrfmesh.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class GroupItemUiStateDiffCallback extends DiffUtil.ItemCallback<GroupItemUIState> {

    @Override
    public boolean areItemsTheSame(@NonNull final GroupItemUIState oldItem, @NonNull final GroupItemUIState newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull final GroupItemUIState oldItem, @NonNull final GroupItemUIState newItem) {
        return oldItem.equals(newItem);
    }
}