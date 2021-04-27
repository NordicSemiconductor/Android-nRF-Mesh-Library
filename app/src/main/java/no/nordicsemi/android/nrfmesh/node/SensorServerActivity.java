package no.nordicsemi.android.nrfmesh.node;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

    private static final String TAG = SensorServerActivity.class.getSimpleName();

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
            clearViews();
            final Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_chart);
            final SensorStatus status = (SensorStatus) meshMessage;
            DeviceProperty deviceProperty;
            DevicePropertyCharacteristic<?> characteristic;
            for (MarshalledSensorData sensorData : status.getMarshalledSensorData()) {
                try {
                    deviceProperty = sensorData.getMarshalledPropertyId().getPropertyId();
                    characteristic = DeviceProperty.
                            getCharacteristic(deviceProperty, sensorData.getRawValues(), 0, sensorData.getRawValues().length);
                    final LayoutContainerBinding binding = LayoutContainerBinding.inflate(getLayoutInflater(), sensorsBinding.sensorInfoContainer, false);
                    binding.getRoot().setClickable(false);
                    binding.image.setImageDrawable(drawable);
                    binding.title.setText(DeviceProperty.getPropertyName(deviceProperty));
                    binding.title.setEllipsize(TextUtils.TruncateAt.END);
                    binding.title.setMaxLines(1);
                    binding.text.setText(characteristic.toString());
                    binding.text.setVisibility(View.VISIBLE);
                    sensorsBinding.sensorInfoContainer.addView(binding.getRoot());
                } catch (Exception ex) {
                    Log.e(TAG, "Error while parsing sensor data: " + ex.toString());
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
                clearViews();
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
                clearViews();
                final ApplicationKey key = mViewModel.getNetworkLiveData().getAppKeys().get(keyIndex);
                sendMessage(new SensorGet(key, null));
            }
        }
    }

    private void clearViews() {
        if (sensorsBinding != null)
            sensorsBinding.sensorInfoContainer.removeAllViewsInLayout();
    }
}
