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
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.RemovableRowItemBinding;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

public class BoundAppKeysAdapter extends RecyclerView.Adapter<BoundAppKeysAdapter.ViewHolder> {

    private final ArrayList<ApplicationKey> appKeys = new ArrayList<>();

    public BoundAppKeysAdapter(@NonNull final LifecycleOwner owner,
                               @NonNull final List<ApplicationKey> appKeys,
                               @NonNull final LiveData<MeshModel> meshModelLiveData) {
        meshModelLiveData.observe(owner, meshModel -> {
            if (meshModel != null) {
                this.appKeys.clear();
                for (Integer index : meshModel.getBoundAppKeyIndexes()) {
                    for (ApplicationKey applicationKey : appKeys) {
                        if (index == applicationKey.getKeyIndex()) {
                            this.appKeys.add(applicationKey);
                        }
                    }
                }
                Collections.sort(this.appKeys, Utils.appKeyComparator);
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public BoundAppKeysAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(RemovableRowItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final BoundAppKeysAdapter.ViewHolder holder, final int position) {
        if (appKeys.size() > 0) {
            final ApplicationKey applicationKey = appKeys.get(position);
            final String appKey = MeshParserUtils.bytesToHex(applicationKey.getKey(), false);
            holder.appKeyName.setText(applicationKey.getName());
            holder.appKey.setText(appKey.toUpperCase());
        }
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return appKeys.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public ApplicationKey getAppKey(final int position) {
        if (!appKeys.isEmpty()) {
            return appKeys.get(position);
        }
        return null;
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(final int position, final ApplicationKey appKey);
    }

    public static final class ViewHolder extends RemovableViewHolder {

        TextView appKeyName;
        TextView appKey;

        private ViewHolder(@NonNull final RemovableRowItemBinding binding) {
            super(binding.getRoot());
            binding.icon.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_vpn_key_24dp));
            appKeyName = binding.title;
            appKey = binding.subtitle;
            final ElevationOverlayProvider provider = new ElevationOverlayProvider(itemView.getContext());
            final int color = provider.compositeOverlayIfNeeded(provider.getThemeSurfaceColor(), 3.5f);
            getSwipeableView().setBackgroundColor(color);
        }
    }
}
