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

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.meshprovisioner.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetKeyAdd;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetKeyDelete;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetKeyGet;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.keys.adapter.AddedNetKeyAdapter;

public class AddNetKeysActivity extends AddKeysActivity implements Injectable,
        AddedNetKeyAdapter.OnItemClickListener {
    private AddedNetKeyAdapter adapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.title_added_net_keys);
        mEmptyView = findViewById(R.id.empty_net_keys);
        adapter = new AddedNetKeyAdapter(this,
                mViewModel.getNetworkLiveData().getMeshNetwork().getNetKeys(), mViewModel.getSelectedMeshNode());
        enableAdapterClickListener(true);
        recyclerViewKeys.setAdapter(adapter);
    }

    @Override
    public void onItemClick(@NonNull final NetworkKey networkKey) {
        if (!checkConnectivity())
            return;
        final MeshMessage meshMessage;
        final String message;
        if (!mViewModel.isNetKeyAdded(networkKey.getKeyIndex())) {
            meshMessage = new ConfigNetKeyAdd(networkKey);
            message = getString(R.string.adding_net_key);
        } else {
            meshMessage = new ConfigNetKeyDelete(networkKey);
            message = getString(R.string.deleting_net_key);
        }
        mViewModel.displaySnackBar(this, container, message, Snackbar.LENGTH_SHORT);
        sendMessage(meshMessage);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        final ConfigNetKeyGet configNetKeyGet = new ConfigNetKeyGet();
        sendMessage(configNetKeyGet);
    }

    @Override
    void enableAdapterClickListener(final boolean enable) {
        adapter.setOnItemClickListener(enable ? this : null);
    }
}
