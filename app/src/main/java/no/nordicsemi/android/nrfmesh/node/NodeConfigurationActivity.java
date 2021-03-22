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

package no.nordicsemi.android.nrfmesh.node;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.models.SigModelParser;
import no.nordicsemi.android.mesh.transport.ConfigCompositionDataGet;
import no.nordicsemi.android.mesh.transport.ConfigCompositionDataStatus;
import no.nordicsemi.android.mesh.transport.ConfigDefaultTtlGet;
import no.nordicsemi.android.mesh.transport.ConfigDefaultTtlSet;
import no.nordicsemi.android.mesh.transport.ConfigDefaultTtlStatus;
import no.nordicsemi.android.mesh.transport.ConfigNodeReset;
import no.nordicsemi.android.mesh.transport.ConfigNodeResetStatus;
import no.nordicsemi.android.mesh.transport.ConfigProxyGet;
import no.nordicsemi.android.mesh.transport.ConfigProxySet;
import no.nordicsemi.android.mesh.transport.ConfigProxyStatus;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.transport.ProxyConfigFilterStatus;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityNodeConfigurationBinding;
import no.nordicsemi.android.nrfmesh.databinding.LayoutContainerBinding;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentConfigurationComplete;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentProxySet;
import no.nordicsemi.android.nrfmesh.keys.AddAppKeysActivity;
import no.nordicsemi.android.nrfmesh.keys.AddNetKeysActivity;
import no.nordicsemi.android.nrfmesh.node.adapter.ElementAdapter;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentElementName;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentNodeName;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentResetNode;
import no.nordicsemi.android.nrfmesh.provisioners.dialogs.DialogFragmentTtl;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.BaseActivity;
import no.nordicsemi.android.nrfmesh.viewmodels.NodeConfigurationViewModel;

