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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.models.ConfigurationClientModel;
import no.nordicsemi.android.mesh.models.ConfigurationServerModel;
import no.nordicsemi.android.mesh.models.SigModel;
import no.nordicsemi.android.mesh.models.SigModelParser;
import no.nordicsemi.android.mesh.transport.ConfigModelAppBind;
import no.nordicsemi.android.mesh.transport.ConfigModelAppUnbind;
import no.nordicsemi.android.mesh.transport.ConfigModelPublicationGet;
import no.nordicsemi.android.mesh.transport.ConfigModelPublicationSet;
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionAdd;
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionDelete;
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionVirtualAddressAdd;
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionVirtualAddressDelete;
import no.nordicsemi.android.mesh.transport.ConfigSigModelAppGet;
import no.nordicsemi.android.mesh.transport.ConfigSigModelSubscriptionGet;
import no.nordicsemi.android.mesh.transport.ConfigVendorModelAppGet;
import no.nordicsemi.android.mesh.transport.ConfigVendorModelSubscriptionGet;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.transport.PublicationSettings;
import no.nordicsemi.android.mesh.utils.CompositionDataParser;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.GroupCallbacks;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.adapter.GroupAddressAdapter;
import no.nordicsemi.android.nrfmesh.di.Injectable;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentConfigStatus;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentDisconnected;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentGroupSubscription;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;
import no.nordicsemi.android.nrfmesh.keys.adapter.BoundAppKeysAdapter;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.BaseActivity;
import no.nordicsemi.android.nrfmesh.viewmodels.ModelConfigurationViewModel;
import no.nordicsemi.android.nrfmesh.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

import static no.nordicsemi.android.nrfmesh.utils.Utils.BIND_APP_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.EXTRA_DATA;
import static no.nordicsemi.android.nrfmesh.utils.Utils.MESSAGE_TIME_OUT;
import static no.nordicsemi.android.nrfmesh.utils.Utils.RESULT_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.SELECT_KEY;

