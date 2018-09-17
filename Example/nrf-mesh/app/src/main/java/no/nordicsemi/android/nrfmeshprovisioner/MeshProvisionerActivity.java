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

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningCapabilities;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningFailedState;
import no.nordicsemi.android.meshprovisioner.states.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.AlgorithmInformationParser;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.ParseInputOOBActions;
import no.nordicsemi.android.meshprovisioner.utils.ParseOutputOOBActions;
import no.nordicsemi.android.meshprovisioner.utils.ParsePublicKeyInformation;
import no.nordicsemi.android.meshprovisioner.utils.ParseStaticOutputOOBInformation;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ProvisioningProgressAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentAppKeyAddStatus;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentAuthenticationInput;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentFlags;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentIvIndex;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentKeyIndex;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentNetworkKey;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentNodeName;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentProvisioningFailedErrorMessage;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentUnicastAddress;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ExtendedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.livedata.ProvisioningStateLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.utils.ProvisioningProgress;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.MeshProvisionerViewModel;

public class MeshProvisionerActivity extends AppCompatActivity implements Injectable,
        DialogFragmentAuthenticationInput.ProvisionerInputFragmentListener,
        DialogFragmentNodeName.DialogFragmentNodeNameListener,
        DialogFragmentNetworkKey.DialogFragmentNetworkKeyListener,
        DialogFragmentKeyIndex.DialogFragmentKeyIndexListener,
        DialogFragmentFlags.DialogFragmentFlagsListener,
        DialogFragmentIvIndex.DialogFragmentIvIndexListener,
        DialogFragmentUnicastAddress.DialogFragmentUnicastAddressListener,
        DialogFragmentProvisioningFailedErrorMessage.DialogFragmentProvisioningFailedErrorListener,
        DialogFragmentAppKeyAddStatus.DialogFragmentAppKeyAddStatusListener {

    private static final String DIALOG_FRAGMENT_PROVISIONING_FAILED = "DIALOG_FRAGMENT_PROVISIONING_FAILED";
    private static final String DIALOG_FRAGMENT_AUTH_INPUT_TAG = "DIALOG_FRAGMENT_AUTH_INPUT_TAG";
    private static final String DIALOG_FRAGMENT_APP_KEY_STATUS = "DIALOG_FRAGMENT_APP_KEY_STATUS";

    @BindView(R.id.container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.provisioning_progress_bar)
    ProgressBar mProvisioningProgressBar;
    @BindView(R.id.data_container)
    ScrollView container;
    @BindView(R.id.capabilities_container)
    View mCapabilitiesContainer;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private MeshProvisionerViewModel mViewModel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesh_provisioner);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        final ExtendedBluetoothDevice device = intent.getParcelableExtra(Utils.EXTRA_DEVICE);
        final String deviceName = device.getName();
        final String deviceAddress = device.getAddress();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(deviceName);
        getSupportActionBar().setSubtitle(deviceAddress);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(MeshProvisionerViewModel.class);
        mViewModel.connect(device);
        mViewModel.getProvisioningData().setNodeName(deviceName);
        // Set up views
        final LinearLayout connectivityProgressContainer = findViewById(R.id.connectivity_progress_container);
        final TextView connectionState = findViewById(R.id.connection_state);
        final Button provisioner = findViewById(R.id.action_provision_device);
        final View provisioningStatusContainer = findViewById(R.id.info_provisioning_status_container);

        final View containerName = findViewById(R.id.container_element_count);
        containerName.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_black_alpha_24dp));
        final TextView nameTitle = containerName.findViewById(R.id.title);
        nameTitle.setText(R.string.summary_name);
        final TextView nameView = containerName.findViewById(R.id.text);
        containerName.setOnClickListener(v -> {
            final String name = mViewModel.getProvisioningData().getNodeName();
            final DialogFragmentNodeName dialogFragmentNodeName = DialogFragmentNodeName.newInstance(name);
            dialogFragmentNodeName.show(getSupportFragmentManager(), null);
        });

        final View containerUnicastAddress = findViewById(R.id.container_supported_algorithm);
        containerUnicastAddress.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(this, R.drawable.ic_lan_black_alpha_24dp));
        final TextView unicastAddressTitle = containerUnicastAddress.findViewById(R.id.title);
        unicastAddressTitle.setText(R.string.summary_unicast_address);
        final TextView unicastAddressView = containerUnicastAddress.findViewById(R.id.text);
        containerUnicastAddress.setOnClickListener(v -> {
            final int unicastAddress = mViewModel.getProvisioningData().getUnicastAddress();
            final DialogFragmentUnicastAddress dialogFragmentFlags = DialogFragmentUnicastAddress.newInstance(unicastAddress);
            dialogFragmentFlags.show(getSupportFragmentManager(), null);
        });

        final View containerAppKey = findViewById(R.id.container_public_key_type);
        containerAppKey.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_black_alpha_24dp));
        final TextView appKeyTitle = containerAppKey.findViewById(R.id.title);
        appKeyTitle.setText(R.string.summary_app_keys);
        final TextView appKeyView = containerAppKey.findViewById(R.id.text);
        containerAppKey.setOnClickListener(v -> {
            final List<String> appKeys = mViewModel.getProvisioningData().getAppKeys();
            final Intent manageAppKeys = new Intent(MeshProvisionerActivity.this, ManageAppKeysActivity.class);
            manageAppKeys.putExtra(ManageAppKeysActivity.APP_KEYS, new ArrayList<>(appKeys));
            startActivityForResult(manageAppKeys, ManageAppKeysActivity.SELECT_APP_KEY);
        });

        mViewModel.getConnectionState().observe(this, connectionState::setText);

        mViewModel.isConnected().observe(this, connected -> {
            if(!connected)
                finish();
        });


        mViewModel.isDeviceReady().observe(this, deviceReady -> {
            if(deviceReady) {
                connectivityProgressContainer.setVisibility(View.GONE);
                if (mViewModel.isProvisioningComplete()) {
                    mProvisioningProgressBar.setVisibility(View.VISIBLE);
                    provisioningStatusContainer.setVisibility(View.VISIBLE);
                    return;
                }
                container.setVisibility(View.VISIBLE);
            }
        });

        mViewModel.isReconnecting().observe(this, isReconnecting -> {
            if(isReconnecting){
                provisioningStatusContainer.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
                mProvisioningProgressBar.setVisibility(View.GONE);
                connectivityProgressContainer.setVisibility(View.VISIBLE);
            }
        });

        mViewModel.getProvisioningData().observe(this, provisioningLiveData -> {
            nameView.setText(provisioningLiveData.getNodeName());
            if(provisioningLiveData.getProvisioningSettings() != null) {
                unicastAddressView.setText(getString(R.string.hex_format, String.format(Locale.US, "%04X", provisioningLiveData.getUnicastAddress())));
                appKeyView.setText(provisioningLiveData.getSelectedAppKey());
            }
        });

        mViewModel.getMeshNode().observe(this, extendedMeshNode -> {
            if(extendedMeshNode.getMeshNode() instanceof UnprovisionedMeshNode) {
                final UnprovisionedMeshNode node = (UnprovisionedMeshNode) extendedMeshNode.getMeshNode();
                if (node.getProvisioningCapabilities() != null) {
                    mProvisioningProgressBar.setVisibility(View.INVISIBLE);
                    provisioner.setText(R.string.provision_action);
                    updateCapabilitiesUi(node.getProvisioningCapabilities());
                }
            }
        });

        provisioner.setOnClickListener(v -> {
            final ExtendedMeshNode meshNode = mViewModel.getMeshNode();
            if(meshNode != null && meshNode.getMeshNode().getProvisioningCapabilities() != null) {
                setupProvisionerStateObservers(provisioningStatusContainer);
                mProvisioningProgressBar.setVisibility(View.VISIBLE);
                mViewModel.startProvisioning();
            } else {
                mViewModel.identifyNode(mViewModel.getProvisioningData().getNodeName());
            }
        });
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
    public void onBackPressed() {
        super.onBackPressed();
        //We disconnect from the device if the user presses the back button
        mViewModel.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ManageAppKeysActivity.SELECT_APP_KEY){
            if(resultCode == RESULT_OK){
                final String appKey = data.getStringExtra(ManageAppKeysActivity.RESULT_APP_KEY);
                if(appKey != null){
                    final int appKeyIndex = mViewModel.getProvisioningData().getAppKeys().indexOf(appKey);//Utils.getKey(mViewModel.getProvisioningData().getAppKeys(), appKey);
                    mViewModel.setSelectedAppKey(appKeyIndex, appKey);
                }
            }
        }
    }

    @Override
    public void onPinInputComplete(final String pin) {
        mViewModel.sendProvisioneePin(pin);
    }

    @Override
    public void onPinInputCanceled() {
        final String message = getString(R.string.provisioning_cancelled);
        final Snackbar snackbar = Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
        mViewModel.disconnect();
    }

    @Override
    public void onNodeNameUpdated(final String nodeName) {
        mViewModel.getProvisioningData().setNodeName(nodeName);
    }

    @Override
    public void onNetworkKeyGenerated(final String networkKey) {
        mViewModel.getProvisioningData().setNetworkKey(networkKey);
    }

    @Override
    public void onKeyIndexGenerated(final int keyIndex) {
        mViewModel.getProvisioningData().setKeyIndex(keyIndex);
    }

    @Override
    public void onFlagsSelected(final int keyRefreshFlag, final int ivUpdateFlag) {
        mViewModel.getProvisioningData().setFlags(MeshParserUtils.parseUpdateFlags(keyRefreshFlag, ivUpdateFlag));
    }

    @Override
    public void setIvIndex(final int ivIndex) {
        mViewModel.getProvisioningData().setIvIndex(ivIndex);
    }

    @Override
    public void setUnicastAddress(final int unicastAddress) {
        mViewModel.getProvisioningData().setUnicastAddress(unicastAddress);
    }

    @Override
    public void onProvisioningFailed() {
        //Provisioning failed so now we go back to the scanner page.
        mViewModel.disconnect();
        finish();
    }

    public void setupProvisionerStateObservers(final View provisioningStatusContainer){
        provisioningStatusContainer.setVisibility(View.VISIBLE);

        final RecyclerView recyclerView = provisioningStatusContainer.findViewById(R.id.recycler_view_provisioning_progress);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ProvisioningProgressAdapter adapter = new ProvisioningProgressAdapter(this, mViewModel.getProvisioningState());
        recyclerView.setAdapter(adapter);

        mViewModel.getProvisioningState().observe(this, provisioningStateLiveData -> {
            final ProvisioningProgress provisionerProgress = provisioningStateLiveData.getProvisionerProgress();
            adapter.refresh(provisioningStateLiveData.getStateList());
            if(provisionerProgress != null) {
                final ProvisioningStateLiveData.ProvisioningLiveDataState state = provisionerProgress.getState();
                switch (state) {
                    case PROVISIONING_FAILED:
                        if (getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_PROVISIONING_FAILED) == null) {
                            final String statusMessage = ProvisioningFailedState.parseProvisioningFailure(getApplicationContext(), provisionerProgress.getStatusReceived());
                            DialogFragmentProvisioningFailedErrorMessage message = DialogFragmentProvisioningFailedErrorMessage.newInstance(getString(R.string.title_error_provisioning_failed), statusMessage);
                            message.show(getSupportFragmentManager(), DIALOG_FRAGMENT_PROVISIONING_FAILED);
                        }
                        break;
                    case PROVISIONING_AUTHENTICATION_INPUT_WAITING:
                        if (getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_AUTH_INPUT_TAG) == null) {
                            DialogFragmentAuthenticationInput dialogFragmentAuthenticationInput = DialogFragmentAuthenticationInput.newInstance();
                            dialogFragmentAuthenticationInput.show(getSupportFragmentManager(), DIALOG_FRAGMENT_AUTH_INPUT_TAG);
                        }
                        break;
                    case APP_KEY_STATUS_RECEIVED:
                        if (getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_APP_KEY_STATUS) == null) {
                            DialogFragmentAppKeyAddStatus fragmentAppKeyAddStatus = DialogFragmentAppKeyAddStatus.newInstance(getString(R.string.title_configuration_compete), getString(R.string.configuration_complete_summary));
                            fragmentAppKeyAddStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_APP_KEY_STATUS);
                        }
                        break;
                }

            }
            container.setVisibility(View.GONE);
        });

    }

    @Override
    public void onAppKeyAddStatusReceived() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", mViewModel.isProvisioningComplete());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void updateCapabilitiesUi(final ProvisioningCapabilities capabilities) {
        mCapabilitiesContainer.setVisibility(View.VISIBLE);

        final String numberOfElements = String.valueOf(capabilities.getNumberOfElements());
        ((TextView)mCapabilitiesContainer.findViewById(R.id.container_element_count).findViewById(R.id.text)).setText(numberOfElements);

        final String algorithm = AlgorithmInformationParser.parseAlgorithm(capabilities.getSupportedAlgorithm());
        ((TextView)mCapabilitiesContainer.findViewById(R.id.container_supported_algorithm).findViewById(R.id.text)).setText(algorithm);

        final String publicKeyType = ParsePublicKeyInformation.parsePublicKeyInformation(capabilities.getPublicKeyType());
        ((TextView)mCapabilitiesContainer.findViewById(R.id.container_public_key_type).findViewById(R.id.text)).setText(publicKeyType);

        final String oobType = ParseStaticOutputOOBInformation.parseStaticOOBActionInformation(capabilities.getStaticOOBType());
        ((TextView)mCapabilitiesContainer.findViewById(R.id.container_static_oob_type).findViewById(R.id.text)).setText(oobType);

        final String outputOobSize = String.valueOf(capabilities.getOutputOOBSize());
        ((TextView)mCapabilitiesContainer.findViewById(R.id.container_output_oob_size).findViewById(R.id.text)).setText(outputOobSize);

        final String outputAction = ParseOutputOOBActions.getOuputOOBActionDescription(capabilities.getOutputOOBAction());
        ((TextView)mCapabilitiesContainer.findViewById(R.id.container_output_actions).findViewById(R.id.text)).setText(outputAction);

        final String inputOobSize = String.valueOf(capabilities.getInputOOBSize());
        ((TextView)mCapabilitiesContainer.findViewById(R.id.container_input_oob_size).findViewById(R.id.text)).setText(inputOobSize);

        final String inputAction = ParseInputOOBActions.getInputOOBActionDescription(capabilities.getOutputOOBAction());
        ((TextView)mCapabilitiesContainer.findViewById(R.id.container_input_actions).findViewById(R.id.text)).setText(inputAction);

    }
}