@AndroidEntryPoint
public class NodeConfigurationActivity extends BaseActivity implements
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

    private ActivityNodeConfigurationBinding binding;

    private boolean mProxyState;
    private boolean mRequestedState = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNodeConfigurationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mViewModel = new ViewModelProvider(this).get(NodeConfigurationViewModel.class);
        initialize();

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(PROGRESS_BAR_STATE)) {
                binding.configurationProgressBar.setVisibility(View.VISIBLE);
                disableClickableViews();
            } else {
                binding.configurationProgressBar.setVisibility(View.INVISIBLE);
                enableClickableViews();
            }
            mRequestedState = savedInstanceState.getBoolean(PROXY_STATE, true);
            mProxyState = savedInstanceState.getBoolean(PROXY_STATE, true);
        }

        if (mViewModel.getSelectedMeshNode().getValue() == null) {
            finish();
        }
        // Set up views
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_node_configuration);

        final LayoutContainerBinding containerNodeName = binding.containerNodeName;
        containerNodeName.image
                .setBackground(ContextCompat.getDrawable(this, R.drawable.ic_label));
        containerNodeName.title.setText(R.string.title_node_name);
        final TextView nodeNameView = containerNodeName.text;
        nodeNameView.setVisibility(View.VISIBLE);
        containerNodeName.getRoot().setOnClickListener(v -> {
            final DialogFragmentNodeName fragment = DialogFragmentNodeName.
                    newInstance(nodeNameView.getText().toString());
            fragment.show(getSupportFragmentManager(), null);
        });
        final Button actionDetails = findViewById(R.id.action_show_details);
        actionDetails.setOnClickListener(v -> startActivity(new Intent(NodeConfigurationActivity.this, NodeDetailsActivity.class)));

        binding.recyclerViewElements.setLayoutManager(new LinearLayoutManager(this));
        final ElementAdapter adapter = new ElementAdapter(this, mViewModel.getSelectedMeshNode());
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);
        binding.recyclerViewElements.setAdapter(adapter);

        binding.containerNetKeys.image
                .setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_24dp));
        binding.containerNetKeys.title.setText(R.string.title_net_keys);
        final TextView netKeySummary = binding.containerNetKeys.text;
        netKeySummary.setVisibility(View.VISIBLE);
        binding.containerNetKeys.getRoot().setOnClickListener(v -> startActivity(new Intent(this, AddNetKeysActivity.class)));

        binding.containerAppKeys.image.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_24dp));
        binding.containerAppKeys.title.setText(R.string.title_app_keys);
        final TextView appKeySummary = binding.containerAppKeys.text;
        appKeySummary.setVisibility(View.VISIBLE);
        binding.containerAppKeys.getRoot().setOnClickListener(v -> startActivity(new Intent(this, AddAppKeysActivity.class)));

        binding.containerTtl.image.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_numeric));
        binding.containerTtl.title.setText(R.string.title_ttl);
        final TextView defaultTtlSummary = binding.containerTtl.text;
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
                binding.compositionActionContainer.setVisibility(View.GONE);
                binding.noElements.setVisibility(View.INVISIBLE);
                binding.recyclerViewElements.setVisibility(View.VISIBLE);
            } else {
                binding.noElements.setVisibility(View.VISIBLE);
                binding.compositionActionContainer.setVisibility(View.VISIBLE);
                binding.recyclerViewElements.setVisibility(View.INVISIBLE);
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

        binding.actionGetCompositionData.setOnClickListener(v -> {
            if (!checkConnectivity(binding.container)) return;
            final ConfigCompositionDataGet configCompositionDataGet = new ConfigCompositionDataGet();
            sendMessage(configCompositionDataGet);
        });

        binding.actionGetDefaultTtl.setOnClickListener(v -> {
            if (!checkConnectivity(binding.container)) return;
            final ConfigDefaultTtlGet defaultTtlGet = new ConfigDefaultTtlGet();
            sendMessage(defaultTtlGet);
        });

        binding.actionSetDefaultTtl.setOnClickListener(v -> {
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
            if (node != null) {
                DialogFragmentTtl fragmentTtl = DialogFragmentTtl.newInstance(node.getTtl() == null ? -1 : node.getTtl());
                fragmentTtl.show(getSupportFragmentManager(), null);
            }
        });

        binding.actionGetProxyState.setOnClickListener(v -> {
            if (!checkConnectivity(binding.container)) return;
            final ConfigProxyGet configProxyGet = new ConfigProxyGet();
            sendMessage(configProxyGet);
        });

        binding.actionSetProxyState.setOnClickListener(v -> {
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

        binding.actionResetNode.setOnClickListener(v -> {
            if (!checkConnectivity(binding.container)) return;
            final DialogFragmentResetNode resetNodeFragment = DialogFragmentResetNode.
                    newInstance(getString(R.string.title_reset_node), getString(R.string.reset_node_rationale_summary));
            resetNodeFragment.show(getSupportFragmentManager(), null);
        });

        updateProxySettingsCardUi();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.setActivityVisible(true);
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
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PROGRESS_BAR_STATE, binding.configurationProgressBar.getVisibility() == View.VISIBLE);
        outState.putBoolean(PROXY_STATE, mProxyState);
        outState.putBoolean(REQUESTED_PROXY_STATE, mRequestedState);
    }

    @Override
    public void onElementClicked(@NonNull final Element element) {
        DialogFragmentElementName.newInstance(element).show(getSupportFragmentManager(), null);
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
            binding.nodeProxyStateCard.setVisibility(View.VISIBLE);
            updateProxySettingsButtonUi();
        }
    }

    private void updateProxySettingsButtonUi() {
        if (mProxyState) {
            binding.proxyStateSummary.setText(R.string.proxy_set_off_rationale);
            binding.actionSetProxyState.setText(R.string.action_proxy_state_set_off);
        } else {
            binding.proxyStateSummary.setText(R.string.proxy_set_on_rationale);
            binding.actionSetProxyState.setText(R.string.action_proxy_state_set_on);
        }
    }


    protected void showProgressBar() {
        mHandler.postDelayed(mRunnableOperationTimeout, Utils.MESSAGE_TIME_OUT);
        disableClickableViews();
        binding.configurationProgressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        enableClickableViews();
        binding.configurationProgressBar.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacks(mRunnableOperationTimeout);
    }

    protected void enableClickableViews() {
        binding.actionGetCompositionData.setEnabled(true);
        binding.actionGetDefaultTtl.setEnabled(true);
        binding.actionSetDefaultTtl.setEnabled(true);
        binding.actionGetProxyState.setEnabled(true);
        binding.actionSetProxyState.setEnabled(true);
        binding.actionResetNode.setEnabled(true);
    }


    protected void disableClickableViews() {
        binding.actionGetCompositionData.setEnabled(false);
        binding.actionGetDefaultTtl.setEnabled(false);
        binding.actionSetDefaultTtl.setEnabled(false);
        binding.actionGetProxyState.setEnabled(false);
        binding.actionSetProxyState.setEnabled(false);
        binding.actionResetNode.setEnabled(false);
    }


    protected void updateMeshMessage(final MeshMessage meshMessage) {
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

    protected void updateClickableViews() {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
        if (meshNode != null && meshNode.isConfigured() &&
                !mViewModel.isModelExists(SigModelParser.CONFIGURATION_SERVER))
            disableClickableViews();
    }

    private void sendMessage(final MeshMessage meshMessage) {
        try {
            if (!checkConnectivity(binding.container))
                return;
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
            if (node != null) {
                mViewModel.getMeshManagerApi().createMeshPdu(node.getUnicastAddress(), meshMessage);
                showProgressBar();
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
