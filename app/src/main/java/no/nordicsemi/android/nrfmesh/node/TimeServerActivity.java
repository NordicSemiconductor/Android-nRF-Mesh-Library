package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.data.TimeZoneOffset;
import no.nordicsemi.android.mesh.models.TimeServer;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.TimeGet;
import no.nordicsemi.android.mesh.transport.TimeStatus;
import no.nordicsemi.android.mesh.transport.TimeZoneGet;
import no.nordicsemi.android.mesh.transport.TimeZoneStatus;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutTimeServerBinding;

@AndroidEntryPoint
public class TimeServerActivity extends ModelConfigurationActivity {

    private static final String TAG = TimeServerActivity.class.getSimpleName();
    private static final long TAI_EPOCH_MILLIS = 946684800000L; // 2000-01-01T00:00:00 UTC in milliseconds

    private TextView mTaiSecondsText;
    private TextView mSubSecondText;
    private TextView mUncertaintyText;
    private TextView mTimeAuthorityText;
    private TextView mUtcDeltaText;
    private TextView mTimeZoneOffsetText;
    private TextView mCurrentTimeText;
    private TextView mCurrentTimeZoneText;
    private TextView mNewTimeZoneText;
    private TextView mTimeOfChangeText;
    private TextView mDeviceTimeText;
    private Button mActionGetTimeZone;
    
