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
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import no.nordicsemi.android.mesh.transport.ConfigAppKeyList;
import no.nordicsemi.android.mesh.transport.ConfigAppKeyStatus;
import no.nordicsemi.android.mesh.transport.ConfigNetKeyStatus;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityAddKeysBinding;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentConfigStatus;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.AddKeysViewModel;
import no.nordicsemi.android.nrfmesh.viewmodels.BaseActivity;

public abstract class AddKeysActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    protected ActivityAddKeysBinding binding;
    abstract void enableAdapterClickListener(final boolean enable);

    @Override
    protected void updateClickableViews() {
        if (mIsConnected) {
            enableClickableViews();
        } else {
            disableClickableViews();
        }
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddKeysBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mViewModel = new ViewModelProvider(this).get(AddKeysViewModel.class);
        initialize();
        mHandler = new Handler(Looper.getMainLooper());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.swipeRefresh.setOnRefreshListener(this);
        binding.recyclerViewKeys.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(binding.recyclerViewKeys.getContext(), DividerItemDecoration.VERTICAL);
        binding.recyclerViewKeys.addItemDecoration(dividerItemDecoration);
        binding.recyclerViewKeys.setItemAnimator(new DefaultItemAnimator());
        binding.fabAdd.hide();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isFinishing()) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void showDialogFragment(@NonNull final String title, @NonNull final String message) {
        if (getSupportFragmentManager().findFragmentByTag(Utils.DIALOG_FRAGMENT_KEY_STATUS) == null) {
            final DialogFragmentConfigStatus fragmentKeyStatus = DialogFragmentConfigStatus.newInstance(title, message);
            fragmentKeyStatus.show(getSupportFragmentManager(), Utils.DIALOG_FRAGMENT_KEY_STATUS);
        }
    }

    protected void showProgressBar() {
        mHandler.postDelayed(mRunnableOperationTimeout, Utils.MESSAGE_TIME_OUT);
        disableClickableViews();
        binding.configurationProgressBar.setVisibility(View.VISIBLE);
    }

    protected final void hideProgressBar() {
        binding.swipeRefresh.setRefreshing(false);
        enableClickableViews();
        binding.configurationProgressBar.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacks(mRunnableOperationTimeout);
    }

    protected void enableClickableViews() {
        enableAdapterClickListener(true);
        binding.recyclerViewKeys.setEnabled(true);
        binding.recyclerViewKeys.setClickable(true);
    }

    protected void disableClickableViews() {
        enableAdapterClickListener(false);
        binding.recyclerViewKeys.setEnabled(false);
        binding.recyclerViewKeys.setClickable(false);
    }

    private void handleStatuses() {
        final MeshMessage message = mViewModel.getMessageQueue().peek();
        if (message != null) {
            sendMessage(message);
        } else {
            mViewModel.displaySnackBar(this, binding.container, getString(R.string.operation_success), Snackbar.LENGTH_SHORT);
        }
    }

    protected void sendMessage(final MeshMessage meshMessage) {
        try {
            if (!checkConnectivity(binding.container))
                return;
            showProgressBar();
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
            if (node != null) {
                mViewModel.getMeshManagerApi().createMeshPdu(node.getUnicastAddress(), meshMessage);
            }
        } catch (IllegalArgumentException ex) {
            hideProgressBar();
            final DialogFragmentError message = DialogFragmentError.
                    newInstance(getString(R.string.title_error), ex.getMessage());
            message.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        if (meshMessage instanceof ConfigNetKeyStatus) {
            final ConfigNetKeyStatus status = (ConfigNetKeyStatus) meshMessage;
            if (status.isSuccessful()) {
                mViewModel.displaySnackBar(this, binding.container, getString(R.string.operation_success), Snackbar.LENGTH_SHORT);
            } else {
                showDialogFragment(getString(R.string.title_netkey_status), status.getStatusCodeName());
            }
        } else if (meshMessage instanceof ConfigAppKeyStatus) {
            final ConfigAppKeyStatus status = (ConfigAppKeyStatus) meshMessage;
            if (status.isSuccessful()) {
                mViewModel.displaySnackBar(this, binding.container, getString(R.string.operation_success), Snackbar.LENGTH_SHORT);
            } else {
                showDialogFragment(getString(R.string.title_appkey_status), status.getStatusCodeName());
            }
        } else if (meshMessage instanceof ConfigAppKeyList) {
            final ConfigAppKeyList status = (ConfigAppKeyList) meshMessage;
            if (!mViewModel.getMessageQueue().isEmpty())
                mViewModel.getMessageQueue().remove();
            if (status.isSuccessful()) {
                handleStatuses();
            } else {
                showDialogFragment(getString(R.string.title_appkey_status), status.getStatusCodeName());
            }
        }
        hideProgressBar();
    }

    @Override
    public void onRefresh() {
        if (!checkConnectivity(binding.container)) {
            binding.swipeRefresh.setRefreshing(false);
        }
    }
}
