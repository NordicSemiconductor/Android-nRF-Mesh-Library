package no.nordicsemi.android.nrfmeshprovisioner.node;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import no.nordicsemi.android.meshprovisioner.models.ConfigurationServerModel;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetworkTransmitGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetworkTransmitSet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetworkTransmitStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigRelayGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigRelaySet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigRelayStatus;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.NetworkTransmitSettings;
import no.nordicsemi.android.meshprovisioner.utils.RelaySettings;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.node.dialog.DialogFragmentNetworkTransmitSettings;
import no.nordicsemi.android.nrfmeshprovisioner.node.dialog.DialogRelayRetransmitSettings;

public class ConfigurationServerActivity extends BaseModelConfigurationActivity implements
        DialogFragmentNetworkTransmitSettings.DialogFragmentNetworkTransmitSettingsListener,
        DialogRelayRetransmitSettings.DialogFragmentRelaySettingsListener {

    private static final String TAG = ConfigurationServerActivity.class.getSimpleName();

    private static final int NETWORK_TRANSMIT_SETTING_UNKNOWN = -1;
    private static final int RELAY_RETRANSMIT_SETTINGS_UNKNOWN = -1;

    private TextView mRelayRetransmitCountText;
    private TextView mRelayRetransmitIntervalStepsText;
    private TextView mNetworkTransmitCountText;
    private TextView mNetworkTransmitIntervalStepsText;

    private int mRelayRetransmitCount = RELAY_RETRANSMIT_SETTINGS_UNKNOWN;
    private int mRelayRetransmitIntervalSteps = RELAY_RETRANSMIT_SETTINGS_UNKNOWN;
    private int mNetworkTransmitCount = NETWORK_TRANSMIT_SETTING_UNKNOWN;
    private int mNetworkTransmitIntervalSteps = NETWORK_TRANSMIT_SETTING_UNKNOWN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof ConfigurationServerModel) {
            //Hide the app key binding, publication adn subscription views since the ConfigurationServerModel does not support app key binding
            mContainerAppKeyBinding.setVisibility(View.GONE);
            mContainerPublication.setVisibility(View.GONE);
            mContainerSubscribe.setVisibility(View.GONE);
            final ConstraintLayout view = findViewById(R.id.node_controls_container);
            final View nodeControlsContainer = LayoutInflater.from(this)
                    .inflate(R.layout.layout_config_server_model, view);

            final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
            if (meshNode != null) {
                final Button actionReadRelayState = nodeControlsContainer.findViewById(R.id.action_relay_retransmit_get);
                mRelayRetransmitIntervalStepsText = nodeControlsContainer.findViewById(R.id.relay_retransmit_interval_steps);
                mActionSetRelayState = nodeControlsContainer.findViewById(R.id.action_relay_retransmit_configure);
                mRelayRetransmitCountText = nodeControlsContainer.findViewById(R.id.relay_retransmit_count);
                if (meshNode.getNodeFeatures().isRelayFeatureSupported()) {
                    final CardView relayCardView = findViewById(R.id.config_relay_set_card);
                    relayCardView.setVisibility(View.VISIBLE);

                    actionReadRelayState.setOnClickListener(v -> {
                        if (!checkConnectivity()) return;
                        getRelayRetransmit();
                    });

                    mActionSetRelayState.setOnClickListener(v -> {
                        if (!checkConnectivity()) return;
                        final RelaySettings relaySettings = meshNode.getRelaySettings();
                        if (relaySettings != null) {
                            mRelayRetransmitCount = meshNode.getRelaySettings().getRelayTransmitCount();
                            mRelayRetransmitIntervalSteps = meshNode.getRelaySettings().getRelayIntervalSteps();
                        }
                        //Assuming relay is enabled by default
                        final DialogRelayRetransmitSettings fragment = DialogRelayRetransmitSettings.newInstance(1, mRelayRetransmitCount, mRelayRetransmitIntervalSteps);
                        fragment.show(getSupportFragmentManager(), null);
                    });
                }
            }


            mNetworkTransmitCountText = nodeControlsContainer.findViewById(R.id.network_transmit_count);
            mNetworkTransmitIntervalStepsText = nodeControlsContainer.findViewById(R.id.network_transmit_interval_steps);

            mReadNetworkTransmitStateButton = nodeControlsContainer.findViewById(R.id.action_network_transmit_get);
            mReadNetworkTransmitStateButton.setOnClickListener(v -> {
                if (!checkConnectivity()) return;
                getNetworkTransmit();
            });

            mSetNetworkTransmitStateButton = nodeControlsContainer.findViewById(R.id.action_network_transmit_configure);
            mSetNetworkTransmitStateButton.setOnClickListener(v -> {
                if (!checkConnectivity()) return;
                if (meshNode != null && meshNode.getNetworkTransmitSettings() != null) {
                    mNetworkTransmitCount = meshNode.getNetworkTransmitSettings().getNetworkTransmitCount();
                    mNetworkTransmitIntervalSteps = meshNode.getNetworkTransmitSettings().getNetworkIntervalSteps();
                }

                final DialogFragmentNetworkTransmitSettings fragment = DialogFragmentNetworkTransmitSettings.newInstance(mNetworkTransmitCount, mNetworkTransmitIntervalSteps);
                fragment.show(getSupportFragmentManager(), null);
            });

            mViewModel.getSelectedMeshNode().observe(this, node -> {
                if (node != null) {
                    updateNetworkTransmitUi(node);
                    updateRelayUi(node);
                }
            });

            if (savedInstanceState == null) {
                updateNetworkTransmitUi(mViewModel.getSelectedMeshNode().getValue());
                updateRelayUi(mViewModel.getSelectedMeshNode().getValue());
            }
        }
    }

    private void getRelayRetransmit() {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        try {
            if (node != null) {
                ConfigRelayGet message = new ConfigRelayGet();
                sendMessage(node.getUnicastAddress(), message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while constructing ConfigNetworkTransmitGet", e);
        }
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        if (meshMessage instanceof ConfigNetworkTransmitStatus) {
            final ConfigNetworkTransmitStatus status = (ConfigNetworkTransmitStatus) meshMessage;
            final ProvisionedMeshNode meshNode = mViewModel.getNetworkLiveData().getMeshNetwork().getNode(status.getSrc());
            updateNetworkTransmitUi(meshNode);
        } else if (meshMessage instanceof ConfigRelayStatus) {
            final ConfigRelayStatus status = (ConfigRelayStatus) meshMessage;
            final ProvisionedMeshNode meshNode = mViewModel.getNetworkLiveData().getMeshNetwork().getNode(status.getSrc());
            updateRelayUi(meshNode);
        }
    }

    @Override
    protected void enableClickableViews() {
        super.enableClickableViews();
        mReadNetworkTransmitStateButton.setEnabled(true);
        mSetNetworkTransmitStateButton.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        mReadNetworkTransmitStateButton.setEnabled(false);
        mSetNetworkTransmitStateButton.setEnabled(false);
    }

    @Override
    public void onNetworkTransmitSettingsEntered(int transmitCount, int transmitIntervalSteps) {
        setNetworkTransmit(transmitCount, transmitIntervalSteps);
    }

    @Override
    public void onRelayRetransmitSet(final int relay, final int retransmitCount, final int retransmitIntervalSteps) {
        setRelayRetransmit(relay, retransmitCount, retransmitIntervalSteps);
    }

    private void getNetworkTransmit() {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        try {
            if (node != null) {
                ConfigNetworkTransmitGet message = new ConfigNetworkTransmitGet();
                sendMessage(node.getUnicastAddress(), message);
            }
        } catch (Exception e) {
            hideProgressBar();
            Log.e(TAG, "Exception while constructing ConfigNetworkTransmitGet", e);
        }
    }

    private void setRelayRetransmit(final int relay, final int relayRetransmit, final int relayRetransmitIntervalSteps) {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        try {
            if (node != null) {
                final ConfigRelaySet message = new ConfigRelaySet(relay, relayRetransmit, relayRetransmitIntervalSteps);
                sendMessage(node.getUnicastAddress(), message);
            }
        } catch (Exception e) {
            hideProgressBar();
            Log.e(TAG, "Exception while ConfigNetworkTransmitSet: " + e.getMessage());
        }
    }

    private void setNetworkTransmit(final int networkTransmitCount, final int networkTransmitIntervalSteps) {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        try {
            if (node != null) {
                final ConfigNetworkTransmitSet message = new ConfigNetworkTransmitSet(networkTransmitCount, networkTransmitIntervalSteps);
                sendMessage(node.getUnicastAddress(), message);
            }
        } catch (Exception e) {
            hideProgressBar();
            Log.e(TAG, "Error ConfigNetworkTransmitSet: " + e.getMessage());
        }
    }

    private void updateNetworkTransmitUi(@NonNull final ProvisionedMeshNode meshNode) {
        final NetworkTransmitSettings networkTransmitSettings = meshNode.getNetworkTransmitSettings();
        if (networkTransmitSettings != null) {
            mSetNetworkTransmitStateButton.setEnabled(true);
            mNetworkTransmitCountText.setText(getResources().getQuantityString(R.plurals.transmit_count,
                    networkTransmitSettings.getTransmissionCount(),
                    networkTransmitSettings.getTransmissionCount()));
            mNetworkTransmitIntervalStepsText.setText(getString(R.string.time_ms,
                    networkTransmitSettings.getNetworkTransmissionInterval()));
        } else {
            mSetNetworkTransmitStateButton.setEnabled(false);
            mNetworkTransmitCountText.setText(getResources().getString(R.string.unknown));
            mNetworkTransmitIntervalStepsText.setText(getResources().getString(R.string.unknown));
        }
    }

    private void updateRelayUi(@NonNull final ProvisionedMeshNode meshNode) {
        final RelaySettings relaySettings = meshNode.getRelaySettings();
        if (relaySettings != null) {
            mActionSetRelayState.setEnabled(true);
            mRelayRetransmitCountText.setText(getResources().getQuantityString(R.plurals.summary_network_transmit_count,
                    relaySettings.getRelayTransmitCount(),
                    relaySettings.getRelayTransmitCount(),
                    relaySettings.getTotalTransmissionsCount()));
            mRelayRetransmitIntervalStepsText.setText(getString(R.string.time_ms,
                    relaySettings.getRetransmissionIntervals()));
        } else {
            mActionSetRelayState.setEnabled(false);
            mRelayRetransmitCountText.setText(getResources().getString(R.string.unknown));
            mRelayRetransmitIntervalStepsText.setText(getResources().getString(R.string.unknown));
        }
    }
}
