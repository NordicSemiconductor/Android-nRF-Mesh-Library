package no.nordicsemi.android.nrfmesh.node.adapter;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

public class NodeDiffCallback extends DiffUtil.ItemCallback<ProvisionedMeshNode> {

    @Override
    public boolean areItemsTheSame(@NonNull final ProvisionedMeshNode oldItem, @NonNull final ProvisionedMeshNode newItem) {
        return oldItem.equals(newItem);
    }

    @SuppressLint("DiffUtilEquals")
    @Override
    public boolean areContentsTheSame(@NonNull final ProvisionedMeshNode oldItem, @NonNull final ProvisionedMeshNode newItem) {
        return oldItem.equals(newItem);
    }
}