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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
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
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.AddAppKeyViewModel;

public class AddAppKeyActivity extends AppCompatActivity implements Injectable,
        MeshKeyListener,
        ManageBoundNetKeyAdapter.OnItemClickListener {

    private static final String APPLICATION_KEY = "APPLICATION_KEY";
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.container)
    CoordinatorLayout container;
    TextView nameView;
    TextView keyView;
    TextView keyIndexView;

    private AddAppKeyViewModel mViewModel;
    private ApplicationKey appKey;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_key);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(AddAppKeyViewModel.class);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
            if (network != null) {
                appKey = network.createAppKey();
            }
        } else {
            appKey = savedInstanceState.getParcelable(APPLICATION_KEY);
        }

        //Bind ui
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.title_add_app_key);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        final View containerKey = findViewById(R.id.container_key);
        containerKey.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_black_alpha_24dp));
        ((TextView) containerKey.findViewById(R.id.title)).setText(R.string.title_app_key);
        keyView = containerKey.findViewById(R.id.text);
        keyView.setVisibility(View.VISIBLE);

        final View containerKeyName = findViewById(R.id.container_key_name);
        containerKeyName.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_label_black_alpha_24dp));
        ((TextView) containerKeyName.findViewById(R.id.title)).setText(R.string.name);
        nameView = containerKeyName.findViewById(R.id.text);
        nameView.setVisibility(View.VISIBLE);

        final View containerKeyIndex = findViewById(R.id.container_key_index);
        containerKeyIndex.setClickable(false);
        containerKeyIndex.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_index));
        ((TextView) containerKeyIndex.findViewById(R.id.title)).setText(R.string.title_key_index);
        keyIndexView = containerKeyIndex.findViewById(R.id.text);
        keyIndexView.setVisibility(View.VISIBLE);

        findViewById(R.id.net_key_container).setVisibility(View.VISIBLE);

        final RecyclerView netKeysRecyclerView = findViewById(R.id.recycler_view_keys);
        netKeysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        netKeysRecyclerView.setItemAnimator(new DefaultItemAnimator());
        final ManageBoundNetKeyAdapter adapter = new ManageBoundNetKeyAdapter(this, mViewModel.getNetworkLiveData().getNetworkKeys(), appKey);
        adapter.setOnItemClickListener(this);
        netKeysRecyclerView.setAdapter(adapter);

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

        updateUi();
    }

    private void updateUi() {
        if (appKey != null) {
            keyView.setText(MeshParserUtils.bytesToHex(appKey.getKey(), false));
            nameView.setText(appKey.getName());
            keyIndexView.setText(String.valueOf(appKey.getKeyIndex()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
                if (save()) {
                    onBackPressed();
                }
                return true;
        }
        return false;
    }

    private boolean save() {
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            return network.addAppKey(appKey);
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(APPLICATION_KEY, appKey);
    }

    @Override
    public boolean onKeyNameUpdated(@NonNull final String name) {
        if (appKey != null) {
            appKey.setName(name);
            nameView.setText(name);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUpdated(final int position, @NonNull final String key) {
        if (appKey != null) {
            this.appKey.setKey(MeshParserUtils.toByteArray(key));
            keyView.setText(key);
            return true;
        }
        return false;
    }

    @Override
    public ApplicationKey updateBoundNetKeyIndex(final int position, @NonNull final NetworkKey networkKey) {
        final ApplicationKey key = appKey;
        key.setBoundNetKeyIndex(networkKey.getKeyIndex());
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            try {
                if (network.updateAppKey(key)) {
                    appKey = key;
                    return key;
                }
            } catch (IllegalArgumentException ex) {
                mViewModel.displaySnackBar(this, container, ex.getMessage(), Snackbar.LENGTH_LONG);
            }
        }
        return appKey;
    }
}
