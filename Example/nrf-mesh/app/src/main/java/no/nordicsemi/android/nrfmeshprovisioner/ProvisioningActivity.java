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
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

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
import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningCapabilities;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningFailedState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.AlgorithmType;
import no.nordicsemi.android.meshprovisioner.utils.AuthenticationOOBMethods;
import no.nordicsemi.android.meshprovisioner.utils.InputOOBAction;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.OutputOOBAction;
import no.nordicsemi.android.meshprovisioner.utils.StaticOOBType;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ProvisioningProgressAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentAuthenticationInput;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigurationComplete;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentProvisioningFailedError;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentSelectOOBType;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentUnicastAddress;
import no.nordicsemi.android.nrfmeshprovisioner.keys.AppKeysActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.dialog.DialogFragmentNodeName;
import no.nordicsemi.android.nrfmeshprovisioner.utils.ProvisionerStates;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ProvisionerProgress;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ProvisioningViewModel;

public class ProvisioningActivity extends AppCompatActivity implements Injectable,
        DialogFragmentSelectOOBType.DialogFragmentSelectOOBTypeListener,
        DialogFragmentAuthenticationInput.ProvisionerInputFragmentListener,
        DialogFragmentNodeName.DialogFragmentNodeNameListener,
        DialogFragmentUnicastAddress.DialogFragmentUnicastAddressListener,
        DialogFragmentProvisioningFailedError.DialogFragmentProvisioningFailedErrorListener,
        DialogFragmentConfigurationComplete.ConfigurationCompleteListener {

    private static final String DIALOG_FRAGMENT_PROVISIONING_FAILED = "DIALOG_FRAGMENT_PROVISIONING_FAILED";
    private static final String DIALOG_FRAGMENT_AUTH_INPUT_TAG = "DIALOG_FRAGMENT_AUTH_INPUT_TAG";
    private static final String DIALOG_FRAGMENT_CONFIGURATION_STATUS = "DIALOG_FRAGMENT_CONFIGURATION_STATUS";

    @BindView(R.id.container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.provisioning_progress_bar)
    ProgressBar mProvisioningProgressBar;
    @BindView(R.id.data_container)
    ScrollView container;
    @BindView(R.id.capabilities_container)
    View mCapabilitiesContainer;
    @BindView(R.id.info_provisioning_status_container)
    View provisioningStatusContainer;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private ProvisioningViewModel mViewModel;

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
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(deviceName);
        getSupportActionBar().setSubtitle(deviceAddress);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(ProvisioningViewModel.class);
        if (savedInstanceState == null)
            mViewModel.connect(this, device, false);

        // Set up views
        final LinearLayout connectivityProgressContainer = findViewById(R.id.connectivity_progress_container);
        final TextView connectionState = findViewById(R.id.connection_state);
        final Button action_provision = findViewById(R.id.action_provision_device);

        final View containerName = findViewById(R.id.container_name);
        containerName.findViewById(R.id.image)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.ic_label_outline_black_alpha_24dp));
        final TextView nameTitle = containerName.findViewById(R.id.title);
        nameTitle.setText(R.string.summary_name);
        final TextView nameView = containerName.findViewById(R.id.text);
        nameView.setVisibility(View.VISIBLE);
        containerName.setOnClickListener(v -> {
            final DialogFragmentNodeName dialogFragmentNodeName = DialogFragmentNodeName.newInstance(deviceName);
            dialogFragmentNodeName.show(getSupportFragmentManager(), null);
        });

        final View containerUnicastAddress = findViewById(R.id.container_unicast);
        containerUnicastAddress.findViewById(R.id.image)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.ic_lan_black_alpha_24dp));
        final TextView unicastAddressTitle = containerUnicastAddress.findViewById(R.id.title);
        unicastAddressTitle.setText(R.string.title_unicast_address);
        final TextView unicastAddressView = containerUnicastAddress.findViewById(R.id.text);
        unicastAddressView.setVisibility(View.VISIBLE);
        containerUnicastAddress.setOnClickListener(v -> {

            final UnprovisionedMeshNode node = mViewModel.getUnprovisionedMeshNode().getValue();
            if (node != null && node.getProvisioningCapabilities() != null) {
                final int elementCount = node.getProvisioningCapabilities().getNumberOfElements();
                final DialogFragmentUnicastAddress dialogFragmentFlags = DialogFragmentUnicastAddress.
                        newInstance(mViewModel.getNetworkLiveData().getMeshNetwork().getUnicastAddress(), elementCount);
                dialogFragmentFlags.show(getSupportFragmentManager(), null);
            }
        });

        final View containerAppKey = findViewById(R.id.container_app_keys);
        containerAppKey.findViewById(R.id.image)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_black_alpha_24dp));
        final TextView appKeyTitle = containerAppKey.findViewById(R.id.title);
        appKeyTitle.setText(R.string.title_app_keys);
        final TextView appKeyView = containerAppKey.findViewById(R.id.text);
        appKeyView.setVisibility(View.VISIBLE);
        containerAppKey.setOnClickListener(v -> {
            final Intent manageAppKeys = new Intent(ProvisioningActivity.this, AppKeysActivity.class);
            manageAppKeys.putExtra(Utils.EXTRA_DATA, Utils.ADD_APP_KEY);
            startActivityForResult(manageAppKeys, Utils.SELECT_KEY);
        });

        mViewModel.getConnectionState().observe(this, connectionState::setText);

        mViewModel.isConnected().observe(this, connected -> {
            final boolean isComplete = mViewModel.isProvisioningComplete();
            if (isComplete) {
                return;
            }

            if (connected != null && !connected)
                finish();
        });

        mViewModel.isDeviceReady().observe(this, deviceReady -> {
            if (mViewModel.getBleMeshManager().isDeviceReady()) {
                connectivityProgressContainer.setVisibility(View.GONE);
                final boolean isComplete = mViewModel.isProvisioningComplete();
                if (isComplete) {
                    mProvisioningProgressBar.setVisibility(View.VISIBLE);
                    provisioningStatusContainer.setVisibility(View.VISIBLE);
                    setupProvisionerStateObservers(provisioningStatusContainer);
                    return;
                }
                container.setVisibility(View.VISIBLE);
            }
        });

        mViewModel.isReconnecting().observe(this, isReconnecting -> {
            if (isReconnecting != null && isReconnecting) {
                mViewModel.getUnprovisionedMeshNode().removeObservers(this);
                provisioningStatusContainer.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
                mProvisioningProgressBar.setVisibility(View.GONE);
                connectivityProgressContainer.setVisibility(View.VISIBLE);
            } else {
                setResultIntent();
            }
        });

        mViewModel.getNetworkLiveData().observe(this, meshNetworkLiveData -> {
            nameView.setText(meshNetworkLiveData.getNodeName());
            final ApplicationKey applicationKey = meshNetworkLiveData.getSelectedAppKey();
            appKeyView.setText(MeshParserUtils.bytesToHex(applicationKey.getKey(), false));
            unicastAddressView.setText(getString(R.string.hex_format,
                    String.format(Locale.US, "%04X", meshNetworkLiveData.getMeshNetwork().getUnicastAddress())));
        });

        mViewModel.getUnprovisionedMeshNode().observe(this, meshNode -> {
            if (meshNode != null) {
                final ProvisioningCapabilities capabilities = meshNode.getProvisioningCapabilities();
                if (capabilities != null) {
                    mProvisioningProgressBar.setVisibility(View.INVISIBLE);
                    action_provision.setText(R.string.provision_action);
                    containerUnicastAddress.setVisibility(View.VISIBLE);
                    final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
                    if (network != null) {
                        try {
                            final int elementCount = capabilities.getNumberOfElements();
                            final Provisioner provisioner = network.getSelectedProvisioner();
                            final int unicast = network.nextAvailableUnicastAddress(elementCount, provisioner);
                            network.assignUnicastAddress(unicast);
                            updateCapabilitiesUi(capabilities);
                        } catch (IllegalArgumentException ex) {
                            action_provision.setEnabled(false);
                            mViewModel.displaySnackBar(this, mCoordinatorLayout, ex.getMessage(), Snackbar.LENGTH_LONG);
                        }
                    }
                }
            }
        });

        action_provision.setOnClickListener(v -> {
            final UnprovisionedMeshNode node = mViewModel.getUnprovisionedMeshNode().getValue();
            if (node == null) {
                device.setName(mViewModel.getNetworkLiveData().getNodeName());
                mViewModel.getNrfMeshRepository().identifyNode(device);
                return;
            }

            if (node.getProvisioningCapabilities() != null) {
                if (node.getProvisioningCapabilities().getAvailableOOBTypes().size() == 1 &&
                        node.getProvisioningCapabilities().getAvailableOOBTypes().get(0) == AuthenticationOOBMethods.NO_OOB_AUTHENTICATION) {
                    onNoOOBSelected();
                } else {
                    final DialogFragmentSelectOOBType fragmentSelectOOBType = DialogFragmentSelectOOBType.newInstance(node.getProvisioningCapabilities());
                    fragmentSelectOOBType.show(getSupportFragmentManager(), null);
                }
            }
        });

        if (savedInstanceState == null)
            mViewModel.getNetworkLiveData().resetSelectedAppKey();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //We disconnect from the device if the user presses the back button
        disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.SELECT_KEY) {
            if (resultCode == RESULT_OK) {
                final ApplicationKey appKey = data.getParcelableExtra(AppKeysActivity.RESULT_APP_KEY);
                if (appKey != null) {
                    mViewModel.getNetworkLiveData().setSelectedAppKey(appKey);
                }
            }
        }
    }

    @Override
    public void onPinInputComplete(final String pin) {
        mViewModel.getMeshManagerApi().setProvisioningAuthentication(pin);
    }

    @Override
    public void onPinInputCanceled() {
        final String message = getString(R.string.provisioning_cancelled);
        final Snackbar snackbar = Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
        disconnect();
    }

    @Override
    public boolean onNodeNameUpdated(@NonNull final String nodeName) {
        mViewModel.getNetworkLiveData().setNodeName(nodeName);
        return true;
    }

    @Override
    public boolean setUnicastAddress(final int unicastAddress) {
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            return network.assignUnicastAddress(unicastAddress);
        }
        return false;
    }

    @Override
    public int getNextUnicastAddress(final int elementCount) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        return network.nextAvailableUnicastAddress(elementCount, network.getSelectedProvisioner());
    }

    @Override
    public void onProvisioningFailed() {
        //Provisioning failed so now we go back to the scanner page.
        disconnect();
        setResultIntent();
    }

    private void disconnect() {
        mViewModel.getUnprovisionedMeshNode().removeObservers(this);
        mViewModel.disconnect();
    }

    public void setupProvisionerStateObservers(final View provisioningStatusContainer) {
        provisioningStatusContainer.setVisibility(View.VISIBLE);

        final RecyclerView recyclerView = provisioningStatusContainer.findViewById(R.id.recycler_view_provisioning_progress);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ProvisioningProgressAdapter adapter = new ProvisioningProgressAdapter(this, mViewModel.getProvisioningStatus());
        recyclerView.setAdapter(adapter);

        mViewModel.getProvisioningStatus().observe(this, provisioningStateLiveData -> {
            if (provisioningStateLiveData != null) {
                final ProvisionerProgress provisionerProgress = provisioningStateLiveData.getProvisionerProgress();
                adapter.refresh(provisioningStateLiveData.getStateList());
                if (provisionerProgress != null) {
                    final ProvisionerStates state = provisionerProgress.getState();
                    switch (state) {
                        case PROVISIONING_FAILED:
                            if (getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_PROVISIONING_FAILED) == null) {
                                final String statusMessage = ProvisioningFailedState.parseProvisioningFailure(getApplicationContext(), provisionerProgress.getStatusReceived());
                                DialogFragmentProvisioningFailedError message = DialogFragmentProvisioningFailedError.newInstance(getString(R.string.title_error_provisioning_failed), statusMessage);
                                message.show(getSupportFragmentManager(), DIALOG_FRAGMENT_PROVISIONING_FAILED);
                            }
                            break;
                        case PROVISIONING_AUTHENTICATION_STATIC_OOB_WAITING:
                            if (getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_AUTH_INPUT_TAG) == null) {
                                DialogFragmentAuthenticationInput dialogFragmentAuthenticationInput = DialogFragmentAuthenticationInput.
                                        newInstance(mViewModel.getUnprovisionedMeshNode().getValue());
                                dialogFragmentAuthenticationInput.show(getSupportFragmentManager(), DIALOG_FRAGMENT_AUTH_INPUT_TAG);
                            }
                            break;
                        case PROVISIONING_AUTHENTICATION_OUTPUT_OOB_WAITING:
                            if (getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_AUTH_INPUT_TAG) == null) {
                                DialogFragmentAuthenticationInput dialogFragmentAuthenticationInput = DialogFragmentAuthenticationInput.
                                        newInstance(mViewModel.getUnprovisionedMeshNode().getValue());
                                dialogFragmentAuthenticationInput.show(getSupportFragmentManager(), DIALOG_FRAGMENT_AUTH_INPUT_TAG);
                            }
                            break;
                        case PROVISIONING_AUTHENTICATION_INPUT_OOB_WAITING:
                            if (getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_AUTH_INPUT_TAG) == null) {
                                DialogFragmentAuthenticationInput dialogFragmentAuthenticationInput = DialogFragmentAuthenticationInput.
                                        newInstance(mViewModel.getUnprovisionedMeshNode().getValue());
                                dialogFragmentAuthenticationInput.show(getSupportFragmentManager(), DIALOG_FRAGMENT_AUTH_INPUT_TAG);
                            }
                            break;
                        case PROVISIONING_AUTHENTICATION_INPUT_ENTERED:
                            final DialogFragmentAuthenticationInput fragment = (DialogFragmentAuthenticationInput) getSupportFragmentManager().
                                    findFragmentByTag(DIALOG_FRAGMENT_AUTH_INPUT_TAG);
                            if (fragment != null)
                                fragment.dismiss();
                            break;
                        case NETWORK_TRANSMIT_STATUS_RECEIVED:
                            if (getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_CONFIGURATION_STATUS) == null) {
                                DialogFragmentConfigurationComplete fragmentConfigComplete = DialogFragmentConfigurationComplete.
                                        newInstance(getString(R.string.title_configuration_compete), getString(R.string.configuration_complete_summary));
                                fragmentConfigComplete.show(getSupportFragmentManager(), DIALOG_FRAGMENT_CONFIGURATION_STATUS);
                            }
                            break;
                        case PROVISIONER_UNASSIGNED:
                            setResultIntent();
                            break;
                        default:
                            break;
                    }

                }
                container.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onConfigurationCompleted() {
        setResultIntent();
    }

    private void setResultIntent() {
        final Intent returnIntent = new Intent();
        if (mViewModel.isProvisioningComplete()) {
            returnIntent.putExtra(Utils.PROVISIONING_COMPLETED, true);
            setResult(Activity.RESULT_OK, returnIntent);
            final ProvisionerProgress progress = mViewModel.getProvisioningStatus().getProvisionerProgress();
            if (progress.getState() == ProvisionerStates.PROVISIONER_UNASSIGNED) {
                returnIntent.putExtra(Utils.PROVISIONER_UNASSIGNED, true);
            } else {
                if (mViewModel.isCompositionDataStatusReceived()) {
                    returnIntent.putExtra(Utils.COMPOSITION_DATA_COMPLETED, true);
                    if (mViewModel.isDefaultTtlReceived()) {
                        returnIntent.putExtra(Utils.DEFAULT_GET_COMPLETED, true);
                        if (mViewModel.isAppKeyAddCompleted()) {
                            returnIntent.putExtra(Utils.APP_KEY_ADD_COMPLETED, true);
                            if (mViewModel.isNetworkRetransmitSetCompleted()) {
                                returnIntent.putExtra(Utils.NETWORK_TRANSMIT_SET_COMPLETED, true);
                            }
                        }
                    }
                }
            }
        }
        finish();
    }

    private void updateCapabilitiesUi(final ProvisioningCapabilities capabilities) {
        mCapabilitiesContainer.setVisibility(View.VISIBLE);

        final String numberOfElements = String.valueOf(capabilities.getNumberOfElements());
        ((TextView) mCapabilitiesContainer.
                findViewById(R.id.container_element_count).
                findViewById(R.id.text)).setText(numberOfElements);

        ((TextView) mCapabilitiesContainer.
                findViewById(R.id.container_supported_algorithm).
                findViewById(R.id.text)).
                setText(parseAlgorithms(capabilities));

        ((TextView) mCapabilitiesContainer.
                findViewById(R.id.container_public_key_type).
                findViewById(R.id.text)).
                setText(capabilities.isPublicKeyInformationAvailable() ? R.string.public_key_information_available : R.string.public_key_information_unavailable);

        ((TextView) mCapabilitiesContainer.
                findViewById(R.id.container_static_oob_type).
                findViewById(R.id.text)).
                setText(capabilities.isStaticOOBInformationAvailable() ? R.string.static_oob_information_available : R.string.static_oob_information_unavailable);

        ((TextView) mCapabilitiesContainer.
                findViewById(R.id.container_output_oob_size)
                .findViewById(R.id.text))
                .setText(String.valueOf(capabilities.getOutputOOBSize()));

        ((TextView) mCapabilitiesContainer.
                findViewById(R.id.container_output_actions).
                findViewById(R.id.text)).
                setText(parseOutputOOBActions(capabilities));

        ((TextView) mCapabilitiesContainer.
                findViewById(R.id.container_input_oob_size).
                findViewById(R.id.text)).
                setText(String.valueOf(capabilities.getInputOOBSize()));

        ((TextView) mCapabilitiesContainer.
                findViewById(R.id.container_input_actions)
                .findViewById(R.id.text))
                .setText(parseInputOOBActions(capabilities));
    }

    private String parseAlgorithms(final ProvisioningCapabilities capabilities) {
        final StringBuilder algorithmTypes = new StringBuilder();
        int count = 0;
        for (AlgorithmType algorithmType : capabilities.getSupportedAlgorithmTypes()) {
            if (count == 0) {
                algorithmTypes.append(AlgorithmType.getAlgorithmTypeDescription(algorithmType));
            } else {
                algorithmTypes.append(", ").append(AlgorithmType.getAlgorithmTypeDescription(algorithmType));
            }
            count++;
        }
        return algorithmTypes.toString();
    }

    private String parseOutputOOBActions(final ProvisioningCapabilities capabilities) {
        if (capabilities.getSupportedOutputOOBActions().isEmpty())
            return getString(R.string.output_oob_actions_unavailable);

        final StringBuilder outputOOBActions = new StringBuilder();
        int count = 0;
        for (OutputOOBAction outputOOBAction : capabilities.getSupportedOutputOOBActions()) {
            if (count == 0) {
                outputOOBActions.append(OutputOOBAction.getOutputOOBActionDescription(outputOOBAction));
            } else {
                outputOOBActions.append(", ").append(OutputOOBAction.getOutputOOBActionDescription(outputOOBAction));
            }
            count++;
        }
        return outputOOBActions.toString();
    }

    private String parseInputOOBActions(final ProvisioningCapabilities capabilities) {
        if (capabilities.getSupportedInputOOBActions().isEmpty())
            return getString(R.string.input_oob_actions_unavailable);

        final StringBuilder inputOOBActions = new StringBuilder();
        int count = 0;
        for (InputOOBAction inputOOBAction : capabilities.getSupportedInputOOBActions()) {
            if (count == 0) {
                inputOOBActions.append(InputOOBAction.getInputOOBActionDescription(inputOOBAction));
            } else {
                inputOOBActions.append(", ").append(InputOOBAction.getInputOOBActionDescription(inputOOBAction));
            }
            count++;
        }
        return inputOOBActions.toString();
    }

    @Override
    public void onNoOOBSelected() {
        final UnprovisionedMeshNode node = mViewModel.getUnprovisionedMeshNode().getValue();
        if (node != null) {
            try {
                node.setNodeName(mViewModel.getNetworkLiveData().getNodeName());
                setupProvisionerStateObservers(provisioningStatusContainer);
                mProvisioningProgressBar.setVisibility(View.VISIBLE);
                mViewModel.getMeshManagerApi().startProvisioning(node);
            } catch (IllegalArgumentException ex) {
                mViewModel.displaySnackBar(this, mCoordinatorLayout, ex.getMessage(), Snackbar.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onStaticOOBSelected(final StaticOOBType staticOOBType) {
        final UnprovisionedMeshNode node = mViewModel.getUnprovisionedMeshNode().getValue();
        if (node != null) {
            try {
                node.setNodeName(mViewModel.getNetworkLiveData().getNodeName());
                setupProvisionerStateObservers(provisioningStatusContainer);
                mProvisioningProgressBar.setVisibility(View.VISIBLE);
                mViewModel.getMeshManagerApi().startProvisioningWithStaticOOB(node);
            } catch (IllegalArgumentException ex) {
                mViewModel.displaySnackBar(this, mCoordinatorLayout, ex.getMessage(), Snackbar.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onOutputOOBActionSelected(final OutputOOBAction action) {
        final UnprovisionedMeshNode node = mViewModel.getUnprovisionedMeshNode().getValue();
        if (node != null) {
            try {
                node.setNodeName(mViewModel.getNetworkLiveData().getNodeName());
                setupProvisionerStateObservers(provisioningStatusContainer);
                mProvisioningProgressBar.setVisibility(View.VISIBLE);
                mViewModel.getMeshManagerApi().startProvisioningWithOutputOOB(node, action);
            } catch (IllegalArgumentException ex) {
                mViewModel.displaySnackBar(this, mCoordinatorLayout, ex.getMessage(), Snackbar.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onInputOOBActionSelected(final InputOOBAction action) {
        final UnprovisionedMeshNode node = mViewModel.getUnprovisionedMeshNode().getValue();
        if (node != null) {
            try {
                node.setNodeName(mViewModel.getNetworkLiveData().getNodeName());
                setupProvisionerStateObservers(provisioningStatusContainer);
                mProvisioningProgressBar.setVisibility(View.VISIBLE);
                mViewModel.getMeshManagerApi().startProvisioningWithInputOOB(node, action);
            } catch (IllegalArgumentException ex) {
                mViewModel.displaySnackBar(this, mCoordinatorLayout, ex.getMessage(), Snackbar.LENGTH_LONG);
            }
        }
    }
}
