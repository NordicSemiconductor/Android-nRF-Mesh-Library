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

package no.nordicsemi.android.nrfmeshprovisioner.keys.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.NetworkKey;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.MeshNetworkLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class ManageNetKeyAdapter extends RecyclerView.Adapter<ManageNetKeyAdapter.ViewHolder> {

    private final List<NetworkKey> networkKeys = new ArrayList<>();
    private final Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public ManageNetKeyAdapter(@NonNull final Context context, @NonNull final MeshNetworkLiveData meshNetworkLiveData) {
        this.mContext = context;
        meshNetworkLiveData.observe((LifecycleOwner) context, networkData -> {
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

    public void setOnItemClickListener(final ManageNetKeyAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ManageNetKeyAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.removable_row_item, parent, false);
        return new ManageNetKeyAdapter.ViewHolder(layoutView);
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

        @BindView(R.id.title)
        TextView netKeyName;
        @BindView(R.id.subtitle)
        TextView netKey;

        private ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.findViewById(R.id.removable).setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    final NetworkKey key = networkKeys.get(getAdapterPosition());
                    mOnItemClickListener.onItemClick(getAdapterPosition(), key);
                }
            });
        }
    }
}
