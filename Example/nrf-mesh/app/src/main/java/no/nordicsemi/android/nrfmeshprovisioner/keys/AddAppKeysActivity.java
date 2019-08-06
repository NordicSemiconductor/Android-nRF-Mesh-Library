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
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.NetworkKey;
import no.nordicsemi.android.meshprovisioner.NodeKey;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyAdd;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyDelete;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyGet;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.keys.adapter.AddedAppKeyAdapter;

public class AddAppKeysActivity extends AddKeysActivity implements Injectable,
        AddedAppKeyAdapter.OnItemClickListener {
    private AddedAppKeyAdapter adapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.title_added_app_keys);
        mEmptyView = findViewById(R.id.empty_app_keys);
        adapter = new AddedAppKeyAdapter(this,
                mViewModel.getNetworkLiveData().getMeshNetwork().getAppKeys(), mViewModel.getSelectedMeshNode());
        enableAdapterClickListener(true);
        recyclerViewKeys.setAdapter(adapter);
        setUpObserver();
    }

    @Override
    public void onItemClick(@NonNull final ApplicationKey appKey) {
        if (!checkConnectivity())
            return;
        final MeshMessage meshMessage;
        final String message;
        final NetworkKey networkKey = mViewModel.getNetworkLiveData().getMeshNetwork().getNetKey(appKey.getBoundNetKeyIndex());
        if (!mViewModel.isAppKeyAdded(appKey.getKeyIndex())) {
            message = getString(R.string.adding_app_key);
            meshMessage = new ConfigAppKeyAdd(networkKey, appKey);
        } else {
            message = getString(R.string.deleting_app_key);
            meshMessage = new ConfigAppKeyDelete(networkKey, appKey);
        }
        mViewModel.displaySnackBar(this, container, message, Snackbar.LENGTH_SHORT);
        sendMessage(meshMessage);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        if (node != null) {
            for (NodeKey key : node.getAddedNetKeys()) {
                final NetworkKey networkKey = mViewModel.getNetworkLiveData().getMeshNetwork().getNetKey(key.getIndex());
                final ConfigAppKeyGet configAppKeyGet = new ConfigAppKeyGet(networkKey);
                mViewModel.getMessageQueue().add(configAppKeyGet);
            }
            sendMessage(mViewModel.getMessageQueue().peek());
        }
    }

    protected void setUpObserver() {
        mViewModel.getNetworkLiveData().observe(this, networkLiveData -> {
            if (networkLiveData != null) {
                final List<ApplicationKey> keys = networkLiveData.getAppKeys();
                if (keys != null) {
                    mEmptyView.setVisibility(keys.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    @Override
    void enableAdapterClickListener(final boolean enable) {
        adapter.setOnItemClickListener(enable ? this : null);
    }
}
