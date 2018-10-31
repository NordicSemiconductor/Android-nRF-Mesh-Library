package no.nordicsemi.android.nrfmeshprovisioner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.models.ConfigurationServerModel;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentNetworkTransmit;

public class ModelConfigurationActivity extends BaseModelConfigurationActivity
    implements DialogFragmentNetworkTransmit.DialogFragmentNetworkTransmitListener{

    @Override
    protected final void addControlsUi(final MeshModel model) {
        if (model instanceof ConfigurationServerModel) {
            final CardView cardView = findViewById(R.id.node_controls_card);
            final View nodeControlsContainer = LayoutInflater.from(this)
                    .inflate(R.layout.layout_configuration_server_controls, cardView);
            // ButterKnife.bind(this, nodeControlsContainer);

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
    public void onNetworkTransmitSet(int transmitCount, int transmitIntervalSteps) {
        Toast.makeText(this, "count: " + transmitCount + ", steps: " + transmitIntervalSteps, Toast.LENGTH_SHORT).show();
    }
}
