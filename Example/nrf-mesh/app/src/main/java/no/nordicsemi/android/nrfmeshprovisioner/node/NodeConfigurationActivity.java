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

package no.nordicsemi.android.nrfmeshprovisioner.node;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.transport.ConfigCompositionDataGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigCompositionDataStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigDefaultTtlGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigDefaultTtlSet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigDefaultTtlStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNodeReset;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNodeResetStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigProxyGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigProxySet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigProxyStatus;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.ProxyConfigFilterStatus;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigurationComplete;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentProxySet;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmeshprovisioner.keys.AddAppKeysActivity;
import no.nordicsemi.android.nrfmeshprovisioner.keys.AddNetKeysActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.adapter.ElementAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.node.dialog.DialogFragmentElementName;
import no.nordicsemi.android.nrfmeshprovisioner.node.dialog.DialogFragmentNodeName;
import no.nordicsemi.android.nrfmeshprovisioner.node.dialog.DialogFragmentResetNode;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.dialogs.DialogFragmentTtl;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NodeConfigurationViewModel;

public class NodeConfigurationActivity extends AppCompatActivity implements Injectable,
        DialogFragmentNodeName.DialogFragmentNodeNameListener,
        DialogFragmentElementName.DialogFragmentElementNameListener,
        DialogFragmentTtl.DialogFragmentTtlListener,
        DialogFragmentProxySet.DialogFragmentProxySetListener,
        ElementAdapter.OnItemClickListener,
        DialogFragmentResetNode.DialogFragmentNodeResetListener,
        DialogFragmentConfigurationComplete.ConfigurationCompleteListener {

    private static final String PROGRESS_BAR_STATE = "PROGRESS_BAR_STATE";
    private static final String PROXY_STATE = "PROXY_STATE";
    private static final String REQUESTED_PROXY_STATE = "REQUESTED_PROXY_STATE";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.container)
    CoordinatorLayout mContainer;
    @BindView(R.id.action_get_composition_data)
    Button actionGetCompositionData;
    @BindView(R.id.node_proxy_state_card)
    View mProxyStateCard;
    @BindView(R.id.proxy_state_summary)
    TextView mProxyStateRationaleSummary;
    @BindView(R.id.action_get_default_ttl)
    Button actionGetDefaultTtl;
    @BindView(R.id.action_set_default_ttl)
    Button actionSetDefaultTtl;
    @BindView(R.id.action_get_proxy_state)
    Button actionGetProxyState;
    @BindView(R.id.action_set_proxy_state)
    Button actionSetProxyState;
    @BindView(R.id.action_reset_node)
    Button actionResetNode;
    @BindView(R.id.recycler_view_elements)
    RecyclerView mRecyclerViewElements;
    @BindView(R.id.configuration_progress_bar)
    ProgressBar mProgressbar;

    private NodeConfigurationViewModel mViewModel;
    private Handler mHandler;
    private boolean mProxyState;
    private boolean mRequestedState = true;
    private boolean mIsConnected;

    private final Runnable mRunnableOperationTimeout = () -> {
        hideProgressBar();
        if (mViewModel.isActivityVisibile()) {
            DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.
                    newInstance(getString(R.string.title_transaction_failed), getString(R.string.operation_timed_out));
            fragmentMessage.show(getSupportFragmentManager(), null);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_configuration);
        ButterKnife.bind(this);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(NodeConfigurationViewModel.class);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(PROGRESS_BAR_STATE)) {
                mProgressbar.setVisibility(View.VISIBLE);
                disableClickableViews();
            } else {
                mProgressbar.setVisibility(View.INVISIBLE);
                enableClickableViews();
            }
            mRequestedState = savedInstanceState.getBoolean(PROXY_STATE, true);
            mProxyState = savedInstanceState.getBoolean(PROXY_STATE, true);
        }

        mHandler = new Handler();
        if (mViewModel.getSelectedMeshNode().getValue() == null) {
            finish();
        }
        // Set up views
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_node_configuration);

        final View containerNodeName = findViewById(R.id.container_node_name);
        containerNodeName.findViewById(R.id.image)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.ic_label_black_alpha_24dp));
        final TextView nodeNameTitle = containerNodeName.findViewById(R.id.title);
        nodeNameTitle.setText(R.string.title_node_name);
        final TextView nodeNameView = containerNodeName.findViewById(R.id.text);
        nodeNameView.setVisibility(View.VISIBLE);
        containerNodeName.setOnClickListener(v -> {
            final DialogFragmentNodeName fragment = DialogFragmentNodeName.
                    newInstance(nodeNameView.getText().toString());
            fragment.show(getSupportFragmentManager(), null);
        });
        final Button actionDetails = findViewById(R.id.action_show_details);
        actionDetails.setOnClickListener(v -> {
            final Intent intent = new Intent(NodeConfigurationActivity.this, NodeDetailsActivity.class);
            startActivity(intent);
        });

        final TextView noElementsFound = findViewById(R.id.no_elements);
        final View compositionActionContainer = findViewById(R.id.composition_action_container);
        mRecyclerViewElements.setLayoutManager(new LinearLayoutManager(this));
        final ElementAdapter adapter = new ElementAdapter(this, mViewModel.getSelectedMeshNode());
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);
        mRecyclerViewElements.setAdapter(adapter);

        final View containerNetKey = findViewById(R.id.container_net_keys);
        containerNetKey.findViewById(R.id.image)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_black_alpha_24dp));
        final TextView keyTitle = containerNetKey.findViewById(R.id.title);
        keyTitle.setText(R.string.title_net_keys);
        final TextView netKeySummary = containerNetKey.findViewById(R.id.text);
        netKeySummary.setVisibility(View.VISIBLE);
        containerNetKey.setOnClickListener(v -> {
            final Intent intent = new Intent(this, AddNetKeysActivity.class);
            startActivity(intent);
        });

        final View containerAppKey = findViewById(R.id.container_app_keys);
        containerAppKey.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_black_alpha_24dp));
        ((TextView) containerAppKey.findViewById(R.id.title)).setText(R.string.title_app_keys);
        final TextView appKeySummary = containerAppKey.findViewById(R.id.text);
        appKeySummary.setVisibility(View.VISIBLE);
        containerAppKey.setOnClickListener(v -> {
            final Intent intent = new Intent(this, AddAppKeysActivity.class);
            startActivity(intent);
        });

        final View containerDefaultTtl = findViewById(R.id.container_ttl);
        containerDefaultTtl.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_numeric));
        ((TextView) containerDefaultTtl.findViewById(R.id.title)).setText(R.string.title_ttl);
        final TextView defaultTtlSummary = containerDefaultTtl.findViewById(R.id.text);
        defaultTtlSummary.setVisibility(View.VISIBLE);

        mViewModel.getSelectedMeshNode().observe(this, meshNode -> {
            if (meshNode == null) {
                finish();
                return;
            }
            getSupportActionBar().setSubtitle(meshNode.getNodeName());
            nodeNameView.setText(meshNode.getNodeName());

            updateClickableViews();

            if (!meshNode.getElements().isEmpty()) {
                compositionActionContainer.setVisibility(View.GONE);
                noElementsFound.setVisibility(View.INVISIBLE);
                mRecyclerViewElements.setVisibility(View.VISIBLE);
            } else {
                noElementsFound.setVisibility(View.VISIBLE);
                compositionActionContainer.setVisibility(View.VISIBLE);
                mRecyclerViewElements.setVisibility(View.INVISIBLE);
            }

            if (!meshNode.getAddedNetKeys().isEmpty()) {
                netKeySummary.setText(String.valueOf(meshNode.getAddedNetKeys().size()));
            } else {
                netKeySummary.setText(R.string.no_app_keys_added);
            }

            if (!meshNode.getAddedAppKeys().isEmpty()) {
                appKeySummary.setText(String.valueOf(meshNode.getAddedAppKeys().size()));
            } else {
                appKeySummary.setText(R.string.no_app_keys_added);
            }

            if (meshNode.getTtl() != null) {
                defaultTtlSummary.setText(String.valueOf(meshNode.getTtl()));
            } else {
                defaultTtlSummary.setText(R.string.unknown);
            }
        });

        actionGetCompositionData.setOnClickListener(v -> {
            if (!checkConnectivity()) return;
            final ConfigCompositionDataGet configCompositionDataGet = new ConfigCompositionDataGet();
            sendMessage(configCompositionDataGet);
        });

        actionGetDefaultTtl.setOnClickListener(v -> {
            if (!checkConnectivity()) return;
            final ConfigDefaultTtlGet defaultTtlGet = new ConfigDefaultTtlGet();
            sendMessage(defaultTtlGet);
        });

        actionSetDefaultTtl.setOnClickListener(v -> {
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
            if (node != null) {
                DialogFragmentTtl fragmentTtl = DialogFragmentTtl.newInstance(node.getTtl() == null ? -1 : node.getTtl());
                fragmentTtl.show(getSupportFragmentManager(), null);
            }
        });

        actionGetProxyState.setOnClickListener(v -> {
            if (!checkConnectivity()) return;
            final ConfigProxyGet configProxyGet = new ConfigProxyGet();
            sendMessage(configProxyGet);
        });

        actionSetProxyState.setOnClickListener(v -> {
            final String message;
            if (mProxyState) {
                message = getString(R.string.proxy_set_off_rationale_summary);
            } else {
                message = getString(R.string.proxy_set_on_rationale_summary);
            }
            final DialogFragmentProxySet resetNodeFragment = DialogFragmentProxySet.
                    newInstance(getString(R.string.title_proxy_state_settings), message, !mProxyState);
            resetNodeFragment.show(getSupportFragmentManager(), null);
        });

        actionResetNode.setOnClickListener(v -> {
            if (!checkConnectivity()) return;
            final DialogFragmentResetNode resetNodeFragment = DialogFragmentResetNode.
                    newInstance(getString(R.string.title_reset_node), getString(R.string.reset_node_rationale_summary));
            resetNodeFragment.show(getSupportFragmentManager(), null);
        });

        mViewModel.getTransactionStatus().observe(this, transactionStatus -> {
            if (transactionStatus != null) {
                hideProgressBar();
                final String message;
                if (transactionStatus.isIncompleteTimerExpired()) {
                    message = getString(R.string.segments_not_received_timed_out);
                } else {
                    message = getString(R.string.operation_timed_out);
                }
                DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance(getString(R.string.title_transaction_failed), message);
                fragmentMessage.show(getSupportFragmentManager(), null);
            }
        });

        mViewModel.isConnectedToProxy().observe(this, isConnected -> {
            if (isConnected != null) {
                mIsConnected = isConnected;
                hideProgressBar();
            }
            updateClickableViews();
            invalidateOptionsMenu();
        });

        mViewModel.getMeshMessage().observe(this, this::updateMeshMessage);

        updateProxySettingsCardUi();

        final Boolean isConnectedToNetwork = mViewModel.isConnectedToProxy().getValue();
        if (isConnectedToNetwork != null) {
            mIsConnected = isConnectedToNetwork;
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.setActivityVisible(true);
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
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
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
    protected void onStop() {
        super.onStop();
        mViewModel.setActivityVisible(false);
        if (isFinishing()) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PROGRESS_BAR_STATE, mProgressbar.getVisibility() == View.VISIBLE);
        outState.putBoolean(PROXY_STATE, mProxyState);
        outState.putBoolean(REQUESTED_PROXY_STATE, mRequestedState);
    }

    @Override
    public void onElementClicked(@NonNull final Element element) {
        final DialogFragmentElementName fragmentElementName = DialogFragmentElementName.newInstance(element);
        fragmentElementName.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onModelClicked(@NonNull final ProvisionedMeshNode meshNode, @NonNull final Element element, @NonNull final MeshModel model) {
        mViewModel.setSelectedElement(element);
        mViewModel.setSelectedModel(model);
        mViewModel.navigateToModelActivity(this, model);
    }

    @Override
    public void onNodeReset() {
        final ConfigNodeReset configNodeReset = new ConfigNodeReset();
        sendMessage(configNodeReset);
    }

    @Override
    public void onConfigurationCompleted() {
        //Do nothing
    }

    @Override
    public boolean onNodeNameUpdated(@NonNull final String nodeName) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        if (node != null) {
            node.setNodeName(nodeName);
            return network.updateNodeName(node, nodeName);
        }
        return false;
    }

    @Override
    public boolean onElementNameUpdated(@NonNull final Element element, @NonNull final String name) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        if (network != null) {
            network.updateElementName(element, name);
        }

        return true;
    }

    private void updateProxySettingsCardUi() {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
        if (meshNode != null && meshNode.getNodeFeatures() != null && meshNode.getNodeFeatures().isProxyFeatureSupported()) {
            mProxyStateCard.setVisibility(View.VISIBLE);
            updateProxySettingsButtonUi();
        }
    }

    private void updateProxySettingsButtonUi() {
        if (mProxyState) {
            mProxyStateRationaleSummary.setText(R.string.proxy_set_off_rationale);
            actionSetProxyState.setText(R.string.action_proxy_state_set_off);
        } else {
            mProxyStateRationaleSummary.setText(R.string.proxy_set_on_rationale);
            actionSetProxyState.setText(R.string.action_proxy_state_set_on);
        }
    }

    private void showProgressbar() {
        mHandler.postDelayed(mRunnableOperationTimeout, Utils.MESSAGE_TIME_OUT);
        disableClickableViews();
        mProgressbar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        enableClickableViews();
        mProgressbar.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacks(mRunnableOperationTimeout);
    }

    private void enableClickableViews() {
        actionGetCompositionData.setEnabled(true);
        actionGetDefaultTtl.setEnabled(true);
        actionSetDefaultTtl.setEnabled(true);
        actionGetProxyState.setEnabled(true);
        actionSetProxyState.setEnabled(true);
        actionResetNode.setEnabled(true);
    }

    private void disableClickableViews() {
        actionGetCompositionData.setEnabled(false);
        actionGetDefaultTtl.setEnabled(false);
        actionSetDefaultTtl.setEnabled(false);
        actionGetProxyState.setEnabled(false);
        actionSetProxyState.setEnabled(false);
        actionResetNode.setEnabled(false);
    }

    private void updateMeshMessage(final MeshMessage meshMessage) {
        if (meshMessage instanceof ProxyConfigFilterStatus) {
            hideProgressBar();
        }
        if (meshMessage instanceof ConfigCompositionDataStatus) {
            hideProgressBar();
        } else if (meshMessage instanceof ConfigDefaultTtlStatus) {
            hideProgressBar();
        } else if (meshMessage instanceof ConfigNodeResetStatus) {
            hideProgressBar();
            finish();
        } else if (meshMessage instanceof ConfigProxyStatus) {
            final ConfigProxyStatus status = (ConfigProxyStatus) meshMessage;
            mProxyState = status.isProxyFeatureEnabled();
            updateProxySettingsCardUi();
            hideProgressBar();
        }
    }

    protected final boolean checkConnectivity() {
        if (!mIsConnected) {
            mViewModel.displayDisconnectedSnackBar(this, mContainer);
            return false;
        }
        return true;
    }

    private void updateClickableViews() {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
        if (meshNode != null && meshNode.isConfigured() &&
                !mViewModel.isModelExists(SigModelParser.CONFIGURATION_SERVER))
            disableClickableViews();
    }

    private void sendMessage(final MeshMessage meshMessage) {
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
            final DialogFragmentError message = DialogFragmentError.
                    newInstance(getString(R.string.title_error), ex.getMessage());
            message.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public boolean setDefaultTtl(final int ttl) {
        final ConfigDefaultTtlSet ttlSet = new ConfigDefaultTtlSet(ttl);
        sendMessage(ttlSet);
        return true;
    }

    @Override
    public void onProxySet(final int state) {
        final ConfigProxySet configProxySet = new ConfigProxySet(state);
        sendMessage(configProxySet);
        mRequestedState = state == 1;
    }
}