public abstract class BaseModelConfigurationActivity extends BaseActivity implements Injectable,
        GroupCallbacks,
        ItemTouchHelperAdapter,
        DialogFragmentDisconnected.DialogFragmentDisconnectedListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String DIALOG_FRAGMENT_CONFIGURATION_STATUS = "DIALOG_FRAGMENT_CONFIGURATION_STATUS";
    private static final String PROGRESS_BAR_STATE = "PROGRESS_BAR_STATE";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.container)
    CoordinatorLayout mContainer;
    @BindView(R.id.app_key_card)
    View mContainerAppKeyBinding;
    @BindView(R.id.action_bind_app_key)
    Button mActionBindAppKey;
    @BindView(R.id.bound_keys)
    TextView mAppKeyView;
    @BindView(R.id.unbind_hint)
    TextView mUnbindHint;

    @BindView(R.id.publish_address_card)
    View mContainerPublication;
    @BindView(R.id.action_set_publication)
    Button mActionSetPublication;
    @BindView(R.id.action_clear_publication)
    Button mActionClearPublication;
    @BindView(R.id.publish_address)
    TextView mPublishAddressView;

    @BindView(R.id.subscription_address_card)
    View mContainerSubscribe;
    @BindView(R.id.action_subscribe_address)
    Button mActionSubscribe;
    @BindView(R.id.subscribe_addresses)
    TextView mSubscribeAddressView;
    @BindView(R.id.subscribe_hint)
    TextView mSubscribeHint;
    @BindView(R.id.configuration_progress_bar)
    ProgressBar mProgressbar;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipe;

    protected List<Integer> mGroupAddress = new ArrayList<>();
    protected List<Integer> mKeyIndexes = new ArrayList<>();
    protected GroupAddressAdapter mSubscriptionAdapter;
    protected BoundAppKeysAdapter mBoundAppKeyAdapter;
    protected Button mActionRead;
    protected Button mActionSetRelayState;
    protected Button mSetNetworkTransmitStateButton;

    private RecyclerView recyclerViewBoundKeys, recyclerViewAddresses;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_configuration);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(ModelConfigurationViewModel.class);
        init();
        ButterKnife.bind(this);

        final String modelName = meshModel.getModelName();

        // Set up views
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(modelName);
        final int modelId = meshModel.getModelId();
        getSupportActionBar().setSubtitle(getString(R.string.model_id, CompositionDataParser.formatModelIdentifier(modelId, true)));

        recyclerViewAddresses = findViewById(R.id.recycler_view_addresses);
        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(this));
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewAddresses);
        mSubscriptionAdapter = new GroupAddressAdapter(this, mViewModel.getNetworkLiveData().getMeshNetwork(), mViewModel.getSelectedModel());
        recyclerViewAddresses.setAdapter(mSubscriptionAdapter);

        recyclerViewBoundKeys = findViewById(R.id.recycler_view_bound_keys);
        recyclerViewBoundKeys.setLayoutManager(new LinearLayoutManager(this));
        final ItemTouchHelper.Callback itemTouchHelperCallbackKeys = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelperKeys = new ItemTouchHelper(itemTouchHelperCallbackKeys);
        itemTouchHelperKeys.attachToRecyclerView(recyclerViewBoundKeys);
        mBoundAppKeyAdapter = new BoundAppKeysAdapter(this, mViewModel.getNetworkLiveData().getAppKeys(), mViewModel.getSelectedModel());
        recyclerViewBoundKeys.setAdapter(mBoundAppKeyAdapter);

        mActionBindAppKey.setOnClickListener(v -> {
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
            if (node != null && !node.isExist(SigModelParser.CONFIGURATION_SERVER)) {
                return;
            }
            if (!checkConnectivity(mContainer)) return;
            final Intent bindAppKeysIntent = new Intent(BaseModelConfigurationActivity.this, AppKeysActivity.class);
            bindAppKeysIntent.putExtra(EXTRA_DATA, BIND_APP_KEY);
            startActivityForResult(bindAppKeysIntent, SELECT_KEY);
        });

        mPublishAddressView.setText(R.string.none);
        mActionSetPublication.setOnClickListener(v -> navigateToPublication());

        mActionClearPublication.setOnClickListener(v -> clearPublication());

        mActionSubscribe.setOnClickListener(v -> {
            if (!checkConnectivity(mContainer)) return;
            final ArrayList<Group> groups = new ArrayList<>(mViewModel.getGroups().getValue());
            final DialogFragmentGroupSubscription fragmentSubscriptionAddress = DialogFragmentGroupSubscription.newInstance(groups);
            fragmentSubscriptionAddress.show(getSupportFragmentManager(), null);
        });

        mViewModel.getTransactionStatus().observe(this, transactionStatus -> {
            if (transactionStatus != null) {
                hideProgressBar();
                final String message = getString(R.string.operation_timed_out);
                DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance("Transaction Failed", message);
                fragmentMessage.show(getSupportFragmentManager(), null);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.setActivityVisible(true);
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PROGRESS_BAR_STATE, mProgressbar.getVisibility() == View.VISIBLE);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean(PROGRESS_BAR_STATE)) {
            mProgressbar.setVisibility(View.VISIBLE);
            disableClickableViews();
        } else {
            mProgressbar.setVisibility(View.INVISIBLE);
            enableClickableViews();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_KEY:
                if (resultCode == RESULT_OK) {
                    final ApplicationKey appKey = data.getParcelableExtra(RESULT_KEY);
                    if (appKey != null) {
                        bindAppKey(appKey.getKeyIndex());
                    }
                }
                break;
            case Utils.HEARTBEAT_SETTINGS_SET:
            case PublicationSettingsActivity.SET_PUBLICATION_SETTINGS:
                if (resultCode == RESULT_OK) {
                    showProgressBar();
                }
                break;
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
    public Group createGroup(@NonNull final String name) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        return network.createGroup(network.getSelectedProvisioner(), name);
    }

    @Override
    public Group createGroup(@NonNull final UUID uuid, final String name) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        return network.createGroup(uuid, null, name);
    }

    @Override
    public boolean onGroupAdded(@NonNull final String name, final int address) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        final Group group = network.createGroup(network.getSelectedProvisioner(), address, name);
        if (network.addGroup(group)) {
            subscribe(group);
            return true;
        }
        return false;
    }

    @Override
    public boolean onGroupAdded(@NonNull final Group group) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        if (network.addGroup(group)) {
            subscribe(group);
            return true;
        }
        return false;
    }

    @Override
    public void subscribe(final Group group) {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
        if (meshNode != null) {
            final Element element = mViewModel.getSelectedElement().getValue();
            if (element != null) {
                final int elementAddress = element.getElementAddress();
                final MeshModel model = mViewModel.getSelectedModel().getValue();
                if (model != null) {
                    final int modelIdentifier = model.getModelId();
                    final MeshMessage configModelSubscriptionAdd;
                    if (group.getAddressLabel() == null) {
                        configModelSubscriptionAdd = new ConfigModelSubscriptionAdd(elementAddress, group.getAddress(), modelIdentifier);
                    } else {
                        configModelSubscriptionAdd = new ConfigModelSubscriptionVirtualAddressAdd(elementAddress, group.getAddressLabel(), modelIdentifier);
                    }
                    sendMessage(meshNode.getUnicastAddress(), configModelSubscriptionAdd);
                }
            }
        }
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        if (viewHolder instanceof GroupAddressAdapter.ViewHolder) {
            deleteSubscription(position);
        } else if (viewHolder instanceof BoundAppKeysAdapter.ViewHolder) {
            unbindAppKey(position);
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {
    }

    @Override
    public void onDisconnected() {
        finish();
    }

    @Override
    public void onRefresh() {
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (!checkConnectivity(mContainer) || model == null) {
            mSwipe.setRefreshing(false);
        }
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        final Element element = mViewModel.getSelectedElement().getValue();
        if (node != null && element != null && model != null) {
            if (model instanceof SigModel) {
                if (!(model instanceof ConfigurationServerModel) && !(model instanceof ConfigurationClientModel)) {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.listing_model_configuration), Snackbar.LENGTH_LONG);
                    final ConfigSigModelAppGet appGet = new ConfigSigModelAppGet(element.getElementAddress(), model.getModelId());
                    final ConfigSigModelSubscriptionGet subscriptionGet = new ConfigSigModelSubscriptionGet(element.getElementAddress(), model.getModelId());
                    mViewModel.getMessageQueue().add(appGet);
                    mViewModel.getMessageQueue().add(subscriptionGet);
                    queuePublicationGetMessage(element.getElementAddress(), model.getModelId());
                    sendMessage(node.getUnicastAddress(), mViewModel.getMessageQueue().peek());
                } else {
                    mSwipe.setRefreshing(false);
                }

            } else {
                mViewModel.displaySnackBar(this, mContainer, getString(R.string.listing_model_configuration), Snackbar.LENGTH_LONG);
                final ConfigVendorModelAppGet appGet = new ConfigVendorModelAppGet(element.getElementAddress(), model.getModelId());
                final ConfigVendorModelSubscriptionGet subscriptionGet = new ConfigVendorModelSubscriptionGet(element.getElementAddress(), model.getModelId());
                mViewModel.getMessageQueue().add(appGet);
                mViewModel.getMessageQueue().add(subscriptionGet);
                queuePublicationGetMessage(element.getElementAddress(), model.getModelId());
                sendMessage(node.getUnicastAddress(), mViewModel.getMessageQueue().peek());
            }
        }
    }

    protected void navigateToPublication() {
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null && !model.getBoundAppKeyIndexes().isEmpty()) {
            final Intent publicationSettings = new Intent(this, PublicationSettingsActivity.class);
            startActivityForResult(publicationSettings, PublicationSettingsActivity.SET_PUBLICATION_SETTINGS);
        } else {
            mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
        }
    }

    private void bindAppKey(final int appKeyIndex) {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
        if (meshNode != null) {
            final Element element = mViewModel.getSelectedElement().getValue();
            if (element != null) {
                final MeshModel model = mViewModel.getSelectedModel().getValue();
                if (model != null) {
                    final ConfigModelAppBind configModelAppUnbind = new ConfigModelAppBind(element.getElementAddress(), model.getModelId(), appKeyIndex);
                    sendMessage(meshNode.getUnicastAddress(), configModelAppUnbind);
                }
            }
        }
    }

    private void unbindAppKey(final int position) {
        if (mBoundAppKeyAdapter.getItemCount() != 0) {
            if (!checkConnectivity(mContainer)) {
                mBoundAppKeyAdapter.notifyItemChanged(position);
                return;
            }
            final ApplicationKey appKey = mBoundAppKeyAdapter.getAppKey(position);
            final int keyIndex = appKey.getKeyIndex();
            final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
            if (meshNode != null) {
                final Element element = mViewModel.getSelectedElement().getValue();
                if (element != null) {
                    final MeshModel model = mViewModel.getSelectedModel().getValue();
                    if (model != null) {
                        final ConfigModelAppUnbind configModelAppUnbind = new ConfigModelAppUnbind(element.getElementAddress(), model.getModelId(), keyIndex);
                        sendMessage(meshNode.getUnicastAddress(), configModelAppUnbind);
                    }
                }
            }
        }
    }

    private void clearPublication() {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
        if (meshNode != null) {
            final Element element = mViewModel.getSelectedElement().getValue();
            if (element != null) {
                final MeshModel model = mViewModel.getSelectedModel().getValue();
                if (model != null) {
                    if (!model.getBoundAppKeyIndexes().isEmpty()) {
                        final int address = MeshAddress.UNASSIGNED_ADDRESS;
                        final int appKeyIndex = model.getPublicationSettings().getAppKeyIndex();
                        final boolean credentialFlag = model.getPublicationSettings().getCredentialFlag();
                        final int ttl = model.getPublicationSettings().getPublishTtl();
                        final int publicationSteps = model.getPublicationSettings().getPublicationSteps();
                        final int publicationResolution = model.getPublicationSettings().getPublicationResolution();
                        final int retransmitCount = model.getPublicationSettings().getPublishRetransmitCount();
                        final int retransmitIntervalSteps = model.getPublicationSettings().getPublishRetransmitIntervalSteps();
                        final ConfigModelPublicationSet configModelPublicationSet = new ConfigModelPublicationSet(element.getElementAddress(), address, appKeyIndex,
                                credentialFlag, ttl, publicationSteps, publicationResolution, retransmitCount, retransmitIntervalSteps, model.getModelId());
                        sendMessage(meshNode.getUnicastAddress(), configModelPublicationSet);
                    } else {
                        mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                    }
                }
            }
        }
    }

    private void deleteSubscription(final int position) {
        if (mSubscriptionAdapter.getItemCount() != 0) {
            if (!checkConnectivity(mContainer)) {
                mSubscriptionAdapter.notifyItemChanged(position);
                return;
            }
            final int address = mGroupAddress.get(position);
            final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
            if (meshNode != null) {
                final Element element = mViewModel.getSelectedElement().getValue();
                if (element != null) {
                    final MeshModel model = mViewModel.getSelectedModel().getValue();
                    if (model != null) {
                        MeshMessage subscriptionDelete = null;
                        if (MeshAddress.isValidGroupAddress(address)) {
                            subscriptionDelete = new ConfigModelSubscriptionDelete(element.getElementAddress(), address, model.getModelId());
                        } else {
                            final UUID uuid = model.getLabelUUID(address);
                            if (uuid != null)
                                subscriptionDelete = new ConfigModelSubscriptionVirtualAddressDelete(element.getElementAddress(), uuid, model.getModelId());
                        }

                        if (subscriptionDelete != null) {
                            sendMessage(meshNode.getUnicastAddress(), subscriptionDelete);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected final void showProgressBar() {
        mHandler.postDelayed(mRunnableOperationTimeout, MESSAGE_TIME_OUT);
        disableClickableViews();
        mProgressbar.setVisibility(View.VISIBLE);
    }

    @Override
    protected final void hideProgressBar() {
        mSwipe.setRefreshing(false);
        enableClickableViews();
        mProgressbar.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacks(mRunnableOperationTimeout);
    }

    @Override
    protected void enableClickableViews() {
        mActionBindAppKey.setEnabled(true);
        mActionSetPublication.setEnabled(true);
        mActionClearPublication.setEnabled(true);
        mActionSubscribe.setEnabled(true);

        if (mActionSetRelayState != null)
            mActionSetRelayState.setEnabled(true);
        if (mSetNetworkTransmitStateButton != null)
            mSetNetworkTransmitStateButton.setEnabled(true);

        if (mActionRead != null && !mActionRead.isEnabled())
            mActionRead.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        mActionBindAppKey.setEnabled(false);
        mActionSetPublication.setEnabled(false);
        mActionClearPublication.setEnabled(false);
        mActionSubscribe.setEnabled(false);

        if (mActionSetRelayState != null)
            mActionSetRelayState.setEnabled(false);
        if (mSetNetworkTransmitStateButton != null)
            mSetNetworkTransmitStateButton.setEnabled(false);
        if (mActionRead != null)
            mActionRead.setEnabled(false);
    }

    protected void updateAppStatusUi(final MeshModel meshModel) {
        final List<Integer> keys = meshModel.getBoundAppKeyIndexes();
        mKeyIndexes.clear();
        mKeyIndexes.addAll(keys);
        if (!keys.isEmpty()) {
            mUnbindHint.setVisibility(View.VISIBLE);
            mAppKeyView.setVisibility(View.GONE);
            recyclerViewBoundKeys.setVisibility(View.VISIBLE);
        } else {
            mUnbindHint.setVisibility(View.GONE);
            mAppKeyView.setVisibility(View.VISIBLE);
            recyclerViewBoundKeys.setVisibility(View.GONE);
        }
    }

    protected void updatePublicationUi(final MeshModel meshModel) {
        final PublicationSettings publicationSettings = meshModel.getPublicationSettings();
        if (publicationSettings != null) {
            final int publishAddress = publicationSettings.getPublishAddress();
            if (publishAddress != MeshAddress.UNASSIGNED_ADDRESS) {
                if (MeshAddress.isValidVirtualAddress(publishAddress)) {
                    final UUID uuid = publicationSettings.getLabelUUID();
                    if (uuid != null) {
                        mPublishAddressView.setText(uuid.toString().toUpperCase(Locale.US));
                    } else {
                        mPublishAddressView.setText(MeshAddress.formatAddress(publishAddress, true));
                    }
                } else {
                    mPublishAddressView.setText(MeshAddress.formatAddress(publishAddress, true));
                }
                mActionClearPublication.setVisibility(View.VISIBLE);
            } else {
                mPublishAddressView.setText(R.string.none);
                mActionClearPublication.setVisibility(View.GONE);
            }
        }
    }

    protected void updateSubscriptionUi(final MeshModel meshModel) {
        final List<Integer> subscriptionAddresses = meshModel.getSubscribedAddresses();
        mGroupAddress.clear();
        mGroupAddress.addAll(subscriptionAddresses);
        if (!subscriptionAddresses.isEmpty()) {
            mSubscribeHint.setVisibility(View.VISIBLE);
            mSubscribeAddressView.setVisibility(View.GONE);
            recyclerViewAddresses.setVisibility(View.VISIBLE);
        } else {
            mSubscribeHint.setVisibility(View.GONE);
            mSubscribeAddressView.setVisibility(View.VISIBLE);
            recyclerViewAddresses.setVisibility(View.GONE);
        }
    }

    protected void sendMessage(@NonNull final MeshMessage meshMessage) {
        try {
            if (!checkConnectivity(mContainer))
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

    protected boolean handleStatuses() {
        final MeshMessage message = mViewModel.getMessageQueue().peek();
        if (message != null) {
            sendMessage(message);
            return true;
        } else {
            mViewModel.displaySnackBar(this, mContainer, getString(R.string.operation_success), Snackbar.LENGTH_SHORT);
        }
        return false;
    }

    protected void sendMessage(final int address, @NonNull final MeshMessage meshMessage) {
        try {
            if (!checkConnectivity(mContainer))
                return;
            mViewModel.getMeshManagerApi().createMeshPdu(address, meshMessage);
            showProgressBar();
        } catch (IllegalArgumentException ex) {
            hideProgressBar();
            DialogFragmentError
                    .newInstance(getString(R.string.title_error), ex.getMessage())
                    .show(getSupportFragmentManager(), null);
        }
    }

    protected void updateClickableViews() {
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null && model.getModelId() == SigModelParser.CONFIGURATION_CLIENT)
            disableClickableViews();
    }

    protected void queuePublicationGetMessage(final int address, final int modelId) {
        final ConfigModelPublicationGet publicationGet = new ConfigModelPublicationGet(address, modelId);
        mViewModel.getMessageQueue().add(publicationGet);
    }

    protected void displayStatusDialogFragment(@NonNull final String title, @NonNull final String message) {
        if (mViewModel.isActivityVisible()) {
            DialogFragmentConfigStatus fragmentAppKeyBindStatus = DialogFragmentConfigStatus.
                    newInstance(title, message);
            fragmentAppKeyBindStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_CONFIGURATION_STATUS);
        }
    }
}
