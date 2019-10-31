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

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.NetworkKey;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.keys.adapter.ManageBoundNetKeyAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.keys.dialogs.DialogFragmentEditAppKey;
import no.nordicsemi.android.nrfmeshprovisioner.keys.dialogs.DialogFragmentKeyName;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.EditAppKeyViewModel;

public class EditAppKeyActivity extends AppCompatActivity implements Injectable,
        MeshKeyListener,
        ManageBoundNetKeyAdapter.OnItemClickListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @BindView(R.id.container)
    View container;

    private EditAppKeyViewModel mViewModel;
    private ApplicationKey appKey;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_key);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(EditAppKeyViewModel.class);
        ButterKnife.bind(this);
        //noinspection ConstantConditions
        final int index = getIntent().getExtras().getInt(AppKeysActivity.EDIT_APP_KEY);
        appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(index);

        //Bind ui
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.title_edit_app_key);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final View containerKey = findViewById(R.id.container_key);
        containerKey.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_lock_open_black_alpha_24dp));
        ((TextView) containerKey.findViewById(R.id.title)).setText(R.string.title_app_key);
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

        findViewById(R.id.net_key_container).setVisibility(View.VISIBLE);
        final RecyclerView netKeysRecyclerView = findViewById(R.id.recycler_view_keys);
        netKeysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        netKeysRecyclerView.setItemAnimator(new DefaultItemAnimator());
        final ManageBoundNetKeyAdapter mAdapter = new ManageBoundNetKeyAdapter(this, mViewModel.getNetworkLiveData().getNetworkKeys(), appKey);
        mAdapter.setOnItemClickListener(this);
        netKeysRecyclerView.setAdapter(mAdapter);

        containerKey.setOnClickListener(v -> {
            if (appKey != null) {
                final DialogFragmentEditAppKey fragment = DialogFragmentEditAppKey.newInstance(appKey.getKeyIndex(), appKey);
                fragment.show(getSupportFragmentManager(), null);
            }
        });

        containerKeyName.setOnClickListener(v -> {
            if (appKey != null) {
                final DialogFragmentKeyName fragment = DialogFragmentKeyName.newInstance(appKey.getName());
                fragment.show(getSupportFragmentManager(), null);
            }
        });

        mViewModel.getNetworkLiveData().observe(this, meshNetworkLiveData -> {
            if (appKey != null) {
                this.appKey = meshNetworkLiveData.getMeshNetwork().getAppKey(appKey.getKeyIndex());
                keyView.setText(MeshParserUtils.bytesToHex(appKey.getKey(), false));
                name.setText(appKey.getName());
                keyIndexView.setText(String.valueOf(appKey.getKeyIndex()));
            }
        });

        if (savedInstanceState == null) {
            keyView.setText(MeshParserUtils.bytesToHex(appKey.getKey(), false));
            name.setText(appKey.getName());
        }
        keyIndexView.setText(String.valueOf(appKey.getKeyIndex()));

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
        if (appKey != null) {
            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if (network != null) {
                appKey.setName(name);
                return network.updateAppKey(appKey);
            }
        }
        return false;
    }

    @Override
    public boolean onKeyUpdated(final int position, @NonNull final String key) {
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            return network.updateAppKey(appKey, key);
        }
        return false;
    }

    @Override
    public ApplicationKey updateBoundNetKeyIndex(final int position, @NonNull final NetworkKey networkKey) {
        try {
            final ApplicationKey key = appKey.clone();
            key.setBoundNetKeyIndex(networkKey.getKeyIndex());
            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if (network != null) {
                try {
                    if (network.updateAppKey(key)) {
                        appKey = key;
                        return key;
                    }
                } catch (IllegalArgumentException ex) {
                    displaySnackBar(ex.getMessage());
                }
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return appKey;
    }

    private void displaySnackBar(final String message) {
        Snackbar.make(container, message, Snackbar.LENGTH_LONG)
                .show();
    }
}