    private Integer mLastReceivedTaiSeconds; // Store last TAI seconds for device time calculation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof TimeServer) {
            final LayoutTimeServerBinding nodeControlsContainer = LayoutTimeServerBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);
            
            mTaiSecondsText = nodeControlsContainer.taiSeconds;
            mSubSecondText = nodeControlsContainer.subSecond;
            mUncertaintyText = nodeControlsContainer.uncertainty;
            mTimeAuthorityText = nodeControlsContainer.timeAuthority;
            mUtcDeltaText = nodeControlsContainer.utcDelta;
            mTimeZoneOffsetText = nodeControlsContainer.timeZoneOffset;
            mCurrentTimeText = nodeControlsContainer.currentTime;
            mCurrentTimeZoneText = nodeControlsContainer.currentTimeZone;
            mNewTimeZoneText = nodeControlsContainer.newTimeZone;
            mTimeOfChangeText = nodeControlsContainer.timeOfChange;
            mDeviceTimeText = nodeControlsContainer.deviceTime;

            mActionRead = nodeControlsContainer.actionRead;
            mActionRead.setOnClickListener(v -> sendTimeGet());

            mActionGetTimeZone = nodeControlsContainer.actionGetTimeZone;
            mActionGetTimeZone.setOnClickListener(v -> sendTimeZoneGet());

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
        if (mActionGetTimeZone != null && !mActionGetTimeZone.isEnabled())
            mActionGetTimeZone.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        if (mActionGetTimeZone != null)
            mActionGetTimeZone.setEnabled(false);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        mSwipe.setOnRefreshListener(this);
        
        if (meshMessage instanceof TimeStatus) {
            final TimeStatus status = (TimeStatus) meshMessage;
            
            final Integer taiSeconds = status.getTaiSeconds();
            final Byte subSecond = status.getSubSecond();
            final Byte uncertainty = status.getUncertainty();
            final Boolean timeAuthority = status.isTimeAuthority();
            final Short utcDelta = status.getUtcDelta();
            final Byte timeZoneOffset = status.getTimeZoneOffset();
            
            if (taiSeconds != null && taiSeconds != 0) {
                mTaiSecondsText.setText(String.valueOf(taiSeconds));
                mLastReceivedTaiSeconds = taiSeconds; // Store for device time calculation
                
                // Convert TAI seconds to human readable time
                // TAI epoch is 2000-01-01T00:00:00 TAI
                long currentTimeMillis = TAI_EPOCH_MILLIS + (taiSeconds * 1000L);
                Date date = new Date(currentTimeMillis);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                // Use phone's local timezone instead of UTC
                sdf.setTimeZone(TimeZone.getDefault());
                mCurrentTimeText.setText(getString(R.string.time_current_display_local, sdf.format(date)));
            } else {
                mTaiSecondsText.setText(R.string.unknown);
                mCurrentTimeText.setText(R.string.time_unknown);
                mLastReceivedTaiSeconds = null;
            }
            
            if (subSecond != null) {
                mSubSecondText.setText(getString(R.string.time_subsecond_display, subSecond & 0xFF));
            } else {
                mSubSecondText.setText(R.string.unknown);
            }
            
            if (uncertainty != null) {
                int uncertaintyMs = (uncertainty & 0xFF) * 10;
                mUncertaintyText.setText(getString(R.string.time_uncertainty_display, uncertaintyMs));
            } else {
                mUncertaintyText.setText(R.string.unknown);
            }
            
            if (timeAuthority != null) {
                mTimeAuthorityText.setText(timeAuthority ? getString(R.string.time_authority_yes) : getString(R.string.time_authority_no));
            } else {
                mTimeAuthorityText.setText(R.string.unknown);
            }
            
            if (utcDelta != null) {
                mUtcDeltaText.setText(getString(R.string.time_utc_delta_display, utcDelta));
            } else {
                mUtcDeltaText.setText(R.string.unknown);
            }
            
            if (timeZoneOffset != null) {
                int offsetMinutes = timeZoneOffset * 15;
                mTimeZoneOffsetText.setText(getString(R.string.time_zone_offset_display, offsetMinutes));
            } else {
                mTimeZoneOffsetText.setText(R.string.unknown);
            }
            
        } else if (meshMessage instanceof TimeZoneStatus) {
            final TimeZoneStatus status = (TimeZoneStatus) meshMessage;
            
            mCurrentTimeZoneText.setText(getString(R.string.time_zone_offset_display, 
                (int)(status.getCurrentTimeZoneOffset().getHours() * 60)));
            mNewTimeZoneText.setText(getString(R.string.time_zone_offset_display, 
                (int)(status.getNewTimeZoneOffset().getHours() * 60)));
            
            // Calculate device time based on current timezone if we have TAI seconds
            updateDeviceTime(status.getCurrentTimeZoneOffset());
            
            if (status.getTimeOfChange() != 0) {
                // Convert TAI seconds to human readable time
                long changeTimeMillis = TAI_EPOCH_MILLIS + (status.getTimeOfChange() * 1000L);
                Date date = new Date(changeTimeMillis);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                mTimeOfChangeText.setText(getString(R.string.time_change_display, sdf.format(date)));
            } else {
                mTimeOfChangeText.setText(R.string.time_no_change_scheduled);
            }
        }
        hideProgressBar();
    }

    private void updateDeviceTime(TimeZoneOffset currentTimeZoneOffset) {
        if (mLastReceivedTaiSeconds != null && mLastReceivedTaiSeconds != 0) {
            // Convert TAI seconds to UTC time
            long utcTimeMillis = TAI_EPOCH_MILLIS + (mLastReceivedTaiSeconds * 1000L);
            
            // Apply device timezone offset
            double offsetHours = currentTimeZoneOffset.getHours();
            long deviceTimeMillis = utcTimeMillis + (long)(offsetHours * 60 * 60 * 1000);
            
            Date deviceDate = new Date(deviceTimeMillis);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Display as absolute time
            mDeviceTimeText.setText(sdf.format(deviceDate));
        } else {
            mDeviceTimeText.setText(R.string.time_unknown);
        }
    }

    public void sendTimeGet() {
        if (!checkConnectivity(mContainer)) return;
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            if (model != null) {
                if (!model.getBoundAppKeyIndexes().isEmpty()) {
                    final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                    final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);

                    final int address = element.getElementAddress();
                    Log.v(TAG, "Sending TimeGet message to element's unicast address: " + MeshAddress.formatAddress(address, true));

                    final TimeGet timeGet = new TimeGet(appKey);
                    sendAcknowledgedMessage(address, timeGet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }

    public void sendTimeZoneGet() {
        if (!checkConnectivity(mContainer)) return;
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            if (model != null) {
                if (!model.getBoundAppKeyIndexes().isEmpty()) {
                    final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                    final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);

                    final int address = element.getElementAddress();
                    Log.v(TAG, "Sending TimeZoneGet message to element's unicast address: " + MeshAddress.formatAddress(address, true));

                    final TimeZoneGet timeZoneGet = new TimeZoneGet(appKey);
                    sendAcknowledgedMessage(address, timeZoneGet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }
}