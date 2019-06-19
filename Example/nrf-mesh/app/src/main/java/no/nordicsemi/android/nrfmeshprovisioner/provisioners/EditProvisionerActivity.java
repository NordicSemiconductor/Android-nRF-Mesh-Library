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

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigError;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.dialogs.DialogFragmentProvisionerAddress;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.dialogs.DialogFragmentProvisionerName;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.EditProvisionerViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RangeView;

public class EditProvisionerActivity extends AppCompatActivity implements Injectable,
        DialogFragmentProvisionerName.DialogFragmentProvisionerNameListener,
        DialogFragmentProvisionerAddress.DialogFragmentAddressListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private TextView provisionerName;
    private TextView provisionerUnicast;
    private RangeView unicastRangeView;
    private RangeView groupRangeView;
    private RangeView sceneRangeView;

    private EditProvisionerViewModel mViewModel;
    private Provisioner mProvisioner;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_provisioner);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(EditProvisionerViewModel.class);

        //Bind ui
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.title_edit_provisioner);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final View containerProvisionerName = findViewById(R.id.container_name);
        containerProvisionerName.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_label_outline_black_alpha_24dp));
        ((TextView) containerProvisionerName.findViewById(R.id.title)).setText(R.string.name);
        provisionerName = containerProvisionerName.findViewById(R.id.text);
        provisionerName.setVisibility(View.VISIBLE);

        final View containerUnicast = findViewById(R.id.container_unicast);
        containerUnicast.setClickable(false);
        containerUnicast.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_index));
        ((TextView) containerUnicast.findViewById(R.id.title)).setText(R.string.title_unicast_address);
        provisionerUnicast = containerUnicast.findViewById(R.id.text);
        provisionerUnicast.setVisibility(View.VISIBLE);

        final View containerUnicastRange = findViewById(R.id.container_unicast_range);
        containerUnicastRange.setClickable(false);
        containerUnicastRange.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_lan_black_alpha_24dp));
        ((TextView) containerUnicastRange.findViewById(R.id.title)).setText(R.string.title_unicast_addresses);
        unicastRangeView = containerUnicastRange.findViewById(R.id.range_view);

        final View containerGroupRange = findViewById(R.id.container_group_range);
        containerGroupRange.setClickable(false);
        containerGroupRange.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_outline_group_work_black_alpha_24dp));
        ((TextView) containerGroupRange.findViewById(R.id.title)).setText(R.string.title_group_addresses);
        groupRangeView = containerGroupRange.findViewById(R.id.range_view);

        final View containerSceneRange = findViewById(R.id.container_scene_range);
        containerSceneRange.setClickable(false);
        containerSceneRange.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_arrow_collapse_black_alpha_24dp));
        ((TextView) containerSceneRange.findViewById(R.id.title)).setText(R.string.title_scenes);
        sceneRangeView = containerSceneRange.findViewById(R.id.range_view);

        containerProvisionerName.setOnClickListener(v -> {
            if (mProvisioner != null) {
                final DialogFragmentProvisionerName fragment = DialogFragmentProvisionerName.newInstance(mProvisioner.getProvisionerName());
                fragment.show(getSupportFragmentManager(), null);
            }
        });

        containerUnicast.setOnClickListener(v -> {
            if (mProvisioner != null) {
                final DialogFragmentProvisionerAddress fragment = DialogFragmentProvisionerAddress.newInstance(mProvisioner.getProvisionerAddress());
                fragment.show(getSupportFragmentManager(), null);
            }
        });

        containerUnicastRange.setOnClickListener(v -> {
            if (mProvisioner != null) {
                mViewModel.setSelectedProvisioner(mProvisioner);
                final Intent intent = new Intent(this, RangesActivity.class);
                intent.putExtra(Utils.RANGE_TYPE, Utils.UNICAST_RANGE);
                startActivity(intent);
            }
        });

        containerGroupRange.setOnClickListener(v -> {
            if (mProvisioner != null) {
                mViewModel.setSelectedProvisioner(mProvisioner);
                final Intent intent = new Intent(this, RangesActivity.class);
                intent.putExtra(Utils.RANGE_TYPE, Utils.GROUP_RANGE);
                startActivity(intent);
            }
        });

        containerSceneRange.setOnClickListener(v -> {
            if (mProvisioner != null) {
                mViewModel.setSelectedProvisioner(mProvisioner);
                final Intent intent = new Intent(this, RangesActivity.class);
                intent.putExtra(Utils.RANGE_TYPE, Utils.SCENE_RANGE);
                startActivity(intent);
            }
        });

        mViewModel.getSelectedProvisioner().observe(this, provisioner -> {
            mProvisioner = provisioner;
            updateUi(provisioner);
        });
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
        if (save()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNameChanged(@NonNull final String name) {
        if (mProvisioner != null) {
            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if (network != null) {
                mProvisioner.setProvisionerName(name);
                if (network.updateProvisioner(mProvisioner)) {
                    provisionerName.setText(mProvisioner.getProvisionerName());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean setAddress(final int sourceAddress) {
        if (mProvisioner != null) {
            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if (network != null) {
                mProvisioner.setProvisionerAddress(sourceAddress);
                if (network.updateProvisioner(mProvisioner)) {
                    provisionerUnicast.setText(MeshAddress.formatAddress(mProvisioner.getProvisionerAddress(), true));
                    return true;
                }
            }
        }
        return false;
    }

    private void updateUi(@NonNull final Provisioner provisioner) {
        provisionerName.setText(provisioner.getProvisionerName());
        if (provisioner.getProvisionerAddress() == 0) {
            provisionerUnicast.setText(R.string.not_assigned);
        } else {
            provisionerUnicast.setText(MeshAddress.formatAddress(provisioner.getProvisionerAddress(), true));
        }

        unicastRangeView.addRanges(provisioner.getAllocatedUnicastRanges());
        groupRangeView.addRanges(provisioner.getAllocatedGroupRanges());
        sceneRangeView.addRanges(provisioner.getAllocatedSceneRanges());

        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            for (Provisioner other : network.getProvisioners()) {
                if (!other.getProvisionerUuid().equalsIgnoreCase(provisioner.getProvisionerUuid())) {
                    unicastRangeView.addOtherRanges(other.getAllocatedUnicastRanges());
                    groupRangeView.addOtherRanges(other.getAllocatedGroupRanges());
                    sceneRangeView.addOtherRanges(other.getAllocatedSceneRanges());
                }
            }
        }
    }

    private boolean save() {
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            try {
                return network.updateProvisioner(mProvisioner);
            } catch (IllegalArgumentException ex) {
                final DialogFragmentConfigError fragment = DialogFragmentConfigError.
                        newInstance(getString(R.string.title_error), ex.getMessage());
                fragment.show(getSupportFragmentManager(), null);
            }
        }
        return false;
    }
}
