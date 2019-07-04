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
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.NetworkKey;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class ManageBoundNetKeyAdapter extends RecyclerView.Adapter<ManageBoundNetKeyAdapter.ViewHolder> {

    private final List<NetworkKey> mNetworkKeys;
    private final Context mContext;
    private ApplicationKey mAppKey;
    private OnItemClickListener mOnItemClickListener;

    public ManageBoundNetKeyAdapter(@NonNull final Context context,
                                    @NonNull final List<NetworkKey> networkKeys,
                                    @NonNull final ApplicationKey appKey) {
        mContext = context;
        mNetworkKeys = networkKeys;
        mAppKey = appKey;
    }

    public void setOnItemClickListener(final ManageBoundNetKeyAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ManageBoundNetKeyAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.removable_row_item2, parent, false);
        return new ManageBoundNetKeyAdapter.ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ManageBoundNetKeyAdapter.ViewHolder holder, final int position) {
        if (mNetworkKeys.size() > 0) {
            final NetworkKey networkKey = mNetworkKeys.get(position);
            holder.netKeyName.setText(networkKey.getName());
            final String key = MeshParserUtils.bytesToHex(networkKey.getKey(), false);
            holder.netKey.setText(key.toUpperCase());
            holder.getSwipeableView().setTag(networkKey);

            if (checkRadio(networkKey)) {
                holder.bound.setChecked(true);
            } else {
                holder.bound.setChecked(false);
            }
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
        ApplicationKey updateBoundNetKeyIndex(final int position, @NonNull final NetworkKey networkKey);
    }

    final class ViewHolder extends RemovableViewHolder {

        @BindView(R.id.title)
        TextView netKeyName;
        @BindView(R.id.subtitle)
        TextView netKey;
        @BindView(R.id.radio)
        RadioButton bound;

        private ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.findViewById(R.id.removable).setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    final NetworkKey netKey = mNetworkKeys.get(getAdapterPosition());
                    final ApplicationKey appKey = mOnItemClickListener.updateBoundNetKeyIndex(getAdapterPosition(), netKey);
                    if (appKey != null) {
                        mAppKey = appKey;
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }
}
