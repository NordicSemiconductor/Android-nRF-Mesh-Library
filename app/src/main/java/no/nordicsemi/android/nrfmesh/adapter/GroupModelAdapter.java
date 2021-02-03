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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.models.SigModelParser;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.GroupElementItemBinding;
import no.nordicsemi.android.nrfmesh.databinding.GroupModelItemBinding;

public class GroupModelAdapter extends RecyclerView.Adapter<GroupModelAdapter.ViewHolder> {
    private Group mGroup;
    private List<Element> mElements;
    private OnItemClickListener mOnItemClickListener;

    /**
     * Constructs the adapter containing the models in a group.
     *
     * @param group Group
     * @param elements elements containing in the group
     */
    public GroupModelAdapter(@NonNull final Group group, @NonNull final List<Element> elements) {
        this.mGroup = group;
        this.mElements = elements;
    }

    public void updateAdapter(@NonNull final Group group, @NonNull final ArrayList<Element> elements) {
        this.mGroup = group;
        this.mElements = elements;
    }

    /**
     * Sets the item click listener for the adapter
     *
     * @param listener {@link OnItemClickListener}
     */
    public void setOnItemClickListener(@NonNull final GroupModelAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(GroupElementItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Element element = mElements.get(position);
        if (element != null) {
            updateGroupItemViewHolder(holder, element);
            if(position == mElements.size() - 1) {
                holder.mDivider.setVisibility(View.GONE);
            }
        }
    }

    private void updateGroupItemViewHolder(final ViewHolder holder, final Element element) {
        final Context context = holder.itemView.getContext();
        holder.elementTitle.setText(MeshAddress.formatAddress(element.getElementAddress(), true));
        holder.mModelContainer.removeAllViews();
        for (Map.Entry<Integer, MeshModel> modelEntry : element.getMeshModels().entrySet()) {
            final MeshModel model = modelEntry.getValue();
            for (Integer address : model.getSubscribedAddresses()) {
                if (mGroup.getAddress() == address) {
                    final GroupModelItemBinding binding = GroupModelItemBinding.inflate(LayoutInflater.from(holder.itemView.getContext()), holder.mModelContainer, false);
                    final ConstraintLayout container = binding.container;
                    final ImageView modelIcon = binding.icon;
                    final TextView modelTitle = binding.modelTitle;
                    modelTitle.setText(model.getModelName());
                    if(MeshParserUtils.isVendorModel(model.getModelId())){
                        modelIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_domain_nordic_medium_gray));
                    } else {
                        switch (model.getModelId()) {
                            case SigModelParser.GENERIC_ON_OFF_SERVER:
                                modelIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_lightbulb_outline_24dp));
                                break;
                            case SigModelParser.GENERIC_ON_OFF_CLIENT:
                                modelIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_light_switch_24dp));
                                break;
                            case SigModelParser.GENERIC_LEVEL_SERVER:
                                modelIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_lightbulb_level_24dp));
                                break;
                            default:
                                modelIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_help_outline_24dp));
                                break;
                        }
                    }
                    container.setOnClickListener(v -> mOnItemClickListener.onModelItemClick(element, model));
                    holder.mModelContainer.addView(binding.getRoot());
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mElements.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onModelItemClick(final Element element, final MeshModel model);
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        TextView elementTitle;
        LinearLayout mModelContainer;
        View mDivider;

        private ViewHolder(final @NonNull GroupElementItemBinding binding) {
            super(binding.getRoot());
            elementTitle = binding.elementTitle;
            mModelContainer = binding.modelContainer;
            mDivider = binding.divider.divider;
        }
    }
}
