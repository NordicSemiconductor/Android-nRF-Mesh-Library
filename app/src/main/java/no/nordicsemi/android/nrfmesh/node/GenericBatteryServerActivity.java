package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.models.GenericBatteryServer;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.GenericBatteryGet;
import no.nordicsemi.android.mesh.transport.GenericBatteryStatus;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutGenericBatteryBinding;

@AndroidEntryPoint
public class GenericBatteryServerActivity extends ModelConfigurationActivity {

    private static final String TAG = GenericBatteryServerActivity.class.getSimpleName();

    private TextView mBatteryLevelText;
    private TextView mTimeToDischargeText;
    private TextView mTimeToChargeText;
    private TextView mBatteryPresenceText;
    private TextView mBatteryIndicatorText;
    private TextView mChargingStateText;
    private TextView mServiceabilityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof GenericBatteryServer) {
            final LayoutGenericBatteryBinding nodeControlsContainer = LayoutGenericBatteryBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);
            
            mBatteryLevelText = nodeControlsContainer.batteryLevel;
            mTimeToDischargeText = nodeControlsContainer.timeToDischarge;
            mTimeToChargeText = nodeControlsContainer.timeToCharge;
            mBatteryPresenceText = nodeControlsContainer.batteryPresence;
            mBatteryIndicatorText = nodeControlsContainer.batteryIndicator;
            mChargingStateText = nodeControlsContainer.chargingState;
            mServiceabilityText = nodeControlsContainer.serviceability;

            mActionRead = nodeControlsContainer.actionRead;
            mActionRead.setOnClickListener(v -> sendGenericBatteryGet());

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
    protected void enableClickableViews() {
        super.enableClickableViews();
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        mSwipe.setOnRefreshListener(this);
        if (meshMessage instanceof GenericBatteryStatus) {
            final GenericBatteryStatus status = (GenericBatteryStatus) meshMessage;
            
            final int batteryLevel = status.getBatteryLevel();
            final int timeToDischarge = status.getTimeToDischarge();
            final int timeToCharge = status.getTimeToCharge();
            final GenericBatteryStatus.BatteryPresence presence = status.getBatteryPresence();
            final GenericBatteryStatus.BatteryIndicator indicator = status.getBatteryIndicator();
            final GenericBatteryStatus.BatteryChargingState chargingState = status.getBatteryChargingState();
            final GenericBatteryStatus.BatteryServiceability serviceability = status.batteryServiceability();
            
            if (batteryLevel == 0xFF) {
                mBatteryLevelText.setText(R.string.unknown);
            } else {
                mBatteryLevelText.setText(getString(R.string.battery_level_percent, batteryLevel));
            }
            
            if (timeToDischarge == 0xFFFFFF) {
                mTimeToDischargeText.setText(R.string.unknown);
            } else {
                mTimeToDischargeText.setText(getString(R.string.battery_time_minutes, timeToDischarge));
            }
            
            if (timeToCharge == 0xFFFFFF) {
                mTimeToChargeText.setText(R.string.unknown);
            } else {
                mTimeToChargeText.setText(getString(R.string.battery_time_minutes, timeToCharge));
            }
            
            mBatteryPresenceText.setText(getBatteryPresenceString(presence));
            mBatteryIndicatorText.setText(getBatteryIndicatorString(indicator));
            mChargingStateText.setText(getChargingStateString(chargingState));
            mServiceabilityText.setText(getServiceabilityString(serviceability));
        }
        hideProgressBar();
    }

    private String getBatteryPresenceString(GenericBatteryStatus.BatteryPresence presence) {
        switch (presence) {
            case NOT_PRESENT:
                return getString(R.string.battery_not_present);
            case REMOVABLE:
                return getString(R.string.battery_removable);
            case NOT_REMOVABLE:
                return getString(R.string.battery_not_removable);
            case UNKNOWN:
            default:
                return getString(R.string.unknown);
        }
    }

    private String getBatteryIndicatorString(GenericBatteryStatus.BatteryIndicator indicator) {
        switch (indicator) {
            case CRITICALLY_LOW:
                return getString(R.string.battery_critically_low);
            case LOW:
                return getString(R.string.battery_low);
            case GOOD:
                return getString(R.string.battery_good);
            case UNKNOWN:
            default:
                return getString(R.string.unknown);
        }
    }

    private String getChargingStateString(GenericBatteryStatus.BatteryChargingState chargingState) {
        switch (chargingState) {
            case NOT_CHARGEABLE:
                return getString(R.string.battery_not_chargeable);
            case NOT_CHARGING:
                return getString(R.string.battery_not_charging);
            case CHARGING:
                return getString(R.string.battery_charging);
            case UNKNOWN:
            default:
                return getString(R.string.unknown);
        }
    }

    private String getServiceabilityString(GenericBatteryStatus.BatteryServiceability serviceability) {
        switch (serviceability) {
            case RESERVED:
                return getString(R.string.battery_serviceability_reserved);
            case SERVICE_NOT_REQUIRED:
                return getString(R.string.battery_service_not_required);
            case SERVICE_REQUIRED:
                return getString(R.string.battery_service_required);
            case UNKNOWN:
            default:
                return getString(R.string.unknown);
        }
    }

    public void sendGenericBatteryGet() {
        if (!checkConnectivity(mContainer)) return;
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            if (model != null) {
                if (!model.getBoundAppKeyIndexes().isEmpty()) {
                    final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                    final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);

                    final int address = element.getElementAddress();
                    Log.v(TAG, "Sending message to element's unicast address: " + MeshAddress.formatAddress(address, true));

                    final GenericBatteryGet genericBatteryGet = new GenericBatteryGet(appKey);
                    sendAcknowledgedMessage(address, genericBatteryGet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }
}