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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.utils.AddressType;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.AddressTypeItemBinding;
import no.nordicsemi.android.nrfmesh.databinding.NoGroupsLayoutBinding;

public class AddressTypeAdapter extends BaseAdapter {

    private final AddressType[] mAddressTypes;
    private final Context mContext;

    public AddressTypeAdapter(@NonNull final Context context,
                              @NonNull final AddressType[] addressTypes) {
        this.mContext = context;
        mAddressTypes = addressTypes;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mAddressTypes.length;
    }

    @Override
    public AddressType getItem(final int position) {
        return mAddressTypes[position];
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        if (mAddressTypes.length > 0) {
            ViewHolder viewHolder;
            if (view == null) {
                final AddressTypeItemBinding binding = AddressTypeItemBinding.inflate(LayoutInflater.from(parent.getContext()));
                view = binding.getRoot();
                viewHolder = new ViewHolder(binding);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            final AddressType addressType = mAddressTypes[position];
            if (addressType == AddressType.GROUP_ADDRESS) {
                viewHolder.addressName.setText(R.string.action_groups);
            } else {
                viewHolder.addressName.setText(AddressType.getTypeName(addressType));
            }
        } else {
            view = NoGroupsLayoutBinding.inflate(LayoutInflater.from(parent.getContext())).getRoot();
        }
        return view;
    }

    private static final class ViewHolder {
        TextView addressName;

        private ViewHolder(final AddressTypeItemBinding binding) {
            addressName = binding.addressName;
        }
    }
}
