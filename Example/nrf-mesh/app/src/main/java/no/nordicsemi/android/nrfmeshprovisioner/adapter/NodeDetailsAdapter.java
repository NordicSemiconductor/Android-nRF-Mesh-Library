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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class NodeDetailsAdapter extends RecyclerView.Adapter<NodeDetailsAdapter.ViewHolder>{

    private final Context mContext;
    private final List<String> mNodeDetails;

    public NodeDetailsAdapter(final Context mContext, final List<String> mNodeDetails) {
        this.mContext = mContext;
        this.mNodeDetails = mNodeDetails;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.device_item, parent, false);
        return new NodeDetailsAdapter.ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        switch (position){
            case 0:
                holder.nodeTitle.setText(R.string.node_name);
                holder.nodeSubtitle.setText(mNodeDetails.get(position));
                break;
            case 1:
                holder.nodeTitle.setText(R.string.node_prov_timestamp);
                holder.nodeSubtitle.setText(mNodeDetails.get(position));
                break;
            case 2:
                holder.nodeTitle.setText(R.string.node_identifier);
                holder.nodeSubtitle.setText(mNodeDetails.get(position));
                break;
            case 3:
                holder.nodeTitle.setText(R.string.node_unicast_address);
                holder.nodeSubtitle.setText(mNodeDetails.get(position));
                break;
            case 4:
                holder.nodeTitle.setText(R.string.node_company_identifier);
                holder.nodeSubtitle.setText(mNodeDetails.get(position));
                break;
            case 5:
                holder.nodeTitle.setText(R.string.node_product_identifier);
                holder.nodeSubtitle.setText(mNodeDetails.get(position));
                break;
            case 6:
                holder.nodeTitle.setText(R.string.node_vendor_identifier);
                holder.nodeSubtitle.setText(mNodeDetails.get(position));
                break;
            case 7:
                holder.nodeTitle.setText(R.string.node_replay_protection_count);
                holder.nodeSubtitle.setText(mNodeDetails.get(position));
                break;
            case 8:
                holder.nodeTitle.setText(R.string.node_features);
                holder.nodeSubtitle.setText(mNodeDetails.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mNodeDetails.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.generic_title)
        TextView nodeTitle;
        @BindView(R.id.generic_subtitle)
        TextView nodeSubtitle;

        private ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
