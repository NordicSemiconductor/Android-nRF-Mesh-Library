package no.nordicsemi.android.nrfmesh.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import no.nordicsemi.android.mesh.utils.AddressArray;

public class AddressArrayDiffCallback extends DiffUtil.ItemCallback<AddressArray> {

    @Override
    public boolean areItemsTheSame(@NonNull final AddressArray oldItem, @NonNull final AddressArray newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull final AddressArray oldItem, @NonNull final AddressArray newItem) {
        return oldItem.equals(newItem);
    }
}