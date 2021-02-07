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

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityEditKeyBinding;
import no.nordicsemi.android.nrfmesh.keys.adapter.ManageBoundNetKeyAdapter;
import no.nordicsemi.android.nrfmesh.keys.dialogs.DialogFragmentEditAppKey;
import no.nordicsemi.android.nrfmesh.keys.dialogs.DialogFragmentKeyName;
import no.nordicsemi.android.nrfmesh.viewmodels.EditAppKeyViewModel;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.bytesToHex;
import static no.nordicsemi.android.nrfmesh.utils.Utils.EDIT_KEY;

@AndroidEntryPoint
public class EditAppKeyActivity extends AppCompatActivity implements
        MeshKeyListener,
        ManageBoundNetKeyAdapter.OnItemClickListener {

    private ActivityEditKeyBinding binding;
    private EditAppKeyViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditKeyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final int index = getIntent().getExtras().getInt(EDIT_KEY);
        mViewModel = new ViewModelProvider(this).get(EditAppKeyViewModel.class);
        mViewModel.selectAppKey(index);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(R.string.title_edit_app_key);
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

        binding.netKeyContainer.setVisibility(View.VISIBLE);
        binding.recyclerViewKeys.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewKeys.setItemAnimator(new DefaultItemAnimator());
        final ManageBoundNetKeyAdapter mAdapter = new ManageBoundNetKeyAdapter(this, mViewModel.getAppKeyLiveData(), mViewModel.getNetworkLiveData().getNetworkKeys());
        mAdapter.setOnItemClickListener(this);
        binding.recyclerViewKeys.setAdapter(mAdapter);

        binding.containerKey.getRoot().setOnClickListener(v -> DialogFragmentEditAppKey.newInstance(mViewModel.getAppKeyLiveData().getValue())
                .show(getSupportFragmentManager(), null));

        binding.containerKeyName.getRoot().setOnClickListener(v -> DialogFragmentKeyName.
                newInstance(mViewModel.getAppKeyLiveData().getValue().getName()).show(getSupportFragmentManager(), null));

        mViewModel.getAppKeyLiveData().observe(this, applicationKey -> {
            if (applicationKey != null) {
                binding.containerKeyName.text.setText(applicationKey.getName());
                binding.containerKey.text.setText(bytesToHex(applicationKey.getKey(), false));
                binding.containerOldKey.text.setText(applicationKey.getOldKey() != null ?
                        bytesToHex(applicationKey.getKey(), false) : getString(R.string.na));
                binding.containerKeyIndex.text.setText(String.valueOf(applicationKey.getKeyIndex()));
            }
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
    public boolean onKeyNameUpdated(@NonNull final String name) {
        return mViewModel.setName(name);
    }

    @Override
    public boolean onKeyUpdated(@NonNull final String key) {
        return mViewModel.setKey(key);
    }

    @Override
    public void updateBoundNetKeyIndex(final int position, @NonNull final NetworkKey networkKey) {
        try {
            mViewModel.setBoundNetKeyIndex(networkKey.getKeyIndex());
        } catch (IllegalArgumentException ex) {
            mViewModel.displaySnackBar(this, binding.container, ex.getMessage(), Snackbar.LENGTH_LONG);
        }
    }
}
