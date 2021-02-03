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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.elevation.ElevationOverlayProvider;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.AddressItemBinding;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

public class GroupAddressAdapter extends RecyclerView.Adapter<GroupAddressAdapter.ViewHolder> {

    private final MeshNetwork network;
    private final ArrayList<Integer> mAddresses = new ArrayList<>();

    public GroupAddressAdapter(@NonNull final Context context, @NonNull final MeshNetwork network, @NonNull final LiveData<MeshModel> meshModelLiveData) {
        this.network = network;
        meshModelLiveData.observe((LifecycleOwner) context, meshModel -> {
            if (meshModel != null) {
                final List<Integer> tempAddresses = meshModel.getSubscribedAddresses();
                if (tempAddresses != null) {
                    mAddresses.clear();
                    mAddresses.addAll(tempAddresses);
                    notifyDataSetChanged();
                }
            }
        });
    }

    @NonNull
    @Override
    public GroupAddressAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new GroupAddressAdapter.ViewHolder(AddressItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupAddressAdapter.ViewHolder holder, final int position) {
        if (mAddresses.size() > 0) {
            final int address = mAddresses.get(position);
            final Group group = network.getGroup(address);
            if (group != null) {
                holder.icon.setImageDrawable(ContextCompat.getDrawable(holder.getSwipeableView().getContext(), R.drawable.ic_outline_group_24dp));
                holder.name.setText(group.getName());
                holder.address.setText(MeshAddress.formatAddress(address, true));
            }
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

    public final class ViewHolder extends RemovableViewHolder {
        ImageView icon;
        TextView name;
        TextView address;

        private ViewHolder(@NonNull final AddressItemBinding binding) {
            super(binding.getRoot());
            icon = binding.icon;
            name = binding.addressId;
            address = binding.title;
            final ElevationOverlayProvider provider = new ElevationOverlayProvider(itemView.getContext());
            final int color = provider.compositeOverlayIfNeeded(provider.getThemeSurfaceColor(), 3.5f);
            getSwipeableView().setBackgroundColor(color);
        }
    }
}
