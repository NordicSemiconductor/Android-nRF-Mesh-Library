package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.data.GenericTransitionTime;
import no.nordicsemi.android.mesh.models.GenericDefaultTransitionTimeServer;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.GenericDefaultTransitionTimeGet;
import no.nordicsemi.android.mesh.transport.GenericDefaultTransitionTimeSet;
import no.nordicsemi.android.mesh.transport.GenericDefaultTransitionTimeStatus;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutGenericDefaultTransitionTimeBinding;

@AndroidEntryPoint
public class GenericDefaultTransitionTimeServerActivity extends ModelConfigurationActivity {

    private static final String TAG = GenericDefaultTransitionTimeServerActivity.class.getSimpleName();

    private TextView mTransitionTimeDisplay;
    private TextView mTransitionTimeValue;
    private Slider mTransitionSlider;
    protected int mTransitionStepResolution;
    protected int mTransitionSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof GenericDefaultTransitionTimeServer) {
            final LayoutGenericDefaultTransitionTimeBinding nodeControlsContainer = LayoutGenericDefaultTransitionTimeBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);

            mActionRead = nodeControlsContainer.actionRead;
            mActionRead.setOnClickListener(v -> sendGenericDefaultTransitionTimeGet());

            mTransitionTimeDisplay = nodeControlsContainer.transitionTimeDisplay;
            mTransitionTimeValue = nodeControlsContainer.transitionTimeValue;
            mTransitionSlider = nodeControlsContainer.transitionSlider;

            // Setup slider
            mTransitionSlider.setValueFrom(0);
            mTransitionSlider.setValueTo(230);
            mTransitionSlider.setValue(0);
            mTransitionSlider.setStepSize(1);

            // Setup slider change listener (based on Generic OnOff Server)
            mTransitionSlider.addOnChangeListener(new Slider.OnChangeListener() {
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
                        mTransitionTimeValue.setText(getString(R.string.transition_time_interval, String.valueOf(res), "s"));
                    } else if (progress >= 63 && progress <= 118) {
                        if (progress > lastValue) {
                            mTransitionSteps = progress - 56;
                            lastValue = progress;
                        } else if (progress < lastValue) {
                            mTransitionSteps = -(56 - progress);
                        }
                        mTransitionStepResolution = 1;
                        mTransitionTimeValue.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionSteps), "s"));
                    } else if (progress >= 119 && progress <= 174) {
                        if (progress > lastValue) {
                            mTransitionSteps = progress - 112;
                            lastValue = progress;
                        } else if (progress < lastValue) {
                            mTransitionSteps = -(112 - progress);
                        }
                        mTransitionStepResolution = 2;
                        mTransitionTimeValue.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionSteps * 10), "s"));
                    } else if (progress >= 175 && progress <= 230) {
                        if (progress >= lastValue) {
                            mTransitionSteps = progress - 168;
                            lastValue = progress;
                        } else {
                            mTransitionSteps = -(168 - progress);
                        }
                        mTransitionStepResolution = 3;
                        mTransitionTimeValue.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionSteps * 10), "min"));
                    }
                }
            });

            // Setup set button
            nodeControlsContainer.actionSet.setOnClickListener(v -> {
                GenericTransitionTime.TransitionResolution resolution = getResolutionFromStepResolution(mTransitionStepResolution);
                GenericTransitionTime.TransitionStep transitionStep = GenericTransitionTime.TransitionStep.Specific(mTransitionSteps);
                GenericTransitionTime transitionTime = new GenericTransitionTime(resolution, transitionStep);
                
                Log.d(TAG, "Setting transition time - Steps: " + mTransitionSteps + ", Resolution: " + resolution + 
                      ", Raw value: 0x" + Integer.toHexString(transitionTime.getValue()) + 
                      ", Milliseconds: " + transitionTime.toMilliseconds());
                
                sendGenericDefaultTransitionTimeSet(transitionTime);
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

    private GenericTransitionTime.TransitionResolution getResolutionFromStepResolution(int stepResolution) {
        switch (stepResolution) {
            case 0: return GenericTransitionTime.TransitionResolution.HUNDRED_MILLISECONDS;
            case 1: return GenericTransitionTime.TransitionResolution.SECOND;
            case 2: return GenericTransitionTime.TransitionResolution.TEN_SECONDS;
            case 3: return GenericTransitionTime.TransitionResolution.TEN_MINUTES;
            default: return GenericTransitionTime.TransitionResolution.SECOND;
        }
    }

    private int getStepResolutionFromResolution(GenericTransitionTime.TransitionResolution resolution) {
        switch (resolution) {
            case HUNDRED_MILLISECONDS: return 0;
            case SECOND: return 1;
            case TEN_SECONDS: return 2;
            case TEN_MINUTES: return 3;
            default: return 1;
        }
    }

    private int getSliderValueFromTransitionTime(GenericTransitionTime transitionTime) {
        int steps = transitionTime.transitionStep.value;
        int stepResolution = getStepResolutionFromResolution(transitionTime.resolution);
        
        switch (stepResolution) {
            case 0: // 100ms resolution: 0-62
                return Math.min(steps, 62);
            case 1: // 1s resolution: 63-118
                return 56 + Math.min(steps, 62);
            case 2: // 10s resolution: 119-174
                return 112 + Math.min(steps, 62);
            case 3: // 10min resolution: 175-230
                return 168 + Math.min(steps, 62);
            default:
                return 0;
        }
    }

    @Override
    protected void enableClickableViews() {
        super.enableClickableViews();
        if (mTransitionSlider != null)
            mTransitionSlider.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        if (mTransitionSlider != null)
            mTransitionSlider.setEnabled(false);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        mSwipe.setOnRefreshListener(this);

        if (meshMessage instanceof GenericDefaultTransitionTimeStatus) {
            final GenericDefaultTransitionTimeStatus status = (GenericDefaultTransitionTimeStatus) meshMessage;
            final GenericTransitionTime transitionTime = status.getGenericTransitionTime();

            Log.d(TAG, "Received GenericDefaultTransitionTimeStatus from: 0x" + 
                  Integer.toHexString(meshMessage.getSrc()));

            if (transitionTime != null) {
                Log.d(TAG, "Received transition time - Steps: " + transitionTime.transitionStep.value + 
                      ", Resolution: " + transitionTime.resolution + 
                      ", Raw value: 0x" + Integer.toHexString(transitionTime.getValue()) + 
                      ", Milliseconds: " + transitionTime.toMilliseconds());
                
                // Update slider position based on received transition time
                int sliderValue = getSliderValueFromTransitionTime(transitionTime);
                mTransitionSlider.setValue(sliderValue);
                
                // Update internal state variables
                mTransitionSteps = transitionTime.transitionStep.value;
                mTransitionStepResolution = getStepResolutionFromResolution(transitionTime.resolution);
                
                // Display formatted transition time
                long milliseconds = transitionTime.toMilliseconds();
                if (milliseconds >= 0) {
                    String timeDisplay = formatTransitionTime(milliseconds);
                    mTransitionTimeDisplay.setText(timeDisplay);
                    Log.d(TAG, "Displayed transition time: " + timeDisplay + ", Slider value: " + sliderValue);
                } else {
                    mTransitionTimeDisplay.setText(getString(R.string.unknown));
                    Log.w(TAG, "Invalid transition time - negative milliseconds: " + milliseconds);
                }
            } else {
                Log.w(TAG, "Received null transition time in status message");
                mTransitionTimeDisplay.setText(getString(R.string.unknown));
            }
        }
        hideProgressBar();
    }

    private String formatTransitionTime(long milliseconds) {
        if (milliseconds == 0) {
            return getString(R.string.immediate);
        } else if (milliseconds < 1000) {
            return milliseconds + " ms";
        } else if (milliseconds < 60000) {
            return (milliseconds / 1000.0) + " s";
        } else {
            return (milliseconds / 60000.0) + " min";
        }
    }

    /**
     * Send generic default transition time get to mesh node
     */
    public void sendGenericDefaultTransitionTimeGet() {
        if (!checkConnectivity(mContainer)) return;
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            if (model != null) {
                if (!model.getBoundAppKeyIndexes().isEmpty()) {
                    final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                    final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);

                    final int address = element.getElementAddress();
                    final GenericDefaultTransitionTimeGet genericDefaultTransitionTimeGet = new GenericDefaultTransitionTimeGet(appKey);
                    Log.d(TAG, "Sending GenericDefaultTransitionTimeGet to address: 0x" + Integer.toHexString(address));
                    sendAcknowledgedMessage(address, genericDefaultTransitionTimeGet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }

    /**
     * Send generic default transition time set to mesh node
     *
     * @param transitionTime GenericTransitionTime
     */
    public void sendGenericDefaultTransitionTimeSet(final GenericTransitionTime transitionTime) {
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
                        final GenericDefaultTransitionTimeSet genericDefaultTransitionTimeSet = new GenericDefaultTransitionTimeSet(appKey, transitionTime);
                        Log.d(TAG, "Sending GenericDefaultTransitionTimeSet to address: 0x" + Integer.toHexString(address) + 
                              " with transition time - Steps: " + transitionTime.transitionStep.value + 
                              ", Resolution: " + transitionTime.resolution + 
                              ", Raw value: 0x" + Integer.toHexString(transitionTime.getValue()));
                        sendAcknowledgedMessage(address, genericDefaultTransitionTimeSet);
                    } else {
                        mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                    }
                }
            }
        }
    }
}