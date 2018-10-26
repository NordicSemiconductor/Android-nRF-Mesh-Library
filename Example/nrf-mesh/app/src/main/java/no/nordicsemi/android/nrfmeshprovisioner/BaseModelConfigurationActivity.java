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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelAppBind;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelAppStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelAppUnbind;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelPublicationSet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelPublicationStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelSubscriptionAdd;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelSubscriptionDelete;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelSubscriptionStatus;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.CompositionDataParser;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.PublicationSettings;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.AddressAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.BoundAppKeysAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigurationStatus;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentDisconnected;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentSubscriptionAddress;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ModelConfigurationViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DATA_MODEL_NAME;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DEVICE;

public abstract class BaseModelConfigurationActivity extends AppCompatActivity implements Injectable,
        DialogFragmentConfigurationStatus.DialogFragmentAppKeyBindStatusListener,
        DialogFragmentSubscriptionAddress.DialogFragmentSubscriptionAddressListener,
        AddressAdapter.OnItemClickListener,
        BoundAppKeysAdapter.OnItemClickListener,
        ItemTouchHelperAdapter,
        DialogFragmentDisconnected.DialogFragmentDisconnectedListener {

    private static final String TAG = BaseModelConfigurationActivity.class.getSimpleName();
    private static final String DIALOG_FRAGMENT_CONFIGURATION_STATUS = "DIALOG_FRAGMENT_CONFIGURATION_STATUS";
    private static final String PROGRESS_BAR_STATE = "PROGRESS_BAR_STATE";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.unbind_hint)
    TextView mUnbindHint;
    @BindView(R.id.action_bind_app_key)
    Button mActionBindAppKey;
    @BindView(R.id.bound_keys)
    TextView mAppKeyView;

    @BindView(R.id.action_set_publication)
    Button mActionSetPublication;
    @BindView(R.id.action_clear_publication_set)
    Button mActionClearPublication;
    @BindView(R.id.publish_address)
    TextView mPublishAddressView;

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
    protected List<byte[]> mGroupAddress = new ArrayList<>();
    protected List<Integer> mKeyIndexes = new ArrayList<>();
    protected AddressAdapter mAddressAdapter;
    protected BoundAppKeysAdapter mBoundAppKeyAdapter;
    protected Button mActionRead;
    private RecyclerView recyclerViewBoundKeys, recyclerViewAddresses;

    /**
     * Adds the control ui for the mesh model
     *
     * @param model mesh model to be controlled
     */
    protected abstract void addControlsUi(final MeshModel model);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_configuration);
        ButterKnife.bind(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ModelConfigurationViewModel.class);
        mHandler = new Handler();
        final Intent intent = getIntent();
        final ProvisionedMeshNode meshNode = intent.getParcelableExtra(EXTRA_DEVICE);

        if (meshNode == null)
            finish();

        final String modelName = intent.getStringExtra(EXTRA_DATA_MODEL_NAME);

        // Set up views
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(modelName);
        final int modelId = mViewModel.getSelectedModel().getMeshModel().getModelId();
        getSupportActionBar().setSubtitle(getString(R.string.model_id, CompositionDataParser.formatModelIdentifier(modelId, false)));

        recyclerViewAddresses = findViewById(R.id.recycler_view_addresses);
        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(this));
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewAddresses);
        mAddressAdapter = new AddressAdapter(this, mViewModel.getSelectedModel());
        recyclerViewAddresses.setAdapter(mAddressAdapter);
        mAddressAdapter.setOnItemClickListener(this);

        recyclerViewBoundKeys = findViewById(R.id.recycler_view_bound_keys);
        recyclerViewBoundKeys.setLayoutManager(new LinearLayoutManager(this));
        final ItemTouchHelper.Callback itemTouchHelperCallbackKeys = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelperKeys = new ItemTouchHelper(itemTouchHelperCallbackKeys);
        itemTouchHelperKeys.attachToRecyclerView(recyclerViewBoundKeys);
        mBoundAppKeyAdapter = new BoundAppKeysAdapter(this, mViewModel.getSelectedModel());
        recyclerViewBoundKeys.setAdapter(mBoundAppKeyAdapter);
        mBoundAppKeyAdapter.setOnItemClickListener(this);

        mActionBindAppKey.setOnClickListener(v -> {
            final Intent bindAppKeysIntent = new Intent(BaseModelConfigurationActivity.this, BindAppKeysActivity.class);
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getMeshNode();
            bindAppKeysIntent.putExtra(ManageAppKeysActivity.APP_KEYS, (Serializable) node.getAddedAppKeys());
            startActivityForResult(bindAppKeysIntent, ManageAppKeysActivity.SELECT_APP_KEY);
        });

        mPublishAddressView.setText(R.string.none);
        mActionSetPublication.setOnClickListener(v -> {
            final MeshModel model = mViewModel.getSelectedModel().getMeshModel();
            if (model != null && !model.getBoundAppkeys().isEmpty()) {
                final Intent publicationSettings = new Intent(this, PublicationSettingsActivity.class);
                publicationSettings.putExtra(EXTRA_DEVICE, model);
                startActivityForResult(publicationSettings, PublicationSettingsActivity.SET_PUBLICATION_SETTINGS);
            } else {
                Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
            }
        });

        mActionClearPublication.setOnClickListener(v -> clearPublication());

        mActionSubscribe.setOnClickListener(v -> {
            final DialogFragmentSubscriptionAddress fragmentSubscriptionAddress = DialogFragmentSubscriptionAddress.newInstance();
            fragmentSubscriptionAddress.show(getSupportFragmentManager(), null);
        });

        mViewModel.getSelectedModel().observe(this, meshModel -> {
            if (meshModel != null) {
                updateAppStatusUi(meshModel);
                updatePublicationUi(meshModel);
                updateSubscriptionUi(meshModel);
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

        addControlsUi(mViewModel.getSelectedModel().getMeshModel());
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
                    final String appKey = data.getStringExtra(ManageAppKeysActivity.RESULT_APP_KEY);
                    final int appKeyIndex = data.getIntExtra(ManageAppKeysActivity.RESULT_APP_KEY_INDEX, -1);
                    if (appKey != null) {
                        bindAppKey(appKeyIndex);
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
    public void onAppKeyBindStatusConfirmed() {

    }

    @Override
    public void setSubscriptionAddress(final byte[] subscriptionAddress) {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getMeshNode();
        final byte[] elementAddress = mViewModel.getSelectedElement().getElement().getElementAddress();
        final int modelIdentifier = mViewModel.getSelectedModel().getMeshModel().getModelId();
        final ConfigModelSubscriptionAdd configModelSubscriptionAdd = new ConfigModelSubscriptionAdd(meshNode, elementAddress, subscriptionAddress, modelIdentifier, 0);
        mViewModel.getMeshManagerApi().sendMeshConfigurationMessage(configModelSubscriptionAdd);
        showProgressbar();
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        if (viewHolder instanceof AddressAdapter.ViewHolder) {
            deleteSubscription(position);
        } else if (viewHolder instanceof BoundAppKeysAdapter.ViewHolder) {
            unbindAppKey(position);
        }
    }

    @Override
    public void onItemClick(final int position, final byte[] address) {

    }

    @Override
    public void onItemClick(final int position, final String appKey) {

    }

    @Override
    public void onDisconnected() {
        finish();
    }

    private void bindAppKey(final int appKeyIndex) {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getMeshNode();
        final Element element = mViewModel.getSelectedElement().getElement();
        final MeshModel model = mViewModel.getSelectedModel().getMeshModel();
        final ConfigModelAppBind configModelAppUnbind = new ConfigModelAppBind(meshNode, element.getElementAddress(), model.getModelId(), appKeyIndex, 0);
        mViewModel.getMeshManagerApi().sendMeshConfigurationMessage(configModelAppUnbind);
        showProgressbar();
    }

    private void unbindAppKey(final int position) {
        if (mBoundAppKeyAdapter.getItemCount() != 0) {
            final String appKey = mBoundAppKeyAdapter.getAppKey(position);
            final int keyIndex = getAppKeyIndex(appKey);
            final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getMeshNode();
            final Element element = mViewModel.getSelectedElement().getElement();
            final MeshModel model = mViewModel.getSelectedModel().getMeshModel();
            final ConfigModelAppUnbind configModelAppUnbind = new ConfigModelAppUnbind(meshNode, element.getElementAddress(), model.getModelId(), keyIndex, 0);
            mViewModel.getMeshManagerApi().sendMeshConfigurationMessage(configModelAppUnbind);
            showProgressbar();
        }
    }

    private void setPublication(final Intent data) {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getMeshNode();
        final Element element = mViewModel.getSelectedElement().getElement();
        final MeshModel model = mViewModel.getSelectedModel().getMeshModel();

        final byte[] publishAddress = data.getByteArrayExtra(PublicationSettingsActivity.RESULT_PUBLISH_ADDRESS);
        final int appKeyIndex = data.getIntExtra(PublicationSettingsActivity.RESULT_APP_KEY_INDEX, -1);
        final boolean credentialFlag = data.getBooleanExtra(PublicationSettingsActivity.RESULT_CREDENTIAL_FLAG, false);
        final int publishTtl = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLISH_TTL, 0);
        final int publicationSteps = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLICATION_STEPS, 0);
        final int resolution = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLICATION_RESOLUTION, 0);
        final int publishRetransmitCount = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLISH_RETRANSMIT_COUNT, 0);
        final int publishRetransmitIntervalSteps = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLISH_RETRANSMIT_INTERVAL_STEPS, 0);
        if (publishAddress != null && appKeyIndex > -1) {
            try {

                final ConfigModelPublicationSet configModelPublicationSet = new ConfigModelPublicationSet(node, element.getElementAddress(), publishAddress,
                        appKeyIndex, credentialFlag, publishTtl, publicationSteps, resolution, publishRetransmitCount, publishRetransmitIntervalSteps, model.getModelId(), 0);
                if (!model.getBoundAppKeyIndexes().isEmpty()) {
                    mViewModel.getMeshManagerApi().sendMeshConfigurationMessage(configModelPublicationSet);
                    showProgressbar();
                } else {
                    Toast.makeText(this, getString(R.string.error_no_app_keys_bound), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void clearPublication() {
        final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getMeshNode();
        final Element element = mViewModel.getSelectedElement().getElement();
        final MeshModel meshModel = mViewModel.getSelectedModel().getMeshModel();
        if (meshModel != null && !meshModel.getBoundAppkeys().isEmpty()) {
            final byte[] address = MeshParserUtils.DISABLED_PUBLICATION_ADDRESS;
            final int appKeyIndex = meshModel.getPublicationSettings().getAppKeyIndex();
            final boolean credentialFlag = meshModel.getPublicationSettings().getCredentialFlag();
            final ConfigModelPublicationSet configModelPublicationSet = new ConfigModelPublicationSet(meshNode, element.getElementAddress(), address, appKeyIndex,
                    credentialFlag, 0, 0, 0, 0, 0, meshModel.getModelId(), 0);
            mViewModel.getMeshManagerApi().sendMeshConfigurationMessage(configModelPublicationSet);
            showProgressbar();
        } else {
            Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
        }
    }

    private void deleteSubscription(final int position) {
        if (mAddressAdapter.getItemCount() != 0) {
            final byte[] address = mGroupAddress.get(position);
            final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getMeshNode();
            final Element element = mViewModel.getSelectedElement().getElement();
            final MeshModel model = mViewModel.getSelectedModel().getMeshModel();
            final ConfigModelSubscriptionDelete configModelAppUnbind = new ConfigModelSubscriptionDelete(meshNode, element.getElementAddress(),
                    address, model.getModelId(), 0);
            mViewModel.getMeshManagerApi().sendMeshConfigurationMessage(configModelAppUnbind);
            showProgressbar();
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

    private Integer getAppKeyIndex(final String appKey) {
        final MeshModel model = mViewModel.getSelectedModel().getMeshModel();
        for (Integer key : model.getBoundAppkeys().keySet()) {
            if (model.getBoundAppkeys().get(key).equals(appKey)) {
                return key;
            }
        }
        return null;
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
                DialogFragmentConfigurationStatus fragmentAppKeyBindStatus = DialogFragmentConfigurationStatus.
                        newInstance(getString(R.string.title_appkey_status), status.getStatusCodeName());
                fragmentAppKeyBindStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_CONFIGURATION_STATUS);
            }
        } else if (meshMessage instanceof ConfigModelPublicationStatus) {
            final ConfigModelPublicationStatus status = (ConfigModelPublicationStatus) meshMessage;
            if (!status.isSuccessful()) {
                DialogFragmentConfigurationStatus fragmentAppKeyBindStatus = DialogFragmentConfigurationStatus.
                        newInstance(getString(R.string.title_publlish_address_status), status.getStatusCodeName());
                fragmentAppKeyBindStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_CONFIGURATION_STATUS);
            }
        } else if (meshMessage instanceof ConfigModelSubscriptionStatus) {
            final ConfigModelSubscriptionStatus status = (ConfigModelSubscriptionStatus) meshMessage;
            if (!status.isSuccessful()) {
                DialogFragmentConfigurationStatus fragmentAppKeyBindStatus = DialogFragmentConfigurationStatus.
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
            final byte[] publishAddress = publicationSettings.getPublishAddress();
            if (publishAddress != null && !Arrays.equals(publishAddress, MeshParserUtils.DISABLED_PUBLICATION_ADDRESS)) {
                mPublishAddressView.setText(MeshParserUtils.bytesToHex(publishAddress, true));
                mActionClearPublication.setVisibility(View.VISIBLE);
            } else {
                mPublishAddressView.setText(R.string.none);
                mActionClearPublication.setVisibility(View.GONE);
            }
        }
    }

    private void updateSubscriptionUi(final MeshModel meshModel) {
        final List<byte[]> subscriptionAddresses = meshModel.getSubscriptionAddresses();
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
