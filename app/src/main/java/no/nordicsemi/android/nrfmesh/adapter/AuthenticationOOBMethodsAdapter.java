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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.utils.AuthenticationOOBMethods;
import no.nordicsemi.android.nrfmesh.databinding.OobTypeItemBinding;

public class AuthenticationOOBMethodsAdapter extends BaseAdapter {

    private final Context mContext;
    private final ArrayList<AuthenticationOOBMethods> mOOBTypes = new ArrayList<>();

    /**
     * Constructs AuthenticationOOBMethodsAdapter
     *
     * @param context  Context
     * @param oobTypes List of oob types
     */
    public AuthenticationOOBMethodsAdapter(@NonNull final Context context, @NonNull final List<AuthenticationOOBMethods> oobTypes) {
        this.mContext = context;
        mOOBTypes.clear();
        mOOBTypes.addAll(oobTypes);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mOOBTypes.isEmpty())
            return 1;
        return mOOBTypes.size();
    }

    @Override
    public AuthenticationOOBMethods getItem(final int position) {
        return mOOBTypes.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;
        if (view == null) {
            final OobTypeItemBinding binding = OobTypeItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            view = binding.getRoot();
            viewHolder = new ViewHolder(binding);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final AuthenticationOOBMethods oobType = mOOBTypes.get(position);
        viewHolder.oobTypeName.setText(AuthenticationOOBMethods.getAuthenticationMethodName(oobType));
        return view;
    }

    public boolean isEmpty() {
        return mOOBTypes.isEmpty();
    }

    public static final class ViewHolder {
        TextView oobTypeName;

        private ViewHolder(final OobTypeItemBinding binding) {
            oobTypeName = binding.oobTypeName;
        }
    }
}
