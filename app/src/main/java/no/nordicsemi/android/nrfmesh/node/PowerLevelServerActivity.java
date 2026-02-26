package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import java.util.Random;

import androidx.annotation.NonNull;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.models.GenericPowerLevelServer;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.GenericPowerLevelGet;
import no.nordicsemi.android.mesh.transport.GenericPowerLevelSet;
import no.nordicsemi.android.mesh.transport.GenericPowerLevelStatus;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutPowerLevelServerBinding;

@AndroidEntryPoint
public class PowerLevelServerActivity extends ModelConfigurationActivity {

    private static final String TAG = PowerLevelServerActivity.class.getSimpleName();

    private TextView powerLevel;
    private TextView time;
    private TextView remainingTime;
    private Slider mTransitionTimeSlider;
    private Slider mDelaySlider;
    private Slider mPowerLevelSlider;

    private int mTransitionStepResolution;
    private int mTransitionSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof GenericPowerLevelServer) {
            final LayoutPowerLevelServerBinding nodeControlsContainer = LayoutPowerLevelServerBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);
            time = nodeControlsContainer.transitionTime;
            remainingTime = nodeControlsContainer.transitionState;
            mTransitionTimeSlider = nodeControlsContainer.transitionSlider;
            mTransitionTimeSlider.setValueFrom(0);
            mTransitionTimeSlider.setValueTo(230);
            mTransitionTimeSlider.setValue(0);
            mTransitionTimeSlider.setStepSize(1);

            mDelaySlider = nodeControlsContainer.delaySlider;
            mDelaySlider.setValueFrom(0);
            mDelaySlider.setValueTo(255);
            mDelaySlider.setValue(0);
            mDelaySlider.setStepSize(1);
            final TextView delayTime = nodeControlsContainer.delayTime;

            powerLevel = nodeControlsContainer.powerLevel;
            mPowerLevelSlider = nodeControlsContainer.powerLevelSlider;
            mPowerLevelSlider.setValueFrom(0);
            mPowerLevelSlider.setValueTo(100);
            mPowerLevelSlider.setValue(0);
            mPowerLevelSlider.setStepSize(1);

            mActionRead = nodeControlsContainer.actionRead;
            mActionRead.setOnClickListener(v -> sendGenericPowerLevelGet());
            mTransitionTimeSlider.addOnChangeListener(new Slider.OnChangeListener() {
                int lastValue = 0;
                double res = 0.0;

                @Override
                public void onValueChange(@NonNull final Slider slider, final float value, final boolean fromUser) {
                    final int progress = (int) value;
                    if (progress >= 0 && progress <= 62) {
                        lastValue = progress;
                        mTransitionStepResolution = 0;
                        mTransitionSteps = progress;
                        res = progress / 10.0;
                        time.setText(getString(R.string.transition_time_interval, String.valueOf(res), "s"));
                    } else if (progress >= 63 && progress <= 118) {
                        if (progress > lastValue) {
                            mTransitionSteps = progress - 56;
                            lastValue = progress;
                        } else if (progress < lastValue) {
                            mTransitionSteps = -(56 - progress);
                        }
                        mTransitionStepResolution = 1;
                        time.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionSteps), "s"));

                    } else if (progress >= 119 && progress <= 174) {
                        if (progress > lastValue) {
                            mTransitionSteps = progress - 112;
                            lastValue = progress;
                        } else if (progress < lastValue) {
                            mTransitionSteps = -(112 - progress);
                        }
                        mTransitionStepResolution = 2;
                        time.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionSteps * 10), "s"));
                    } else if (progress >= 175 && progress <= 230) {
                        if (progress >= lastValue) {
                            mTransitionSteps = progress - 168;
                            lastValue = progress;
                        } else {
                            mTransitionSteps = -(168 - progress);
                        }
                        mTransitionStepResolution = 3;
                        time.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionSteps * 10), "min"));
                    }
                }
            });

            mDelaySlider.addOnChangeListener((slider, value, fromUser) ->
                    delayTime.setText(getString(R.string.transition_time_interval, String.valueOf((int) value * MeshParserUtils.GENERIC_ON_OFF_5_MS), "ms")));

            mPowerLevelSlider.addOnChangeListener((slider, value, fromUser) ->
                    powerLevel.setText(String.valueOf((int) value) + "%"));

            mPowerLevelSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull final Slider slider) {

                }

                @Override
                public void onStopTrackingTouch(@NonNull final Slider slider) {
                    final int powerLevelPercentage = (int) slider.getValue();
                    final int powerLevelValue = percentageToRawValue(powerLevelPercentage);
                    final int delay = (int) mDelaySlider.getValue();
                    sendGenericPowerLevelSet(powerLevelValue, delay);
                }
            });

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
        mTransitionTimeSlider.setEnabled(true);
        mDelaySlider.setEnabled(true);
        mPowerLevelSlider.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        mTransitionTimeSlider.setEnabled(false);
        mDelaySlider.setEnabled(false);
        mPowerLevelSlider.setEnabled(false);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        if (meshMessage instanceof GenericPowerLevelStatus) {
            final GenericPowerLevelStatus status = (GenericPowerLevelStatus) meshMessage;
            hideProgressBar();
            final int presentPowerLevel = status.getPresentPowerLevel();
            final Integer targetPowerLevel = status.getTargetPowerLevel();
            final int steps = status.getTransitionSteps();
            final int resolution = status.getTransitionResolution();
            if (targetPowerLevel == null) {
                final int presentPercentage = rawValueToPercentage(presentPowerLevel);
                powerLevel.setText(presentPercentage + "%");
                remainingTime.setVisibility(View.GONE);
                mPowerLevelSlider.setValue(presentPercentage);
            } else {
                final int targetPercentage = rawValueToPercentage(targetPowerLevel);
                powerLevel.setText(targetPercentage + "%");
                remainingTime.setText(getString(R.string.remaining_time, MeshParserUtils.getRemainingTransitionTime(resolution, steps)));
                remainingTime.setVisibility(View.VISIBLE);
                mPowerLevelSlider.setValue(targetPercentage);
            }
        }
        hideProgressBar();
    }

    /**
     * Send generic power level get to mesh node
     */
    public void sendGenericPowerLevelGet() {
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
                    final GenericPowerLevelGet genericPowerLevelGet = new GenericPowerLevelGet(appKey);
                    sendAcknowledgedMessage(address, genericPowerLevelGet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }

    /**
     * Send generic power level set to mesh node
     *
     * @param powerLevel power level value
     * @param delay message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     */
    public void sendGenericPowerLevelSet(final int powerLevel, final Integer delay) {
        if (!checkConnectivity(mContainer)) return;
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        if (node != null) {
            final Element element = mViewModel.getSelectedElement().getValue();
            if (element != null) {
                final MeshModel model = mViewModel.getSelectedModel().getValue();
                if (model != null) {
                    if (!model.getBoundAppKeyIndexes().isEmpty()) {
                        final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                        final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);
                        final int address = element.getElementAddress();
                        final GenericPowerLevelSet genericPowerLevelSet = new GenericPowerLevelSet(appKey, mTransitionSteps, mTransitionStepResolution, delay, powerLevel,
                                new Random().nextInt());
                        sendAcknowledgedMessage(address, genericPowerLevelSet);
                    } else {
                        mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                    }
                }
            }
        }
    }

    /**
     * Convert raw power level value (0-65535) to percentage (0-100)
     * According to Mesh Model Spec section 3.1.5.1, Generic Power Actual should be represented as percentage
     *
     * @param rawValue raw power level value (0-65535)
     * @return percentage value (0-100)
     */
    private int rawValueToPercentage(final int rawValue) {
        return Math.round((rawValue * 100.0f) / 65535.0f);
    }

    /**
     * Convert percentage (0-100) to raw power level value (0-65535)
     * According to Mesh Model Spec section 3.1.5.1, Generic Power Actual should be represented as percentage
     *
     * @param percentage percentage value (0-100)
     * @return raw power level value (0-65535)
     */
    private int percentageToRawValue(final int percentage) {
        return Math.round((percentage * 65535.0f) / 100.0f);
    }
}