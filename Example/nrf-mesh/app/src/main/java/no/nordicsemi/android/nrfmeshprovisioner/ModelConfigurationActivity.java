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
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigModelAppStatus;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.models.GenericOnOffServerModel;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.utils.CompositionDataParser;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.AddressAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.BoundAppKeysAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigurationStatus;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentDisconnected;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentSubscriptionAddress;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmeshprovisioner.utils.HexKeyListener;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ModelConfigurationViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DATA_MODEL_NAME;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DEVICE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_ELEMENT_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_MODEL_ID;

public class ModelConfigurationActivity extends AppCompatActivity implements Injectable,
        DialogFragmentConfigurationStatus.DialogFragmentAppKeyBindStatusListener,
        DialogFragmentSubscriptionAddress.DialogFragmentSubscriptionAddressListener,
        AddressAdapter.OnItemClickListener,
        BoundAppKeysAdapter.OnItemClickListener,
        ItemTouchHelperAdapter,
        DialogFragmentDisconnected.DialogFragmentDisconnectedListener {

    private static final String TAG = ModelConfigurationActivity.class.getSimpleName();
    private static final String DIALOG_FRAGMENT_CONFIGURATION_STATUS = "DIALOG_FRAGMENT_CONFIGURATION_STATUS";
    private static final String PROGRESS_BAR_STATE = "PROGRESS_BAR_STATE";
    private static final long DELAY = 10000;

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
    private int mTransitionStepResolution;
    private int mTransitionStep;

    private Handler mHandler;
    private ModelConfigurationViewModel mViewModel;
    private List<byte[]> mGroupAddress = new ArrayList<>();
    private List<Integer> mKeyIndexes = new ArrayList<>();
    private AddressAdapter mAddressAdapter;
    private BoundAppKeysAdapter mBoundAppKeyAdapter;
    private Button mActionOnOff;
    private Button mActionRead;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_configuration);
        ButterKnife.bind(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ModelConfigurationViewModel.class);
        mHandler = new Handler();
        final Intent intent = getIntent();
        final ProvisionedMeshNode meshNode = intent.getParcelableExtra(EXTRA_DEVICE);
        final int elementAddress = intent.getExtras().getInt(EXTRA_ELEMENT_ADDRESS);
        final int modelId = intent.getExtras().getInt(EXTRA_MODEL_ID);

        if(meshNode == null)
            finish();

        final String modelName = intent.getStringExtra(EXTRA_DATA_MODEL_NAME);

        if(savedInstanceState != null){
            if (savedInstanceState.getBoolean(PROGRESS_BAR_STATE)) {
                mProgressbar.setVisibility(View.VISIBLE);
                disableClickableViews();
            } else {
                mProgressbar.setVisibility(View.INVISIBLE);
                enableClickableViews();
            }
        } else {
            mViewModel.setModel(meshNode, elementAddress, modelId);
        }

        // Set up views
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(modelName);
        getSupportActionBar().setSubtitle(getString(R.string.model_id, CompositionDataParser.formatModelIdentifier(modelId, false)));

        final RecyclerView recyclerViewAddresses = findViewById(R.id.recycler_view_addresses);
        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(this));
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewAddresses);
        mAddressAdapter = new AddressAdapter(this, mViewModel.getMeshModel());
        recyclerViewAddresses.setAdapter(mAddressAdapter);
        mAddressAdapter.setOnItemClickListener(this);

        final RecyclerView recyclerViewBoundKeys = findViewById(R.id.recycler_view_bound_keys);
        recyclerViewBoundKeys.setLayoutManager(new LinearLayoutManager(this));
        final ItemTouchHelper.Callback itemTouchHelperCallbackKeys = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelperKeys = new ItemTouchHelper(itemTouchHelperCallbackKeys);
        itemTouchHelperKeys.attachToRecyclerView(recyclerViewBoundKeys);
        mBoundAppKeyAdapter = new BoundAppKeysAdapter(this, mViewModel.getMeshModel());
        recyclerViewBoundKeys.setAdapter(mBoundAppKeyAdapter);
        mBoundAppKeyAdapter.setOnItemClickListener(this);

        mActionBindAppKey.setOnClickListener(v -> {
            final Intent bindAppKeysIntent = new Intent(ModelConfigurationActivity.this, BindAppKeysActivity.class);
            final ProvisionedMeshNode node = ((ProvisionedMeshNode)mViewModel.getExtendedMeshNode().getMeshNode());
            bindAppKeysIntent.putExtra(ManageAppKeysActivity.APP_KEYS, (Serializable) node.getAddedAppKeys());
            startActivityForResult(bindAppKeysIntent, ManageAppKeysActivity.SELECT_APP_KEY);
        });

        mPublishAddressView.setText(R.string.none);
        mActionSetPublication.setOnClickListener(v -> {
            final MeshModel meshModel = mViewModel.getMeshModel().getValue();
            if(meshModel != null && !meshModel.getBoundAppkeys().isEmpty()) {
                final Intent publicationSettings = new Intent(this, PublicationSettingsActivity.class);
                final Element element = meshNode.getElements().get(elementAddress);
                final MeshModel model = element.getMeshModels().get(modelId);
                publicationSettings.putExtra(EXTRA_DEVICE, model);
                startActivityForResult(publicationSettings, PublicationSettingsActivity.SET_PUBLICATION_SETTINGS);
            } else {
                Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
            }
        });

        mActionClearPublication.setOnClickListener(v -> {
            final MeshModel meshModel = mViewModel.getMeshModel().getValue();
            if(meshModel != null && !meshModel.getBoundAppkeys().isEmpty()) {
                mViewModel.sendConfigModelPublicationSet(MeshParserUtils.DISABLED_PUBLICATION_ADDRESS, meshModel.getPublishAppKeyIndexInt(),
                        false, 0, 0, 0, 0, 0);
                showProgressbar();
            } else {
                Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
            }
        });

        mActionSubscribe.setOnClickListener(v -> {
            final DialogFragmentSubscriptionAddress fragmentSubscriptionAddress = DialogFragmentSubscriptionAddress.newInstance();
            fragmentSubscriptionAddress.show(getSupportFragmentManager(), null);
        });

        mViewModel.getMeshModel().observe(this, meshModel -> {
            if(meshModel != null) {
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

                final byte[] publishAddress = meshModel.getPublishAddress();
                if (publishAddress != null && !Arrays.equals(publishAddress, MeshParserUtils.DISABLED_PUBLICATION_ADDRESS)) {
                    mPublishAddressView.setText(MeshParserUtils.bytesToHex(publishAddress, true));
                    mActionClearPublication.setVisibility(View.VISIBLE);
                } else {
                    mPublishAddressView.setText(R.string.none);
                    mActionClearPublication.setVisibility(View.GONE);
                }

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
        });

        mViewModel.getAppKeyBindStatusLiveData().observe(this, appKeyBindStatusLiveData -> {
            if(!appKeyBindStatusLiveData.isSuccess()){
                final String statusMessage = ConfigModelAppStatus.parseStatusMessage(this, appKeyBindStatusLiveData.getStatus());
                DialogFragmentConfigurationStatus fragmentAppKeyBindStatus = DialogFragmentConfigurationStatus.newInstance(getString(R.string.title_appkey_status), statusMessage);
                fragmentAppKeyBindStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_CONFIGURATION_STATUS);
            }
            hideProgressBar();
        });

        mViewModel.getConfigModelPublicationStatusLiveData().observe(this, configModelPublicationStatusLiveData -> {
            if(!configModelPublicationStatusLiveData.isSuccessful()){
                final String statusMessage = ConfigModelAppStatus.parseStatusMessage(this, configModelPublicationStatusLiveData.getStatus());
                DialogFragmentConfigurationStatus fragmentAppKeyBindStatus = DialogFragmentConfigurationStatus.newInstance(getString(R.string.title_publlish_address_status), statusMessage);
                fragmentAppKeyBindStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_CONFIGURATION_STATUS);
            }
            hideProgressBar();
        });

        mViewModel.getConfigModelSubscriptionStatusLiveData().observe(this, configModelSubscriptionStatus -> {
            if(!configModelSubscriptionStatus.isSuccessful()){
                final String statusMessage = ConfigModelAppStatus.parseStatusMessage(this, configModelSubscriptionStatus.getStatus());
                DialogFragmentConfigurationStatus fragmentAppKeyBindStatus = DialogFragmentConfigurationStatus.newInstance(getString(R.string.title_publlish_address_status), statusMessage);
                fragmentAppKeyBindStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_CONFIGURATION_STATUS);
            }
            hideProgressBar();
        });

        mViewModel.getTransactionStatus().observe(this, transactionFailedLiveData -> {
            hideProgressBar();
            final String message = getString(R.string.operation_timed_out);
            DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance("Transaction Failed", message);
            fragmentMessage.show(getSupportFragmentManager(), null);
        });

        mViewModel.isConnected().observe(this, aBoolean -> {
            final DialogFragmentDisconnected dialogFragmentDisconnected = DialogFragmentDisconnected.newInstance(getString(R.string.title_disconnected_error),
                    getString(R.string.disconnected_network_rationale));
            dialogFragmentDisconnected.show(getSupportFragmentManager(), null);
        });

        addNodeControlsUi();

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
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ManageAppKeysActivity.SELECT_APP_KEY:
                if(resultCode == RESULT_OK){
                    final String appKey = data.getStringExtra(ManageAppKeysActivity.RESULT_APP_KEY);
                    final int appKeyIndex = data.getIntExtra(ManageAppKeysActivity.RESULT_APP_KEY_INDEX, -1);
                    if(appKey != null){
                        mViewModel.sendBindAppKey(appKeyIndex);
                        showProgressbar();
                    }
                }
                break;
            case PublicationSettingsActivity.SET_PUBLICATION_SETTINGS:
                if(resultCode == RESULT_OK){
                    final byte[] publishAddress = data.getByteArrayExtra(PublicationSettingsActivity.RESULT_PUBLISH_ADDRESS);
                    final int appKeyIndex = data.getIntExtra(PublicationSettingsActivity.RESULT_APP_KEY_INDEX, -1);
                    final boolean credentialFlag = data.getBooleanExtra(PublicationSettingsActivity.RESULT_CREDENTIAL_FLAG, false);
                    final int publishTtl = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLISH_TTL, 0);
                    final int publicationSteps = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLICATION_STEPS, 0);
                    final int publicationResolution = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLICATION_RESOLUTION, 0);
                    final int publishRetransmitCount = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLISH_RETRANSMIT_COUNT, 0);
                    final int publishRetransmitIntervalSteps = data.getIntExtra(PublicationSettingsActivity.RESULT_PUBLISH_RETRANSMIT_INTERVAL_STEPS, 0);
                    if(publishAddress != null && appKeyIndex > -1){
                        try {
                            mViewModel.sendConfigModelPublicationSet(publishAddress,appKeyIndex, credentialFlag, publishTtl, publicationSteps, publicationResolution, publishRetransmitCount, publishRetransmitIntervalSteps);
                            showProgressbar();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isFinishing()){
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onAppKeyBindStatusConfirmed() {

    }

    @Override
    public void setSubscriptionAddress(final byte[] subscriptionAddress) {
        mViewModel.sendConfigModelSubscriptionAdd(subscriptionAddress);
        showProgressbar();
    }

    private void showProgressbar(){
        disableClickableViews();
        mProgressbar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        enableClickableViews();
        mProgressbar.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacks(mOperationTimeout);
    }

    private final Runnable mOperationTimeout = () -> {
        hideProgressBar();
        DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance(getString(R.string.title_transaction_failed), getString(R.string.operation_timed_out));
        fragmentMessage.show(getSupportFragmentManager(), null);
    };

    private void enableClickableViews(){
        mActionBindAppKey.setEnabled(true);
        mActionSetPublication.setEnabled(true);
        mActionClearPublication.setEnabled(true);
        mActionSubscribe.setEnabled(true);

        if(mActionOnOff != null && !mActionOnOff.isEnabled())
            mActionOnOff.setEnabled(true);

        if(mActionRead != null && !mActionRead.isEnabled())
            mActionRead.setEnabled(true);
    }

    private void disableClickableViews(){
        mActionBindAppKey.setEnabled(false);
        mActionSetPublication.setEnabled(false);
        mActionClearPublication.setEnabled(false);
        mActionSubscribe.setEnabled(false);

        if(mActionOnOff != null)
            mActionOnOff.setEnabled(false);

        if(mActionRead != null)
            mActionRead.setEnabled(false);

    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        if(viewHolder instanceof AddressAdapter.ViewHolder) {
            if (mAddressAdapter.getItemCount() != 0) {
                final byte[] address = mGroupAddress.get(position);
                mViewModel.sendConfigModelSubscriptionDelete(address);
                showProgressbar();
            }
        } else if(viewHolder instanceof BoundAppKeysAdapter.ViewHolder) {
            if (mBoundAppKeyAdapter.getItemCount() != 0) {
                final String appKey = mBoundAppKeyAdapter.getAppKey(position);
                final int keyIndex = getAppKeyIndex(appKey);
                mViewModel.sendUnbindAppKey(keyIndex);
                showProgressbar();
            }
        }
    }

    @Override
    public void onItemClick(final int position, final byte[] address) {

    }

    private void addNodeControlsUi(){
        final MeshModel model = mViewModel.getMeshModel().getValue();
        if(model != null){
            if(model instanceof GenericOnOffServerModel) {
                final CardView cardView = findViewById(R.id.node_controls_card);
                final View nodeControlsContainer = LayoutInflater.from(this).inflate(R.layout.layout_generic_on_off, cardView);
                final TextView time = nodeControlsContainer.findViewById(R.id.transition_time);
                final TextView onOffState = nodeControlsContainer.findViewById(R.id.on_off_state);
                final TextView remainingTime = nodeControlsContainer.findViewById(R.id.transition_state);
                final SeekBar transitionTimeSeekBar = nodeControlsContainer.findViewById(R.id.transition_seekbar);
                transitionTimeSeekBar.setProgress(0);
                transitionTimeSeekBar.incrementProgressBy(1);
                transitionTimeSeekBar.setMax(230);

                final SeekBar delaySeekBar = nodeControlsContainer.findViewById(R.id.delay_seekbar);
                delaySeekBar.setProgress(0);
                delaySeekBar.setMax(255);
                final TextView delayTime = nodeControlsContainer.findViewById(R.id.delay_time);

                mActionOnOff = nodeControlsContainer.findViewById(R.id.action_on_off);
                mActionRead = nodeControlsContainer.findViewById(R.id.action_read);
                mActionOnOff.setOnClickListener(v -> {
                    try {
                        final ProvisionedMeshNode node = (ProvisionedMeshNode) mViewModel.getExtendedMeshNode().getMeshNode();
                        if(mActionOnOff.getText().toString().equals(getString(R.string.action_generic_on))){
                            mViewModel.sendGenericOnOff(node, mTransitionStep, mTransitionStepResolution, delaySeekBar.getProgress(), true);
                        } else {
                            mViewModel.sendGenericOnOff(node, mTransitionStep, mTransitionStepResolution, delaySeekBar.getProgress(), false);
                        }
                        showProgressbar();
                    } catch (IllegalArgumentException ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                mActionRead.setOnClickListener(v -> {
                    final ProvisionedMeshNode node = (ProvisionedMeshNode) mViewModel.getExtendedMeshNode().getMeshNode();
                    mViewModel.sendGenericOnOffGet(node);
                    showProgressbar();
                });

                transitionTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    int lastValue = 0;
                    double res = 0.0;
                    @Override
                    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

                        if(progress >= 0 && progress <= 62) {
                            lastValue = progress;
                            mTransitionStepResolution = 0;
                            mTransitionStep = progress;
                            res = progress / 10.0;
                            time.setText(getString(R.string.transition_time_interval, String.valueOf(res), "s"));
                        } else if(progress >= 63 && progress <= 118) {
                            if(progress > lastValue) {
                                mTransitionStep = progress - 56;
                                lastValue = progress;
                            } else if (progress < lastValue){
                                mTransitionStep = -(56 - progress);
                            }
                            mTransitionStepResolution = 1;
                            time.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionStep), "s"));

                        } else if(progress >= 119 && progress <= 174) {
                            if(progress > lastValue) {
                                mTransitionStep = progress - 112;
                                lastValue = progress;
                            } else if (progress < lastValue){
                                mTransitionStep = -(112 - progress);
                            }
                            mTransitionStepResolution = 2;
                            time.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionStep * 10), "s"));
                        } else if(progress >= 175 && progress <= 230){
                            if(progress >= lastValue) {
                                mTransitionStep = progress - 168;
                                lastValue = progress;
                            } else if (progress < lastValue){
                                mTransitionStep = -(168 - progress);
                            }
                            mTransitionStepResolution = 3;
                            time.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionStep * 10), "min"));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(final SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(final SeekBar seekBar) {

                    }
                });

                delaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                        delayTime.setText(getString(R.string.transition_time_interval, String.valueOf(progress * MeshParserUtils.GENERIC_ON_OFF_5_MS), "ms"));
                    }

                    @Override
                    public void onStartTrackingTouch(final SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(final SeekBar seekBar) {

                    }
                });

                mViewModel.getGenericOnOffState().observe(this, genericOnOffStatusUpdate -> {
                    hideProgressBar();
                    final boolean presentState = genericOnOffStatusUpdate.isPresentOnOff();
                    final Boolean targetOnOff = genericOnOffStatusUpdate.getTargetOnOff();
                    final int steps = genericOnOffStatusUpdate.getSteps();
                    final int resolution = genericOnOffStatusUpdate.getResolution();
                    if(targetOnOff == null){
                        if (presentState) {
                            onOffState.setText(R.string.generic_state_on);
                            mActionOnOff.setText(R.string.action_generic_off);
                        } else {
                            onOffState.setText(R.string.generic_state_off);
                            mActionOnOff.setText(R.string.action_generic_on);
                        }
                        remainingTime.setVisibility(View.GONE);
                    } else {
                        if (!targetOnOff) {
                            onOffState.setText(R.string.generic_state_on);
                            mActionOnOff.setText(R.string.action_generic_off);
                        } else {
                            onOffState.setText(R.string.generic_state_off);
                            mActionOnOff.setText(R.string.action_generic_on);
                        }
                        remainingTime.setText(getString(R.string.remaining_time, MeshParserUtils.getRemainingTransitionTime(resolution, steps)));
                        remainingTime.setVisibility(View.VISIBLE);
                    }
                });
            } else if (model instanceof VendorModel){
                final CardView cardView = findViewById(R.id.node_controls_card);
                final View nodeControlsContainer = LayoutInflater.from(this).inflate(R.layout.layout_vendor_model_controls, cardView);

                final CheckBox chkAcknowledged = nodeControlsContainer.findViewById(R.id.chk_acknowledged);
                final TextInputLayout opCodeLayout = nodeControlsContainer.findViewById(R.id.op_code_layout);
                final TextInputEditText opCodeEditText = nodeControlsContainer.findViewById(R.id.op_code);

                final KeyListener hexKeyListener = new HexKeyListener();

                final TextInputLayout parametersLayout = nodeControlsContainer.findViewById(R.id.parameters_layout);
                final TextInputEditText parametersEditText = nodeControlsContainer.findViewById(R.id.parameters);
                final View messageContainer = nodeControlsContainer.findViewById(R.id.received_message_container);
                final TextView receivedMessage = nodeControlsContainer.findViewById(R.id.received_message);
                final Button actionSend = nodeControlsContainer.findViewById(R.id.action_send);

                opCodeEditText.setKeyListener(hexKeyListener);
                opCodeEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

                    }

                    @Override
                    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                        opCodeLayout.setError(null);
                    }

                    @Override
                    public void afterTextChanged(final Editable s) {

                    }
                });

                parametersEditText.setKeyListener(hexKeyListener);
                parametersEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

                    }

                    @Override
                    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                        parametersLayout.setError(null);
                    }

                    @Override
                    public void afterTextChanged(final Editable s) {

                    }
                });

                actionSend.setOnClickListener(v -> {
                    messageContainer.setVisibility(View.GONE);
                    final String opCode = opCodeEditText.getText().toString().trim();

                    try {
                        if(!validateInput(opCode)) {
                            return;
                        }
                    } catch (Exception ex) {
                        opCodeLayout.setError(ex.getMessage());
                        return;
                    }

                    final String parameters = parametersEditText.getText().toString().trim();
                    final byte[] params;
                    if(TextUtils.isEmpty(parameters) && parameters.length() == 0){
                        params = null;
                    } else {
                        try {
                            if(!validateInput(parameters)) {
                                return;
                            }
                        } catch (Exception ex) {
                            opCodeLayout.setError(ex.getMessage());
                            return;
                        }
                        params = MeshParserUtils.toByteArray(parameters);
                    }

                    if(model.getBoundAppKeyIndexes().isEmpty()) {
                        Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
                        return;
                    }

                    final ProvisionedMeshNode node = (ProvisionedMeshNode) mViewModel.getExtendedMeshNode().getMeshNode();
                    if(chkAcknowledged.isChecked()){
                        mViewModel.sendVendorModelAcknowledgedMessage(node, model, model.getBoundAppKeyIndexes().get(0), Integer.parseInt(opCode, 16), params);
                    } else {
                        mViewModel.sendVendorModelUnacknowledgedMessage(node, model, model.getBoundAppKeyIndexes().get(0), Integer.parseInt(opCode, 16), params);
                    }
                });

                mViewModel.getVendorModelState().observe(this, bytes -> {
                    messageContainer.setVisibility(View.VISIBLE);
                    receivedMessage.setText(MeshParserUtils.bytesToHex(bytes, false));
                });
            }
        }
    }

    private boolean validateInput(final String input) throws IllegalArgumentException{
        if(TextUtils.isEmpty(input)){
            throw new IllegalArgumentException(getString(R.string.error_empty_value));
        }

        if(!input.matches(Utils.HEX_PATTERN) || input.startsWith("0x")) {
            throw new IllegalArgumentException(getString(R.string.invalid_hex_value));
        }
        return true;
    }

    @Override
    public void onItemClick(final int position, final String appKey) {

    }

    private Integer getAppKeyIndex(final String appKey){
        final MeshModel model = mViewModel.getMeshModel().getValue();
        for(Integer key : model.getBoundAppkeys().keySet()){
            if(model.getBoundAppkeys().get(key).equals(appKey)){
                return key;
            }
        }
        return null;
    }

    @Override
    public void onDisconnected() {
        finish();
    }
}
