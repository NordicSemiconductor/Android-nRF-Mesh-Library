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

import com.google.android.material.elevation.ElevationOverlayProvider;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.utils.AddressArray;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.mesh.utils.ProxyFilter;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.AddressItemBinding;
import no.nordicsemi.android.nrfmesh.utils.AddressArrayDiffCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

public class FilterAddressAdapter extends RecyclerView.Adapter<FilterAddressAdapter.ViewHolder> {

    private final AsyncListDiffer<AddressArray> differ = new AsyncListDiffer<>(this, new AddressArrayDiffCallback());

    public void updateData(@NonNull final ProxyFilter filter) {
        final ArrayList<AddressArray> addresses = new ArrayList<>(filter.getAddresses());
        differ.submitList(addresses);
    }

    public void clearData() {
        differ.submitList(new ArrayList<>());
    }

    public void clearRow(@NonNull final ProxyFilter filter, final int position) {
        final ArrayList<AddressArray> addresses = new ArrayList<>(filter.getAddresses());
        addresses.remove(position);
        differ.submitList(addresses);
    }

    @NonNull
    @Override
    public FilterAddressAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final AddressItemBinding binding = AddressItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilterAddressAdapter.ViewHolder holder, int position) {
        final byte[] address = differ.getCurrentList().get(position).getAddress();
        holder.address.setText(MeshParserUtils.bytesToHex(address, true));
        if (MeshAddress.isValidGroupAddress(address)) {
            holder.addressTitle.setText(R.string.title_group_address);
        } else if (MeshAddress.isValidUnicastAddress(address)) {
            holder.addressTitle.setText(R.string.title_unicast_address);
        } else if (MeshAddress.isValidVirtualAddress(address)) {
            holder.addressTitle.setText(R.string.virtual_address);
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

    public static final class ViewHolder extends RemovableViewHolder {
        FrameLayout container;
        TextView addressTitle;
        TextView address;

        private ViewHolder(final AddressItemBinding binding) {
            super(binding.getRoot());
            container = binding.container;
            addressTitle = binding.addressId;
            address = binding.title;
            final ElevationOverlayProvider provider = new ElevationOverlayProvider(itemView.getContext());
            final int color = provider.compositeOverlayIfNeeded(provider.getThemeSurfaceColor(), 3.5f);
            getSwipeableView().setBackgroundColor(color);
            container.setClickable(false);
        }
    }
}
