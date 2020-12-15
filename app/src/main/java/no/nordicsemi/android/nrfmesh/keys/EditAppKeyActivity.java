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
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.di.Injectable;
import no.nordicsemi.android.nrfmesh.keys.adapter.ManageBoundNetKeyAdapter;
import no.nordicsemi.android.nrfmesh.keys.dialogs.DialogFragmentEditAppKey;
import no.nordicsemi.android.nrfmesh.keys.dialogs.DialogFragmentKeyName;
import no.nordicsemi.android.nrfmesh.viewmodels.EditAppKeyViewModel;

import static no.nordicsemi.android.nrfmesh.utils.Utils.EDIT_KEY;

public class EditAppKeyActivity extends AppCompatActivity implements Injectable,
        MeshKeyListener,
        ManageBoundNetKeyAdapter.OnItemClickListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @BindView(R.id.container)
    View container;

    private EditAppKeyViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_key);
        ButterKnife.bind(this);
        final int index = getIntent().getExtras().getInt(EDIT_KEY);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(EditAppKeyViewModel.class);
        mViewModel.selectAppKey(index);

        //Bind ui
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_edit_app_key);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final View containerKey = findViewById(R.id.container_key);
        containerKey.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_lock_open_24dp));
        ((TextView) containerKey.findViewById(R.id.title)).setText(R.string.title_app_key);
        final TextView keyView = containerKey.findViewById(R.id.text);
        keyView.setVisibility(View.VISIBLE);

        final View containerKeyName = findViewById(R.id.container_key_name);
        containerKeyName.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_label));
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
        final ManageBoundNetKeyAdapter mAdapter = new ManageBoundNetKeyAdapter(this, mViewModel.getAppKeyLiveData(), mViewModel.getNetworkLiveData().getNetworkKeys());
        mAdapter.setOnItemClickListener(this);
        netKeysRecyclerView.setAdapter(mAdapter);

        containerKey.setOnClickListener(v -> {
            final ApplicationKey appKey = mViewModel.getAppKeyLiveData().getValue();
            final DialogFragmentEditAppKey fragment = DialogFragmentEditAppKey.newInstance(appKey.getKeyIndex(), appKey);
            fragment.show(getSupportFragmentManager(), null);
        });

        containerKeyName.setOnClickListener(v -> {
            final DialogFragmentKeyName fragment = DialogFragmentKeyName.newInstance(mViewModel.getAppKeyLiveData().getValue().getName());
            fragment.show(getSupportFragmentManager(), null);
        });

        mViewModel.getAppKeyLiveData().observe(this, applicationKey -> {
            if (applicationKey != null) {
                keyView.setText(MeshParserUtils.bytesToHex(applicationKey.getKey(), false));
                name.setText(applicationKey.getName());
                keyIndexView.setText(String.valueOf(applicationKey.getKeyIndex()));
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
    public boolean onKeyUpdated(final int position, @NonNull final String key) {
        return mViewModel.setKey(key);
    }

    @Override
    public void updateBoundNetKeyIndex(final int position, @NonNull final NetworkKey networkKey) {
        try {
            mViewModel.setBoundNetKeyIndex(networkKey.getKeyIndex());
        } catch (IllegalArgumentException ex) {
            displaySnackBar(ex.getMessage());
        }
    }

    private void displaySnackBar(final String message) {
        Snackbar.make(container, message, Snackbar.LENGTH_LONG)
                .show();
    }
}
