package no.nordicsemi.android.nrfmesh.scenes.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class SceneItemDiffCallback extends DiffUtil.ItemCallback<SceneUiState> {

    @Override
    public boolean areItemsTheSame(@NonNull final SceneUiState oldItem, @NonNull final SceneUiState newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull final SceneUiState oldItem, @NonNull final SceneUiState newItem) {
        return oldItem.equals(newItem);
    }
}