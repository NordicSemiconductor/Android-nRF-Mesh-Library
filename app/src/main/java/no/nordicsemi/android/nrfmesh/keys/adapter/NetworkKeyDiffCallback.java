package no.nordicsemi.android.nrfmesh.keys.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import no.nordicsemi.android.mesh.NetworkKey;

public class NetworkKeyDiffCallback extends DiffUtil.ItemCallback<NetworkKey> {

    @Override
    public boolean areItemsTheSame(@NonNull final NetworkKey oldItem, @NonNull final NetworkKey newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull final NetworkKey oldItem, @NonNull final NetworkKey newItem) {
        return oldItem.equals(newItem);
    }
}