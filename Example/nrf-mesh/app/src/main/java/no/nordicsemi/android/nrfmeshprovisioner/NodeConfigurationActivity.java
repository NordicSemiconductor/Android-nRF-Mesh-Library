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

package no.nordicsemi.android.nrfmeshprovisioner;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.models.ConfigurationServerModel;
import no.nordicsemi.android.meshprovisioner.models.GenericLevelServerModel;
import no.nordicsemi.android.meshprovisioner.models.GenericOnOffServerModel;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyAdd;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigCompositionDataGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigCompositionDataStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNodeReset;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNodeResetStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigProxyGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigProxySet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigProxyStatus;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.ProxyConfigAddAddressToFilter;
import no.nordicsemi.android.meshprovisioner.transport.ProxyConfigFilterStatus;
import no.nordicsemi.android.meshprovisioner.transport.ProxyConfigRemoveAddressFromFilter;
import no.nordicsemi.android.meshprovisioner.transport.ProxyConfigSetFilterType;
import no.nordicsemi.android.meshprovisioner.utils.AddressArray;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.ProxyFilter;
import no.nordicsemi.android.meshprovisioner.utils.ProxyFilterType;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.AddedAppKeyAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ElementAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.FilterAddressAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentAppKeyAddStatus;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentFilterAddAddress;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentProxySet;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentResetNode;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NodeConfigurationViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class NodeConfigurationActivity extends AppCompatActivity implements Injectable,
        ElementAdapter.OnItemClickListener,
        DialogFragmentAppKeyAddStatus.DialogFragmentAppKeyAddStatusListener,
        DialogFragmentProxySet.DialogFragmentProxySetListener,
        DialogFragmentFilterAddAddress.DialogFragmentFilterAddressListener,
        DialogFragmentResetNode.DialogFragmentNodeResetListener,
        AddedAppKeyAdapter.OnItemClickListener,
        ItemTouchHelperAdapter {

    private final static String TAG = NodeConfigurationActivity.class.getSimpleName();
    private static final String PROGRESS_BAR_STATE = "PROGRESS_BAR_STATE";
    private static final String PROXY_STATE = "PROXY_STATE";
    private static final String REQUESTED_PROXY_STATE = "REQUESTED_PROXY_STATE";
    private static final String DIALOG_FRAGMENT_APP_KEY_STATUS = "DIALOG_FRAGMENT_APP_KEY_STATUS";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.main_container)
    NestedScrollView mContainer;
    @BindView(R.id.action_get_compostion_data)
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
    @BindView(R.id.filter_switch)
    Switch actionSwitchFilter;
    @BindView(R.id.action_add_address)
    Button actionAddFilterAddress;
    @BindView(R.id.action_clear_addresses)
    Button actionClearFilterAddress;
    @BindView(R.id.action_reset_node)
    Button actionResetNode;
    @BindView(R.id.recycler_view_elements)
    RecyclerView mRecyclerViewElements;
    @BindView(R.id.composition_data_card)
    CardView mCompositionDataCard;
    @BindView(R.id.proxy_filter_card)
    CardView mProxyFilterCard;
    @BindView(R.id.configuration_progress_bar)
    ProgressBar mProgressbar;

    private NodeConfigurationViewModel mViewModel;
    private Handler mHandler;
    private boolean mProxyState;
    private boolean mRequestedState = true;


    private final Runnable mOperationTimeout = this::hideProgressBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesh_node_configuration);
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
        // Set up views
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_node_configuration);
        getSupportActionBar().setSubtitle(mViewModel.getSelectedMeshNode().getValue().getNodeName());

        final TextView noElementsFound = findViewById(R.id.no_elements);
        final TextView noAppKeysFound = findViewById(R.id.no_app_keys);
        final View compositionActionContainer = findViewById(R.id.composition_action_container);
        mRecyclerViewElements.setLayoutManager(new LinearLayoutManager(this));
        final ElementAdapter adapter = new ElementAdapter(this, mViewModel.getSelectedMeshNode());
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);
        mRecyclerViewElements.setAdapter(adapter);

        final RecyclerView recyclerViewAppKeys = findViewById(R.id.recycler_view_app_keys);
        recyclerViewAppKeys.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAppKeys.setItemAnimator(new DefaultItemAnimator());
        final AddedAppKeyAdapter appKeyAdapter = new AddedAppKeyAdapter(this, mViewModel.getSelectedMeshNode());
        recyclerViewAppKeys.setAdapter(appKeyAdapter);

        final TextView noAddressesAdded = findViewById(R.id.no_addresses);
        final RecyclerView recyclerViewAddresses = findViewById(R.id.recycler_view_addresses);

        final Integer unicast = mViewModel.getConnectedMeshNodeAddress().getValue();
        if (unicast != null && unicast == mViewModel.getSelectedMeshNode().getValue().getUnicastAddress()) {
            mProxyFilterCard.setVisibility(View.VISIBLE);
            recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewAddresses.setItemAnimator(new DefaultItemAnimator());
            final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
            final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
            itemTouchHelper.attachToRecyclerView(recyclerViewAddresses);
            final FilterAddressAdapter addressAdapter = new FilterAddressAdapter(this, mViewModel.getSelectedMeshNode());
            recyclerViewAddresses.setAdapter(addressAdapter);
        }

        mViewModel.getSelectedMeshNode().observe(this, meshNode -> {
            if (meshNode == null) {
                finish();
                return;
            }

            if (!meshNode.getElements().isEmpty()) {
                compositionActionContainer.setVisibility(View.GONE);
                noElementsFound.setVisibility(View.INVISIBLE);
                mRecyclerViewElements.setVisibility(View.VISIBLE);
            } else {
                noElementsFound.setVisibility(View.VISIBLE);
                compositionActionContainer.setVisibility(View.VISIBLE);
                mRecyclerViewElements.setVisibility(View.INVISIBLE);
            }

            if (!meshNode.getAddedApplicationKeys().isEmpty()) {
                noAppKeysFound.setVisibility(View.GONE);
                recyclerViewAppKeys.setVisibility(View.VISIBLE);
            } else {
                noAppKeysFound.setVisibility(View.VISIBLE);
                recyclerViewAppKeys.setVisibility(View.GONE);
            }
            final ProxyFilter filter = meshNode.getProxyFilter();
            if (filter != null) {
                actionSwitchFilter.setChecked(filter.getFilterType().getType() == ProxyFilterType.WHITE_LIST_FILTER);
                if (!filter.getAddresses().isEmpty()) {
                    noAddressesAdded.setVisibility(View.GONE);
                    recyclerViewAddresses.setVisibility(View.VISIBLE);
                    actionClearFilterAddress.setVisibility(View.VISIBLE);
                } else {
                    noAddressesAdded.setVisibility(View.VISIBLE);
                    recyclerViewAddresses.setVisibility(View.GONE);
                    actionClearFilterAddress.setVisibility(View.GONE);
                }
            }
        });

        actionGetCompositionData.setOnClickListener(v -> {
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
            final ConfigCompositionDataGet configCompositionDataGet = new ConfigCompositionDataGet();
            mViewModel.getMeshManagerApi().sendMeshMessage(node.getUnicastAddress(), configCompositionDataGet);
            showProgressbar();
        });

        actionAddAppkey.setOnClickListener(v -> {
            final Intent addAppKeys = new Intent(NodeConfigurationActivity.this, ManageAppKeysActivity.class);
            addAppKeys.putExtra(Utils.EXTRA_DATA, Utils.ADD_APP_KEY);
            startActivityForResult(addAppKeys, ManageAppKeysActivity.SELECT_APP_KEY);
        });

        actionGetProxyState.setOnClickListener(v -> {
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
            final ConfigProxyGet configProxyGet = new ConfigProxyGet();
            mViewModel.getMeshManagerApi().sendMeshMessage(node.getUnicastAddress(), configProxyGet);
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

        actionSwitchFilter.setOnClickListener(v -> {
            if (((Switch)v).isChecked()) {
                setFilter(new ProxyFilterType(ProxyFilterType.WHITE_LIST_FILTER));
                actionSwitchFilter.setText(R.string.white_list_filter);
            } else {
                setFilter(new ProxyFilterType(ProxyFilterType.BLACK_LIST_FILTER));
                actionSwitchFilter.setText(R.string.black_list_filter);
            }
        });

        actionAddFilterAddress.setOnClickListener(v -> {
            final ProxyFilter filter = mViewModel.getSelectedMeshNode().getValue().getProxyFilter();
            final ProxyFilterType filterType;
            if (filter == null) {
                filterType = new ProxyFilterType(ProxyFilterType.WHITE_LIST_FILTER);
            } else {
                filterType = filter.getFilterType();
            }
            final DialogFragmentFilterAddAddress filterAddAddress = DialogFragmentFilterAddAddress.newInstance(filterType);
            filterAddAddress.show(getSupportFragmentManager(), null);
        });

        actionClearFilterAddress.setOnClickListener(v -> removeAddresses());

        actionResetNode.setOnClickListener(v -> {
            final DialogFragmentResetNode resetNodeFragment = DialogFragmentResetNode.
                    newInstance(getString(R.string.title_reset_node), getString(R.string.reset_node_rationale_summary));
            resetNodeFragment.show(getSupportFragmentManager(), null);
        });

        mViewModel.getTransactionStatus().observe(this, transactionStatus -> {
            hideProgressBar();
            final String message;
            if (transactionStatus.isIncompleteTimerExpired()) {
                message = getString(R.string.segments_not_received_timed_out);
            } else {
                message = getString(R.string.operation_timed_out);
            }
            DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance(getString(R.string.title_transaction_failed), message);
            fragmentMessage.show(getSupportFragmentManager(), null);
        });

        mViewModel.isConnectedToProxy().observe(this, isConnected -> {
            if (isConnected != null && !isConnected)
                finish();
        });

        mViewModel.getMeshMessageLiveData().observe(this, this::updateMeshMessage);

        updateProxySettingsCardUi();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ManageAppKeysActivity.SELECT_APP_KEY) {
            if (resultCode == RESULT_OK) {
                final ApplicationKey appKey = data.getParcelableExtra(Utils.RESULT_APP_KEY);
                if (appKey != null) {
                    showProgressbar();
                    final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
                    final NetworkKey networkKey = mViewModel.getMeshManagerApi().getMeshNetwork().getPrimaryNetworkKey();
                    final ConfigAppKeyAdd configAppKeyAdd = new ConfigAppKeyAdd(networkKey, appKey);
                    mViewModel.getMeshManagerApi().sendMeshMessage(node.getUnicastAddress(), configAppKeyAdd);
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
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PROGRESS_BAR_STATE, mProgressbar.getVisibility() == View.VISIBLE);
        outState.putBoolean(PROXY_STATE, mProxyState);
        outState.putBoolean(REQUESTED_PROXY_STATE, mRequestedState);
    }

    @Override
    public void onElementItemClick(final ProvisionedMeshNode meshNode, final Element element, final MeshModel model) {
        mViewModel.setSelectedElement(element);
        mViewModel.setSelectedModel(model);
        startActivity(model);
    }

    @Override
    public void onAppKeyAddStatusReceived() {

    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        if (viewHolder instanceof FilterAddressAdapter.ViewHolder) {
            removeAddress(position);
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {

    }

    @Override
    public void onItemClick(final ApplicationKey appKey) {

    }

    @Override
    public void onNodeReset() {
        try {
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
            final ConfigNodeReset configNodeReset = new ConfigNodeReset();
            mViewModel.getMeshManagerApi().sendMeshMessage(node.getUnicastAddress(), configNodeReset);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public void onProxySet(@ConfigProxySet.ProxyState final int state) {
        try {
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
            final ConfigProxySet configProxySet = new ConfigProxySet(state);
            mViewModel.getMeshManagerApi().sendMeshMessage(node.getUnicastAddress(), configProxySet);
            mRequestedState = state == 1;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void updateProxySettingsCardUi() {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
        if (meshNode.getNodeFeatures() != null && meshNode.getNodeFeatures().isProxyFeatureSupported()) {
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
        disableClickableViews();
        mProgressbar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mHandler.removeCallbacks(mOperationTimeout);
        enableClickableViews();
        mProgressbar.setVisibility(View.INVISIBLE);
    }

    private void enableClickableViews() {
        actionGetCompositionData.setEnabled(true);
        actionAddAppkey.setEnabled(true);
        actionGetProxyState.setEnabled(true);
        actionSetProxyState.setEnabled(true);
        actionSwitchFilter.setEnabled(true);
        actionAddFilterAddress.setEnabled(true);
        actionClearFilterAddress.setEnabled(true);
        actionResetNode.setEnabled(true);
    }

    private void disableClickableViews() {
        actionGetCompositionData.setEnabled(false);
        actionAddAppkey.setEnabled(false);
        actionGetProxyState.setEnabled(false);
        actionSetProxyState.setEnabled(false);
        actionSwitchFilter.setEnabled(false);
        actionAddFilterAddress.setEnabled(false);
        actionClearFilterAddress.setEnabled(false);
        actionResetNode.setEnabled(false);
    }

    /**
     * Start activity based on the type of the model
     *
     * <p> This way we can seperate the ui logic for different activities</p>
     *
     * @param model model
     */
    private void startActivity(final MeshModel model) {
        final Intent intent;
        if (model instanceof ConfigurationServerModel) {
            intent = new Intent(this, ConfigurationServerActivity.class);
        } else if (model instanceof GenericOnOffServerModel) {
            intent = new Intent(this, GenericOnOffServerActivity.class);
        } else if (model instanceof GenericLevelServerModel) {
            intent = new Intent(this, GenericLevelServerActivity.class);
        } else if (model instanceof VendorModel) {
            intent = new Intent(this, VendorModelActivity.class);
        } else {
            intent = new Intent(this, ModelConfigurationActivity.class);
        }
        startActivity(intent);
    }

    private void updateMeshMessage(final MeshMessage meshMessage) {
        if (meshMessage instanceof ProxyConfigFilterStatus) {
            hideProgressBar();
        }
        if (meshMessage instanceof ConfigCompositionDataStatus) {
            hideProgressBar();
        } else if (meshMessage instanceof ConfigAppKeyStatus) {
            if (getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_APP_KEY_STATUS) == null) {
                if (!((ConfigAppKeyStatus) meshMessage).isSuccessful()) {
                    final DialogFragmentAppKeyAddStatus fragmentAppKeyAddStatus = DialogFragmentAppKeyAddStatus.
                            newInstance(getString(R.string.title_appkey_status), ((ConfigAppKeyStatus) meshMessage).getStatusCodeName());
                    fragmentAppKeyAddStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_APP_KEY_STATUS);
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

    @Override
    public void addAddresses(final List<AddressArray> addresses) {
        final ProxyConfigAddAddressToFilter addAddressToFilter = new ProxyConfigAddAddressToFilter(addresses);
        mViewModel.getMeshManagerApi().sendMeshMessage(MeshAddress.UNASSIGNED_ADDRESS, addAddressToFilter);
    }

    private void removeAddress(final int position) {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
        if(meshNode != null) {
            final ProxyFilter proxyFilter = meshNode.getProxyFilter();
            if(proxyFilter != null) {
                final AddressArray addressArr = proxyFilter.getAddresses().get(position);
                final List<AddressArray> addresses = new ArrayList<>();
                addresses.add(addressArr);
                final ProxyConfigRemoveAddressFromFilter removeAddressFromFilter = new ProxyConfigRemoveAddressFromFilter(addresses);
                mViewModel.getMeshManagerApi().sendMeshMessage(MeshAddress.UNASSIGNED_ADDRESS, removeAddressFromFilter);
                showProgressbar();
            }
        }
    }

    private void removeAddresses() {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
        if (meshNode != null) {
            final ProxyFilter proxyFilter = meshNode.getProxyFilter();
            if (proxyFilter != null) {
                if (!proxyFilter.getAddresses().isEmpty()) {
                    final ProxyConfigRemoveAddressFromFilter removeAddressFromFilter = new ProxyConfigRemoveAddressFromFilter(proxyFilter.getAddresses());
                    mViewModel.getMeshManagerApi().sendMeshMessage(MeshAddress.UNASSIGNED_ADDRESS, removeAddressFromFilter);
                }
            }
        }
    }

    private void setFilter(final ProxyFilterType filterType) {
        showProgressbar();
        final ProxyConfigSetFilterType setFilterType = new ProxyConfigSetFilterType(filterType);
        mViewModel.getMeshManagerApi().sendMeshMessage(MeshAddress.UNASSIGNED_ADDRESS, setFilterType);
    }
}
