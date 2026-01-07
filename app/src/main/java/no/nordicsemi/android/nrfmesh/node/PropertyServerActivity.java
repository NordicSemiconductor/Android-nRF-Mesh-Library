package no.nordicsemi.android.nrfmesh.node;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.sql.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.logger.MeshLogger;
import no.nordicsemi.android.mesh.models.GenericManufacturerPropertyServer;
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty;
import no.nordicsemi.android.mesh.sensorutils.DevicePropertyCharacteristic;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.GenericManufacturerPropertiesGet;
import no.nordicsemi.android.mesh.transport.GenericManufacturerPropertiesStatus;
import no.nordicsemi.android.mesh.transport.GenericManufacturerPropertyGet;
import no.nordicsemi.android.mesh.transport.GenericPropertyStatus;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutContainerBinding;
import no.nordicsemi.android.nrfmesh.databinding.LayoutPropertiesBinding;

@AndroidEntryPoint
public class PropertyServerActivity extends ModelConfigurationActivity {

    private static final String TAG = PropertyServerActivity.class.getSimpleName();

    private LayoutPropertiesBinding propertiesBinding;

    //private final Map<Short, TextView> textViewMap = new HashMap<>();
    private final Queue<Short> propertyIdsToGet = new ArrayDeque<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof GenericManufacturerPropertyServer) {
            propertiesBinding = LayoutPropertiesBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);

            propertiesBinding.actionGet.setOnClickListener(v -> sendPropertiesGet());

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

        if (meshMessage instanceof GenericPropertyStatus) {
            GenericPropertyStatus status = (GenericPropertyStatus)meshMessage;
            DeviceProperty deviceProperty = DeviceProperty.from(status.getPropertyId());

            byte[] rawValue = status.getPropertyValue();
            DevicePropertyCharacteristic<?> characteristic = DeviceProperty.getCharacteristic(deviceProperty, rawValue, 0, rawValue.length);

            MeshLogger.info(TAG, "Got Property, ID: " + deviceProperty.getPropertyId() + ", Value: " + characteristic);

            TextView view = propertiesBinding.propertiesInfoContainer.findViewWithTag(deviceProperty.getPropertyId());
            if (view != null) {
                view.setText(characteristic.toString());
            } else {
                MeshLogger.warn(TAG, "Could not find TextView with tag: " + deviceProperty.getPropertyId());
            }
        }

        if (meshMessage instanceof GenericManufacturerPropertiesStatus) {
            clearViews();
            final Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_chart);
            final GenericManufacturerPropertiesStatus status = (GenericManufacturerPropertiesStatus) meshMessage;

            if (status.getPropertyIds().isEmpty()) {
                // Show info "No properties found"
                final LayoutContainerBinding binding = LayoutContainerBinding.inflate(getLayoutInflater(), propertiesBinding.propertiesInfoContainer, false);
                binding.title.setText(R.string.properties_non_found);
                propertiesBinding.propertiesInfoContainer.addView(binding.getRoot());
            } else {
                // Add all found properties to the layout
                for (Short id : status.getPropertyIds()) {
                    try {
                        DeviceProperty deviceProperty = DeviceProperty.from(id);

                        final LayoutContainerBinding binding = LayoutContainerBinding.inflate(getLayoutInflater(), propertiesBinding.propertiesInfoContainer, false);
                        binding.image.setImageDrawable(drawable);
                        binding.title.setText(DeviceProperty.getPropertyName(deviceProperty));
                        binding.title.setEllipsize(TextUtils.TruncateAt.END);
                        binding.title.setMaxLines(1);

                        binding.text.setTag(id); // Setting the tag to the propertyId to later update its value
                        binding.text.setText(R.string.properties_loading);
                        binding.text.setVisibility(View.VISIBLE);

                        propertiesBinding.propertiesInfoContainer.addView(binding.getRoot());
                        this.propertyIdsToGet.add(id);

                    } catch (Exception ex) {
                        Log.e(TAG, "Error while parsing sensor data: " + ex.toString());
                    }
                }
            }

            mViewModel.removeMessage();
            handleStatuses();
        }

        Short id = this.propertyIdsToGet.poll();
        if (id != null) {
            sendPropertyGet(DeviceProperty.from(id));
        } else {
            hideProgressBar();
        }

    }

    @Override
    public void onRefresh() {
        if (!mViewModel.getSelectedModel().getValue().getBoundAppKeyIndexes().isEmpty()) {
            final Integer keyIndex = mViewModel.getSelectedModel().getValue().getBoundAppKeyIndexes().get(0);
            if (keyIndex != null) {
                final ApplicationKey key = mViewModel.getNetworkLiveData().getAppKeys().get(keyIndex);
                mViewModel.getMessageQueue().add(new GenericManufacturerPropertiesGet(key));
                clearViews();
                sendMessage(mViewModel.getMessageQueue().peek());
            }
        }
    }

    @Override
    protected void enableClickableViews() {
        super.enableClickableViews();
        propertiesBinding.actionGet.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        propertiesBinding.actionGet.setEnabled(false);
    }

    private void sendPropertiesGet() {
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            if (!mViewModel.getSelectedModel().getValue().getBoundAppKeyIndexes().isEmpty()) {
                final Integer keyIndex = mViewModel.getSelectedModel().getValue().getBoundAppKeyIndexes().get(0);
                if (keyIndex != null) {
                    clearViews();
                    final ApplicationKey key = mViewModel.getNetworkLiveData().getAppKeys().get(keyIndex);
                    sendAcknowledgedMessage(element.getElementAddress(), new GenericManufacturerPropertiesGet(key));
                }
            }
        }
    }

    private void sendPropertyGet(DeviceProperty property) {
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            if (!mViewModel.getSelectedModel().getValue().getBoundAppKeyIndexes().isEmpty()) {
                final Integer keyIndex = mViewModel.getSelectedModel().getValue().getBoundAppKeyIndexes().get(0);
                if (keyIndex != null) {
                    final ApplicationKey key = mViewModel.getNetworkLiveData().getAppKeys().get(keyIndex);
                    sendAcknowledgedMessage(element.getElementAddress(), new GenericManufacturerPropertyGet(key, property));
                }
            }
        }
    }

    private void clearViews() {
        if (propertiesBinding != null)
            propertiesBinding.propertiesInfoContainer.removeAllViewsInLayout();
    }
}
