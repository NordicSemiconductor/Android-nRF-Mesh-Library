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

package no.nordicsemi.android.nrfmesh.keys;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityEditKeyBinding;
import no.nordicsemi.android.nrfmesh.keys.dialogs.DialogFragmentEditNetKey;
import no.nordicsemi.android.nrfmesh.keys.dialogs.DialogFragmentKeyName;
import no.nordicsemi.android.nrfmesh.viewmodels.AddNetKeyViewModel;

@AndroidEntryPoint
public class AddNetKeyActivity extends AppCompatActivity implements MeshKeyListener {

    private ActivityEditKeyBinding binding;
    private AddNetKeyViewModel mViewModel;


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditKeyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mViewModel = new ViewModelProvider(this).get(AddNetKeyViewModel.class);

        //Bind ui
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(R.string.title_add_net_key);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        binding.containerKey.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_24dp));
        binding.containerKey.title.setText(R.string.key);
        binding.containerKey.text.setVisibility(View.VISIBLE);

        binding.containerKeyName.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_label));
        binding.containerKeyName.title.setText(R.string.name);
        binding.containerKeyName.text.setVisibility(View.VISIBLE);

        binding.containerKeyIndex.getRoot().setClickable(false);
        binding.containerKeyIndex.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_index));
        binding.containerKeyIndex.title.setText(R.string.title_key_index);
        binding.containerKeyIndex.text.setVisibility(View.VISIBLE);

        binding.containerKey.getRoot().setOnClickListener(v ->
                DialogFragmentEditNetKey.newInstance(mViewModel.getNetworkKeyLiveData().getValue())
                        .show(getSupportFragmentManager(), null));

        binding.containerKeyName.getRoot().setOnClickListener(v ->
                DialogFragmentKeyName.newInstance(mViewModel.getNetworkKeyLiveData().getValue().getName())
                        .show(getSupportFragmentManager(), null));

        mViewModel.getNetworkKeyLiveData().observe(this, this::updateUi);
    }

    private void updateUi(@NonNull final NetworkKey networkKey) {
        if (networkKey != null) {
            binding.containerKey.text.setText(MeshParserUtils.bytesToHex(networkKey.getKey(), false));
            binding.containerKeyName.text.setText(networkKey.getName());
            binding.containerKeyIndex.text.setText(String.valueOf(networkKey.getKeyIndex()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_save) {
            try {
                if (mViewModel.addNetKey())
                    onBackPressed();
            } catch (IllegalArgumentException ex) {
                mViewModel.displaySnackBar(this, binding.container, ex.getMessage(), Snackbar.LENGTH_LONG);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyNameUpdated(@NonNull final String name) {
        mViewModel.setName(name);
        return true;
    }

    @Override
    public boolean onKeyUpdated(@NonNull final String key) {
        mViewModel.setKey(MeshParserUtils.toByteArray(key));
        return true;
    }
}
