package no.nordicsemi.android.nrfmeshprovisioner;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentNetworkTransmitSettings;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogRelayRetransmitSettings;

public class ConfigurationServerActivity extends BaseModelConfigurationActivity implements
        DialogFragmentNetworkTransmitSettings.DialogFragmentNetworkTransmitSettingsListener,
        DialogRelayRetransmitSettings.DialogFragmentRelaySettingsListener {

    private static final String TAG = ConfigurationServerActivity.class.getSimpleName();

    private static final int NETWORK_TRANSMIT_SETTING_UNKNOWN = -1;
    private static final int RELAY_RETRANSMIT_SETTINGS_UNKNOWN = -1;

    private Button mActionReadRelayState;
    private Button mActionSetRelayState;
    private TextView mRelayRetransmitCountText;
    private TextView mRelayRetransmitIntervalStepsText;

    private Button mReadNetworkTransmitStateButton;
    private Button mSetNetworkTransmitStateButton;
    private TextView mNetworkTransmitCountText;
    private TextView mNetworkTransmitIntervalStepsText;

    private int mRelay;
    private int mRelayRetransmitCount = RELAY_RETRANSMIT_SETTINGS_UNKNOWN;
    private int mRelayRetransmitIntervalSteps = RELAY_RETRANSMIT_SETTINGS_UNKNOWN;
    private int mNetworkTransmitCount = NETWORK_TRANSMIT_SETTING_UNKNOWN;
    private int mNetworkTransmitIntervalSteps = NETWORK_TRANSMIT_SETTING_UNKNOWN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof ConfigurationServerModel) {
            //Hide the subscription view since the ConfigurationServerModel is not a subscribe model
            mContainerSubscribe.setVisibility(View.GONE);
            final ConstraintLayout view = findViewById(R.id.node_controls_container);
            final View nodeControlsContainer = LayoutInflater.from(this)
                    .inflate(R.layout.layout_config_server_model, view);

            final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
            if(meshNode != null) {
                if(meshNode.getNodeFeatures().isRelayFeatureSupported()) {
                    final CardView relayCardView = findViewById(R.id.config_relay_set_card);
                    relayCardView.setVisibility(View.VISIBLE);
                    mRelayRetransmitCountText = nodeControlsContainer.findViewById(R.id.relay_retransmit_count);
                    mRelayRetransmitIntervalStepsText = nodeControlsContainer.findViewById(R.id.relay_retransmit_interval_steps);

                    mActionReadRelayState = nodeControlsContainer.findViewById(R.id.action_relay_retransmit_get);
                    mActionReadRelayState.setOnClickListener(v -> getRelayRetransmit());

                    mActionSetRelayState = nodeControlsContainer.findViewById(R.id.action_relay_retransmit_configure);
                    mActionSetRelayState.setOnClickListener(v -> {
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
            mReadNetworkTransmitStateButton.setOnClickListener(v -> getNetworkTransmit());

            mSetNetworkTransmitStateButton = nodeControlsContainer.findViewById(R.id.action_network_transmit_configure);
            mSetNetworkTransmitStateButton.setOnClickListener(v -> {
                if (meshNode.getNetworkTransmitSettings() != null) {
                    mNetworkTransmitCount = meshNode.getNetworkTransmitSettings().getNetworkTransmitCount();
                    mNetworkTransmitIntervalSteps = meshNode.getNetworkTransmitSettings().getNetworkIntervalSteps();
                }

                final DialogFragmentNetworkTransmitSettings fragment = DialogFragmentNetworkTransmitSettings.newInstance(mNetworkTransmitCount, mNetworkTransmitIntervalSteps);
                fragment.show(getSupportFragmentManager(), null);
            });

            mViewModel.getSelectedMeshNode().observe(this, node -> {
                updateNetworkTransmitUi(node);
                updateRelayUi(node);
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
            if(node != null) {
                ConfigRelayGet message = new ConfigRelayGet();
                mViewModel.getMeshManagerApi().sendMeshMessage(node.getUnicastAddress(), message);
                showProgressbar();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while constructing ConfigNetworkTransmitGet", e);
        }
    }

    private void getNetworkTransmit() {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        try {
            if(node != null) {
                ConfigNetworkTransmitGet message = new ConfigNetworkTransmitGet();
                mViewModel.getMeshManagerApi().sendMeshMessage(node.getUnicastAddress(), message);
                showProgressbar();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while constructing ConfigNetworkTransmitGet", e);
        }
    }

    private void setRelayRetransmit(final int relay, final int relayRetransmit, final int relayRetransmitIntervalSteps) {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        try {
            if(node != null) {
                final ConfigRelaySet message = new ConfigRelaySet(relay, relayRetransmit, relayRetransmitIntervalSteps);
                mViewModel.getMeshManagerApi().sendMeshMessage(node.getUnicastAddress(), message);
                showProgressbar();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while ConfigNetworkTransmitSet: " + e.getMessage());
        }
    }

    private void setNetworkTransmit(final int networkTransmitCount, final int networkTransmitIntervalSteps) {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        try {
            if(node != null) {
                final ConfigNetworkTransmitSet message = new ConfigNetworkTransmitSet(networkTransmitCount, networkTransmitIntervalSteps);
                mViewModel.getMeshManagerApi().sendMeshMessage(node.getUnicastAddress(), message);
                showProgressbar();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ConfigNetworkTransmitSet: " + e.getMessage());
        }
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        if (meshMessage instanceof ConfigNetworkTransmitStatus) {
            final ConfigNetworkTransmitStatus status = (ConfigNetworkTransmitStatus) meshMessage;
            final ProvisionedMeshNode meshNode = mViewModel.getMeshManagerApi().getMeshNetwork().getProvisionedNode(status.getSrc());
            updateNetworkTransmitUi(meshNode);
        } else if (meshMessage instanceof ConfigRelayStatus) {
            final ConfigRelayStatus status = (ConfigRelayStatus) meshMessage;
            final ProvisionedMeshNode meshNode = mViewModel.getMeshManagerApi().getMeshNetwork().getProvisionedNode(status.getSrc());
            updateRelayUi(meshNode);
        }
    }

    private void updateNetworkTransmitUi(final ProvisionedMeshNode meshNode) {
        if(meshNode != null) {
            final NetworkTransmitSettings networkTransmitSettings = meshNode.getNetworkTransmitSettings();
            if (networkTransmitSettings != null) {
                mSetNetworkTransmitStateButton.setEnabled(true);
                mNetworkTransmitCountText.setText(getString(R.string.text_network_transmit_count,
                        networkTransmitSettings.getTransmissionCount()));
                mNetworkTransmitIntervalStepsText.setText(getString(R.string.text_network_transmit_interval_steps,
                        networkTransmitSettings.getNetworkTransmissionInterval()));
            } else {
                mSetNetworkTransmitStateButton.setEnabled(false);
                mNetworkTransmitCountText.setText(getResources().getString(R.string.unknown));
                mNetworkTransmitIntervalStepsText.setText(getResources().getString(R.string.unknown));
            }
        }
    }

    private void updateRelayUi(@NonNull final ProvisionedMeshNode meshNode) {
        //noinspection ConstantConditions
        if(meshNode != null) {
            final RelaySettings relaySettings = meshNode.getRelaySettings();
            if (relaySettings != null) {
                mActionSetRelayState.setEnabled(true);
                mRelayRetransmitCountText.setText(getString(R.string.summary_network_transmit_count,
                        relaySettings.getRelayTransmitCount(),
                        relaySettings.getTotalTransmissionsCount()));
                mRelayRetransmitIntervalStepsText.setText(getString(R.string.text_network_transmit_interval_steps,
                        relaySettings.getRetransmissionIntervals()));
            } else {
                mActionSetRelayState.setEnabled(false);
                mRelayRetransmitCountText.setText(getResources().getString(R.string.unknown));
                mRelayRetransmitIntervalStepsText.setText(getResources().getString(R.string.unknown));
            }
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
}
