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

package no.nordicsemi.android.nrfmesh.keys.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.elevation.ElevationOverlayProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.NodeKey;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.RemovableRowItemBinding;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.MeshNetworkLiveData;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

public class ManageNetKeyAdapter extends RecyclerView.Adapter<ManageNetKeyAdapter.ViewHolder> {

    private final List<NetworkKey> networkKeys = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public ManageNetKeyAdapter(@NonNull final LifecycleOwner owner, @NonNull final MeshNetworkLiveData meshNetworkLiveData) {
        meshNetworkLiveData.observe(owner, networkData -> {
            final List<NetworkKey> keys = networkData.getNetworkKeys();
            if (keys != null) {
                networkKeys.clear();
                networkKeys.addAll(keys);
                networkKeys.remove(0);
                Collections.sort(networkKeys, Utils.netKeyComparator);
            }
            notifyDataSetChanged();
        });
    }

    public ManageNetKeyAdapter(@NonNull final LifecycleOwner owner,
                               @NonNull final LiveData<ProvisionedMeshNode> meshNodeLiveData,
                               @NonNull final List<NetworkKey> netKeys) {
        meshNodeLiveData.observe(owner, node -> {
            networkKeys.clear();
            for (NodeKey key : node.getAddedNetKeys()) {
                for (NetworkKey networkKey : netKeys) {
                    if (networkKey.getKeyIndex() == key.getIndex()) {
                        networkKeys.add(networkKey);
                    }
                }
            }
            Collections.sort(networkKeys, Utils.netKeyComparator);
            notifyDataSetChanged();
        });
    }

    public void setOnItemClickListener(final ManageNetKeyAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ManageNetKeyAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ManageNetKeyAdapter.ViewHolder(RemovableRowItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ManageNetKeyAdapter.ViewHolder holder, final int position) {
        if (networkKeys.size() > 0) {
            final NetworkKey networkKey = networkKeys.get(position);
            holder.netKeyName.setText(networkKey.getName());
            final String key = MeshParserUtils.bytesToHex(networkKey.getKey(), false);
            holder.netKey.setText(key.toUpperCase());
            holder.getSwipeableView().setTag(networkKey);
        }
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return networkKeys.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(final int position, @NonNull final NetworkKey networkKey);
    }

    final class ViewHolder extends RemovableViewHolder {
        FrameLayout container;
        TextView netKeyName;
        TextView netKey;

        private ViewHolder(final @NonNull RemovableRowItemBinding binding) {
            super(binding.getRoot());
            binding.icon.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_vpn_key_24dp));
            container = binding.container;
            netKeyName = binding.title;
            netKey = binding.subtitle;
            final ElevationOverlayProvider provider = new ElevationOverlayProvider(itemView.getContext());
            final int color = provider.compositeOverlayIfNeeded(provider.getThemeSurfaceColor(), 3.5f);
            getSwipeableView().setBackgroundColor(color);
            container.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    final NetworkKey key = networkKeys.get(getAdapterPosition());
                    mOnItemClickListener.onItemClick(getAdapterPosition(), key);
                }
            });
        }
    }
}
