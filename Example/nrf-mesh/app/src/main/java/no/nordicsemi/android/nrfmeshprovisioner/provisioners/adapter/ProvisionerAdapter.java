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

package no.nordicsemi.android.nrfmeshprovisioner.provisioners.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.MeshNetworkLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class ProvisionerAdapter extends RecyclerView.Adapter<ProvisionerAdapter.ViewHolder> {

    private final List<Provisioner> mProvisioners = new ArrayList<>();
    private final Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public ProvisionerAdapter(@NonNull final Context context, @NonNull final MeshNetworkLiveData meshNetworkLiveData) {
        this.mContext = context;
        meshNetworkLiveData.observe((LifecycleOwner) context, networkData -> {
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
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.removable_row_item1, parent, false);
        return new ProvisionerAdapter.ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProvisionerAdapter.ViewHolder holder, final int position) {
        final Provisioner provisioner = mProvisioners.get(position);
        holder.provisionerName.setText(provisioner.getProvisionerName());
        if (provisioner.getProvisionerAddress() == null) {
            holder.provisionerSummary.setText(mContext.getString(R.string.unicast_address,
                    mContext.getString(R.string.address_unassigned)));
        } else {
            holder.provisionerSummary.setText(mContext.getString(R.string.unicast_address,
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

        @BindView(R.id.icon)
        ImageView icon;
        @BindView(R.id.title)
        TextView provisionerName;
        @BindView(R.id.subtitle)
        TextView provisionerSummary;

        private ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_account_key_black_alpha_24dp));

            view.findViewById(R.id.removable).setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    final Provisioner provisioner = mProvisioners.get(getAdapterPosition());
                    mOnItemClickListener.onItemClick(getAdapterPosition(), provisioner);
                }
            });
        }
    }
}
