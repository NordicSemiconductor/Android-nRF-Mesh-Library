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

package no.nordicsemi.android.nrfmeshprovisioner.node.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.CompositionDataParser;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class ElementAdapter extends RecyclerView.Adapter<ElementAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Element> mElements = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    private ProvisionedMeshNode mProvisionedMeshNode;

    public ElementAdapter(@NonNull final Context context, @NonNull final LiveData<ProvisionedMeshNode> meshNodeLiveData) {
        this.mContext = context.getApplicationContext();
        meshNodeLiveData.observe((LifecycleOwner) context, meshNode -> {
            if (meshNode != null) {
                mProvisionedMeshNode = meshNode;
                mElements.clear();
                mElements.addAll(mProvisionedMeshNode.getElements().values());
                notifyDataSetChanged();
            }
        });
    }


    public void setOnItemClickListener(@NonNull final ElementAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.element_item, parent, false);
        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Element element = mElements.get(position);
        final int modelCount = element.getMeshModels().size();
        holder.mElementTitle.setText(element.getName());
        holder.mElementSubtitle.setText(mContext.getString(R.string.model_count, modelCount));

        final List<MeshModel> models = new ArrayList<>(element.getMeshModels().values());
        inflateModelViews(holder, models);
    }

    private void inflateModelViews(final ViewHolder holder, final List<MeshModel> models) {
        //Remove all child views to avoid duplicating
        holder.mModelContainer.removeAllViews();
        for (int i = 0; i < models.size(); i++) {
            final MeshModel model = models.get(i);
            final View modelView = LayoutInflater.from(mContext).inflate(R.layout.model_item, holder.mElementContainer, false);
            modelView.setTag(model.getModelId());
            final TextView modelNameView = modelView.findViewById(R.id.title);
            final TextView modelIdView = modelView.findViewById(R.id.subtitle);
            modelNameView.setText(model.getModelName());
            if (model instanceof VendorModel) {
                modelIdView.setText(mContext.getString(R.string.format_vendor_model_id, CompositionDataParser.formatModelIdentifier(model.getModelId(), true)));
            } else {
                modelIdView.setText(mContext.getString(R.string.format_sig_model_id, CompositionDataParser.formatModelIdentifier((short) model.getModelId(), true)));
            }

            modelView.setOnClickListener(v -> {
                final int position = holder.getAdapterPosition();
                final Element element = mElements.get(position);
                mOnItemClickListener.onModelClicked(mProvisionedMeshNode, element, model);
            });
            holder.mModelContainer.addView(modelView);
        }
    }

    @Override
    public int getItemCount() {
        return mElements.size();
    }

    @Override
    public long getItemId(final int position) {
        return mElements.get(position).getElementAddress();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public interface OnItemClickListener {
        void onElementClicked(@NonNull final Element element);

        void onModelClicked(@NonNull final ProvisionedMeshNode meshNode, @NonNull final Element element, @NonNull final MeshModel model);
    }

    final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.element_item_container)
        ConstraintLayout mElementContainer;
        @BindView(R.id.icon)
        ImageView mIcon;
        @BindView(R.id.element_title)
        TextView mElementTitle;
        @BindView(R.id.element_subtitle)
        TextView mElementSubtitle;
        @BindView(R.id.element_expand)
        ImageButton mElementExpand;
        @BindView(R.id.edit)
        ImageButton mEdit;
        @BindView(R.id.model_container)
        LinearLayout mModelContainer;

        private ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            mElementExpand.setOnClickListener(this);
            mEdit.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            if (v.getId() == R.id.element_expand) {
                if (mModelContainer.getVisibility() == View.VISIBLE) {
                    mElementExpand.setImageResource(R.drawable.ic_round_expand_more_black_alpha_24dp);
                    mModelContainer.setVisibility(View.GONE);
                } else {
                    mElementExpand.setImageResource(R.drawable.ic_round_expand_less_black_alpha_24dp);
                    mModelContainer.setVisibility(View.VISIBLE);
                }
            } else if (v.getId() == R.id.edit) {
                mOnItemClickListener.onElementClicked(mElements.get(getAdapterPosition()));
            }
        }
    }
}
