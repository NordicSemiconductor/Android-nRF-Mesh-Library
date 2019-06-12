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

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RangeView;

public class RangesActivity extends AppCompatActivity implements Injectable,
        RangeAdapter.OnItemClickListener,
        RangeListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private RangeView mRangeView;
    private int mType;
    private RangesViewModel mViewModel;
    private RangeAdapter mRangeAdapter;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranges);
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
        final ExtendedFloatingActionButton fab_resolve = findViewById(R.id.fab_resolve);

        final Provisioner provisioner = mViewModel.getSelectedProvisioner().getValue();
        switch (mType) {
            case Utils.GROUP_RANGE:
                startAddress.setText(MeshAddress.formatAddress(MeshAddress.START_GROUP_ADDRESS, true));
                endAddress.setText(MeshAddress.formatAddress(MeshAddress.END_GROUP_ADDRESS, true));
                getSupportActionBar().setTitle(R.string.title_edit_group_ranges);
                mRangeAdapter = new RangeAdapter(this, provisioner.getAllocatedGroupRanges(), mViewModel.getMeshNetworkLiveData().getProvisioners());
                break;
            case Utils.SCENE_RANGE:
                startAddress.setText(MeshAddress.formatAddress(0x0000, true));
                endAddress.setText(MeshAddress.formatAddress(0xFFFF, true));
                getSupportActionBar().setTitle(R.string.title_edit_scene_ranges);
                mRangeAdapter = new RangeAdapter(this, provisioner.getAllocatedSceneRanges(), mViewModel.getMeshNetworkLiveData().getProvisioners());
                break;
            default:
            case Utils.UNICAST_RANGE:
                startAddress.setText(MeshAddress.formatAddress(MeshAddress.START_UNICAST_ADDRESS, true));
                endAddress.setText(MeshAddress.formatAddress(MeshAddress.END_UNICAST_ADDRESS, true));
                getSupportActionBar().setTitle(R.string.title_edit_unicast_ranges);
                mRangeAdapter = new RangeAdapter(this, provisioner.getAllocatedUnicastRanges(), mViewModel.getMeshNetworkLiveData().getProvisioners());
                break;
        }

        mRangeAdapter.setOnItemClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mRangeAdapter);

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

        fab_resolve.setOnClickListener(v -> {

        });

        updateRangesBar();
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
            updateRangesBar();
        }
    }

    private void updateRangesBar() {
        updateRanges();
        updateOtherRanges();
    }

    private void updateRanges() {
        final Provisioner provisioner = mViewModel.getSelectedProvisioner().getValue();
        mRangeView.clearRanges();
        if (provisioner != null) {
            switch (mType) {
                case Utils.GROUP_RANGE:
                    mRangeView.addRanges(provisioner.getAllocatedGroupRanges());
                    break;
                case Utils.SCENE_RANGE:
                    mRangeView.addRanges(provisioner.getAllocatedSceneRanges());
                    break;
                default:
                case Utils.UNICAST_RANGE:
                    mRangeView.addRanges(provisioner.getAllocatedUnicastRanges());
                    break;
            }
        }
    }

    private void updateOtherRanges() {
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            mRangeView.clearOtherRanges();
            for (Provisioner provisioner : network.getProvisioners()) {
                switch (mType) {
                    case Utils.GROUP_RANGE:
                        mRangeView.addOtherRanges(provisioner.getAllocatedGroupRanges());
                        break;
                    case Utils.SCENE_RANGE:
                        mRangeView.addOtherRanges(provisioner.getAllocatedSceneRanges());
                        break;
                    default:
                    case Utils.UNICAST_RANGE:
                        mRangeView.addOtherRanges(provisioner.getAllocatedUnicastRanges());
                        break;
                }
            }
        }
    }

    private void updateData(@NonNull final Range range) {
        final Provisioner provisioner = mViewModel.getSelectedProvisioner().getValue();
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
}
