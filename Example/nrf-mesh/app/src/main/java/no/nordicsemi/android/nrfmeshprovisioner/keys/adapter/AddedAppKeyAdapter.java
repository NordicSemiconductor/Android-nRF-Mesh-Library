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
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.NodeKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class AddedAppKeyAdapter extends RecyclerView.Adapter<AddedAppKeyAdapter.ViewHolder> {

    private final List<ApplicationKey> appKeys = new ArrayList<>();
    private final List<ApplicationKey> addedAppKeys = new ArrayList<>();
    private final Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public AddedAppKeyAdapter(@NonNull final Context context,
                              @NonNull final List<ApplicationKey> appKeys,
                              @NonNull final LiveData<ProvisionedMeshNode> meshNodeLiveData) {
        this.mContext = context;
        this.appKeys.clear();
        this.appKeys.addAll(appKeys);
        Collections.sort(this.appKeys, Utils.appKeyComparator);
        meshNodeLiveData.observe((LifecycleOwner) context, meshNode -> {
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

    @NonNull
    @Override
    public AddedAppKeyAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.row_item_key, parent, false);
        return new AddedAppKeyAdapter.ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final AddedAppKeyAdapter.ViewHolder holder, final int position) {
        final ApplicationKey key = appKeys.get(position);
        holder.keyName.setText(key.getName());
        final String appKey = MeshParserUtils.bytesToHex(key.getKey(), false);
        holder.key.setText(appKey.toUpperCase());
        if (addedAppKeys.contains(key)) {
            holder.check.setChecked(true);
        } else {
            holder.check.setChecked(false);
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

    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(@NonNull final ApplicationKey appKey);
    }

    final class ViewHolder extends RemovableViewHolder {

        @BindView(R.id.title)
        TextView keyName;
        @BindView(R.id.subtitle)
        TextView key;
        @BindView(R.id.check)
        CheckBox check;

        private ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            check.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    final ApplicationKey key = appKeys.get(getAdapterPosition());
                    mOnItemClickListener.onItemClick(key);
                }
            });
        }
    }
}
