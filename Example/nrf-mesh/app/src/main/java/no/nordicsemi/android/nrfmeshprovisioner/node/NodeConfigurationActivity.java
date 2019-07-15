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
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.NetworkKey;
import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyAdd;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyDelete;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigCompositionDataGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigCompositionDataStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNodeReset;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNodeResetStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigProxyGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigProxyStatus;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.ProxyConfigFilterStatus;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigError;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigurationComplete;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentProxySet;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmeshprovisioner.keys.AppKeysActivity;
import no.nordicsemi.android.nrfmeshprovisioner.keys.adapter.AddedAppKeyAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.keys.adapter.AddedNetKeyAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.node.adapter.ElementAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.node.dialog.DialogFragmentElementName;
import no.nordicsemi.android.nrfmeshprovisioner.node.dialog.DialogFragmentNodeName;
import no.nordicsemi.android.nrfmeshprovisioner.node.dialog.DialogFragmentResetNode;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NodeConfigurationViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class NodeConfigurationActivity extends AppCompatActivity implements Injectable,
        DialogFragmentNodeName.DialogFragmentNodeNameListener,
        DialogFragmentElementName.DialogFragmentElementNameListener,
        ElementAdapter.OnItemClickListener,
        DialogFragmentResetNode.DialogFragmentNodeResetListener,
        AddedAppKeyAdapter.OnItemClickListener,
        ItemTouchHelperAdapter,
        DialogFragmentConfigurationComplete.ConfigurationCompleteListener {

    private final static String TAG = NodeConfigurationActivity.class.getSimpleName();
    private static final String PROGRESS_BAR_STATE = "PROGRESS_BAR_STATE";
    private static final String PROXY_STATE = "PROXY_STATE";
    private static final String REQUESTED_PROXY_STATE = "REQUESTED_PROXY_STATE";
    private static final String DIALOG_FRAGMENT_APP_KEY_STATUS = "DIALOG_FRAGMENT_APP_KEY_STATUS";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.container)
    CoordinatorLayout mContainer;
    @BindView(R.id.nested_scroll_view)
    NestedScrollView mNestedScrollView;
    @BindView(R.id.action_get_composition_data)
    Button actionGetCompositionData;
    @BindView(R.id.action_add_app_keys)
    Button actionAddAppkey;
    @BindView(R.id.node_proxy_state_card)
    View mProxyStateCard;
    @BindView(R.id.proxy_state_summary)
    TextView mProxyStateRationaleSummary;
    @BindView(R.id.action_get_proxy_state)
    Button actionGetProxyState;
    @BindView(R.id.action_set_proxy_state)
    Button actionSetProxyState;
    @BindView(R.id.action_reset_node)
    Button actionResetNode;
    @BindView(R.id.recycler_view_elements)
    RecyclerView mRecyclerViewElements;
    @BindView(R.id.composition_data_card)
    CardView mCompositionDataCard;
    @BindView(R.id.configuration_progress_bar)
    ProgressBar mProgressbar;

    private NodeConfigurationViewModel mViewModel;
    private Handler mHandler;
    private boolean mProxyState;
    private boolean mRequestedState = true;
    private boolean mIsConnected;
    private AddedAppKeyAdapter appKeyAdapter;

    private final Runnable mOperationTimeout = () -> {
        hideProgressBar();
        DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance(getString(R.string.title_transaction_failed), getString(R.string.operation_timed_out));
        fragmentMessage.show(getSupportFragmentManager(), null);
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_configuration);
        ButterKnife.bind(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(NodeConfigurationViewModel.class);

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
        final TextView noAppKeysFound = findViewById(R.id.no_app_keys);
        final View compositionActionContainer = findViewById(R.id.composition_action_container);
        mRecyclerViewElements.setLayoutManager(new LinearLayoutManager(this));
        final ElementAdapter adapter = new ElementAdapter(this, mViewModel.getSelectedMeshNode());
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);
        mRecyclerViewElements.setAdapter(adapter);

        final RecyclerView recyclerViewAppKeys = findViewById(R.id.recycler_view_added_app_keys);
        recyclerViewAppKeys.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAppKeys.setItemAnimator(new DefaultItemAnimator());
        mViewModel.getMeshNetworkLiveData().getAppKeys();
        appKeyAdapter = new AddedAppKeyAdapter(this,
                mViewModel.getMeshNetworkLiveData().getAppKeys(), mViewModel.getSelectedMeshNode().getValue());
        recyclerViewAppKeys.setAdapter(appKeyAdapter);

        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewAppKeys);

        final RecyclerView recyclerViewNetKeys = findViewById(R.id.recycler_view_net_keys);
        recyclerViewNetKeys.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNetKeys.setItemAnimator(new DefaultItemAnimator());
        mViewModel.getMeshNetworkLiveData().getAppKeys();
        final AddedNetKeyAdapter netKeyAdapter = new AddedNetKeyAdapter(this,
                mViewModel.getMeshNetworkLiveData().getNetworkKeys(), mViewModel.getSelectedMeshNode());
        recyclerViewNetKeys.setAdapter(netKeyAdapter);

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

            if (!meshNode.getAddedAppKeyIndexes().isEmpty()) {
                noAppKeysFound.setVisibility(View.GONE);
                recyclerViewAppKeys.setVisibility(View.VISIBLE);
            } else {
                noAppKeysFound.setVisibility(View.VISIBLE);
                recyclerViewAppKeys.setVisibility(View.GONE);
            }
        });

        actionGetCompositionData.setOnClickListener(v -> {
            if (!checkConnectivity()) return;
            final ConfigCompositionDataGet configCompositionDataGet = new ConfigCompositionDataGet();
            sendMessage(configCompositionDataGet);
        });

        actionAddAppkey.setOnClickListener(v -> {
            if (!checkConnectivity()) return;
            final Intent addAppKeys = new Intent(NodeConfigurationActivity.this, AppKeysActivity.class);
            addAppKeys.putExtra(Utils.EXTRA_DATA, Utils.ADD_APP_KEY);
            startActivityForResult(addAppKeys, AppKeysActivity.SELECT_APP_KEY);
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
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppKeysActivity.SELECT_APP_KEY) {
            if (resultCode == RESULT_OK) {
                final ApplicationKey appKey = data.getParcelableExtra(Utils.RESULT_APP_KEY);
                if (appKey != null) {
                    final NetworkKey networkKey = mViewModel.getMeshNetworkLiveData().getMeshNetwork().getPrimaryNetworkKey();
                    if (networkKey != null) {
                        final ConfigAppKeyAdd configAppKeyAdd = new ConfigAppKeyAdd(networkKey, appKey);
                        sendMessage(configAppKeyAdd);
                    }
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
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
    public void onItemClick(final ApplicationKey appKey) {

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
        final MeshNetwork network = mViewModel.getMeshNetworkLiveData().getMeshNetwork();
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        if (node != null) {
            node.setNodeName(nodeName);
            return network.updateNodeName(node, nodeName);
        }
        return false;
    }

    @Override
    public boolean onElementNameUpdated(@NonNull final Element element, @NonNull final String name) {
        final MeshNetwork network = mViewModel.getMeshNetworkLiveData().getMeshNetwork();
        if (network != null) {
            network.updateElementName(element, name);
        }

        return true;
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        deleteAppKey(viewHolder.getAdapterPosition());
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {

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
        mHandler.postDelayed(mOperationTimeout, Utils.MESSAGE_TIME_OUT);
        disableClickableViews();
        mProgressbar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        enableClickableViews();
        mProgressbar.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacks(mOperationTimeout);
    }

    private void enableClickableViews() {
        actionGetCompositionData.setEnabled(true);
        actionAddAppkey.setEnabled(true);
        actionGetProxyState.setEnabled(true);
        actionSetProxyState.setEnabled(true);
        actionResetNode.setEnabled(true);
    }

    private void disableClickableViews() {
        actionGetCompositionData.setEnabled(false);
        actionAddAppkey.setEnabled(false);
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
        } else if (meshMessage instanceof ConfigAppKeyStatus) {
            if (!((ConfigAppKeyStatus) meshMessage).isSuccessful()) {
                if (getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_APP_KEY_STATUS) == null) {
                    final DialogFragmentConfigurationComplete fragmentAppKeyAddStatus = DialogFragmentConfigurationComplete.
                            newInstance(getString(R.string.title_appkey_status), ((ConfigAppKeyStatus) meshMessage).getStatusCodeName());
                    fragmentAppKeyAddStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_APP_KEY_STATUS);
                }
            } else {
                final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
                if (node != null) {
                    appKeyAdapter.updateAppKeyAdapter(node, mViewModel.getMeshNetworkLiveData().getMeshNetwork().getAppKeys());
                }
            }
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

    private void deleteAppKey(final int position) {
        if (!checkConnectivity()) {
            appKeyAdapter.notifyItemChanged(position);
            return;
        }

        final ApplicationKey applicationKey = appKeyAdapter.getItem(position);
        if (applicationKey != null) {
            final MeshNetwork network = mViewModel.getMeshNetworkLiveData().getMeshNetwork();
            if (network != null) {
                final NetworkKey networkKey = network.getNetKey(applicationKey.getBoundNetKeyIndex());
                final ConfigAppKeyDelete configAppKeyDelete = new ConfigAppKeyDelete(networkKey, applicationKey);
                sendMessage(configAppKeyDelete);
            }
        }
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
            final DialogFragmentConfigError message = DialogFragmentConfigError.
                    newInstance(getString(R.string.title_error), ex.getMessage());
            message.show(getSupportFragmentManager(), null);
        }
    }
}
