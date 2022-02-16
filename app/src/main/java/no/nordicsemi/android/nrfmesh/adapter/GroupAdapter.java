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

package no.nordicsemi.android.nrfmesh.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.GroupItemBinding;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private final AsyncListDiffer<GroupItemUIState> differ = new AsyncListDiffer<>(this, new GroupItemUiStateDiffCallback());
    private OnItemClickListener mOnItemClickListener;


    public void updateAdapter(final List<GroupItemUIState> groups) {
        differ.submitList(groups);
    }

    public void setOnItemClickListener(@NonNull final OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public GroupAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final GroupItemBinding binding = GroupItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GroupAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupAdapter.ViewHolder holder, final int position) {
        if (getItemCount() > 0) {
            final GroupItemUIState state = differ.getCurrentList().get(position);
            if (state != null) {
                holder.groupName.setText(state.getName());
                holder.groupAddress.setText(holder.groupAddress.getContext().
                        getString(R.string.group_address_summary, MeshAddress.formatAddress(state.getAddress(), true)));
                holder.groupDeviceCount.setText(holder.groupDeviceCount.getContext().getResources().getQuantityString(R.plurals.device_count,
                        state.getSubscribedModels(), state.getSubscribedModels()));
            }
        }
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    /**
     * Returns the number of models associated to the group in a particular position
     *
     * @param position position
     */
    public int getModelCount(final int position) {
        if (position >= 0 && !differ.getCurrentList().isEmpty() && position < differ.getCurrentList().size()) {
            return differ.getCurrentList().get(position).getSubscribedModels();
        }
        return 0;
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(final int address);
    }

    public final class ViewHolder extends RemovableViewHolder {
        FrameLayout container;
        TextView groupName;
        TextView groupAddress;
        TextView groupDeviceCount;

        private ViewHolder(final GroupItemBinding binding) {
            super(binding.getRoot());
            container = binding.container;
            groupName = binding.groupName;
            groupAddress = binding.groupAddress;
            groupDeviceCount = binding.groupDeviceCount;
            binding.container.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(differ.getCurrentList().get(getAbsoluteAdapterPosition()).getAddress());
                }
            });
        }
    }
}
