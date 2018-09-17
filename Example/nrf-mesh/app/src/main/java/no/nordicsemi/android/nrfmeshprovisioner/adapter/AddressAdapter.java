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

package no.nordicsemi.android.nrfmeshprovisioner.adapter;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.ModelConfigurationActivity;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    private final ArrayList<byte[]> mAddresses = new ArrayList<>();
    private final Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public AddressAdapter(final ModelConfigurationActivity context, final LiveData<MeshModel> meshModelLiveData) {
        this.mContext = context;
        meshModelLiveData.observe(context, meshModel -> {
            if(meshModel != null) {
                final List<byte[]> tempAddresses = meshModel.getSubscriptionAddresses();
                if (tempAddresses != null) {
                    mAddresses.clear();
                    mAddresses.addAll(tempAddresses);
                    notifyDataSetChanged();
                }
            }
        });
    }

    public void setOnItemClickListener(final AddressAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public AddressAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.address_item, parent, false);
        return new AddressAdapter.ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final AddressAdapter.ViewHolder holder, final int position) {
        if(mAddresses.size() > 0) {
            final String address = MeshParserUtils.bytesToHex(mAddresses.get(position), true);
            holder.address.setText(address);
        }
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mAddresses.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(final int position, final byte[] address);
    }

    public final class ViewHolder extends RemovableViewHolder {

        @BindView(R.id.address)
        TextView address;

        private ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.findViewById(R.id.removable).setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(getAdapterPosition(), mAddresses.get(getAdapterPosition()));
                }
            });
        }
    }
}
