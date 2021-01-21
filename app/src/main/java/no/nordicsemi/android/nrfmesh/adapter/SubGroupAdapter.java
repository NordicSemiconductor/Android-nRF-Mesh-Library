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
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.models.SigModelParser;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.utils.CompanyIdentifiers;
import no.nordicsemi.android.mesh.utils.CompositionDataParser;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.GroupedAppKeyItemBinding;
import no.nordicsemi.android.nrfmesh.databinding.GroupedItemBinding;

public class SubGroupAdapter extends RecyclerView.Adapter<SubGroupAdapter.ViewHolder> {

    private final MeshNetwork mMeshNetwork;
    private List<MeshModel> mModels;
    private SparseArray<SparseIntArray> mGroupedKeyModels;
    private Group mGroup;
    private OnItemClickListener mOnItemClickListener;

    public SubGroupAdapter(@NonNull final Context context,
                           @NonNull final MeshNetwork network,
                           @NonNull final LiveData<Group> groupedModels) {
        this.mMeshNetwork = network;
        groupedModels.observe((LifecycleOwner) context, group -> {
            mGroup = group;
            updateAdapterData();
        });
    }

    public void updateAdapterData() {
        final Group group = mGroup;
        mModels = mMeshNetwork.getModels(group);
        mGroupedKeyModels = groupModelsBasedOnAppKeys();
        notifyDataSetChanged();
    }


    public void setOnItemClickListener(@NonNull final SubGroupAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(GroupedAppKeyItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (mGroupedKeyModels.size() > 0) {
            final int keyIndex = mGroupedKeyModels.keyAt(position);
            holder.groupItemContainer.setTag(keyIndex);
            final ApplicationKey key = mMeshNetwork.getAppKey(keyIndex);
            holder.mGroupAppKeyTitle.setText(key.getName());
            final SparseIntArray groupedModels = mGroupedKeyModels.valueAt(position);
            holder.mGroupGrid.setRowCount(1);
            //Remove all child views to avoid duplicating
            holder.mGroupGrid.removeAllViews();
            for (int i = 0; i < groupedModels.size(); i++) {
                final int modelId = groupedModels.keyAt(i);
                final int count = groupedModels.valueAt(i);
                inflateView(holder, keyIndex, modelId, count, i);
            }
        }
    }

    private void inflateView(@NonNull final ViewHolder holder, final int keyIndex, final int modelId, final int modelCount, final int position) {
        final Context context = holder.itemView.getContext();
        final GroupedItemBinding binding = GroupedItemBinding.inflate(LayoutInflater.from(context), holder.mGroupGrid, false);
        final CardView groupContainerCard = binding.groupContainerCard;
        final ImageView icon = binding.icon;
        final TextView groupSummary = binding.groupSummary;
        final Button on = binding.actionOn;
        final Button off = binding.actionOff;
        if (MeshParserUtils.isVendorModel(modelId)) {
            icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_domain_48dp));
            binding.containerButtons.setVisibility(View.INVISIBLE);
            binding.containerVendor.setVisibility(View.VISIBLE);
            final TextView modelIdView = binding.subtitle;
            modelIdView.setText(CompositionDataParser.formatModelIdentifier(modelId, true));
            final TextView companyIdView = binding.companyId;
            final int companyIdentifier = MeshParserUtils.getCompanyIdentifier(modelId);
            companyIdView.setText(CompanyIdentifiers.getCompanyName((short) companyIdentifier));
            groupSummary.setText(context.getResources().getQuantityString(R.plurals.device_count, modelCount, modelCount));
        } else {
            switch (modelId) {
                case SigModelParser.GENERIC_ON_OFF_SERVER:
                    groupSummary.setText(context.getResources().getQuantityString(R.plurals.light_count, modelCount, modelCount));
                    break;
                case SigModelParser.GENERIC_ON_OFF_CLIENT:
                    icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_light_switch_48dp));
                    binding.containerButtons.setVisibility(View.INVISIBLE);
                    groupSummary.setText(context.getResources().getQuantityString(R.plurals.switch_count, modelCount, modelCount));
                    break;
                case SigModelParser.GENERIC_LEVEL_SERVER:
                    icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_lightbulb_level_48dp));
                    groupSummary.setText(context.getResources().getQuantityString(R.plurals.dimmer_count, modelCount, modelCount));
                    break;
                default:
                    icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_help_outline_48dp));
                    binding.containerButtons.setVisibility(View.INVISIBLE);
                    groupSummary.setText(context.getResources().getQuantityString(R.plurals.device_count, modelCount, modelCount));
                    break;
            }
        }

        groupContainerCard.setOnClickListener(v -> onSubGroupItemClicked(keyIndex, modelId));

        on.setOnClickListener(v -> toggleState(keyIndex, modelId, true));
        off.setOnClickListener(v -> toggleState(keyIndex, modelId, false));

        holder.mGroupGrid.addView(binding.getRoot(), position);
    }

    private void toggleState(final int appKeyIndex, final int modelId, final boolean state) {
        mOnItemClickListener.toggle(appKeyIndex, modelId, state);
    }

    private void onSubGroupItemClicked(final int keyIndex, final int modelIdentifier) {
        mOnItemClickListener.onSubGroupItemClick(keyIndex, modelIdentifier);
    }

    @Override
    public int getItemCount() {
        if (mGroupedKeyModels == null) return 0;
        return mGroupedKeyModels.size();
    }

    @Override
    public long getItemId(final int position) {
        if (mGroupedKeyModels != null)
            mGroupedKeyModels.keyAt(position);
        return super.getItemId(position);
    }

    public int getModelCount() {
        return mModels.size();
    }

    public List<MeshModel> getModels() {
        return mModels;
    }

    private SparseArray<SparseIntArray> groupModelsBasedOnAppKeys() {
        final SparseArray<SparseIntArray> groupedKeyModels = new SparseArray<>();
        for (MeshModel model : mModels) {
            for (Integer keyIndex : model.getBoundAppKeyIndexes()) {
                final SparseIntArray sparseIntArr = groupedKeyModels.get(keyIndex, new SparseIntArray());
                final int modelIdCount = sparseIntArr.get(model.getModelId(), 0);
                sparseIntArr.put(model.getModelId(), modelIdCount + 1);
                groupedKeyModels.put(keyIndex, sparseIntArr);
            }
        }
        return groupedKeyModels;
    }

    public interface OnItemClickListener {

        void onSubGroupItemClick(final int appKeyIndex, final int modelId);

        void toggle(final int appKeyIndex, final int modelId, final boolean isChecked);

    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout groupItemContainer;
        TextView mGroupAppKeyTitle;
        GridLayout mGroupGrid;

        private ViewHolder(@NonNull final GroupedAppKeyItemBinding binding) {
            super(binding.getRoot());
            groupItemContainer = binding.groupAppKeyItemContainer;
            mGroupAppKeyTitle = binding.grpAppKeyTitle;
            mGroupGrid = binding.grpGrid;
        }
    }
}
