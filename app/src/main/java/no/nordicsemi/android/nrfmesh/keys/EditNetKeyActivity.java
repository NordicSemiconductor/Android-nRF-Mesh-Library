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
import android.view.MenuItem;
import android.view.View;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityEditKeyBinding;
import no.nordicsemi.android.nrfmesh.keys.dialogs.DialogFragmentEditNetKey;
import no.nordicsemi.android.nrfmesh.keys.dialogs.DialogFragmentKeyName;
import no.nordicsemi.android.nrfmesh.viewmodels.EditNetKeyViewModel;

import static java.text.DateFormat.getDateTimeInstance;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.bytesToHex;
import static no.nordicsemi.android.nrfmesh.utils.Utils.EDIT_KEY;

@AndroidEntryPoint
public class EditNetKeyActivity extends AppCompatActivity implements MeshKeyListener {

    private ActivityEditKeyBinding binding;
    private EditNetKeyViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditKeyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mViewModel = new ViewModelProvider(this).get(EditNetKeyViewModel.class);
        final int index = getIntent().getIntExtra(EDIT_KEY, 0);
        mViewModel.selectNetKey(index);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.title_edit_net_key);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.containerKeyName.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_label));
        binding.containerKeyName.title.setText(R.string.name);
        binding.containerKeyName.text.setVisibility(View.VISIBLE);

        binding.containerKey.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_24dp));
        binding.containerKey.title.setText(R.string.key);
        binding.containerKey.text.setVisibility(View.VISIBLE);

        binding.containerOldKey.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_24dp));
        binding.containerOldKey.title.setText(R.string.old_key);
        binding.containerOldKey.text.setVisibility(View.VISIBLE);
        binding.containerOldKey.getRoot().setVisibility(View.VISIBLE);

        binding.containerKeyIndex.getRoot().setClickable(false);
        binding.containerKeyIndex.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_index));
        binding.containerKeyIndex.title.setText(R.string.title_key_index);
        binding.containerKeyIndex.text.setVisibility(View.VISIBLE);

        binding.containerPhase.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_numeric));
        binding.containerPhase.title.setText(R.string.phase);
        binding.containerPhase.text.setVisibility(View.VISIBLE);
        binding.containerPhase.getRoot().setVisibility(View.VISIBLE);

        binding.containerSecurity.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_numeric));
        binding.containerSecurity.title.setText(R.string.security);
        binding.containerSecurity.text.setVisibility(View.VISIBLE);
        binding.containerSecurity.getRoot().setVisibility(View.VISIBLE);

        binding.containerLastModified.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_shield_key));
        binding.containerLastModified.title.setText(R.string.last_modified);
        binding.containerLastModified.text.setVisibility(View.VISIBLE);
        binding.containerLastModified.getRoot().setVisibility(View.VISIBLE);

        binding.containerKey.getRoot().setOnClickListener(v -> {
            final NetworkKey networkKey = mViewModel.getNetworkKeyLiveData().getValue();
            if (networkKey != null)
                DialogFragmentEditNetKey.newInstance(networkKey)
                        .show(getSupportFragmentManager(), null);
        });

        binding.containerKeyName.getRoot().setOnClickListener(v -> {
            final NetworkKey networkKey = mViewModel.getNetworkKeyLiveData().getValue();
            if (networkKey != null) {
                DialogFragmentKeyName.newInstance(networkKey.getName())
                        .show(getSupportFragmentManager(), null);
            }
        });

        mViewModel.getNetworkKeyLiveData().observe(this, key -> {
            binding.containerKeyName.text.setText(key.getName());
            binding.containerKey.text.setText(bytesToHex(key.getKey(), false));
            binding.containerOldKey.text.setText(key.getOldKey() != null ? bytesToHex(key.getOldKey(), false) : getString(R.string.na));
            binding.containerKeyIndex.text.setText(String.valueOf(key.getKeyIndex()));
            binding.containerPhase.text.setText(key.getPhaseDescription());
            binding.containerSecurity.text.setText(key.isMinSecurity() ? getString(R.string.secure) : getString(R.string.insecure));
            binding.containerLastModified.text.setText(getDateTimeInstance().format(new Date(key.getTimestamp())));
        });
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
        return mViewModel.setName(name);
    }

    @Override
    public boolean onKeyUpdated(@NonNull final String key) {
        return mViewModel.setKey(key);
    }
}
