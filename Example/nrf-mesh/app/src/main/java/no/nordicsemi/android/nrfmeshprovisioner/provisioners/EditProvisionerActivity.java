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

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import java.util.Collections;

import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.keys.dialogs.DialogFragmentEditAppKey;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.dialogs.DialogFragmentProvisionerAddress;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.dialogs.DialogFragmentProvisionerName;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.EditProvisionerViewModel;

public class EditProvisionerActivity extends AppCompatActivity implements Injectable,
        DialogFragmentProvisionerName.DialogFragmentProvisionerNameListener,
        DialogFragmentProvisionerAddress.DialogFragmentAddressListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private EditProvisionerViewModel mViewModel;
    private Provisioner provisioner;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_provisioner);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(EditProvisionerViewModel.class);

        //noinspection ConstantConditions
        final int index = getIntent().getExtras().getInt(ProvisionersActivity.EDIT_PROVISIONER);
        provisioner = mViewModel.getMeshNetworkLiveData().getProvisioners().get(index);

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
        final TextView provisionerName = containerProvisionerName.findViewById(R.id.text);
        provisionerName.setVisibility(View.VISIBLE);

        final View containerUnicast = findViewById(R.id.container_unicast);
        containerUnicast.setClickable(false);
        containerUnicast.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_index));
        ((TextView) containerUnicast.findViewById(R.id.title)).setText(R.string.title_unicast_address);
        final TextView provisionerUnicast = containerUnicast.findViewById(R.id.text);
        provisionerUnicast.setVisibility(View.VISIBLE);

        final View containerUnicastRange = findViewById(R.id.container_unicast_range);
        containerUnicastRange.setClickable(false);
        containerUnicastRange.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_lan_black_alpha_24dp));
        ((TextView) containerUnicastRange.findViewById(R.id.title)).setText(R.string.title_unicast_addresses);
        final TextView unicastRange = containerUnicastRange.findViewById(R.id.text);
        unicastRange.setVisibility(View.VISIBLE);

        final View containerGroupRange = findViewById(R.id.container_group_range);
        containerGroupRange.setClickable(false);
        containerGroupRange.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_outline_group_work_black_alpha_24dp));
        ((TextView) containerGroupRange.findViewById(R.id.title)).setText(R.string.title_group_addresses);
        final TextView groupRange = containerGroupRange.findViewById(R.id.text);
        groupRange.setVisibility(View.VISIBLE);

        final View containerSceneRange = findViewById(R.id.container_scene_range);
        containerSceneRange.setClickable(false);
        containerSceneRange.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_arrow_collapse_black_alpha_24dp));
        ((TextView) containerSceneRange.findViewById(R.id.title)).setText(R.string.title_scenes);
        final TextView sceneRange = containerSceneRange.findViewById(R.id.text);
        sceneRange.setVisibility(View.VISIBLE);

        containerProvisionerName.setOnClickListener(v -> {
            if (provisioner != null) {
                final DialogFragmentProvisionerName fragment = DialogFragmentProvisionerName.newInstance(provisioner.getProvisionerName());
                fragment.show(getSupportFragmentManager(), null);
            }
        });

        containerUnicast.setOnClickListener(v -> {
            if (provisioner != null) {
                final DialogFragmentProvisionerAddress fragment = DialogFragmentProvisionerAddress.newInstance(provisioner.getProvisionerAddress());
                fragment.show(getSupportFragmentManager(), null);
            }
        });

        mViewModel.getMeshNetworkLiveData().observe(this, meshNetworkLiveData -> {
            if(provisioner != null) {
                provisionerName.setText(provisioner.getProvisionerName());
                provisionerUnicast.setText(MeshAddress.formatAddress(provisioner.getProvisionerAddress(), true));
                unicastRange.setText(getString(R.string.summary_ranges, provisioner.getAllocatedUnicastRanges().size()));
                groupRange.setText(getString(R.string.summary_ranges, provisioner.getAllocatedGroupRanges().size()));
                sceneRange.setText(getString(R.string.summary_ranges, provisioner.getAllocatedSceneRanges().size()));
            }
        });

        if(savedInstanceState == null){
            provisionerName.setText(provisioner.getProvisionerName());
            provisionerUnicast.setText(MeshAddress.formatAddress(provisioner.getProvisionerAddress(), true));
        }

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
    public boolean onNameChanged(@NonNull final String name) {
        if(provisioner != null) {
            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if(network != null) {
                provisioner.setProvisionerName(name);
                return network.updateProvisioner(provisioner);
            }
        }
        return false;
    }

    @Override
    public boolean setAddress(final int sourceAddress) {
        if(provisioner != null) {
            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if(network != null) {
                provisioner.setProvisionerAddress(sourceAddress);
                return network.updateProvisioner(provisioner);
            }
        }
        return false;
    }
}
