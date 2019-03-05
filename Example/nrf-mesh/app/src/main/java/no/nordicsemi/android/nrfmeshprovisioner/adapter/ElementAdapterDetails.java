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
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.utils.CompositionDataParser;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class ElementAdapterDetails extends RecyclerView.Adapter<ElementAdapterDetails.ViewHolder> {

    private final Context mContext;
    private final List<Element> mElements;
    private OnItemClickListener mOnItemClickListener;

    public ElementAdapterDetails(@NonNull final Context mContext, @NonNull final List<Element> elements) {
        this.mContext = mContext;
        mElements = elements;
    }


    public void setOnItemClickListener(final ElementAdapterDetails.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.element_item_details, parent, false);
        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Element element = mElements.get(position);
        holder.mElementContainer.setTag(element);
        final int modelCount = element.getSigModelCount() + element.getVendorModelCount();
        holder.mElementTitle.setText(mContext.getString(R.string.element_address, MeshAddress.formatAddress(element.getElementAddress(), false)));
        holder.mElementSubtitle.setText(mContext.getString(R.string.model_count, modelCount));

        final List<MeshModel> models = new ArrayList<>(element.getMeshModels().values());
        inflateModelViews(holder, models);

    }

    private void inflateModelViews(final ElementAdapterDetails.ViewHolder holder, final List<MeshModel> models){
        //Remove all child views to avoid duplicating
        holder.mModelContainer.removeAllViews();
        for(MeshModel model : models) {
            final View modelView = LayoutInflater.from(mContext).inflate(R.layout.model_item_details, holder.mElementContainer, false);
            modelView.setTag(model.getModelId());
            final TextView modelNameView = modelView.findViewById(R.id.address);
            final TextView modelIdView = modelView.findViewById(R.id.model_id);
            modelNameView.setText(model.getModelName());
            if(model instanceof VendorModel){
                modelIdView.setText(mContext.getString(R.string.format_vendor_model_id, CompositionDataParser.formatModelIdentifier(model.getModelId(), true)));
            } else {
                modelIdView.setText(mContext.getString(R.string.format_sig_model_id, CompositionDataParser.formatModelIdentifier((short) model.getModelId(), true)));
            }

            holder.mModelContainer.addView(modelView);
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
        void onItemClick(final int position);
    }

    final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.element_item_container)
        ConstraintLayout mElementContainer;
        @BindView(R.id.element_title)
        TextView mElementTitle;
        @BindView(R.id.element_subtitle)
        TextView mElementSubtitle;
        @BindView(R.id.element_expand)
        ImageView mElementExpand;
        @BindView(R.id.model_container)
        LinearLayout mModelContainer;

        private ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            mElementContainer.setOnClickListener(this);

        }

        @Override
        public void onClick(final View v) {
            switch (v.getId()){
                case R.id.element_item_container:
                    if(mModelContainer.getVisibility() == View.VISIBLE){
                        mElementExpand.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_round_expand_more_black_alpha_24dp));
                        mModelContainer.setVisibility(View.GONE);
                    } else {
                        mElementExpand.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_round_expand_less_black_alpha_24dp));
                        mModelContainer.setVisibility(View.VISIBLE);
                    }
                    mOnItemClickListener.onItemClick(getAdapterPosition());
                    break;
                default:
                    break;
            }
        }
    }
}
