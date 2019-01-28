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
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class SubGroupAdapter extends RecyclerView.Adapter<SubGroupAdapter.ViewHolder> {

    private final String TAG = SubGroupAdapter.class.getSimpleName();
    private final Context mContext;
    private final MeshNetwork mMeshNetwork;
    private final List<MeshModel> models = new ArrayList<>();
    private SparseArray<SparseIntArray> mGroupedKeyModels = new SparseArray<>();
    private SparseIntArray mGroupedModels = new SparseIntArray();
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

    private void updateAdapterData() {
        final Group group = mGroup;
        models.clear();
        models.addAll(mMeshNetwork.getModels(group));
        groupModels();
        groupModelsBasedOnAppKeys();
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
                inflateView(holder, modelId, count, i);
            }
        }
    }

    private void inflateView(@NonNull final ViewHolder holder, final int modelId, final int modelCount, final int index) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.grouped_item, holder.mGroupGrid, false);
        view.setTag(modelId);
        final CardView groupContainerCard = view.findViewById(R.id.group_container_card);
        final ImageView icon = view.findViewById(R.id.icon);
        final TextView groupSummary = view.findViewById(R.id.group_summary);
        final Switch toggle = view.findViewById(R.id.switch_on_off);
        toggle.setTag(modelId);
        switch (modelId) {
            case SigModelParser.GENERIC_ON_OFF_SERVER:
                groupSummary.setText(mContext.getString(R.string.light_count, modelCount));
                groupContainerCard.setOnClickListener(v -> {
                    final int appKeyIndex = (int) holder.groupItemContainer.getTag();
                    final int modelIdentifier = (int) v.findViewById(R.id.switch_on_off).getTag();
                    onSubGroupItemClicked(appKeyIndex, modelIdentifier);
                });
                break;
            case SigModelParser.GENERIC_LEVEL_SERVER:
                groupSummary.setText(mContext.getString(R.string.dimmer_count, modelCount));
                groupContainerCard.setOnClickListener(v -> {
                    final int appKeyIndex = (int) holder.groupItemContainer.getTag();
                    final int modelIdentifier = (int) v.findViewById(R.id.switch_on_off).getTag();
                    onSubGroupItemClicked(appKeyIndex, modelIdentifier);
                });
                break;
            default:
                icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_help_outline_nordic_medium_grey_48dp));
                toggle.setVisibility(View.GONE);
                groupSummary.setText(mContext.getString(R.string.unknown_device_count, modelCount));
                break;
        }

        toggle.setOnClickListener((v -> {
            final boolean isChecked = ((Switch) v).isChecked();
            if (mIsConnected) {
                final int appKeyIndex = (int) holder.groupItemContainer.getTag();
                final int modelIdentifier = (int) v.getTag();
                mOnItemClickListener.toggle(appKeyIndex, modelIdentifier, isChecked);
            } else {
                toggle.setChecked(!isChecked);
                Toast.makeText(mContext, R.string.please_connect_to_network, Toast.LENGTH_SHORT).show();
            }
        }));

        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_lightbulb_outline_nordic_grass_48dp));
            } else {
                icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_lightbulb_outline_nordic_medium_grey_48dp));
            }
        });

        holder.mGroupGrid.addView(view, index);
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
        if (mGroupedKeyModels == null)
            return 0;
        return mGroupedKeyModels.size();
    }

    @Override
    public long getItemId(final int position) {
        if (mGroupedKeyModels != null)
            mGroupedKeyModels.keyAt(position);
        return super.getItemId(position);
    }

    /**
     * Group the models based on the model id
     */
    private void groupModels() {
        mGroupedModels.clear();
        for (MeshModel model : models) {
            final int modelCount = mGroupedModels.get(model.getModelId(), 0);
            mGroupedModels.put(model.getModelId(), modelCount + 1);
        }
    }

    private void groupModelsBasedOnAppKeys() {
        mGroupedKeyModels.clear();
        for (MeshModel model : models) {
            for (Integer keyIndex : model.getBoundAppKeyIndexes()) {
                final SparseIntArray sparseIntArr = mGroupedKeyModels.get(keyIndex, new SparseIntArray());
                final int modelIdCount = sparseIntArr.get(model.getModelId(), 0);
                sparseIntArr.put(model.getModelId(), modelIdCount + 1);
                mGroupedKeyModels.put(keyIndex, sparseIntArr);
            }
        }
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
