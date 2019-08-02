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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ProvisionerProgress;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ProvisioningStatusLiveData;

public class ProvisioningProgressAdapter extends RecyclerView.Adapter<ProvisioningProgressAdapter.ViewHolder> {
    private final Context mContext;
    private final List<ProvisionerProgress> mProgress = new ArrayList<>();

    public ProvisioningProgressAdapter(@NonNull final Context mContext, @NonNull final ProvisioningStatusLiveData provisioningProgress) {
        this.mContext = mContext;
        this.mProgress.addAll(provisioningProgress.getStateList());
    }

    @NonNull
    @Override
    public ProvisioningProgressAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.progress_item, parent, false);
        return new ProvisioningProgressAdapter.ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final ProvisionerProgress provisioningProgress = mProgress.get(position);
        if(provisioningProgress != null) {
            holder.image.setImageDrawable(ContextCompat.getDrawable(mContext, provisioningProgress.getResId()));
            holder.progress.setText(provisioningProgress.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return mProgress.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void refresh(final ArrayList<ProvisionerProgress> stateList) {
        mProgress.clear();
        mProgress.addAll(stateList);
        notifyDataSetChanged();
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.text)
        TextView progress;

        private ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
