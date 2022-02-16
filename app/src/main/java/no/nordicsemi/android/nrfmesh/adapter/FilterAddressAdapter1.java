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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.utils.AddressArray;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.databinding.CustomChipBinding;
import no.nordicsemi.android.nrfmesh.utils.AddressArrayDiffCallback;

public class FilterAddressAdapter1 extends RecyclerView.Adapter<FilterAddressAdapter1.ViewHolder> {

    private final AsyncListDiffer<AddressArray> differ = new AsyncListDiffer<>(this, new AddressArrayDiffCallback());
    private OnItemClickListener itemClickListener;

    public void update(@NonNull final ArrayList<AddressArray> addresses) {
        final List<AddressArray> addressArray = new ArrayList<>(addresses);
        differ.submitList(addressArray);
    }

    public void setOnItemClickListener(final OnItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public FilterAddressAdapter1.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final CustomChipBinding binding = CustomChipBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FilterAddressAdapter1.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilterAddressAdapter1.ViewHolder holder, final int position) {
        final byte[] address = differ.getCurrentList().get(position).getAddress();
        holder.address.setText(MeshParserUtils.bytesToHex(address, true));
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

    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(final int position);
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        TextView address;

        private ViewHolder(final CustomChipBinding binding) {
            super(binding.getRoot());
            address = binding.title;
            binding.imgDelete.setOnClickListener(v -> {
                final int position = getAbsoluteAdapterPosition();
                itemClickListener.onItemClick(position);
            });
        }
    }
}
