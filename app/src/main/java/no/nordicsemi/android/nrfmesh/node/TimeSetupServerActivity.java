package no.nordicsemi.android.nrfmesh.node;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
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
import no.nordicsemi.android.mesh.MeshTAITime;
import no.nordicsemi.android.mesh.data.TimeZoneOffset;
import no.nordicsemi.android.mesh.models.TimeSetupServer;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.TimeGet;
import no.nordicsemi.android.mesh.transport.TimeSet;
import no.nordicsemi.android.mesh.transport.TimeStatus;
import no.nordicsemi.android.mesh.transport.TimeZoneGet;
import no.nordicsemi.android.mesh.transport.TimeZoneSet;
import no.nordicsemi.android.mesh.transport.TimeZoneStatus;
import no.nordicsemi.android.mesh.transport.TimeRoleGet;
import no.nordicsemi.android.mesh.transport.TimeRoleSet;
import no.nordicsemi.android.mesh.transport.TimeRoleStatus;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutTimeSetupServerBinding;

@AndroidEntryPoint
public class TimeSetupServerActivity extends ModelConfigurationActivity {

    private static final String TAG = TimeSetupServerActivity.class.getSimpleName();
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
    private TextView mTimeRoleText;
    private Button mActionGetTimeZone;
    private Button mActionSetTime;
    private Button mActionSetTimeZone;
    private Button mActionGetTimeRole;
    private Button mActionSetTimeRole;

