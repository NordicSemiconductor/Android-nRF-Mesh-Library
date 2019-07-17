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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyAdd;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetKeyAdd;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetKeyStatus;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigError;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigStatus;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.AddKeysViewModel;

public abstract class AddKeysActivity extends AppCompatActivity implements Injectable {

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

    protected Handler mHandler;
    protected View mEmptyView;

    protected AddKeysViewModel mViewModel;
    protected boolean mIsConnected;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(AddKeysViewModel.class);
        setContentView(R.layout.activity_add_keys);
        ButterKnife.bind(this);
        mHandler = new Handler();
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerViewKeys.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerViewKeys.getContext(), DividerItemDecoration.VERTICAL);
        recyclerViewKeys.addItemDecoration(dividerItemDecoration);
        recyclerViewKeys.setItemAnimator(new DefaultItemAnimator());
        fab.hide();

        mViewModel.getMeshMessage().observe(this, meshMessage -> {
            if (meshMessage instanceof ConfigNetKeyStatus) {
                showDialogFragment(getString(R.string.title_netkey_status), ((ConfigNetKeyStatus) meshMessage).getStatusCodeName());
            } else if (meshMessage instanceof ConfigAppKeyStatus) {
                showDialogFragment(getString(R.string.title_appkey_status), ((ConfigAppKeyStatus) meshMessage).getStatusCodeName());
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

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!checkConnectivity()) return;
        if (requestCode == Utils.ADD_NET_KEY) {
            if (resultCode == RESULT_OK) {
                final NetworkKey netKey = data.getParcelableExtra(Utils.RESULT_KEY);
                if (netKey != null) {
                    final ConfigNetKeyAdd configAppKeyAdd = new ConfigNetKeyAdd(netKey);
                    sendMessage(configAppKeyAdd);
                }
            }
        } else if (requestCode == Utils.ADD_APP_KEY) {
            if (resultCode == RESULT_OK) {
                final ApplicationKey appKey = data.getParcelableExtra(Utils.RESULT_KEY);
                if (appKey != null) {
                    final NetworkKey networkKey = mViewModel.getNetworkLiveData().getMeshNetwork().getNetKey(appKey.getBoundNetKeyIndex());
                    if (networkKey != null) {
                        final ConfigAppKeyAdd configAppKeyAdd = new ConfigAppKeyAdd(networkKey, appKey);
                        sendMessage(configAppKeyAdd);
                    }
                }
            }
        }
    }

    private void showDialogFragment(@NonNull final String title, @NonNull final String message) {
        if (getSupportFragmentManager().findFragmentByTag(Utils.DIALOG_FRAGMENT_KEY_STATUS) == null) {
            final DialogFragmentConfigStatus fragmentKeyStatus = DialogFragmentConfigStatus.newInstance(title, message);
            fragmentKeyStatus.show(getSupportFragmentManager(), Utils.DIALOG_FRAGMENT_KEY_STATUS);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
        enableClickableViews();
        mProgressbar.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacks(mOperationTimeout);
    }

    private final Runnable mOperationTimeout = () -> {
        hideProgressBar();
        DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance(getString(R.string.title_transaction_failed), getString(R.string.operation_timed_out));
        fragmentMessage.show(getSupportFragmentManager(), null);
    };

    protected void enableClickableViews() {
        recyclerViewKeys.setEnabled(true);
    }

    protected void disableClickableViews() {
        recyclerViewKeys.setEnabled(false);
    }

    protected void sendMessage(final MeshMessage meshMessage) {
        try {
            if (!checkConnectivity())
                return;
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
            if (node != null) {
                mViewModel.getMeshManagerApi().createMeshPdu(node.getUnicastAddress(), meshMessage);
                showProgressbar();
            }
        } catch (IllegalArgumentException ex) {
            hideProgressBar();
            final DialogFragmentConfigError message = DialogFragmentConfigError.
                    newInstance(getString(R.string.title_error), ex.getMessage());
            message.show(getSupportFragmentManager(), null);
        }
    }
}
