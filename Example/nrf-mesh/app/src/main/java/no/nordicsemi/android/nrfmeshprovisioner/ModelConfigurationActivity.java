package no.nordicsemi.android.nrfmeshprovisioner;

import android.content.Intent;
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

public class ModelConfigurationActivity extends BaseModelConfigurationActivity {

    private static final String TAG = ModelConfigurationActivity.class.getSimpleName();

    private static final int NETWORK_TRANSMIT_SETTING_UNKNOWN = -1;

    private static final String NETWORK_TRANSMIT_COUNT = "NETWORK_TRANSMIT_COUNT";
    private static final String NETWORK_TRANSMIT_INTERVAL_STEPS = "NETWORK_TRANSMIT_INTERVAL_STEPS";

    Button mReadNetworkTransmitStateButton;
    Button mConfigureNetworkTransmitStateButton;
    TextView mNetworkTransmitCountText;
    TextView mNetworkTransmitIntervalStepsText;

    int mNetworkTransmitCount = NETWORK_TRANSMIT_SETTING_UNKNOWN;
    int mNetworkTransmitIntervalSteps = NETWORK_TRANSMIT_SETTING_UNKNOWN;


    @Override
    protected final void addControlsUi(final MeshModel model) {
        if (model instanceof ConfigurationServerModel) {
            final CardView cardView = findViewById(R.id.node_controls_card);
            final View nodeControlsContainer = LayoutInflater.from(this)
                    .inflate(R.layout.layout_configuration_server_controls, cardView);

            mNetworkTransmitCountText = nodeControlsContainer.findViewById(R.id.network_transmit_count);
            mNetworkTransmitIntervalStepsText = nodeControlsContainer.findViewById(R.id.network_transmit_interval_steps);

            mReadNetworkTransmitStateButton = nodeControlsContainer.findViewById(R.id.action_network_transmit_get);
            mReadNetworkTransmitStateButton.setOnClickListener(v -> {
                getNetworkTransmit();
            });

            mConfigureNetworkTransmitStateButton = nodeControlsContainer.findViewById(R.id.action_network_transmit_configure);
            mConfigureNetworkTransmitStateButton.setOnClickListener(v -> {
                final Intent transmitSettings = new Intent(this, NetworkTransmitSettingsActivity.class);
                if (mNetworkTransmitCount != NETWORK_TRANSMIT_SETTING_UNKNOWN) {
                    transmitSettings.putExtra(NetworkTransmitSettingsActivity.TRANSMIT_COUNT, mNetworkTransmitCount);
                }
                if (mNetworkTransmitIntervalSteps != NETWORK_TRANSMIT_SETTING_UNKNOWN) {
                    transmitSettings.putExtra(NetworkTransmitSettingsActivity.TRANSMIT_INTERVAL_STEPS, mNetworkTransmitIntervalSteps);
                }
                startActivityForResult(transmitSettings, NetworkTransmitSettingsActivity.SET_NETWORK_TRANSMIT_SETTINGS);
            });
            // Disable the Configuration button until the Network Transmit State is known
            mConfigureNetworkTransmitStateButton.setEnabled(false);

            updateUi();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NETWORK_TRANSMIT_COUNT, mNetworkTransmitCount);
        outState.putInt(NETWORK_TRANSMIT_INTERVAL_STEPS, mNetworkTransmitIntervalSteps);
    }


    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mNetworkTransmitCount = savedInstanceState.getInt(NETWORK_TRANSMIT_COUNT);
        mNetworkTransmitIntervalSteps = savedInstanceState.getInt(NETWORK_TRANSMIT_INTERVAL_STEPS);
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case NetworkTransmitSettingsActivity.SET_NETWORK_TRANSMIT_SETTINGS:
                if (resultCode == RESULT_OK) {
                    final int networkTransmitCount = data.getIntExtra(NetworkTransmitSettingsActivity.TRANSMIT_COUNT, NETWORK_TRANSMIT_SETTING_UNKNOWN);
                    final int networkTransmitIntervalSteps = data.getIntExtra(NetworkTransmitSettingsActivity.TRANSMIT_INTERVAL_STEPS, NETWORK_TRANSMIT_SETTING_UNKNOWN);
                    setNetworkTransmit(networkTransmitCount, networkTransmitIntervalSteps);
                }
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
            ConfigNetworkTransmitSet message = new ConfigNetworkTransmitSet(node, networkTransmitCount, networkTransmitIntervalSteps, 0);
            mViewModel.getMeshManagerApi().sendMeshConfigurationMessage(message);
            showProgressbar();
        } catch (Exception e) {
            Log.e(TAG, "Error while constructing ConfigNetworkTransmitSet", e);
        }
    }


    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        if (meshMessage instanceof ConfigNetworkTransmitStatus) {
            ConfigNetworkTransmitStatus status = (ConfigNetworkTransmitStatus) meshMessage;
            mNetworkTransmitCount = status.getNetworkTransmitCount();
            mNetworkTransmitIntervalSteps = status.getNetworkTransmitIntervalSteps();
            updateUi();
        }
        super.updateMeshMessage(meshMessage);
    }


    private void updateUi() {
        if (mNetworkTransmitCount == NETWORK_TRANSMIT_SETTING_UNKNOWN) {
            mNetworkTransmitCountText.setText(getResources().getString(R.string.text_network_transmit_unknown));
        } else {
            mNetworkTransmitCountText.setText(getResources().getString(R.string.text_network_transmit_count, mNetworkTransmitCount + 1));
        }
        if (mNetworkTransmitIntervalSteps == NETWORK_TRANSMIT_SETTING_UNKNOWN) {
            mNetworkTransmitIntervalStepsText.setText(getResources().getString(R.string.text_network_transmit_unknown));
        } else {
            mNetworkTransmitIntervalStepsText.setText(getResources().getString(R.string.text_network_transmit_interval_steps, mNetworkTransmitIntervalSteps + 1));
        }
    }


    @Override
    protected void enableClickableViews() {
        super.enableClickableViews();
        mReadNetworkTransmitStateButton.setEnabled(true);
        // Only enable the Configure button if the Network Transmit State is known
        if (mNetworkTransmitCount != NETWORK_TRANSMIT_SETTING_UNKNOWN && mNetworkTransmitIntervalSteps != NETWORK_TRANSMIT_SETTING_UNKNOWN) {
            mConfigureNetworkTransmitStateButton.setEnabled(true);
        }
    }


    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        mReadNetworkTransmitStateButton.setEnabled(false);
        mConfigureNetworkTransmitStateButton.setEnabled(false);
    }
}
