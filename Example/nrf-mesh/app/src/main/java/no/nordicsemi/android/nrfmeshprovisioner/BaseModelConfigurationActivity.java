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
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.DispatchingAndroidInjector;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelAppBind;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelAppStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelAppUnbind;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelPublicationSet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelPublicationStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelSubscriptionAdd;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelSubscriptionDelete;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelSubscriptionStatus;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.CompositionDataParser;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.PublicationSettings;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.BoundAppKeysAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.GroupAddressAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigStatus;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentDisconnected;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentGroupSubscription;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ModelConfigurationViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public abstract class BaseModelConfigurationActivity extends AppCompatActivity implements Injectable,
        DialogFragmentGroupSubscription.DialogFragmentSubscriptionAddressListener,
        BoundAppKeysAdapter.OnItemClickListener,
        ItemTouchHelperAdapter,
        DialogFragmentDisconnected.DialogFragmentDisconnectedListener {

    private static final String DIALOG_FRAGMENT_CONFIGURATION_STATUS = "DIALOG_FRAGMENT_CONFIGURATION_STATUS";
    private static final String PROGRESS_BAR_STATE = "PROGRESS_BAR_STATE";

    @Inject
    DispatchingAndroidInjector<Fragment> mDispatchingAndroidInjector;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.action_bind_app_key)
    Button mActionBindAppKey;
    @BindView(R.id.bound_keys)
    TextView mAppKeyView;
    @BindView(R.id.unbind_hint)
    TextView mUnbindHint;

    @BindView(R.id.action_set_publication)
    Button mActionSetPublication;
    @BindView(R.id.action_clear_publication_set)
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

    protected Handler mHandler;
    protected ModelConfigurationViewModel mViewModel;
    protected List<Integer> mGroupAddress = new ArrayList<>();
    protected List<Integer> mKeyIndexes = new ArrayList<>();
    protected GroupAddressAdapter mSubscriptionAdapter;
    protected BoundAppKeysAdapter mBoundAppKeyAdapter;
    protected Button mActionRead;
    private RecyclerView recyclerViewBoundKeys, recyclerViewAddresses;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_configuration);
        ButterKnife.bind(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ModelConfigurationViewModel.class);
        mHandler = new Handler();

        final MeshModel meshModel = mViewModel.getSelectedModel().getValue();
        //noinspection ConstantConditions
        final String modelName = meshModel.getModelName();

        // Set up views
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(modelName);
        final int modelId = meshModel.getModelId();
        getSupportActionBar().setSubtitle(getString(R.string.model_id, CompositionDataParser.formatModelIdentifier(modelId, true)));

        recyclerViewAddresses = findViewById(R.id.recycler_view_addresses);
        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(this));
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewAddresses);
        mSubscriptionAdapter = new GroupAddressAdapter(this, mViewModel.getMeshManagerApi().getMeshNetwork(), mViewModel.getSelectedModel());
        recyclerViewAddresses.setAdapter(mSubscriptionAdapter);

        recyclerViewBoundKeys = findViewById(R.id.recycler_view_bound_keys);
        recyclerViewBoundKeys.setLayoutManager(new LinearLayoutManager(this));
        final ItemTouchHelper.Callback itemTouchHelperCallbackKeys = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelperKeys = new ItemTouchHelper(itemTouchHelperCallbackKeys);
        itemTouchHelperKeys.attachToRecyclerView(recyclerViewBoundKeys);
        mBoundAppKeyAdapter = new BoundAppKeysAdapter(this, mViewModel.getSelectedModel());
        recyclerViewBoundKeys.setAdapter(mBoundAppKeyAdapter);
        mBoundAppKeyAdapter.setOnItemClickListener(this);

        mActionBindAppKey.setOnClickListener(v -> {
            final Intent bindAppKeysIntent = new Intent(BaseModelConfigurationActivity.this, ManageAppKeysActivity.class);
            bindAppKeysIntent.putExtra(Utils.EXTRA_DATA, Utils.BIND_APP_KEY);
            startActivityForResult(bindAppKeysIntent, ManageAppKeysActivity.SELECT_APP_KEY);
        });

        mPublishAddressView.setText(R.string.none);
        mActionSetPublication.setOnClickListener(v -> {
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            handleAppKeyBind(model);
        });

        mActionClearPublication.setOnClickListener(v -> clearPublication());

        mActionSubscribe.setOnClickListener(v -> {
            //noinspection ConstantConditions
            final ArrayList<Group> groups = new ArrayList<>(mViewModel.getGroups().getValue());
            final DialogFragmentGroupSubscription fragmentSubscriptionAddress = DialogFragmentGroupSubscription.newInstance(groups);
            fragmentSubscriptionAddress.show(getSupportFragmentManager(), null);
        });

        mViewModel.getSelectedModel().observe(this, model -> {
            if (model != null) {
                updateAppStatusUi(model);
                updatePublicationUi(model);
                updateSubscriptionUi(model);
            }
        });

        mViewModel.getTransactionStatus().observe(this, transactionFailedLiveData -> {
            hideProgressBar();
            final String message = getString(R.string.operation_timed_out);
            DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance("Transaction Failed", message);
            fragmentMessage.show(getSupportFragmentManager(), null);
        });

        mViewModel.isConnectedToProxy().observe(this, aBoolean -> {
            if (aBoolean != null && !aBoolean) {
                final DialogFragmentDisconnected dialogFragmentDisconnected = DialogFragmentDisconnected.newInstance(getString(R.string.title_disconnected_error),
                        getString(R.string.disconnected_network_rationale));
                dialogFragmentDisconnected.show(getSupportFragmentManager(), null);
            }
        });

        mViewModel.getMeshMessageLiveData().observe(this, this::updateMeshMessage);
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
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PROGRESS_BAR_STATE, mProgressbar.getVisibility() == View.VISIBLE);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
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
            case ManageAppKeysActivity.SELECT_APP_KEY:
                if (resultCode == RESULT_OK) {
                    final ApplicationKey appKey = data.getParcelableExtra(ManageAppKeysActivity.RESULT_APP_KEY);
                    if (appKey != null) {
                        bindAppKey(appKey.getKeyIndex());
                    }
                }
                break;
            case PublicationSettingsActivity.SET_PUBLICATION_SETTINGS:
                if (resultCode == RESULT_OK) {
                    setPublication(data);
                }
                break;
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
    public void setGroupSubscription(@NonNull final String name, final int address) {
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        final Group group = new Group(address, network.getMeshUUID());
        group.setName(name);
        network.addGroup(group);
        subscribe(address);
    }

    @Override
    public void setGroupSubscription(@NonNull final Group group) {
        subscribe(group.getGroupAddress());
    }

    private void subscribe(final int address) {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
        if (meshNode != null) {
            final Element element = mViewModel.getSelectedElement().getValue();
            if (element != null) {
                final int elementAddress = element.getElementAddress();
                final MeshModel model = mViewModel.getSelectedModel().getValue();
                if (model != null) {
                    final int modelIdentifier = model.getModelId();
                    final ConfigModelSubscriptionAdd configModelSubscriptionAdd = new ConfigModelSubscriptionAdd(elementAddress, address, modelIdentifier);
                    mViewModel.getMeshManagerApi().sendMeshMessage(meshNode.getUnicastAddress(), configModelSubscriptionAdd);
                    showProgressbar();
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
    public void onItemClick(final int position, final ApplicationKey appKey) {

    }

    @Override
    public void onDisconnected() {
        finish();
    }

    protected void handleAppKeyBind(final MeshModel model) {
        if (model != null && !model.getBoundAppKeyIndexes().isEmpty()) {
            final Intent publicationSettings = new Intent(this, PublicationSettingsActivity.class);
            startActivityForResult(publicationSettings, PublicationSettingsActivity.SET_PUBLICATION_SETTINGS);
        } else {
            Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
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
                    mViewModel.getMeshManagerApi().sendMeshMessage(meshNode.getUnicastAddress(), configModelAppUnbind);
                    showProgressbar();
                }
            }
        }
    }

    private void unbindAppKey(final int position) {
        if (mBoundAppKeyAdapter.getItemCount() != 0) {
            final ApplicationKey appKey = mBoundAppKeyAdapter.getAppKey(position);
            final int keyIndex = appKey.getKeyIndex();//getAppKeyIndex(appKey);
            final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
            if (meshNode != null) {
                final Element element = mViewModel.getSelectedElement().getValue();
                if (element != null) {
                    final MeshModel model = mViewModel.getSelectedModel().getValue();
                    if (model != null) {
                        final ConfigModelAppUnbind configModelAppUnbind = new ConfigModelAppUnbind(element.getElementAddress(), model.getModelId(), keyIndex);
                        mViewModel.getMeshManagerApi().sendMeshMessage(meshNode.getUnicastAddress(), configModelAppUnbind);
                        showProgressbar();
                    }
                }
            }
        }
    }

    private void setPublication(final Intent data) {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        if (node != null) {
            final Element element = mViewModel.getSelectedElement().getValue();
            if (element != null) {
                final MeshModel model = mViewModel.getSelectedModel().getValue();
                if (model != null) {
                    final int publishAddress = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLISH_ADDRESS, 0);
                    final int appKeyIndex = data.getIntExtra(PublicationSettingsActivity.RESULT_APP_KEY_INDEX, -1);
                    final boolean credentialFlag = data.getBooleanExtra(PublicationSettingsActivity.RESULT_CREDENTIAL_FLAG, false);
                    final int publishTtl = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLISH_TTL, 0);
                    final int publicationSteps = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLICATION_STEPS, 0);
                    final int resolution = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLICATION_RESOLUTION, 0);
                    final int publishRetransmitCount = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLISH_RETRANSMIT_COUNT, 0);
                    final int publishRetransmitIntervalSteps = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLISH_RETRANSMIT_INTERVAL_STEPS, 0);
                    if (appKeyIndex > -1) {
                        try {
                            final ConfigModelPublicationSet configModelPublicationSet = new ConfigModelPublicationSet(element.getElementAddress(),
                                    publishAddress, appKeyIndex, credentialFlag, publishTtl,
                                    publicationSteps, resolution, publishRetransmitCount, publishRetransmitIntervalSteps, model.getModelId());
                            if (!model.getBoundAppKeyIndexes().isEmpty()) {
                                mViewModel.getMeshManagerApi().sendMeshMessage(node.getUnicastAddress(), configModelPublicationSet);
                                showProgressbar();
                            } else {
                                Toast.makeText(this, getString(R.string.error_no_app_keys_bound), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
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
                        final int address = MeshParserUtils.DISABLED_PUBLICATION_ADDRESS;
                        final int appKeyIndex = model.getPublicationSettings().getAppKeyIndex();
                        final boolean credentialFlag = model.getPublicationSettings().getCredentialFlag();
                        final ConfigModelPublicationSet configModelPublicationSet = new ConfigModelPublicationSet(element.getElementAddress(), address, appKeyIndex,
                                credentialFlag, 0, 0, 0, 0, 0, model.getModelId());
                        mViewModel.getMeshManagerApi().sendMeshMessage(meshNode.getUnicastAddress(), configModelPublicationSet);
                        showProgressbar();
                    } else {
                        Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    private void deleteSubscription(final int position) {
        if (mSubscriptionAdapter.getItemCount() != 0) {
            final int address = mGroupAddress.get(position);
            final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
            if (meshNode != null) {
                final Element element = mViewModel.getSelectedElement().getValue();
                if (element != null) {
                    final MeshModel model = mViewModel.getSelectedModel().getValue();
                    if (model != null) {
                        final ConfigModelSubscriptionDelete configModelAppUnbind = new ConfigModelSubscriptionDelete(element.getElementAddress(),
                                address, model.getModelId());
                        mViewModel.getMeshManagerApi().sendMeshMessage(meshNode.getUnicastAddress(), configModelAppUnbind);
                        showProgressbar();
                    }
                }
            }
        }
    }

    protected final void showProgressbar() {
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
        mActionBindAppKey.setEnabled(true);
        mActionSetPublication.setEnabled(true);
        mActionClearPublication.setEnabled(true);
        mActionSubscribe.setEnabled(true);

        if (mActionRead != null && !mActionRead.isEnabled())
            mActionRead.setEnabled(true);

    }

    protected void disableClickableViews() {
        mActionBindAppKey.setEnabled(false);
        mActionSetPublication.setEnabled(false);
        mActionClearPublication.setEnabled(false);
        mActionSubscribe.setEnabled(false);

        if (mActionRead != null)
            mActionRead.setEnabled(false);


    }

    /**
     * Update the mesh message
     *
     * @param meshMessage {@link MeshMessage} mesh message status
     */
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        if (meshMessage instanceof ConfigModelAppStatus) {
            final ConfigModelAppStatus status = (ConfigModelAppStatus) meshMessage;
            if (!status.isSuccessful()) {
                DialogFragmentConfigStatus fragmentAppKeyBindStatus = DialogFragmentConfigStatus.
                        newInstance(getString(R.string.title_appkey_status), status.getStatusCodeName());
                fragmentAppKeyBindStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_CONFIGURATION_STATUS);
            }
        } else if (meshMessage instanceof ConfigModelPublicationStatus) {
            final ConfigModelPublicationStatus status = (ConfigModelPublicationStatus) meshMessage;
            if (!status.isSuccessful()) {
                DialogFragmentConfigStatus fragmentAppKeyBindStatus = DialogFragmentConfigStatus.
                        newInstance(getString(R.string.title_publlish_address_status), status.getStatusCodeName());
                fragmentAppKeyBindStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_CONFIGURATION_STATUS);
            }
        } else if (meshMessage instanceof ConfigModelSubscriptionStatus) {
            final ConfigModelSubscriptionStatus status = (ConfigModelSubscriptionStatus) meshMessage;
            if (!status.isSuccessful()) {
                DialogFragmentConfigStatus fragmentAppKeyBindStatus = DialogFragmentConfigStatus.
                        newInstance(getString(R.string.title_publlish_address_status), status.getStatusCodeName());
                fragmentAppKeyBindStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_CONFIGURATION_STATUS);
            }
        }
        hideProgressBar();
    }

    private void updateAppStatusUi(final MeshModel meshModel) {
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

    private void updatePublicationUi(final MeshModel meshModel) {
        final PublicationSettings publicationSettings = meshModel.getPublicationSettings();
        if (publicationSettings != null) {
            final int publishAddress = publicationSettings.getPublishAddress();
            if (publishAddress != MeshParserUtils.DISABLED_PUBLICATION_ADDRESS) {
                mPublishAddressView.setText(MeshAddress.formatAddress(publishAddress, true));
                mActionClearPublication.setVisibility(View.VISIBLE);
            } else {
                mPublishAddressView.setText(R.string.none);
                mActionClearPublication.setVisibility(View.GONE);
            }
        }
    }

    private void updateSubscriptionUi(final MeshModel meshModel) {
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
}
