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

package no.nordicsemi.android.nrfmeshprovisioner.keys;

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
import no.nordicsemi.android.meshprovisioner.NetworkKey;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.keys.dialogs.DialogFragmentEditNetKey;
import no.nordicsemi.android.nrfmeshprovisioner.keys.dialogs.DialogFragmentKeyName;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.EditNetKeyViewModel;

public class EditNetKeyActivity extends AppCompatActivity implements Injectable, MeshKeyListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private EditNetKeyViewModel mViewModel;
    private NetworkKey networkKey;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_key);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(EditNetKeyViewModel.class);

        //noinspection ConstantConditions
        final int index = getIntent().getExtras().getInt(NetKeysActivity.EDIT_NET_KEY);
        networkKey = mViewModel.getNetworkLiveData().getMeshNetwork().getNetKey(index);

        //Bind ui
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.title_edit_net_key);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final View containerKey = findViewById(R.id.container_key);
        containerKey.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_lock_open_black_alpha_24dp));
        ((TextView) containerKey.findViewById(R.id.title)).setText(R.string.title_net_key);
        final TextView keyView = containerKey.findViewById(R.id.text);
        keyView.setVisibility(View.VISIBLE);

        final View containerKeyName = findViewById(R.id.container_key_name);
        containerKeyName.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_label_black_alpha_24dp));
        ((TextView) containerKeyName.findViewById(R.id.title)).setText(R.string.name);
        final TextView name = containerKeyName.findViewById(R.id.text);
        name.setVisibility(View.VISIBLE);

        final View containerKeyIndex = findViewById(R.id.container_key_index);
        containerKeyIndex.setClickable(false);
        containerKeyIndex.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_index));
        ((TextView) containerKeyIndex.findViewById(R.id.title)).setText(R.string.title_key_index);
        final TextView keyIndexView = containerKeyIndex.findViewById(R.id.text);
        keyIndexView.setVisibility(View.VISIBLE);

        containerKey.setOnClickListener(v -> {
            if (networkKey != null) {
                final DialogFragmentEditNetKey fragment = DialogFragmentEditNetKey.newInstance(networkKey.getKeyIndex(), networkKey);
                fragment.show(getSupportFragmentManager(), null);
            }
        });

        containerKeyName.setOnClickListener(v -> {
            if (networkKey != null) {
                final DialogFragmentKeyName fragment = DialogFragmentKeyName.newInstance(networkKey.getName());
                fragment.show(getSupportFragmentManager(), null);
            }
        });

        mViewModel.getNetworkLiveData().observe(this, meshNetworkLiveData -> {
            if (networkKey != null) {
                this.networkKey = meshNetworkLiveData.getMeshNetwork().getNetKey(networkKey.getKeyIndex());
                keyView.setText(MeshParserUtils.bytesToHex(networkKey.getKey(), false));
                name.setText(networkKey.getName());
                keyIndexView.setText(String.valueOf(networkKey.getKeyIndex()));
            }
        });

        if (savedInstanceState == null) {
            keyView.setText(MeshParserUtils.bytesToHex(networkKey.getKey(), false));
            name.setText(networkKey.getName());
        }
        keyIndexView.setText(String.valueOf(networkKey.getKeyIndex()));

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyNameUpdated(@NonNull final String name) {
        if (networkKey != null) {
            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if (network != null) {
                networkKey.setName(name);
                return network.updateNetKey(networkKey);
            }
        }
        return false;
    }

    @Override
    public boolean onKeyUpdated(final int position, @NonNull final String key) {
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            return network.updateNetKey(networkKey, key);
        }
        return false;
    }
}
