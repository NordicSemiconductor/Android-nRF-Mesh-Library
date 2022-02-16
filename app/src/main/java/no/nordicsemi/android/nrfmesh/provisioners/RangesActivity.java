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

package no.nordicsemi.android.nrfmesh.provisioners;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.AddressRange;
import no.nordicsemi.android.mesh.AllocatedGroupRange;
import no.nordicsemi.android.mesh.AllocatedSceneRange;
import no.nordicsemi.android.mesh.AllocatedUnicastRange;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.Provisioner;
import no.nordicsemi.android.mesh.Range;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityRangesBinding;
import no.nordicsemi.android.nrfmesh.provisioners.adapter.RangeAdapter;
import no.nordicsemi.android.nrfmesh.provisioners.dialogs.DialogFragmentGroupRange;
import no.nordicsemi.android.nrfmesh.provisioners.dialogs.DialogFragmentSceneRange;
import no.nordicsemi.android.nrfmesh.provisioners.dialogs.DialogFragmentUnicastRange;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.RangesViewModel;
import no.nordicsemi.android.nrfmesh.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

@AndroidEntryPoint
public class RangesActivity extends AppCompatActivity implements
        RangeAdapter.OnItemClickListener,
        ItemTouchHelperAdapter,
        RangeListener {

    private ActivityRangesBinding binding;
    private RangesViewModel mViewModel;

    private int mType;
    private RangeAdapter<? extends Range> mRangeAdapter;
    private Provisioner provisioner;

    private final Comparator<AddressRange> addressRangeComparator = (addressRange1, addressRange2) ->
            Integer.compare(addressRange1.getLowAddress(), addressRange2.getLowAddress());

    private final Comparator<AllocatedSceneRange> sceneRangeComparator = (sceneRange1, sceneRange2) ->
            Integer.compare(sceneRange1.getFirstScene(), sceneRange2.getFirstScene());

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRangesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mViewModel = new ViewModelProvider(this).get(RangesViewModel.class);
        mType = getIntent().getIntExtra(Utils.RANGE_TYPE, Utils.UNICAST_RANGE);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.infoRanges.getRoot().setVisibility(View.VISIBLE);

        provisioner = mViewModel.getSelectedProvisioner().getValue();
        if (provisioner != null) {
            switch (mType) {
                case Utils.GROUP_RANGE:
                    binding.infoRanges.startAddress.setText(MeshAddress.formatAddress(MeshAddress.START_GROUP_ADDRESS, true));
                    binding.infoRanges.endAddress.setText(MeshAddress.formatAddress(MeshAddress.END_GROUP_ADDRESS, true));
                    getSupportActionBar().setTitle(R.string.title_edit_group_ranges);
                    mRangeAdapter = new RangeAdapter<>(provisioner.getProvisionerUuid(),
                            provisioner.getAllocatedGroupRanges(),
                            mViewModel.getNetworkLiveData().getProvisioners());
                    break;
                case Utils.SCENE_RANGE:
                    binding.infoRanges.startAddress.setText(MeshAddress.formatAddress(0x0000, true));
                    binding.infoRanges.endAddress.setText(MeshAddress.formatAddress(0xFFFF, true));
                    getSupportActionBar().setTitle(R.string.title_edit_scene_ranges);
                    mRangeAdapter = new RangeAdapter<>(provisioner.getProvisionerUuid(),
                            provisioner.getAllocatedSceneRanges(),
                            mViewModel.getNetworkLiveData().getProvisioners());
                    break;
                default:
                case Utils.UNICAST_RANGE:
                    binding.infoRanges.startAddress.setText(MeshAddress.formatAddress(MeshAddress.START_UNICAST_ADDRESS, true));
                    binding.infoRanges.endAddress.setText(MeshAddress.formatAddress(MeshAddress.END_UNICAST_ADDRESS, true));
                    getSupportActionBar().setTitle(R.string.title_edit_unicast_ranges);
                    mRangeAdapter = new RangeAdapter<>(provisioner.getProvisionerUuid(),
                            provisioner.getAllocatedUnicastRanges(),
                            mViewModel.getNetworkLiveData().getProvisioners());
                    break;
            }
        } else {
            finish();
        }
        mRangeAdapter.setOnItemClickListener(this);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(binding.recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        binding.recyclerView.addItemDecoration(dividerItemDecoration);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(mRangeAdapter);
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);

        binding.fabAdd.setOnClickListener(v -> {
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
        binding.fabResolve.setOnClickListener(v -> resolveRanges());
        updateUi();
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
        if (provisioner != null) {
            mViewModel.setSelectedProvisioner(provisioner);
        }
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
        if (provisioner != null) {
            provisioner.addRange(range);
            updateData(range);
            updateUi();
        }
    }

    @Override
    public void updateRange(@NonNull final Range oldRange, final Range newRange) {
        if (provisioner != null) {
            provisioner.updateRange(oldRange, newRange);
            updateData(oldRange);
            updateUi();
        }
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        if (provisioner != null) {
            final int position = viewHolder.getAbsoluteAdapterPosition();
            try {
                final Range range = mRangeAdapter.getItem(position);
                //mRangeAdapter.removeItem(position);
                displaySnackBar(range);
                provisioner.removeRange(range);
                updateData(range);
                updateUi();
            } catch (Exception ex) {
                mViewModel.displaySnackBar(this, binding.container,
                        ex.getMessage() == null ? getString(R.string.unknwon_error) : ex.getMessage(), Snackbar.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {

    }

    private void updateRanges() {
        if (provisioner != null) {
            binding.infoRanges.range.clearRanges();
            switch (mType) {
                case Utils.GROUP_RANGE:
                    binding.infoRanges.range.addRanges(provisioner.getAllocatedGroupRanges());
                    break;
                case Utils.SCENE_RANGE:
                    binding.infoRanges.range.addRanges(provisioner.getAllocatedSceneRanges());
                    break;
                default:
                case Utils.UNICAST_RANGE:
                    binding.infoRanges.range.addRanges(provisioner.getAllocatedUnicastRanges());
                    break;
            }
        }
    }

    private void updateOtherRanges() {
        if (provisioner != null) {
            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if (network != null) {
                binding.infoRanges.range.clearOtherRanges();
                for (Provisioner other : network.getProvisioners()) {
                    if (!other.getProvisionerUuid().equalsIgnoreCase(provisioner.getProvisionerUuid()))
                        switch (mType) {
                            case Utils.GROUP_RANGE:
                                binding.infoRanges.range.addOtherRanges(other.getAllocatedGroupRanges());
                                break;
                            case Utils.SCENE_RANGE:
                                binding.infoRanges.range.addOtherRanges(other.getAllocatedSceneRanges());
                                break;
                            default:
                            case Utils.UNICAST_RANGE:
                                binding.infoRanges.range.addOtherRanges(other.getAllocatedUnicastRanges());
                                break;
                        }
                }
            }
        }
    }

    private void updateResolveFab() {
        if (provisioner != null) {
            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if (network != null) {
                for (Provisioner other : network.getProvisioners()) {
                    if (!other.getProvisionerUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())){
                        switch (mType) {
                            case Utils.GROUP_RANGE:
                                if (provisioner.hasOverlappingGroupRanges(other.getAllocatedGroupRanges())) {
                                    binding.fabResolve.show();
                                    return;
                                } else {
                                    binding.fabResolve.hide();
                                }
                                break;
                            case Utils.SCENE_RANGE:
                                if (provisioner.hasOverlappingSceneRanges(other.getAllocatedSceneRanges())) {
                                    binding.fabResolve.show();
                                    return;
                                } else {
                                    binding.fabResolve.hide();
                                }
                                break;
                            default:
                            case Utils.UNICAST_RANGE:
                                if (provisioner.hasOverlappingUnicastRanges(other.getAllocatedUnicastRanges())) {
                                    binding.fabResolve.show();
                                    return;
                                } else {
                                    binding.fabResolve.hide();
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    private void updateEmptyView() {
        // Show the empty view
        if (mRangeAdapter.isEmpty()) {
            binding.empty.getRoot().setVisibility(View.VISIBLE);
        } else {
            binding.empty.getRoot().setVisibility(View.GONE);
        }
    }

    private void updateData(@NonNull final Range range) {
        if (provisioner != null) {
            if (range instanceof AllocatedUnicastRange) {
                mRangeAdapter.updateData(provisioner.getAllocatedUnicastRanges());
            } else if (range instanceof AllocatedGroupRange) {
                mRangeAdapter.updateData(provisioner.getAllocatedGroupRanges());
            } else {
                mRangeAdapter.updateData(provisioner.getAllocatedSceneRanges());
            }
        }
    }

    private void displaySnackBar(@NonNull final Range range) {
        if (provisioner != null) {
            Snackbar.make(binding.container, getString(R.string.range_deleted), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.undo), view -> {
                        //mRangeAdapter.addItem(position, range);
                        provisioner.addRange(range);
                        updateData(range);
                        updateUi();
                    })
                    .setActionTextColor(getResources().getColor(R.color.colorSecondary))
                    .show();
        }
    }

    private void resolveRanges() {
        switch (mType) {
            case Utils.GROUP_RANGE:
                removeConflictingGroupRanges();
                break;
            case Utils.SCENE_RANGE:
                removeConflictingSceneRanges();
                break;
            default:
            case Utils.UNICAST_RANGE:
                removeConflictingUnicastRanges();
                break;
        }
    }

    private void removeConflictingUnicastRanges() {
        if (provisioner != null) {
            List<AllocatedUnicastRange> ranges = new ArrayList<>(provisioner.getAllocatedUnicastRanges());
            Collections.sort(ranges, addressRangeComparator);
            for (Provisioner p : mViewModel.getNetworkLiveData().getProvisioners()) {
                if (!p.getProvisionerUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                    final List<AllocatedUnicastRange> otherRanges = new ArrayList<>(p.getAllocatedUnicastRanges());
                    Collections.sort(otherRanges, addressRangeComparator);
                    for (AllocatedUnicastRange otherRange : otherRanges) {
                        ranges = AddressRange.minus(ranges, otherRange);
                    }
                }
            }
            mRangeAdapter.updateData(ranges);
            provisioner.setAllocatedUnicastRanges(ranges);
            updateUi();
        }
    }

    private void removeConflictingGroupRanges() {
        if (provisioner != null) {
            List<AllocatedGroupRange> ranges = new ArrayList<>(provisioner.getAllocatedGroupRanges());
            Collections.sort(ranges, addressRangeComparator);
            for (Provisioner p : mViewModel.getNetworkLiveData().getProvisioners()) {
                if (!p.getProvisionerUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                    final List<AllocatedGroupRange> otherRanges = new ArrayList<>(p.getAllocatedGroupRanges());
                    Collections.sort(otherRanges, addressRangeComparator);
                    for (AllocatedGroupRange otherRange : otherRanges) {
                        ranges = AddressRange.minus(ranges, otherRange);
                    }
                }
            }
            Collections.sort(ranges, addressRangeComparator);
            provisioner.setAllocatedGroupRanges(ranges);
            mRangeAdapter.updateData(ranges);
            updateUi();
        }
    }

    private void removeConflictingSceneRanges() {
        if (provisioner != null) {
            List<AllocatedSceneRange> ranges = new ArrayList<>(provisioner.getAllocatedSceneRanges());
            Collections.sort(ranges, sceneRangeComparator);
            for (Provisioner p : mViewModel.getNetworkLiveData().getProvisioners()) {
                if (!p.getProvisionerUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                    final List<AllocatedSceneRange> otherRanges = new ArrayList<>(p.getAllocatedSceneRanges());
                    Collections.sort(otherRanges, sceneRangeComparator);
                    for (AllocatedSceneRange otherRange : otherRanges) {
                        ranges = AllocatedSceneRange.minus(ranges, otherRange);
                    }
                }
            }

            Collections.sort(ranges, sceneRangeComparator);
            provisioner.setAllocatedSceneRanges(ranges);
            mRangeAdapter.updateData(ranges);
            updateUi();
        }
    }

    private void updateUi() {
        updateRanges();
        updateOtherRanges();
        updateEmptyView();
        updateResolveFab();
    }
}
