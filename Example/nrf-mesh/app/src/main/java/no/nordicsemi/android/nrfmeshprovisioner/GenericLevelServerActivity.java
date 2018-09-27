package no.nordicsemi.android.nrfmeshprovisioner;

import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.models.GenericLevelServerModel;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public class GenericLevelServerActivity extends BaseModelConfigurationActivity {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private int mTransitionStepResolution;
    private int mTransitionStep;
    private SeekBar mTransitionTimeSeekBar;
    private SeekBar mDelaySeekBar;
    private SeekBar mLevelSeekBar;

    @Override
    protected final void addControlsUi(final MeshModel model) {
        if (model instanceof GenericLevelServerModel) {
            final CardView cardView = findViewById(R.id.node_controls_card);
            final View nodeControlsContainer = LayoutInflater.from(this).inflate(R.layout.layout_generic_level, cardView);
            final TextView time = nodeControlsContainer.findViewById(R.id.transition_time);
            final TextView remainingTime = nodeControlsContainer.findViewById(R.id.transition_state);
            mTransitionTimeSeekBar = nodeControlsContainer.findViewById(R.id.transition_seekbar);
            mTransitionTimeSeekBar.setProgress(0);
            mTransitionTimeSeekBar.incrementProgressBy(1);
            mTransitionTimeSeekBar.setMax(230);

            mDelaySeekBar = nodeControlsContainer.findViewById(R.id.delay_seekbar);
            mDelaySeekBar.setProgress(0);
            mDelaySeekBar.setMax(255);
            final TextView delayTime = nodeControlsContainer.findViewById(R.id.delay_time);

            final TextView level = nodeControlsContainer.findViewById(R.id.level);
            mLevelSeekBar = nodeControlsContainer.findViewById(R.id.level_seekbar);
            mLevelSeekBar.setProgress(0);
            mLevelSeekBar.setMax(100);

            mActionRead = nodeControlsContainer.findViewById(R.id.action_read);
            mActionRead.setOnClickListener(v -> {
                final ProvisionedMeshNode node = (ProvisionedMeshNode) mViewModel.getExtendedMeshNode().getMeshNode();
                mViewModel.sendGenericLevelGet(node);
                showProgressbar();
            });

            mViewModel.getGenericLevelState().observe(this, genericLevelStatusUpdate -> {
                hideProgressBar();
                final int presentLevel = genericLevelStatusUpdate.getPresentLevel();
                final Integer targetLevel = genericLevelStatusUpdate.getTargetLevel();
                final int steps = genericLevelStatusUpdate.getSteps();
                final int resolution = genericLevelStatusUpdate.getResolution();
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
                mLevelSeekBar.setProgress(levelPercent);
            });

            mTransitionTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

            mDelaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

            mLevelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                    level.setText(getString(R.string.generic_level_percent, progress));
                }

                @Override
                public void onStartTrackingTouch(final SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(final SeekBar seekBar) {
                    showProgressbar();
                    final int level = seekBar.getProgress();
                    final int delay = mDelaySeekBar.getProgress();
                    final ProvisionedMeshNode node = (ProvisionedMeshNode) mViewModel.getExtendedMeshNode().getMeshNode();
                    final int genericLevel = ((level * 65535) / 100) - 32768;
                    mViewModel.sendGenericLevelSet(node, genericLevel, mTransitionStep, mTransitionStepResolution, delay);
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
        mTransitionTimeSeekBar.setEnabled(true);
        mDelaySeekBar.setEnabled(true);
        mLevelSeekBar.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        mTransitionTimeSeekBar.setEnabled(false);
        mDelaySeekBar.setEnabled(false);
        mLevelSeekBar.setEnabled(false);
    }
}
