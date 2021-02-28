package no.nordicsemi.android.nrfmesh.node;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.models.SensorServer;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.sensorutils.DevicePropertyCharacteristic;
import no.nordicsemi.android.mesh.sensorutils.MarshalledSensorData;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.SensorGet;
import no.nordicsemi.android.mesh.transport.SensorStatus;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutContainerBinding;
import no.nordicsemi.android.nrfmesh.databinding.LayoutSensorsBinding;

@AndroidEntryPoint
public class SensorServerActivity extends ModelConfigurationActivity {

    private LayoutSensorsBinding sensorsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof SensorServer) {
            sensorsBinding = LayoutSensorsBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);

            sensorsBinding.actionGet.setOnClickListener(v -> sendSensorGet());

            mViewModel.getSelectedModel().observe(this, meshModel -> {
                if (meshModel != null) {
                    updateAppStatusUi(meshModel);
                    updatePublicationUi(meshModel);
                    updateSubscriptionUi(meshModel);
                }
            });
        }
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        if (meshMessage instanceof SensorStatus) {
            final Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_chart);
            final SensorStatus status = (SensorStatus) meshMessage;
            DeviceProperty deviceProperty;
            DevicePropertyCharacteristic<?> characteristic;
            for (MarshalledSensorData sensorData : status.getMarshalledSensorData()) {
                if(sensorData.getRawValues().length > 0) {
                    deviceProperty = sensorData.getMarshalledPropertyId().getPropertyId();
                    characteristic = DeviceProperty.
                            getCharacteristic(deviceProperty, sensorData.getRawValues(), 0, sensorData.getRawValues().length);
                    final LayoutContainerBinding binding = LayoutContainerBinding.inflate(getLayoutInflater(), sensorsBinding.sensorInfoContainer, false);
                    binding.getRoot().setClickable(false);
                    binding.image.setImageDrawable(drawable);
                    binding.title.setText(DeviceProperty.getPropertyName(deviceProperty));
                    binding.text.setText(characteristic.toString());
                    binding.text.setVisibility(View.VISIBLE);
                    sensorsBinding.sensorInfoContainer.addView(binding.getRoot());
                }
            }
            mViewModel.removeMessage();
            handleStatuses();
        }
        hideProgressBar();
    }

    @Override
    public void onRefresh() {
        if (mViewModel.getSelectedModel().getValue().getBoundAppKeyIndexes().size() > 0) {
            final Integer keyIndex = mViewModel.getSelectedModel().getValue().getBoundAppKeyIndexes().get(0);
            if (keyIndex != null) {
                final ApplicationKey key = mViewModel.getNetworkLiveData().getAppKeys().get(keyIndex);
                mViewModel.getMessageQueue().add(new SensorGet(key, null));
                if (sensorsBinding != null)
                    sensorsBinding.getRoot().removeAllViewsInLayout();
                sendMessage(mViewModel.getMessageQueue().peek());
            }
        }
    }

    @Override
    protected void enableClickableViews() {
        super.enableClickableViews();
        sensorsBinding.actionGet.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        sensorsBinding.actionGet.setEnabled(false);
    }

    private void sendSensorGet() {
        if (mViewModel.getSelectedModel().getValue().getBoundAppKeyIndexes().size() > 0) {
            final Integer keyIndex = mViewModel.getSelectedModel().getValue().getBoundAppKeyIndexes().get(0);
            if (keyIndex != null) {
                if (sensorsBinding != null)
                    sensorsBinding.sensorInfoContainer.removeAllViewsInLayout();
                final ApplicationKey key = mViewModel.getNetworkLiveData().getAppKeys().get(keyIndex);
                sendMessage(new SensorGet(key, null));
            }
        }
    }
}
