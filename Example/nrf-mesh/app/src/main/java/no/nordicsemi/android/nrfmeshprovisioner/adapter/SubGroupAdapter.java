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

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.utils.CompanyIdentifiers;
import no.nordicsemi.android.meshprovisioner.utils.CompositionDataParser;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class SubGroupAdapter extends RecyclerView.Adapter<SubGroupAdapter.ViewHolder> {

    private final Context mContext;
    private MeshNetwork mMeshNetwork;
    private List<MeshModel> mModels;
    private SparseArray<SparseIntArray> mGroupedKeyModels;
    private boolean mIsConnected = false;
    private Group mGroup;
    private OnItemClickListener mOnItemClickListener;

    public SubGroupAdapter(@NonNull final Context context,
                           @NonNull final MeshNetwork network,
                           @NonNull final LiveData<Group> groupedModels,
                           @NonNull final LiveData<Boolean> connectedState) {
        this.mContext = context;
        this.mMeshNetwork = network;
        groupedModels.observe((LifecycleOwner) context, group -> {
            mGroup = group;
            updateAdapterData();
        });

        connectedState.observe((LifecycleOwner) context, isConnected -> {
            if (isConnected != null) {
                this.mIsConnected = isConnected;
                updateAdapterData();
            }
        });
    }

    public void updateAdapterData() {
        final Group group = mGroup;
        mModels = mMeshNetwork.getModels(group);
        mGroupedKeyModels = groupModelsBasedOnAppKeys();
        notifyDataSetChanged();
    }


    public void setOnItemClickListener(final SubGroupAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View layoutView = LayoutInflater.from(mContext).inflate(R.layout.grouped_app_key_item, parent, false);
        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (mGroupedKeyModels.size() > 0) {
            final int keyIndex = mGroupedKeyModels.keyAt(position);
            holder.groupItemContainer.setTag(keyIndex);
            final ApplicationKey key = mMeshNetwork.getAppKey(keyIndex);
            final String keyTitle = key.getName() + " " + keyIndex;
            holder.mGroupAppKeyTitle.setText(keyTitle);
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
        final View view = LayoutInflater.from(mContext).inflate(R.layout.grouped_item, holder.mGroupGrid, false);
        final CardView groupContainerCard = view.findViewById(R.id.group_container_card);
        final ImageView icon = view.findViewById(R.id.icon);
        final TextView groupSummary = view.findViewById(R.id.group_summary);
        final Button on = view.findViewById(R.id.action_on);
        final Button off = view.findViewById(R.id.action_off);
        if (MeshParserUtils.isVendorModel(modelId)) {
            icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_domain_nordic_medium_gray_48dp));
            view.findViewById(R.id.container_buttons).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.container_vendor).setVisibility(View.VISIBLE);
            final TextView modelIdView = view.findViewById(R.id.model_id);
            modelIdView.setText(CompositionDataParser.formatModelIdentifier(modelId, true));
            final TextView companyIdView = view.findViewById(R.id.company_id);
            final int companyIdentifier = MeshParserUtils.getCompanyIdentifier(modelId);
            companyIdView.setText(String.valueOf(CompanyIdentifiers.getCompanyName((short) companyIdentifier)));
            groupSummary.setText(mContext.getString(R.string.unknown_device_count, modelCount));
        } else {
            switch (modelId) {
                case SigModelParser.GENERIC_ON_OFF_SERVER:
                    groupSummary.setText(mContext.getString(R.string.light_count, modelCount));
                    break;
                case SigModelParser.GENERIC_ON_OFF_CLIENT:
                    icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_light_switch_nordic_medium_grey_48dp));
                    view.findViewById(R.id.container_buttons).setVisibility(View.INVISIBLE);
                    groupSummary.setText(mContext.getString(R.string.switch_count, modelCount));
                    break;
                case SigModelParser.GENERIC_LEVEL_SERVER:
                    icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_lightbulb_level_nordic_sun_outline_48dp));
                    groupSummary.setText(mContext.getString(R.string.dimmer_count, modelCount));
                    break;
                default:
                    icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_help_outline_nordic_medium_grey_48dp));
                    view.findViewById(R.id.container_buttons).setVisibility(View.INVISIBLE);
                    groupSummary.setText(mContext.getString(R.string.unknown_device_count, modelCount));
                    break;
            }
        }

        groupContainerCard.setOnClickListener(v -> {
            onSubGroupItemClicked(keyIndex, modelId);
        });

        on.setOnClickListener(v -> toggleState(keyIndex, modelId, true));
        off.setOnClickListener(v -> toggleState(keyIndex, modelId, false));

        holder.mGroupGrid.addView(view, position);
    }

    private void toggleState(final int appKeyIndex, final int modelId, final boolean state) {
        if (mIsConnected) {
            mOnItemClickListener.toggle(appKeyIndex, modelId, state);
        } else {
            Toast.makeText(mContext, R.string.please_connect_to_network, Toast.LENGTH_SHORT).show();
        }
    }

    private void onSubGroupItemClicked(final int keyIndex, final int modelIdentifier) {
        if (mIsConnected) {
            mOnItemClickListener.onSubGroupItemClick(keyIndex, modelIdentifier);
        } else {
            Toast.makeText(mContext, R.string.please_connect_to_network, Toast.LENGTH_SHORT).show();
        }
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

    public int getModelCount(){
        return mModels.size();
    }

    public List<MeshModel> getModels(){
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

    final class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.group_app_key_item_container)
        ConstraintLayout groupItemContainer;
        @BindView(R.id.grp_app_key_title)
        TextView mGroupAppKeyTitle;
        @BindView(R.id.grp_grid)
        GridLayout mGroupGrid;

        private ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
