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

package no.nordicsemi.android.nrfmeshprovisioner.provisioners;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.AllocatedGroupRange;
import no.nordicsemi.android.meshprovisioner.AllocatedSceneRange;
import no.nordicsemi.android.meshprovisioner.AllocatedUnicastRange;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.Range;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.adapter.RangeAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.dialogs.DialogFragmentGroupRange;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.dialogs.DialogFragmentSceneRange;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.dialogs.DialogFragmentUnicastRange;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.RangesViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RangeView;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class RangesActivity extends AppCompatActivity implements Injectable,
        RangeAdapter.OnItemClickListener,
        ItemTouchHelperAdapter,
        RangeListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    //UI Bindings
    @BindView(android.R.id.empty)
    View mEmptyView;
    @BindView(R.id.container)
    View container;
    @BindView(R.id.fab_resolve)
    ExtendedFloatingActionButton mFabResolve;
    private RangeView mRangeView;
    private int mType;
    private RangesViewModel mViewModel;
    private RangeAdapter mRangeAdapter;
    private Provisioner mProvisioner;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranges);
        ButterKnife.bind(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(RangesViewModel.class);
        mType = getIntent().getExtras().getInt(Utils.RANGE_TYPE);
        //Bind ui
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        final View rangesContainer = findViewById(R.id.info_ranges);
        rangesContainer.findViewById(R.id.container).setVisibility(View.VISIBLE);
        mRangeView = rangesContainer.findViewById(R.id.range);
        final TextView startAddress = rangesContainer.findViewById(R.id.start_address);
        final TextView endAddress = rangesContainer.findViewById(R.id.end_address);
        final ExtendedFloatingActionButton fab_add = findViewById(R.id.fab_add);

        mProvisioner = mViewModel.getSelectedProvisioner().getValue();

        switch (mType) {
            case Utils.GROUP_RANGE:
                startAddress.setText(MeshAddress.formatAddress(MeshAddress.START_GROUP_ADDRESS, true));
                endAddress.setText(MeshAddress.formatAddress(MeshAddress.END_GROUP_ADDRESS, true));
                getSupportActionBar().setTitle(R.string.title_edit_group_ranges);
                mRangeAdapter = new RangeAdapter(this, mProvisioner.getAllocatedGroupRanges(), mViewModel.getMeshNetworkLiveData().getProvisioners());
                break;
            case Utils.SCENE_RANGE:
                startAddress.setText(MeshAddress.formatAddress(0x0000, true));
                endAddress.setText(MeshAddress.formatAddress(0xFFFF, true));
                getSupportActionBar().setTitle(R.string.title_edit_scene_ranges);
                mRangeAdapter = new RangeAdapter(this, mProvisioner.getAllocatedSceneRanges(), mViewModel.getMeshNetworkLiveData().getProvisioners());
                break;
            default:
            case Utils.UNICAST_RANGE:
                startAddress.setText(MeshAddress.formatAddress(MeshAddress.START_UNICAST_ADDRESS, true));
                endAddress.setText(MeshAddress.formatAddress(MeshAddress.END_UNICAST_ADDRESS, true));
                getSupportActionBar().setTitle(R.string.title_edit_unicast_ranges);
                mRangeAdapter = new RangeAdapter(this, mProvisioner.getAllocatedUnicastRanges(), mViewModel.getMeshNetworkLiveData().getProvisioners());
                break;
        }

        mRangeAdapter.setOnItemClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mRangeAdapter);
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        fab_add.setOnClickListener(v -> {
            switch (mType) {
                case Utils.GROUP_RANGE:
                    final DialogFragmentGroupRange groupRange = DialogFragmentGroupRange.newInstance(null);
                    groupRange.show(getSupportFragmentManager(), null);
                    break;
                case Utils.SCENE_RANGE:
                    final DialogFragmentSceneRange sceneRange = DialogFragmentSceneRange.newInstance(null);
                    sceneRange.show(getSupportFragmentManager(), null);
                    break;
                default:
                case Utils.UNICAST_RANGE:
                    final DialogFragmentUnicastRange unicastRange = DialogFragmentUnicastRange.newInstance(null);
                    unicastRange.show(getSupportFragmentManager(), null);
                    break;
            }
        });

        mFabResolve.setOnClickListener(v -> resolveRanges());

        updateRanges();
        updateOtherRanges();
        updateResolveFab();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mViewModel.setSelectedProvisioner(mProvisioner);
    }

    @Override
    public void onItemClick(final int position, @NonNull final Range range) {
        if (range instanceof AllocatedUnicastRange) {
            final DialogFragmentUnicastRange fragment = DialogFragmentUnicastRange.newInstance((AllocatedUnicastRange) range);
            fragment.show(getSupportFragmentManager(), null);
        } else if (range instanceof AllocatedGroupRange) {
            final DialogFragmentGroupRange fragment = DialogFragmentGroupRange.newInstance((AllocatedGroupRange) range);
            fragment.show(getSupportFragmentManager(), null);
        } else {
            final DialogFragmentSceneRange fragment = DialogFragmentSceneRange.newInstance((AllocatedSceneRange) range);
            fragment.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public void addRange(@NonNull final Range range) {
        final Provisioner provisioner = mViewModel.getSelectedProvisioner().getValue();
        if (provisioner != null) {
            provisioner.addRange(range);
            updateData(range);
            updateRanges();
            updateOtherRanges();
            updateResolveFab();
        }
    }

    private void updateRanges() {
        if (mProvisioner != null) {
            mRangeView.clearRanges();
            switch (mType) {
                case Utils.GROUP_RANGE:
                    mRangeView.addRanges(mProvisioner.getAllocatedGroupRanges());
                    break;
                case Utils.SCENE_RANGE:
                    mRangeView.addRanges(mProvisioner.getAllocatedSceneRanges());
                    break;
                default:
                case Utils.UNICAST_RANGE:
                    mRangeView.addRanges(mProvisioner.getAllocatedUnicastRanges());
                    break;
            }
        }
    }

    private void updateOtherRanges() {
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            mRangeView.clearOtherRanges();
            for (Provisioner other : network.getProvisioners()) {
                if (!other.getProvisionerUuid().equalsIgnoreCase(mProvisioner.getProvisionerUuid()))
                    switch (mType) {
                        case Utils.GROUP_RANGE:
                            mRangeView.addOtherRanges(other.getAllocatedGroupRanges());
                            break;
                        case Utils.SCENE_RANGE:
                            mRangeView.addOtherRanges(other.getAllocatedSceneRanges());
                            break;
                        default:
                        case Utils.UNICAST_RANGE:
                            mRangeView.addOtherRanges(other.getAllocatedUnicastRanges());
                            break;
                    }
            }
        }
    }

    private void updateResolveFab() {
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            for (Provisioner other : network.getProvisioners()) {
                if (!other.getProvisionerUuid().equalsIgnoreCase(mProvisioner.getProvisionerUuid()))
                    switch (mType) {
                        case Utils.GROUP_RANGE:
                            if (mProvisioner.hasOverlappingGroupRanges(other.getAllocatedGroupRanges())) {
                                mFabResolve.show(true);
                            } else {
                                mFabResolve.hide(true);
                            }
                            break;
                        case Utils.SCENE_RANGE:
                            if (mProvisioner.hasOverlappingSceneRanges(other.getAllocatedSceneRanges())) {
                                mFabResolve.show(true);
                            } else {
                                mFabResolve.hide(true);
                            }
                            break;
                        default:
                        case Utils.UNICAST_RANGE:
                            if (mProvisioner.hasOverlappingUnicastRanges(other.getAllocatedUnicastRanges())) {
                                mFabResolve.show(true);
                            } else {
                                mFabResolve.hide(true);
                            }
                            break;
                    }
            }
        }
    }

    private void updateData(@NonNull final Range range) {
        if (mProvisioner != null) {
            if (range instanceof AllocatedUnicastRange) {
                mRangeAdapter.updateData(mProvisioner.getAllocatedUnicastRanges());
            } else if (range instanceof AllocatedGroupRange) {
                mRangeAdapter.updateData(mProvisioner.getAllocatedGroupRanges());
            } else {
                mRangeAdapter.updateData(mProvisioner.getAllocatedSceneRanges());
            }
        }
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        if (mProvisioner != null) {
            final int position = viewHolder.getAdapterPosition();
            try {
                final Range range = mRangeAdapter.getItem(position);
                mRangeAdapter.removeItem(position);
                displaySnackBar(position, range);
                // Show the empty view
                if (mRangeAdapter.isEmpty()) {
                    mFabResolve.hide();
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            } catch (Exception ex) {
                mRangeAdapter.notifyDataSetChanged();
                displaySnackBar(ex.getMessage());
            }
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {

    }

    private void displaySnackBar(final int position, final Range range) {
        Snackbar.make(container, getString(R.string.range_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), view -> {
                    mEmptyView.setVisibility(View.GONE);
                    mRangeAdapter.addItem(position, range);
                })
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryDark))
                .show();
    }

    private void displaySnackBar(final String message) {
        Snackbar.make(container, message, Snackbar.LENGTH_LONG)
                .show();
    }

    private void resolveRanges() {
        switch (mType) {
            case Utils.GROUP_RANGE:
                removeConflictingGroupRanges();
                break;
            case Utils.SCENE_RANGE:
                final DialogFragmentSceneRange sceneRange = DialogFragmentSceneRange.newInstance(null);
                sceneRange.show(getSupportFragmentManager(), null);
                break;
            default:
            case Utils.UNICAST_RANGE:
                removeConflictingUnicastRanges();
                break;
        }
    }

    private void removeConflictingUnicastRanges() {
        if (mProvisioner != null) {
            final List<AllocatedUnicastRange> ranges = new ArrayList<>(mProvisioner.getAllocatedUnicastRanges());
            for (int i = 0; i < ranges.size(); i++) {
                AllocatedUnicastRange range = ranges.get(i);
                for (Provisioner p : mViewModel.getMeshNetworkLiveData().getProvisioners()) {
                    for (AllocatedUnicastRange otherRange : p.getAllocatedUnicastRanges()) {
                        final AllocatedUnicastRange result = range.minus(otherRange);
                        if (result != null) {
                            ranges.set(i, range);
                        }
                    }
                }
            }
            mProvisioner.setAllocatedUnicastRanges(ranges);
            mRangeAdapter.updateData(ranges);
            updateRanges();
            updateOtherRanges();
            updateResolveFab();
        }
    }

    private void removeConflictingGroupRanges() {
        if (mProvisioner != null) {
            final List<AllocatedGroupRange> ranges = new ArrayList<>(mProvisioner.getAllocatedGroupRanges());
            for (int i = 0; i < ranges.size(); i++) {
                AllocatedGroupRange range = ranges.get(i);
                for (Provisioner p : mViewModel.getMeshNetworkLiveData().getProvisioners()) {
                    for (AllocatedGroupRange otherRange : p.getAllocatedGroupRanges()) {
                        range = range.minus(otherRange);
                        ranges.set(i, range);
                    }
                }
            }
            mProvisioner.setAllocatedGroupRanges(ranges);
            mRangeAdapter.updateData(ranges);
            updateRanges();
            updateOtherRanges();
            updateResolveFab();
        }
    }
}
