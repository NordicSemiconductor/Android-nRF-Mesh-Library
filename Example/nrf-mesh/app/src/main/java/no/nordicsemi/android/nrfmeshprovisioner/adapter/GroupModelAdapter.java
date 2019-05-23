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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class GroupModelAdapter extends RecyclerView.Adapter<GroupModelAdapter.ViewHolder> {

    private final Context mContext;
    private Group mGroup;
    private List<Element> mElements;
    private OnItemClickListener mOnItemClickListener;

    /**
     * Constructs the adapter containing the models in a group.
     *
     * @param context  Context
     * @param group Group
     * @param elements elements containing in the group
     */
    public GroupModelAdapter(@NonNull final Context context, @NonNull final Group group, @NonNull final List<Element> elements) {
        this.mContext = context;
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
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.group_element_item, parent, false);
        return new ViewHolder(layoutView);
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
        holder.elementTitle.setText(MeshAddress.formatAddress(element.getElementAddress(), true));
        holder.mModelContainer.removeAllViews();
        for (Map.Entry<Integer, MeshModel> modelEntry : element.getMeshModels().entrySet()) {
            final MeshModel model = modelEntry.getValue();
            for (Integer address : model.getSubscribedAddresses()) {
                if (mGroup.getAddress() == address) {
                    final View view = LayoutInflater.from(mContext).inflate(R.layout.group_model_item, holder.mModelContainer, false);
                    final ConstraintLayout container = view.findViewById(R.id.container);
                    final ImageView modelIcon = view.findViewById(R.id.icon);
                    final TextView modelTitle = view.findViewById(R.id.model_title);
                    modelTitle.setText(model.getModelName());
                    if(MeshParserUtils.isVendorModel(model.getModelId())){
                        modelIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_domain_nordic_medium_gray_24dp));
                    } else {
                        switch (model.getModelId()) {
                            case SigModelParser.GENERIC_ON_OFF_SERVER:
                                modelIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_lightbulb_outline_nordic_medium_grey_24dp));
                                break;
                            case SigModelParser.GENERIC_ON_OFF_CLIENT:
                                modelIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_light_switch_nordic_medium_grey_24dp));
                                break;
                            case SigModelParser.GENERIC_LEVEL_SERVER:
                                modelIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_lightbulb_level_nordic_medium_gray_outline_24dp));
                                break;
                            default:
                                modelIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_help_outline_nordic_medium_grey_24dp));
                                break;
                        }
                    }
                    container.setOnClickListener(v -> mOnItemClickListener.onModelItemClick(element, model));
                    holder.mModelContainer.addView(view);
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

    final class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.element_title)
        TextView elementTitle;
        @BindView(R.id.model_container)
        LinearLayout mModelContainer;
        @BindView(R.id.divider)
        View mDivider;

        private ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
