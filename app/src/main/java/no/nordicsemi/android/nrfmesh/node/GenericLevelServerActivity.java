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
import no.nordicsemi.android.mesh.models.GenericLevelServerModel;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.GenericLevelGet;
import no.nordicsemi.android.mesh.transport.GenericLevelSet;
import no.nordicsemi.android.mesh.transport.GenericLevelStatus;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutGenericLevelBinding;

@AndroidEntryPoint
public class GenericLevelServerActivity extends ModelConfigurationActivity {

    private static final String TAG = GenericOnOffServerActivity.class.getSimpleName();

    private TextView level;
    private TextView time;
    private TextView remainingTime;
    private Slider mTransitionTimeSlider;
    private Slider mDelaySlider;
    private Slider mLevelSlider;

    private int mTransitionStepResolution;
    private int mTransitionSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof GenericLevelServerModel) {
            final LayoutGenericLevelBinding nodeControlsContainer = LayoutGenericLevelBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);
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

            level = nodeControlsContainer.level;
            mLevelSlider = nodeControlsContainer.levelSeekBar;
            mLevelSlider.setValueTo(0);
            mLevelSlider.setValueTo(100);
            mLevelSlider.setValue(0);
            mLevelSlider.setStepSize(1);

            mActionRead = nodeControlsContainer.actionRead;
            mActionRead.setOnClickListener(v -> sendGenericLevelGet());
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

            mLevelSlider.addOnChangeListener((slider, value, fromUser) ->
                    level.setText(getString(R.string.generic_level_percent, (int) value)));

            mLevelSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull final Slider slider) {

                }

                @Override
                public void onStopTrackingTouch(@NonNull final Slider slider) {
                    final int level = (int) slider.getValue();
                    final int delay = (int) mDelaySlider.getValue();
                    final int genericLevel = ((level * 65535) / 100) - 32768;
                    sendGenericLevel(genericLevel, delay);
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
        mLevelSlider.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        mTransitionTimeSlider.setEnabled(false);
        mDelaySlider.setEnabled(false);
        mLevelSlider.setEnabled(false);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        if (meshMessage instanceof GenericLevelStatus) {
            final GenericLevelStatus status = (GenericLevelStatus) meshMessage;
            hideProgressBar();
            final int presentLevel = status.getPresentLevel();
            final Integer targetLevel = status.getTargetLevel();
            final int steps = status.getTransitionSteps();
            final int resolution = status.getTransitionResolution();
            final int levelPercent;
            if (targetLevel == null) {
                levelPercent = ((presentLevel + 32768) * 100) / 65535;
                level.setText(getString(R.string.generic_level_percent, levelPercent));
                remainingTime.setVisibility(View.GONE);
            } else {
                levelPercent = ((targetLevel + 32768) * 100) / 65535;
                level.setText(getString(R.string.generic_level_percent, levelPercent));
                remainingTime.setText(getString(R.string.remaining_time, MeshParserUtils.getRemainingTransitionTime(resolution, steps)));
                remainingTime.setVisibility(View.VISIBLE);
            }
            mLevelSlider.setValue(levelPercent);
        }
        hideProgressBar();
    }

    /**
     * Send generic on off get to mesh node
     */
    public void sendGenericLevelGet() {
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
                    final GenericLevelGet genericLevelGet = new GenericLevelGet(appKey);
                    sendMessage(address, genericLevelGet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }

    /**
     * Send generic level set to mesh node
     *
     * @param level level
     * @param delay message execution delay in 5ms steps. After this delay milliseconds the model will execute the required behaviour.
     */
    public void sendGenericLevel(final int level, final Integer delay) {
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
                        final GenericLevelSet genericLevelSet = new GenericLevelSet(appKey, mTransitionSteps, mTransitionStepResolution, delay, level,
                                new Random().nextInt());
                        sendMessage(address, genericLevelSet);
                    } else {
                        mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                    }
                }
            }
        }
    }
}
