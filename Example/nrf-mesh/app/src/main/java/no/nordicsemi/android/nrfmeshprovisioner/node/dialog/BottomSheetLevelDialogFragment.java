package no.nordicsemi.android.nrfmeshprovisioner.node.dialog;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class BottomSheetLevelDialogFragment extends BottomSheetDialogFragment {
    private static final String KEY_INDEX = "KEY_INDEX";
    private static final String MODEL_ID = "MODEL_ID";
    private int mKeyIndex;

    private int mTransitionStepResolution;
    private int mTransitionSteps;

    public interface BottomSheetLevelListener {
        void toggleLevel(final int keyIndex, final int level, final int mTransitionSteps, final int mTransitionStepResolution, final int progress);
    }

    public static BottomSheetLevelDialogFragment getInstance(final int appKeyIndex) {
        final BottomSheetLevelDialogFragment fragment = new BottomSheetLevelDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(KEY_INDEX, appKeyIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mKeyIndex = getArguments().getInt(KEY_INDEX);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View nodeControlsContainer = inflater.inflate(R.layout.layout_generic_level_bottom_sheet, container, false);

        final TextView time = nodeControlsContainer.findViewById(R.id.transition_time);
        final SeekBar transitionTimeSeekBar = nodeControlsContainer.findViewById(R.id.transition_seekbar);
        transitionTimeSeekBar.setProgress(0);
        transitionTimeSeekBar.incrementProgressBy(1);
        transitionTimeSeekBar.setMax(230);

        final SeekBar delaySeekBar = nodeControlsContainer.findViewById(R.id.delay_seekbar);
        delaySeekBar.setProgress(0);
        delaySeekBar.setMax(255);
        final TextView delayTime = nodeControlsContainer.findViewById(R.id.delay_time);

        final TextView level = nodeControlsContainer.findViewById(R.id.level);
        final SeekBar levelSeekBar = nodeControlsContainer.findViewById(R.id.level_seek_bar);
        levelSeekBar.setProgress(0);
        levelSeekBar.setMax(100);

        transitionTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int lastValue = 0;
            double res = 0.0;

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

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

        levelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                level.setText(getString(R.string.generic_level_percent, progress));
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                final int level = seekBar.getProgress();
                final int delay = delaySeekBar.getProgress();
                final int genericLevel = ((level * 65535) / 100) - 32768;
                ((BottomSheetLevelListener) requireActivity()).toggleLevel(mKeyIndex, genericLevel, mTransitionSteps, mTransitionStepResolution, delay);
            }
        });
        return nodeControlsContainer;
    }
}
