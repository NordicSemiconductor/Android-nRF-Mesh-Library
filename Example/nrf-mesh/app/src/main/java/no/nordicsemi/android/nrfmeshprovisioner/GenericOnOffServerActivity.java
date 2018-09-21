package no.nordicsemi.android.nrfmeshprovisioner;

import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.models.GenericOnOffServerModel;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public class GenericOnOffServerActivity extends BaseModelConfigurationActivity {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private Button mActionOnOff;
    protected int mTransitionStepResolution;
    protected int mTransitionStep;

    @Override
    protected final void addControlsUi(final MeshModel model) {
        if (model instanceof GenericOnOffServerModel) {
            final CardView cardView = findViewById(R.id.node_controls_card);
            final View nodeControlsContainer = LayoutInflater.from(this).inflate(R.layout.layout_generic_on_off, cardView);
            final TextView time = nodeControlsContainer.findViewById(R.id.transition_time);
            final TextView onOffState = nodeControlsContainer.findViewById(R.id.on_off_state);
            final TextView remainingTime = nodeControlsContainer.findViewById(R.id.transition_state);
            final SeekBar transitionTimeSeekBar = nodeControlsContainer.findViewById(R.id.transition_seekbar);
            transitionTimeSeekBar.setProgress(0);
            transitionTimeSeekBar.incrementProgressBy(1);
            transitionTimeSeekBar.setMax(230);

            final SeekBar delaySeekBar = nodeControlsContainer.findViewById(R.id.delay_seekbar);
            delaySeekBar.setProgress(0);
            delaySeekBar.setMax(255);
            final TextView delayTime = nodeControlsContainer.findViewById(R.id.delay_time);

            mActionOnOff = nodeControlsContainer.findViewById(R.id.action_on_off);
            mActionOnOff.setOnClickListener(v -> {
                try {
                    final ProvisionedMeshNode node = (ProvisionedMeshNode) mViewModel.getExtendedMeshNode().getMeshNode();
                    if (mActionOnOff.getText().toString().equals(getString(R.string.action_generic_on))) {
                        mViewModel.sendGenericOnOff(node, mTransitionStep, mTransitionStepResolution, delaySeekBar.getProgress(), true);
                    } else {
                        mViewModel.sendGenericOnOff(node, mTransitionStep, mTransitionStepResolution, delaySeekBar.getProgress(), false);
                    }
                    showProgressbar();
                } catch (IllegalArgumentException ex) {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            mActionRead = nodeControlsContainer.findViewById(R.id.action_read);
            mActionRead.setOnClickListener(v -> {
                final ProvisionedMeshNode node = (ProvisionedMeshNode) mViewModel.getExtendedMeshNode().getMeshNode();
                mViewModel.sendGenericOnOffGet(node);
                showProgressbar();
            });

            transitionTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int lastValue = 0;
                double res = 0.0;

                @Override
                public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

                    if (progress >= 0 && progress <= 62) {
                        lastValue = progress;
                        mTransitionStepResolution = 0;
                        mTransitionStep = progress;
                        res = progress / 10.0;
                        time.setText(getString(R.string.transition_time_interval, String.valueOf(res), "s"));
                    } else if (progress >= 63 && progress <= 118) {
                        if (progress > lastValue) {
                            mTransitionStep = progress - 56;
                            lastValue = progress;
                        } else if (progress < lastValue) {
                            mTransitionStep = -(56 - progress);
                        }
                        mTransitionStepResolution = 1;
                        time.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionStep), "s"));

                    } else if (progress >= 119 && progress <= 174) {
                        if (progress > lastValue) {
                            mTransitionStep = progress - 112;
                            lastValue = progress;
                        } else if (progress < lastValue) {
                            mTransitionStep = -(112 - progress);
                        }
                        mTransitionStepResolution = 2;
                        time.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionStep * 10), "s"));
                    } else if (progress >= 175 && progress <= 230) {
                        if (progress >= lastValue) {
                            mTransitionStep = progress - 168;
                            lastValue = progress;
                        } else if (progress < lastValue) {
                            mTransitionStep = -(168 - progress);
                        }
                        mTransitionStepResolution = 3;
                        time.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionStep * 10), "min"));
                    }
                }

                @Override
                public void onStartTrackingTouch(final SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(final SeekBar seekBar) {

                }
            });

            delaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                    delayTime.setText(getString(R.string.transition_time_interval, String.valueOf(progress * MeshParserUtils.GENERIC_ON_OFF_5_MS), "ms"));
                }

                @Override
                public void onStartTrackingTouch(final SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(final SeekBar seekBar) {

                }
            });

            mViewModel.getGenericOnOffState().observe(this, genericOnOffStatusUpdate -> {
                hideProgressBar();
                final boolean presentState = genericOnOffStatusUpdate.isPresentOnOff();
                final Boolean targetOnOff = genericOnOffStatusUpdate.getTargetOnOff();
                final int steps = genericOnOffStatusUpdate.getSteps();
                final int resolution = genericOnOffStatusUpdate.getResolution();
                if (targetOnOff == null) {
                    if (presentState) {
                        onOffState.setText(R.string.generic_state_on);
                        mActionOnOff.setText(R.string.action_generic_off);
                    } else {
                        onOffState.setText(R.string.generic_state_off);
                        mActionOnOff.setText(R.string.action_generic_on);
                    }
                    remainingTime.setVisibility(View.GONE);
                } else {
                    if (!targetOnOff) {
                        onOffState.setText(R.string.generic_state_on);
                        mActionOnOff.setText(R.string.action_generic_off);
                    } else {
                        onOffState.setText(R.string.generic_state_off);
                        mActionOnOff.setText(R.string.action_generic_on);
                    }
                    remainingTime.setText(getString(R.string.remaining_time, MeshParserUtils.getRemainingTransitionTime(resolution, steps)));
                    remainingTime.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void enableClickableViews() {
        super.enableClickableViews();
        if(mActionOnOff != null && !mActionOnOff.isEnabled())
            mActionOnOff.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        if(mActionOnOff != null)
            mActionOnOff.setEnabled(false);
    }
}
