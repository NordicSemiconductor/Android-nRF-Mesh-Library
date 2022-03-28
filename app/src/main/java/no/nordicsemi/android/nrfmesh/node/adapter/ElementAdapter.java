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

package no.nordicsemi.android.nrfmesh.node.adapter;

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
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.models.VendorModel;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.CompositionDataParser;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ElementItemBinding;

public class ElementAdapter extends RecyclerView.Adapter<ElementAdapter.ViewHolder> {
    private final AsyncListDiffer<Element> differ = new AsyncListDiffer<>(this, new ElementDiffCallback());
    private OnItemClickListener mOnItemClickListener;
    private ProvisionedMeshNode meshNode;

    public void update(final ProvisionedMeshNode meshNode) {
        this.meshNode = meshNode;
        differ.submitList(populateList(meshNode));
    }

    private List<Element> populateList(@NonNull final ProvisionedMeshNode meshNode) {
        final List<Element> elements = new ArrayList<>();
        for (Element element : meshNode.getElements().values()) {
            try {
                elements.add(element.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return elements;
    }

    public void setOnItemClickListener(@NonNull final ElementAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(ElementItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position, @NonNull final List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            if ((Boolean) payloads.get(0)) {
                holder.mElementTitle.setText(differ.getCurrentList().get(position).getName());
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Element element = differ.getCurrentList().get(position);
        final int modelCount = element.getMeshModels().size();
        holder.mElementTitle.setText(element.getName());
        holder.mElementSubtitle.setText(holder.mElementSubtitle.getContext().getString(R.string.model_count, modelCount));
        inflateModelViews(holder, new ArrayList<>(element.getMeshModels().values()));
    }

    private void inflateModelViews(final ViewHolder holder, final List<MeshModel> models) {
        //Remove all child views to avoid duplicating
        holder.mModelContainer.removeAllViews();
        final Context context = holder.mModelContainer.getContext();
        for (int i = 0; i < models.size(); i++) {
            final MeshModel model = models.get(i);
            final View modelView = LayoutInflater.from(context).inflate(R.layout.model_item, holder.mElementContainer, false);
            modelView.setTag(model.getModelId());
            final TextView modelNameView = modelView.findViewById(R.id.title);
            final TextView modelIdView = modelView.findViewById(R.id.subtitle);
            modelNameView.setText(model.getModelName());
            if (model instanceof VendorModel) {
                modelIdView.setText(context.getString(R.string.format_vendor_model_id, CompositionDataParser.formatModelIdentifier(model.getModelId(), true)));
            } else {
                modelIdView.setText(context.getString(R.string.format_sig_model_id, CompositionDataParser.formatModelIdentifier(model.getModelId(), true)));
            }
            modelView.setOnClickListener(v -> {
                final int position = holder.getBindingAdapterPosition();
                final Element element = differ.getCurrentList().get(position);
                mOnItemClickListener.onModelClicked(meshNode, element, model);
            });
            holder.mModelContainer.addView(modelView);
        }
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    @Override
    public long getItemId(final int position) {
        return differ.getCurrentList().get(position).getElementAddress();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public interface OnItemClickListener {
        void onElementClicked(@NonNull final Element element);

        void onModelClicked(@NonNull final ProvisionedMeshNode meshNode, @NonNull final Element element, @NonNull final MeshModel model);
    }

    final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ConstraintLayout mElementContainer;
        ImageView mIcon;
        TextView mElementTitle;
        TextView mElementSubtitle;
        ImageButton mElementExpand;
        ImageButton mEdit;
        LinearLayout mModelContainer;

        private ViewHolder(@NonNull final ElementItemBinding binding) {
            super(binding.getRoot());
            mElementContainer = binding.elementItemContainer;
            mIcon = binding.icon;
            mElementTitle = binding.elementTitle;
            mElementSubtitle = binding.elementSubtitle;
            mElementExpand = binding.elementExpand;
            mEdit = binding.edit;
            mModelContainer = binding.modelContainer;
            mElementExpand.setOnClickListener(this);
            mEdit.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            if (v.getId() == R.id.element_expand) {
                if (mModelContainer.getVisibility() == View.VISIBLE) {
                    mElementExpand.setImageResource(R.drawable.ic_round_expand_more);
                    mModelContainer.setVisibility(View.GONE);
                } else {
                    mElementExpand.setImageResource(R.drawable.ic_round_expand_less);
                    mModelContainer.setVisibility(View.VISIBLE);
                }
            } else if (v.getId() == R.id.edit) {
                mOnItemClickListener.onElementClicked(differ.getCurrentList().get(getAbsoluteAdapterPosition()));
            }
        }
    }
}
