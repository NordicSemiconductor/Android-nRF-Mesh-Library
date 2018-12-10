package no.nordicsemi.android.nrfmeshprovisioner;

import android.os.Bundle;
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
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.NetworkTransmitSettings;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentNetworkTransmitSettings;

public class ConfigurationServerActivity extends BaseModelConfigurationActivity
        implements DialogFragmentNetworkTransmitSettings.DialogFragmentNetworkTransmitSettingsListener {

    private static final String TAG = ConfigurationServerActivity.class.getSimpleName();

    private static final int NETWORK_TRANSMIT_SETTING_UNKNOWN = -1;

    private static final String NETWORK_TRANSMIT_COUNT = "NETWORK_TRANSMIT_COUNT";
    private static final String NETWORK_TRANSMIT_INTERVAL_STEPS = "NETWORK_TRANSMIT_INTERVAL_STEPS";

    private Button mReadNetworkTransmitStateButton;
    private Button mSetNetworkTransmitStateButton;
    private TextView mNetworkTransmitCountText;
    private TextView mNetworkTransmitIntervalStepsText;

    private int mNetworkTransmitCount = NETWORK_TRANSMIT_SETTING_UNKNOWN;
    private int mNetworkTransmitIntervalSteps = NETWORK_TRANSMIT_SETTING_UNKNOWN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MeshModel model = mViewModel.getSelectedModel().getMeshModel();
        if (model instanceof ConfigurationServerModel) {
            final CardView cardView = findViewById(R.id.node_controls_card);
            final View nodeControlsContainer = LayoutInflater.from(this)
                    .inflate(R.layout.layout_configuration_server_controls, cardView);


            mNetworkTransmitCountText = nodeControlsContainer.findViewById(R.id.network_transmit_count);
            mNetworkTransmitIntervalStepsText = nodeControlsContainer.findViewById(R.id.network_transmit_interval_steps);

            mReadNetworkTransmitStateButton = nodeControlsContainer.findViewById(R.id.action_network_transmit_get);
            mReadNetworkTransmitStateButton.setOnClickListener(v -> getNetworkTransmit());

            mSetNetworkTransmitStateButton = nodeControlsContainer.findViewById(R.id.action_network_transmit_configure);
            mSetNetworkTransmitStateButton.setOnClickListener(v -> {
                final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getMeshNode();
                if(meshNode.getNetworkTransmitSettings() != null) {
                    mNetworkTransmitCount = meshNode.getNetworkTransmitSettings().getNetworkTransmitCount();
                    mNetworkTransmitIntervalSteps = meshNode.getNetworkTransmitSettings().getNetworkIntervalSteps();
                }

                final DialogFragmentNetworkTransmitSettings fragment =
                        DialogFragmentNetworkTransmitSettings.newInstance(mNetworkTransmitCount, mNetworkTransmitIntervalSteps);
                fragment.show(getSupportFragmentManager(), null);
            });

            mViewModel.getSelectedMeshNode().observe(this, this::updateUi);

            if (savedInstanceState == null)
                updateUi(mViewModel.getSelectedMeshNode().getMeshNode());
        }
    }

    private void getNetworkTransmit() {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getMeshNode();
        try {
            ConfigNetworkTransmitGet message = new ConfigNetworkTransmitGet(node, 0);
            mViewModel.getMeshManagerApi().sendMeshConfigurationMessage(message);
            showProgressbar();
        } catch (Exception e) {
            Log.e(TAG, "Error while constructing ConfigNetworkTransmitGet", e);
        }
    }

    private void setNetworkTransmit(final int networkTransmitCount, final int networkTransmitIntervalSteps) {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getMeshNode();
        try {
            final ConfigNetworkTransmitSet message = new ConfigNetworkTransmitSet(node, networkTransmitCount, networkTransmitIntervalSteps, 0);
            mViewModel.getMeshManagerApi().sendMeshConfigurationMessage(message);
            showProgressbar();
        } catch (Exception e) {
            Log.e(TAG, "Error ConfigNetworkTransmitSet: " + e.getMessage());
        }
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        if (meshMessage instanceof ConfigNetworkTransmitStatus) {
            final ConfigNetworkTransmitStatus status = (ConfigNetworkTransmitStatus) meshMessage;
            updateUi(status.getMeshNode());
        }
    }

    private void updateUi(final ProvisionedMeshNode meshNode) {
        final NetworkTransmitSettings networkTransmitSettings = meshNode.getNetworkTransmitSettings();
        if (networkTransmitSettings != null) {
            mSetNetworkTransmitStateButton.setEnabled(true);
            mNetworkTransmitCountText.setText(getString(R.string.text_network_transmit_count,
                    networkTransmitSettings.getTransmissionCount()));
            mNetworkTransmitIntervalStepsText.setText(getString(R.string.text_network_transmit_interval_steps,
                    networkTransmitSettings.getNetworkTransmissionInterval()));
        } else {
            mSetNetworkTransmitStateButton.setEnabled(false);
            mNetworkTransmitCountText.setText(getResources().getString(R.string.text_network_transmit_unknown));
            mNetworkTransmitIntervalStepsText.setText(getResources().getString(R.string.text_network_transmit_unknown));
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
        this.setNetworkTransmit(transmitCount, transmitIntervalSteps);
    }
}
