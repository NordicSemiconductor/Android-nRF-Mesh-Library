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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.NodeKey;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.CheckableRowItemBinding;
import no.nordicsemi.android.nrfmesh.utils.Utils;

public class AddedAppKeyAdapter extends RecyclerView.Adapter<AddedAppKeyAdapter.ViewHolder> {

    private final List<ApplicationKey> appKeys = new ArrayList<>();
    private final List<ApplicationKey> addedAppKeys = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    private boolean enableSelection = true;

    public AddedAppKeyAdapter(@NonNull final LifecycleOwner owner,
                              @NonNull final List<ApplicationKey> appKeys,
                              @NonNull final LiveData<ProvisionedMeshNode> meshNodeLiveData) {
        this.appKeys.clear();
        this.appKeys.addAll(appKeys);
        Collections.sort(this.appKeys, Utils.appKeyComparator);
        meshNodeLiveData.observe((LifecycleOwner) owner, meshNode -> {
            addedAppKeys.clear();
            for (NodeKey nodeKey : meshNode.getAddedAppKeys()) {
                for (ApplicationKey applicationKey : appKeys) {
                    if (nodeKey.getIndex() == applicationKey.getKeyIndex()) {
                        addedAppKeys.add(applicationKey);
                    }
                }
            }
            Collections.sort(addedAppKeys, Utils.appKeyComparator);
            notifyDataSetChanged();
        });
    }

    public void setOnItemClickListener(final AddedAppKeyAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void enableDisableKeySelection(final boolean flag) {
        enableSelection = flag;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddedAppKeyAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new AddedAppKeyAdapter.ViewHolder(CheckableRowItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final AddedAppKeyAdapter.ViewHolder holder, final int position) {
        final ApplicationKey key = appKeys.get(position);
        holder.keyName.setText(key.getName());
        final String appKey = MeshParserUtils.bytesToHex(key.getKey(), false);
        holder.key.setText(appKey.toUpperCase());
        holder.check.setChecked(addedAppKeys.contains(key));
        holder.check.setEnabled(enableSelection);
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

    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(@NonNull final ApplicationKey appKey);
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView keyName;
        TextView key;
        CheckBox check;

        private ViewHolder(@NonNull final CheckableRowItemBinding binding) {
            super(binding.getRoot());
            binding.icon.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_vpn_key_24dp));
            keyName = binding.title;
            key = binding.subtitle;
            check = binding.check;
            check.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    final ApplicationKey key = appKeys.get(getAdapterPosition());
                    mOnItemClickListener.onItemClick(key);
                }
            });
        }
    }
}
