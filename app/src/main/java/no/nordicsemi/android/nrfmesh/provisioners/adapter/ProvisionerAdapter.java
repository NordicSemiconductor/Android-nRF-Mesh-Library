/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrfmesh.provisioners.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.elevation.ElevationOverlayProvider;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.Provisioner;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.RemovableRowItemProvisionerBinding;
import no.nordicsemi.android.nrfmesh.viewmodels.MeshNetworkLiveData;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

public class ProvisionerAdapter extends RecyclerView.Adapter<ProvisionerAdapter.ViewHolder> {

    private final List<Provisioner> mProvisioners = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public ProvisionerAdapter(@NonNull final LifecycleOwner owner, @NonNull final MeshNetworkLiveData meshNetworkLiveData) {
        meshNetworkLiveData.observe(owner, networkData -> {
            final MeshNetwork network = meshNetworkLiveData.getMeshNetwork();
            final List<Provisioner> provisioners = network.getProvisioners();
            mProvisioners.clear();
            mProvisioners.addAll(provisioners);
            final Provisioner provisioner = network.getSelectedProvisioner();
            mProvisioners.remove(provisioner);
            notifyDataSetChanged();
        });
    }

    public void setOnItemClickListener(final ProvisionerAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ProvisionerAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ProvisionerAdapter.ViewHolder(RemovableRowItemProvisionerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ProvisionerAdapter.ViewHolder holder, final int position) {
        final Provisioner provisioner = mProvisioners.get(position);
        holder.provisionerName.setText(provisioner.getProvisionerName());
        final Context context = holder.provisionerName.getContext();
        if (provisioner.getProvisionerAddress() == null) {
            holder.provisionerSummary.setText(context.getString(R.string.unicast_address,
                    holder.provisionerName.getContext().getString(R.string.address_unassigned)));
        } else {
            holder.provisionerSummary.setText(context.getString(R.string.unicast_address,
                    MeshAddress.formatAddress(provisioner.getProvisionerAddress(), true)));
        }
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mProvisioners.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public Provisioner getItem(final int position) {
        return mProvisioners.get(position);
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(final int position, @NonNull final Provisioner provisioner);
    }

    final class ViewHolder extends RemovableViewHolder {
        TextView provisionerName;
        TextView provisionerSummary;

        private ViewHolder(@NonNull final RemovableRowItemProvisionerBinding binding) {
            super(binding.getRoot());
            provisionerName = binding.title;
            provisionerSummary = binding.subtitle;
            final ElevationOverlayProvider provider = new ElevationOverlayProvider(itemView.getContext());
            final int color = provider.compositeOverlayIfNeeded(provider.getThemeSurfaceColor(), 3.5f);
            getSwipeableView().setBackgroundColor(color);
            binding.icon.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_account_key));
            binding.container.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    final Provisioner provisioner = mProvisioners.get(getAdapterPosition());
                    mOnItemClickListener.onItemClick(getAdapterPosition(), provisioner);
                }
            });
        }
    }
}
