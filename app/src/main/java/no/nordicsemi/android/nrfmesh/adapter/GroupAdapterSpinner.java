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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.databinding.GroupSubscriptionItemBinding;
import no.nordicsemi.android.nrfmesh.databinding.NoGroupsLayoutBinding;

public class GroupAdapterSpinner extends BaseAdapter {

    private final ArrayList<Group> mGroups = new ArrayList<>();

    public GroupAdapterSpinner(@NonNull final List<Group> groups) {
        mGroups.clear();
        mGroups.addAll(groups);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mGroups.isEmpty())
            return 1;
        return mGroups.size();
    }

    @Override
    public Group getItem(final int position) {
        return mGroups.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        if (!mGroups.isEmpty()) {
            ViewHolder viewHolder;
            if (view == null) {
                final GroupSubscriptionItemBinding binding = GroupSubscriptionItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                view = binding.getRoot();
                viewHolder = new ViewHolder(binding);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            final Group group = mGroups.get(position);
            viewHolder.groupName.setText(group.getName());
            viewHolder.address.setText(MeshAddress.formatAddress(group.getAddress(), true));
        } else {
            view = NoGroupsLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot();
        }
        return view;
    }

    public boolean isEmpty() {
        return mGroups.isEmpty();
    }

    private static final class ViewHolder {
        TextView groupName;
        TextView address;

        private ViewHolder(final GroupSubscriptionItemBinding binding) {
            groupName = binding.groupName;
            address = binding.title;
        }
    }
}