    private Calendar selectedDateTime = Calendar.getInstance();
    private Integer mLastReceivedTaiSeconds; // Store last TAI seconds for device time calculation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof TimeSetupServer) {
            final LayoutTimeSetupServerBinding nodeControlsContainer = LayoutTimeSetupServerBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);
            
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
            mTimeRoleText = nodeControlsContainer.timeRole;

            mActionRead = nodeControlsContainer.actionRead;
            mActionRead.setOnClickListener(v -> sendTimeGet());

            mActionGetTimeZone = nodeControlsContainer.actionGetTimeZone;
            mActionGetTimeZone.setOnClickListener(v -> sendTimeZoneGet());

            mActionSetTime = nodeControlsContainer.actionSetTime;
            mActionSetTime.setOnClickListener(v -> showTimeSetDialog());

            mActionSetTimeZone = nodeControlsContainer.actionSetTimeZone;
            mActionSetTimeZone.setOnClickListener(v -> showTimeZoneSetDialog());

            mActionGetTimeRole = nodeControlsContainer.actionGetTimeRole;
            mActionGetTimeRole.setOnClickListener(v -> sendTimeRoleGet());

            mActionSetTimeRole = nodeControlsContainer.actionSetTimeRole;
            mActionSetTimeRole.setOnClickListener(v -> showTimeRoleSetDialog());

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
        if (mActionSetTime != null && !mActionSetTime.isEnabled())
            mActionSetTime.setEnabled(true);
        if (mActionSetTimeZone != null && !mActionSetTimeZone.isEnabled())
            mActionSetTimeZone.setEnabled(true);
        if (mActionGetTimeRole != null && !mActionGetTimeRole.isEnabled())
            mActionGetTimeRole.setEnabled(true);
        if (mActionSetTimeRole != null && !mActionSetTimeRole.isEnabled())
            mActionSetTimeRole.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        if (mActionGetTimeZone != null)
            mActionGetTimeZone.setEnabled(false);
        if (mActionSetTime != null)
            mActionSetTime.setEnabled(false);
        if (mActionSetTimeZone != null)
            mActionSetTimeZone.setEnabled(false);
        if (mActionGetTimeRole != null)
            mActionGetTimeRole.setEnabled(false);
        if (mActionSetTimeRole != null)
            mActionSetTimeRole.setEnabled(false);
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
        } else if (meshMessage instanceof TimeRoleStatus) {
            final TimeRoleStatus status = (TimeRoleStatus) meshMessage;
            
            final Byte timeRole = status.getTimeRole();
            if (timeRole != null) {
                String roleDescription = status.getTimeRoleDescription();
                if (roleDescription != null) {
                    mTimeRoleText.setText(roleDescription);
                } else {
                    mTimeRoleText.setText(R.string.time_role_unknown);
                }
            } else {
                mTimeRoleText.setText(R.string.unknown);
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

    private void showTimeSetDialog() {
        Calendar now = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedDateTime.set(Calendar.MINUTE, minute);
                                selectedDateTime.set(Calendar.SECOND, 0);
                                selectedDateTime.set(Calendar.MILLISECOND, 0);
                                sendTimeSet();
                            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                    timePickerDialog.show();
                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        
        datePickerDialog.show();
    }

    private void showTimeZoneSetDialog() {
        // Common timezone offsets in hours
        final String[] timezoneNames = {
            "UTC-12:00 (Baker Island)",
            "UTC-11:00 (American Samoa)",
            "UTC-10:00 (Hawaii)",
            "UTC-09:00 (Alaska)",
            "UTC-08:00 (Pacific Time)",
            "UTC-07:00 (Mountain Time)",
            "UTC-06:00 (Central Time)",
            "UTC-05:00 (Eastern Time)",
            "UTC-04:00 (Atlantic Time)",
            "UTC-03:00 (Brazil)",
            "UTC-02:00 (Mid-Atlantic)",
            "UTC-01:00 (Azores)",
            "UTC+00:00 (Greenwich Mean Time)",
            "UTC+01:00 (Central European Time)",
            "UTC+02:00 (Eastern European Time)",
            "UTC+03:00 (Moscow Time)",
            "UTC+04:00 (Gulf Time)",
            "UTC+05:00 (Pakistan Time)",
            "UTC+05:30 (India Standard Time)",
            "UTC+06:00 (Bangladesh Time)",
            "UTC+07:00 (Thailand Time)",
            "UTC+08:00 (China Standard Time)",
            "UTC+09:00 (Japan Standard Time)",
            "UTC+09:30 (Australian Central Time)",
            "UTC+10:00 (Australian Eastern Time)",
            "UTC+11:00 (Solomon Islands Time)",
            "UTC+12:00 (New Zealand Time)"
        };
        
        final double[] timezoneOffsets = {
            -12.0, -11.0, -10.0, -9.0, -8.0, -7.0, -6.0, -5.0, -4.0, -3.0, -2.0, -1.0,
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 5.5, 6.0, 7.0, 8.0, 9.0, 9.5, 10.0, 11.0, 12.0
        };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Timezone")
                .setItems(timezoneNames, (dialog, which) -> {
                    double selectedOffset = timezoneOffsets[which];
                    
                    // Ask when to apply the timezone change
                    showTimeOfChangeDialog(selectedOffset, timezoneNames[which]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showTimeOfChangeDialog(double timezoneOffsetHours, String timezoneName) {
        final String[] changeOptions = {
            "Apply immediately",
            "Apply in 1 hour",
            "Apply in 24 hours",
            "Apply in 1 week"
        };
        
        final int[] changeDelayHours = { 0, 1, 24, 168 }; // 168 = 24*7
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("When to apply " + timezoneName + "?")
                .setItems(changeOptions, (dialog, which) -> {
                    Calendar changeTime = Calendar.getInstance();
                    changeTime.add(Calendar.HOUR, changeDelayHours[which]);
                    
                    // Convert to TAI seconds since epoch 2000-01-01T00:00:00 TAI
                    long timeOfChangeSeconds = (changeTime.getTimeInMillis() - TAI_EPOCH_MILLIS) / 1000;
                    
                    // Create TimeZoneOffset from hours
                    TimeZoneOffset newOffset = TimeZoneOffset.encode(timezoneOffsetHours);
                    
                    sendTimeZoneSet(newOffset, timeOfChangeSeconds);
                    
                    // Show confirmation
                    String message;
                    if (changeDelayHours[which] == 0) {
                        message = "Timezone changed immediately to " + timezoneName;
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        message = "Timezone change scheduled for " + sdf.format(changeTime.getTime()) + 
                                 "\nNew timezone: " + timezoneName;
                    }
                    
                    mViewModel.displaySnackBar(this, mContainer, message, Snackbar.LENGTH_LONG);
                })
                .setNegativeButton("Cancel", null)
                .show();
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

    private void sendTimeSet() {
        if (!checkConnectivity(mContainer)) return;
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            if (model != null) {
                if (!model.getBoundAppKeyIndexes().isEmpty()) {
                    final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                    final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);

                    final int address = element.getElementAddress();
                    Log.v(TAG, "Sending TimeSet message to element's unicast address: " + MeshAddress.formatAddress(address, true));

                    // Convert selected time to TAI seconds since epoch 2000-01-01T00:00:00 TAI
                    long totalMillis = selectedDateTime.getTimeInMillis() - TAI_EPOCH_MILLIS;
                    int taiSeconds = (int) (totalMillis / 1000);
                    
                    // Calculate sub-second value: fractional part of seconds in units of 1/256th seconds
                    int remainingMillis = (int) (totalMillis % 1000);
                    byte subSecond = (byte) ((remainingMillis * 256) / 1000);

                    // Create MeshTAITime with proper sub-second precision
                    MeshTAITime taiTime = new MeshTAITime(
                        taiSeconds,          // TAI seconds
                        subSecond,          // Sub-second (fractional part in 1/256th seconds)
                        (byte) 10,          // Uncertainty (100ms)
                        true,               // Time authority
                        (short) 37,         // UTC Delta (typical current value)
                        (byte) 0            // Time zone offset (UTC)
                    );

                    final TimeSet timeSet = new TimeSet(appKey, taiTime);
                    sendAcknowledgedMessage(address, timeSet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }

    private void sendTimeZoneSet(TimeZoneOffset newOffset, long timeOfChange) {
        if (!checkConnectivity(mContainer)) return;
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            if (model != null) {
                if (!model.getBoundAppKeyIndexes().isEmpty()) {
                    final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                    final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);

                    final int address = element.getElementAddress();
                    Log.v(TAG, "Sending TimeZoneSet message to element's unicast address: " + MeshAddress.formatAddress(address, true));

                    final TimeZoneSet timeZoneSet = new TimeZoneSet(appKey, newOffset, timeOfChange);
                    sendAcknowledgedMessage(address, timeZoneSet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }

    public void sendTimeRoleGet() {
        if (!checkConnectivity(mContainer)) return;
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            if (model != null) {
                if (!model.getBoundAppKeyIndexes().isEmpty()) {
                    final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                    final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);

                    final int address = element.getElementAddress();
                    Log.v(TAG, "Sending TimeRoleGet message to element's unicast address: " + MeshAddress.formatAddress(address, true));

                    final TimeRoleGet timeRoleGet = new TimeRoleGet(appKey);
                    sendAcknowledgedMessage(address, timeRoleGet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }

    private void showTimeRoleSetDialog() {
        final String[] roleNames = {
            getString(R.string.time_role_none),
            getString(R.string.time_role_authority),
            getString(R.string.time_role_relay),
            getString(R.string.time_role_client)
        };
        
        final byte[] roleValues = { 0x00, 0x01, 0x02, 0x03 };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Time Role")
                .setItems(roleNames, (dialog, which) -> {
                    byte selectedRole = roleValues[which];
                    sendTimeRoleSet(selectedRole);
                    
                    // Show confirmation
                    String message = "Time role set to: " + roleNames[which];
                    mViewModel.displaySnackBar(this, mContainer, message, Snackbar.LENGTH_LONG);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendTimeRoleSet(byte timeRole) {
        if (!checkConnectivity(mContainer)) return;
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            if (model != null) {
                if (!model.getBoundAppKeyIndexes().isEmpty()) {
                    final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                    final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);

                    final int address = element.getElementAddress();
                    Log.v(TAG, "Sending TimeRoleSet message to element's unicast address: " + MeshAddress.formatAddress(address, true));

                    final TimeRoleSet timeRoleSet = new TimeRoleSet(appKey, timeRole);
                    sendAcknowledgedMessage(address, timeRoleSet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }
}