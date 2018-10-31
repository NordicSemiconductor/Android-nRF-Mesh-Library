package no.nordicsemi.android.nrfmeshprovisioner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.models.ConfigurationServerModel;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetworkTransmitSet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetworkTransmitStatus;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

public class ModelConfigurationActivity extends BaseModelConfigurationActivity {

    private static final String TAG = ModelConfigurationActivity.class.getSimpleName();

    TextView mNetworkTransmitCountText;
    TextView mNetworkTransmitIntervalStepsText;

    @Override
    protected final void addControlsUi(final MeshModel model) {
        if (model instanceof ConfigurationServerModel) {
            final CardView cardView = findViewById(R.id.node_controls_card);
            final View nodeControlsContainer = LayoutInflater.from(this)
                    .inflate(R.layout.layout_configuration_server_controls, cardView);

            mNetworkTransmitCountText = nodeControlsContainer.findViewById(R.id.network_transmit_count);
            mNetworkTransmitIntervalStepsText = nodeControlsContainer.findViewById(R.id.network_transmit_interval_steps);

            Button configureButton = nodeControlsContainer.findViewById(R.id.action_network_transmit_configure);
            configureButton.setOnClickListener(v -> {
                final Intent transmitSettings = new Intent(this, NetworkTransmitSettingsActivity.class);
                transmitSettings.putExtra(NetworkTransmitSettingsActivity.TRANSMIT_COUNT, 0);
                transmitSettings.putExtra(NetworkTransmitSettingsActivity.TRANSMIT_INTERVAL_STEPS, 0);
                startActivityForResult(transmitSettings, NetworkTransmitSettingsActivity.SET_NETWORK_TRANSMIT_SETTINGS);  // FIXME: necessary with result?
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case NetworkTransmitSettingsActivity.SET_NETWORK_TRANSMIT_SETTINGS:
                if (resultCode == RESULT_OK) {
                    final int networkTransmitCount = data.getIntExtra(NetworkTransmitSettingsActivity.TRANSMIT_COUNT, 0);
                    final int networkTransmitIntervalSteps = data.getIntExtra(NetworkTransmitSettingsActivity.TRANSMIT_INTERVAL_STEPS, 0);
                    setNetworkTransmit(networkTransmitCount, networkTransmitIntervalSteps);
                }
        }
    }

    private void setNetworkTransmit(final int networkTransmitCount, final int networkTransmitIntervalSteps) {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getMeshNode();
        try {
            Log.e(TAG, "Preparing to send: " + networkTransmitCount + ", " + networkTransmitIntervalSteps);
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
            final int transmitCount = status.getNetworkTransmitCount();
            final int transmitIntervalSteps = status.getNetworkTransmitIntervalSteps();

            Log.e(TAG, "Received " + transmitCount + ", " + transmitIntervalSteps);

            mNetworkTransmitCountText.setText(getResources().getString(
                    R.string.text_network_transmit_count, transmitCount + 1));
            mNetworkTransmitIntervalStepsText.setText(getResources().getString(
                    R.string.text_network_transmit_interval_steps, transmitIntervalSteps + 1));
        }
        super.updateMeshMessage(meshMessage);
    }

}
