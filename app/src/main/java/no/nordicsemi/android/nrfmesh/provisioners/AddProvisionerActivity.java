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

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.AllocatedGroupRange;
import no.nordicsemi.android.mesh.AllocatedSceneRange;
import no.nordicsemi.android.mesh.AllocatedUnicastRange;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.Provisioner;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityEditProvisionerBinding;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.provisioners.dialogs.DialogFragmentProvisionerAddress;
import no.nordicsemi.android.nrfmesh.provisioners.dialogs.DialogFragmentProvisionerName;
import no.nordicsemi.android.nrfmesh.provisioners.dialogs.DialogFragmentTtl;
import no.nordicsemi.android.nrfmesh.provisioners.dialogs.DialogFragmentUnassign;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.AddProvisionerViewModel;

@AndroidEntryPoint
public class AddProvisionerActivity extends AppCompatActivity implements
        DialogFragmentProvisionerName.DialogFragmentProvisionerNameListener,
        DialogFragmentTtl.DialogFragmentTtlListener,
        DialogFragmentProvisionerAddress.ProvisionerAddressListener,
        DialogFragmentUnassign.DialogFragmentUnassignListener {

    private ActivityEditProvisionerBinding binding;
    private AddProvisionerViewModel mViewModel;
    private Provisioner mProvisioner;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProvisionerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mViewModel = new ViewModelProvider(this).get(AddProvisionerViewModel.class);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(R.string.title_add_provisioner);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        binding.containerName.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_label_outline));
        binding.containerName.title.setText(R.string.name);
        binding.containerName.text.setVisibility(View.VISIBLE);

        binding.containerUnicast.getRoot().setClickable(false);
        binding.containerUnicast.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_index));
        binding.containerUnicast.title.setText(R.string.title_unicast_address);
        binding.containerUnicast.text.setVisibility(View.VISIBLE);

        binding.containerTtl.getRoot().setClickable(false);
        binding.containerTtl.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_timer));
        binding.containerTtl.title.setText(R.string.title_ttl);
        binding.containerTtl.text.setVisibility(View.VISIBLE);

        binding.checkProvisioner.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mProvisioner != null) {
                mProvisioner.setLastSelected(isChecked);
            }
        });

        binding.containerUnicastRange.getRoot().setClickable(false);
        binding.containerUnicastRange.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_lan_24dp));
        binding.containerUnicastRange.title.setText(R.string.title_unicast_addresses);

        binding.containerGroupRange.getRoot().setClickable(false);
        binding.containerGroupRange.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_outline_group_24dp));
        binding.containerGroupRange.title.setText(R.string.title_group_addresses);

        binding.containerSceneRange.getRoot().setClickable(false);
        binding.containerSceneRange.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_scene));
        binding.containerSceneRange.title.setText(R.string.title_scenes);

        binding.containerName.getRoot().setOnClickListener(v -> {
            if (mProvisioner != null) {
                final DialogFragmentProvisionerName fragment = DialogFragmentProvisionerName.newInstance(mProvisioner.getProvisionerName());
                fragment.show(getSupportFragmentManager(), null);
            }
        });

        binding.containerUnicast.getRoot().setOnClickListener(v -> {
            if (mProvisioner != null) {
                final DialogFragmentProvisionerAddress fragment = DialogFragmentProvisionerAddress.newInstance(mProvisioner.getProvisionerAddress());
                fragment.show(getSupportFragmentManager(), null);
            }
        });

        binding.containerTtl.getRoot().setOnClickListener(v -> {
            if (mProvisioner != null) {
                final DialogFragmentTtl fragment = DialogFragmentTtl.newInstance(mProvisioner.getGlobalTtl());
                fragment.show(getSupportFragmentManager(), null);
            }
        });

        binding.containerUnicastRange.getRoot().setOnClickListener(v -> {
            if (mProvisioner != null) {
                final Intent intent = new Intent(this, RangesActivity.class);
                intent.putExtra(Utils.RANGE_TYPE, Utils.UNICAST_RANGE);
                startActivity(intent);
            }
        });

        binding.containerGroupRange.getRoot().setOnClickListener(v -> {
            if (mProvisioner != null) {
                final Intent intent = new Intent(this, RangesActivity.class);
                intent.putExtra(Utils.RANGE_TYPE, Utils.GROUP_RANGE);
                startActivity(intent);
            }
        });

        binding.containerSceneRange.getRoot().setOnClickListener(v -> {
            if (mProvisioner != null) {
                final Intent intent = new Intent(this, RangesActivity.class);
                intent.putExtra(Utils.RANGE_TYPE, Utils.SCENE_RANGE);
                startActivity(intent);
            }
        });

        if (savedInstanceState == null) {
            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if (network != null) {
                final AllocatedUnicastRange unicastRange = network.nextAvailableUnicastAddressRange(0x199A);
                final AllocatedGroupRange groupRange = network.nextAvailableGroupAddressRange(0x0C9A);
                final AllocatedSceneRange sceneRange = network.nextAvailableSceneAddressRange(0x3334);
                final Provisioner provisioner = network.createProvisioner("nRF Mesh Provisioner", unicastRange, groupRange, sceneRange);
                final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                provisioner.setProvisionerName(adapter.getName());
                mViewModel.setSelectedProvisioner(provisioner);
            }
        }

        mViewModel.getSelectedProvisioner().observe(this, provisioner -> {
            if (provisioner != null) {
                mProvisioner = provisioner;
                updateUi();
            }
        });

        updateUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        } else if (id == R.id.action_save) {
            if (save()) {
                onBackPressed();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onNameChanged(@NonNull final String name) {
        if (mProvisioner != null) {
            mProvisioner.setProvisionerName(name);
            updateUi();
            return true;
        }
        return false;
    }

    @Override
    public boolean setAddress(final int sourceAddress) {
        if (mProvisioner != null) {
            if (mProvisioner.assignProvisionerAddress(sourceAddress)) {
                updateUi();
                return true;
            }
        }
        return false;
    }

    @Override
    public void unassignProvisioner() {
        if (mProvisioner != null) {
            final DialogFragmentUnassign fragmentUnassign = DialogFragmentUnassign
                    .newInstance(getString(R.string.title_unassign_provisioner), getString(R.string.summary_unassign_provisioner));
            fragmentUnassign.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public void onProvisionerUnassigned() {
        if (mProvisioner != null) {
            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if (network != null) {
                binding.containerUnicast.text.setText(R.string.unicast_address_unassigned);
                mProvisioner.assignProvisionerAddress(null);
                network.disableConfigurationCapabilities(mProvisioner);
            }
        }
    }

    @Override
    public boolean setDefaultTtl(final int ttl) {
        if (mProvisioner != null) {
            mProvisioner.setGlobalTtl(ttl);
            return true;
        }
        return false;
    }

    private void updateUi() {
        if (mProvisioner != null) {
            binding.containerName.text.setText(mProvisioner.getProvisionerName());
            if (mProvisioner.getProvisionerAddress() == null) {
                binding.containerUnicast.text.setText(R.string.not_assigned);
            } else {
                binding.containerUnicast.text.setText(MeshAddress.formatAddress(mProvisioner.getProvisionerAddress(), true));
            }

            binding.containerUnicastRange.rangeView.clearRanges();
            binding.containerGroupRange.rangeView.clearRanges();
            binding.containerSceneRange.rangeView.clearRanges();

            binding.containerUnicastRange.rangeView.addRanges(mProvisioner.getAllocatedUnicastRanges());
            binding.containerGroupRange.rangeView.addRanges(mProvisioner.getAllocatedGroupRanges());
            binding.containerSceneRange.rangeView.addRanges(mProvisioner.getAllocatedSceneRanges());

            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if (network != null) {
                final String ttl = String.valueOf(mProvisioner.getGlobalTtl());
                binding.containerTtl.text.setText(ttl);
                binding.containerUnicastRange.rangeView.clearOtherRanges();
                binding.containerGroupRange.rangeView.clearOtherRanges();
                binding.containerSceneRange.rangeView.clearOtherRanges();
                for (Provisioner other : network.getProvisioners()) {
                    binding.containerUnicastRange.rangeView.addOtherRanges(other.getAllocatedUnicastRanges());
                    binding.containerGroupRange.rangeView.addOtherRanges(other.getAllocatedGroupRanges());
                    binding.containerSceneRange.rangeView.addOtherRanges(other.getAllocatedSceneRanges());
                }
            }
        }
    }

    private boolean save() {
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            try {
                return network.addProvisioner(mProvisioner);
            } catch (IllegalArgumentException ex) {
                final DialogFragmentError fragment = DialogFragmentError.
                        newInstance(getString(R.string.title_error), ex.getMessage());
                fragment.show(getSupportFragmentManager(), null);
            }
        }
        return false;
    }
}
