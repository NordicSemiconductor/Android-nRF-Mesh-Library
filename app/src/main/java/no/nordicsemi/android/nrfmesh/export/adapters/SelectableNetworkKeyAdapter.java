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

package no.nordicsemi.android.nrfmesh.export.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.elevation.ElevationOverlayProvider;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.CheckableRowItemBinding;
import no.nordicsemi.android.nrfmesh.viewmodels.MeshNetworkLiveData;

public class SelectableNetworkKeyAdapter extends RecyclerView.Adapter<SelectableNetworkKeyAdapter.ViewHolder> {

    private final List<NetworkKey> mNetworkKeys = new ArrayList<>();
    private OnItemCheckedChangedListener mOnItemClickListener;

    public SelectableNetworkKeyAdapter(@NonNull final LifecycleOwner owner, @NonNull final MeshNetworkLiveData meshNetworkLiveData) {
        meshNetworkLiveData.observe(owner, networkData -> {
            final MeshNetwork network = meshNetworkLiveData.getMeshNetwork();
            mNetworkKeys.clear();
            mNetworkKeys.addAll(network.getNetKeys());
            notifyDataSetChanged();
        });
    }

    public void setOnItemCheckedChangedListener(final OnItemCheckedChangedListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public SelectableNetworkKeyAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(CheckableRowItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final SelectableNetworkKeyAdapter.ViewHolder holder, final int position) {
        final NetworkKey key = mNetworkKeys.get(position);
        holder.networkKeyName.setText(key.getName());
        holder.networkKey.setText(MeshParserUtils.bytesToHex(key.getKey(), false).toUpperCase());
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mNetworkKeys.size();
    }

    @FunctionalInterface
    public interface OnItemCheckedChangedListener {
        void onNetworkKeyChecked(@NonNull final NetworkKey networkKey, final boolean isChecked);
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        View container;
        ImageView icon;
        TextView networkKeyName;
        TextView networkKey;
        MaterialCheckBox materialCheckBox;

        private ViewHolder(@NonNull final CheckableRowItemBinding binding) {
            super(binding.getRoot());
            container = binding.container;
            icon = binding.icon;
            networkKeyName = binding.title;
            networkKey = binding.subtitle;
            materialCheckBox = binding.check;
            final ElevationOverlayProvider provider = new ElevationOverlayProvider(itemView.getContext());
            final int color = provider.compositeOverlayIfNeeded(provider.getThemeSurfaceColor(), 3.5f);
            container.setBackgroundColor(color);
            icon.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_vpn_key_24dp));
            materialCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onNetworkKeyChecked(mNetworkKeys.get(getAdapterPosition()), isChecked);
            });
        }
    }
}
