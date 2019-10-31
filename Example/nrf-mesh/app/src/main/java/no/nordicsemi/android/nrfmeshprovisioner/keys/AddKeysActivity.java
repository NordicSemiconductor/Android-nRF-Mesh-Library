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
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyList;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetKeyStatus;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigStatus;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.AddKeysViewModel;

public abstract class AddKeysActivity extends AppCompatActivity implements Injectable, SwipeRefreshLayout.OnRefreshListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.container)
    protected CoordinatorLayout container;
    @BindView(R.id.recycler_view_keys)
    protected RecyclerView recyclerViewKeys;
    @BindView(R.id.fab_add)
    protected ExtendedFloatingActionButton fab;
    @BindView(R.id.configuration_progress_bar)
    protected ProgressBar mProgressbar;
    @BindView(R.id.swipe_refresh)
    protected SwipeRefreshLayout mSwipe;

    protected Handler mHandler;
    protected View mEmptyView;

    protected AddKeysViewModel mViewModel;
    protected boolean mIsConnected;

    abstract void enableAdapterClickListener(final boolean enable);

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(AddKeysViewModel.class);
        setContentView(R.layout.activity_add_keys);
        ButterKnife.bind(this);
        mHandler = new Handler();
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSwipe.setOnRefreshListener(this);
        recyclerViewKeys.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerViewKeys.getContext(), DividerItemDecoration.VERTICAL);
        recyclerViewKeys.addItemDecoration(dividerItemDecoration);
        recyclerViewKeys.setItemAnimator(new DefaultItemAnimator());
        fab.hide();

        mViewModel.getMeshMessage().observe(this, meshMessage -> {
            if (meshMessage instanceof ConfigNetKeyStatus) {
                final ConfigNetKeyStatus status = (ConfigNetKeyStatus) meshMessage;
                if (status.isSuccessful()) {
                    mViewModel.displaySnackBar(this, container, getString(R.string.operation_success), Snackbar.LENGTH_SHORT);
                } else {
                    showDialogFragment(getString(R.string.title_netkey_status), status.getStatusCodeName());
                }
            } else if (meshMessage instanceof ConfigAppKeyStatus) {
                final ConfigAppKeyStatus status = (ConfigAppKeyStatus) meshMessage;
                if (status.isSuccessful()) {
                    mViewModel.displaySnackBar(this, container, getString(R.string.operation_success), Snackbar.LENGTH_SHORT);
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
        });

        mViewModel.isConnectedToProxy().observe(this, isConnected -> {
            if (isConnected != null) {
                mIsConnected = isConnected;
                hideProgressBar();
            }
            invalidateOptionsMenu();
        });

        final Boolean isConnectedToNetwork = mViewModel.isConnectedToProxy().getValue();
        if (isConnectedToNetwork != null) {
            mIsConnected = isConnectedToNetwork;
        }
        invalidateOptionsMenu();

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (mIsConnected) {
            getMenuInflater().inflate(R.menu.disconnect, menu);
        } else {
            getMenuInflater().inflate(R.menu.connect, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_connect:
                mViewModel.navigateToScannerActivity(this, false, Utils.CONNECT_TO_NETWORK, false);
                return true;
            case R.id.action_disconnect:
                mViewModel.disconnect();
                return true;
            default:
                return false;
        }
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

    protected final boolean checkConnectivity() {
        if (!mIsConnected) {
            mViewModel.displayDisconnectedSnackBar(this, container);
            return false;
        }
        return true;
    }

    protected final void showProgressbar() {
        mHandler.postDelayed(mOperationTimeout, Utils.MESSAGE_TIME_OUT);
        disableClickableViews();
        mProgressbar.setVisibility(View.VISIBLE);
    }

    protected final void hideProgressBar() {
        mSwipe.setRefreshing(false);
        enableClickableViews();
        mProgressbar.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacks(mOperationTimeout);
    }

    private final Runnable mOperationTimeout = () -> {
        hideProgressBar();
        mViewModel.getMessageQueue().clear();
        DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance(getString(R.string.title_transaction_failed), getString(R.string.operation_timed_out));
        fragmentMessage.show(getSupportFragmentManager(), null);
    };

    protected void enableClickableViews() {
        enableAdapterClickListener(true);
        recyclerViewKeys.setEnabled(true);
        recyclerViewKeys.setClickable(true);
    }

    protected void disableClickableViews() {
        enableAdapterClickListener(false);
        recyclerViewKeys.setEnabled(false);
        recyclerViewKeys.setClickable(false);
    }

    private void handleStatuses() {
        final MeshMessage message = mViewModel.getMessageQueue().peek();
        if (message != null) {
            sendMessage(message);
        } else {
            mViewModel.displaySnackBar(this, container, getString(R.string.operation_success), Snackbar.LENGTH_SHORT);
        }
    }

    protected void sendMessage(final MeshMessage meshMessage) {
        try {
            if (!checkConnectivity())
                return;
            showProgressbar();
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
    public void onRefresh() {
        if (!checkConnectivity()) {
            mSwipe.setRefreshing(false);
        }
    }
}
