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
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.material.elevation.ElevationOverlayProvider;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.databinding.RemovableRowItem2Binding;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

public class ManageBoundNetKeyAdapter extends RecyclerView.Adapter<ManageBoundNetKeyAdapter.ViewHolder> {

    private final List<NetworkKey> mNetworkKeys;
    private ApplicationKey mAppKey;
    private OnItemClickListener mOnItemClickListener;

    public ManageBoundNetKeyAdapter(@NonNull final LifecycleOwner owner, @NonNull final LiveData<ApplicationKey> appKey, @NonNull final List<NetworkKey> networkKeys) {
        appKey.observe(owner, applicationKey -> {
            mAppKey = applicationKey;
            notifyDataSetChanged();
        });
        mNetworkKeys = networkKeys;

    }

    public void setOnItemClickListener(final ManageBoundNetKeyAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ManageBoundNetKeyAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ManageBoundNetKeyAdapter.ViewHolder(RemovableRowItem2Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ManageBoundNetKeyAdapter.ViewHolder holder, final int position) {
        if (mNetworkKeys.size() > 0) {
            final NetworkKey networkKey = mNetworkKeys.get(position);
            holder.netKeyName.setText(networkKey.getName());
            final String key = MeshParserUtils.bytesToHex(networkKey.getKey(), false);
            holder.netKey.setText(key.toUpperCase());
            holder.getSwipeableView().setTag(networkKey);
            holder.bound.setChecked(checkRadio(networkKey));
        }
    }

    private boolean checkRadio(@NonNull final NetworkKey key) {
        return key.getKeyIndex() == mAppKey.getBoundNetKeyIndex();
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mNetworkKeys.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void updateBoundNetKeyIndex(final int position, @NonNull final NetworkKey networkKey);
    }

    final class ViewHolder extends RemovableViewHolder {

        TextView netKeyName;
        TextView netKey;
        RadioButton bound;

        private ViewHolder(final @NonNull RemovableRowItem2Binding binding) {
            super(binding.getRoot());
            netKeyName = binding.title;
            netKey = binding.subtitle;
            bound = binding.radio;
            final ElevationOverlayProvider provider = new ElevationOverlayProvider(itemView.getContext());
            final int color = provider.compositeOverlayIfNeeded(provider.getThemeSurfaceColor(), 3.5f);
            getSwipeableView().setBackgroundColor(color);
            binding.container.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    final NetworkKey netKey = mNetworkKeys.get(getAdapterPosition());
                    mOnItemClickListener.updateBoundNetKeyIndex(getAdapterPosition(), netKey);
                }
            });
        }
    }
}
